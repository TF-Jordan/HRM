package yowyob.comops.api.treasury.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import yowyob.comops.api.treasury.application.port.in.GetBankStatementUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankStatementsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterBankStatementCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankStatementUseCase;
import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.application.port.out.BankStatementRepository;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.BankStatementNotFoundException;
import yowyob.comops.api.treasury.domain.TreasuryConsistencyException;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import yowyob.comops.api.treasury.domain.model.BankStatement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BankStatementApplicationService implements RegisterBankStatementUseCase, GetBankStatementUseCase, ListBankStatementsUseCase {

    private final BankStatementRepository bankStatementRepository;
    private final BankAccountRepository bankAccountRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public BankStatementApplicationService(BankStatementRepository bankStatementRepository,
            BankAccountRepository bankAccountRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.bankStatementRepository = bankStatementRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<BankStatement> registerStatement(RegisterBankStatementCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(bankAccountRepository.findById(command.bankAccountId())
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(command.bankAccountId())))
                .doOnNext(account -> validateAccountScope(account, command.organizationId()))
                .flatMap(account -> resolveStatementNumber(command)
                        .map(statementNumber -> BankStatement.register(command.tenantId(), command.organizationId(),
                                command.bankAccountId(), statementNumber, command.statementDate(),
                                command.openingBalance(), command.closingBalance())))
                .flatMap(bankStatementRepository::save)
                .flatMap(saved -> businessEventPublisher.publish(statementRegisteredEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<BankStatement> getStatement(UUID statementId) {
        return bankStatementRepository.findById(statementId)
                .switchIfEmpty(Mono.error(new BankStatementNotFoundException(statementId)));
    }

    @Override
    public Flux<BankStatement> listStatements(UUID tenantId, UUID organizationId, UUID bankAccountId) {
        return bankAccountId != null
                ? bankStatementRepository.findByBankAccountId(tenantId, bankAccountId)
                        .filter(statement -> statement.organizationId().equals(organizationId))
                : bankStatementRepository.findByOrganizationId(tenantId, organizationId);
    }

    private Mono<String> resolveStatementNumber(RegisterBankStatementCommand command) {
        if (command.statementNumber() != null && !command.statementNumber().isBlank()) {
            return Mono.just(command.statementNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), null, DocumentTypes.BANK_STATEMENT));
    }

    private void validateAccountScope(BankAccount bankAccount, UUID organizationId) {
        if (!bankAccount.organizationId().equals(organizationId)) {
            throw new TreasuryConsistencyException("bank account does not belong to the provided organization");
        }
    }

    private BusinessEvent statementRegisteredEvent(BankStatement statement) {
        return BusinessEvent.now(statement.tenantId(), statement.organizationId(), "BANK_STATEMENT_REGISTERED",
                "BANK_STATEMENT", statement.id(), payload(
                        "bankAccountId", statement.bankAccountId(),
                        "statementNumber", statement.statementNumber(),
                        "statementDate", statement.statementDate(),
                        "openingBalance", statement.openingBalance(),
                        "closingBalance", statement.closingBalance()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
