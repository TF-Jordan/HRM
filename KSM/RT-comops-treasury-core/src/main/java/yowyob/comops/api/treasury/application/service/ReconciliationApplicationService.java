package yowyob.comops.api.treasury.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import yowyob.comops.api.treasury.application.port.in.CloseReconciliationUseCase;
import yowyob.comops.api.treasury.application.port.in.GetReconciliationUseCase;
import yowyob.comops.api.treasury.application.port.in.ListReconciliationsUseCase;
import yowyob.comops.api.treasury.application.port.in.OpenReconciliationCommand;
import yowyob.comops.api.treasury.application.port.in.OpenReconciliationUseCase;
import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.application.port.out.BankStatementRepository;
import yowyob.comops.api.treasury.application.port.out.ReconciliationRepository;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.BankStatementNotFoundException;
import yowyob.comops.api.treasury.domain.ReconciliationNotFoundException;
import yowyob.comops.api.treasury.domain.TreasuryConsistencyException;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import yowyob.comops.api.treasury.domain.model.BankStatement;
import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReconciliationApplicationService implements OpenReconciliationUseCase, CloseReconciliationUseCase,
        GetReconciliationUseCase, ListReconciliationsUseCase {
    private final ReconciliationRepository repository;
    private final BankAccountRepository bankAccountRepository;
    private final BankStatementRepository bankStatementRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public ReconciliationApplicationService(ReconciliationRepository repository,
            BankAccountRepository bankAccountRepository,
            BankStatementRepository bankStatementRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.repository = repository;
        this.bankAccountRepository = bankAccountRepository;
        this.bankStatementRepository = bankStatementRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }
    @Override
    public Mono<Reconciliation> open(OpenReconciliationCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<BankAccount> bankAccountMono = bankAccountRepository.findById(command.bankAccountId())
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(command.bankAccountId())));
        Mono<BankStatement> statementMono = bankStatementRepository.findById(command.statementId())
                .switchIfEmpty(Mono.error(new BankStatementNotFoundException(command.statementId())));
        return transactionalExecutor.transactional(Mono.zip(bankAccountMono, statementMono)
                .doOnNext(tuple -> validateConsistency(tuple.getT1(), tuple.getT2(), command.organizationId()))
                .flatMap(tuple -> resolveReferenceNumber(command)
                        .map(referenceNumber -> Reconciliation.open(command.tenantId(), command.organizationId(),
                                command.bankAccountId(), command.statementId(), referenceNumber)))
                .flatMap(repository::save)
                .flatMap(saved -> businessEventPublisher.publish(reconciliationOpenedEvent(saved)).thenReturn(saved)));
    }
    @Override
    public Mono<Reconciliation> close(UUID reconciliationId) {
        return transactionalExecutor.transactional(repository.findById(reconciliationId)
                .switchIfEmpty(Mono.error(new ReconciliationNotFoundException(reconciliationId)))
                .map(Reconciliation::close)
                .flatMap(repository::save)
                .flatMap(saved -> businessEventPublisher.publish(reconciliationClosedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<Reconciliation> getReconciliation(UUID reconciliationId) {
        return repository.findById(reconciliationId)
                .switchIfEmpty(Mono.error(new ReconciliationNotFoundException(reconciliationId)));
    }

    @Override
    public Flux<Reconciliation> listReconciliations(UUID tenantId, UUID organizationId, UUID bankAccountId) {
        return bankAccountId != null
                ? repository.findByBankAccountId(tenantId, bankAccountId)
                        .filter(reconciliation -> reconciliation.organizationId().equals(organizationId))
                : repository.findByOrganizationId(tenantId, organizationId);
    }

    private Mono<String> resolveReferenceNumber(OpenReconciliationCommand command) {
        if (command.referenceNumber() != null && !command.referenceNumber().isBlank()) {
            return Mono.just(command.referenceNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), null, DocumentTypes.RECONCILIATION));
    }

    private void validateConsistency(BankAccount bankAccount, BankStatement bankStatement, UUID organizationId) {
        if (!bankAccount.organizationId().equals(organizationId)) {
            throw new TreasuryConsistencyException("bank account does not belong to the provided organization");
        }
        if (!bankStatement.organizationId().equals(organizationId)) {
            throw new TreasuryConsistencyException("bank statement does not belong to the provided organization");
        }
        if (!bankStatement.bankAccountId().equals(bankAccount.id())) {
            throw new TreasuryConsistencyException("bank statement does not belong to the provided bank account");
        }
    }

    private BusinessEvent reconciliationOpenedEvent(Reconciliation reconciliation) {
        return BusinessEvent.now(reconciliation.tenantId(), reconciliation.organizationId(),
                "RECONCILIATION_OPENED", "RECONCILIATION", reconciliation.id(), payload(
                        "referenceNumber", reconciliation.referenceNumber(),
                        "bankAccountId", reconciliation.bankAccountId(),
                        "statementId", reconciliation.statementId(),
                        "status", reconciliation.status()));
    }

    private BusinessEvent reconciliationClosedEvent(Reconciliation reconciliation) {
        return BusinessEvent.now(reconciliation.tenantId(), reconciliation.organizationId(),
                "RECONCILIATION_CLOSED", "RECONCILIATION", reconciliation.id(), payload(
                        "referenceNumber", reconciliation.referenceNumber(),
                        "bankAccountId", reconciliation.bankAccountId(),
                        "statementId", reconciliation.statementId(),
                        "status", reconciliation.status(),
                        "closedAt", reconciliation.closedAt()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
