package yowyob.comops.api.treasury.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import yowyob.comops.api.treasury.application.port.in.ClearCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.in.GetCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.in.ListCheckPaymentsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterCheckPaymentCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.application.port.out.CheckPaymentRepository;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.CheckPaymentNotFoundException;
import yowyob.comops.api.treasury.domain.TreasuryConsistencyException;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import yowyob.comops.api.treasury.domain.model.CheckPayment;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CheckPaymentApplicationService implements RegisterCheckPaymentUseCase, GetCheckPaymentUseCase,
        ListCheckPaymentsUseCase, ClearCheckPaymentUseCase {
    private final CheckPaymentRepository repository;
    private final BankAccountRepository bankAccountRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public CheckPaymentApplicationService(CheckPaymentRepository repository,
            BankAccountRepository bankAccountRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.repository = repository;
        this.bankAccountRepository = bankAccountRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<CheckPayment> register(RegisterCheckPaymentCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(bankAccountRepository.findById(command.bankAccountId())
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(command.bankAccountId())))
                .doOnNext(account -> validateAccountScope(account, command.organizationId()))
                .flatMap(account -> resolveCheckNumber(command)
                        .map(checkNumber -> CheckPayment.issue(command.tenantId(), command.organizationId(),
                                command.bankAccountId(), checkNumber, command.amount(), command.beneficiary())))
                .flatMap(repository::save)
                .flatMap(saved -> businessEventPublisher.publish(checkIssuedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<CheckPayment> getCheckPayment(UUID checkPaymentId) {
        return repository.findById(checkPaymentId)
                .switchIfEmpty(Mono.error(new CheckPaymentNotFoundException(checkPaymentId)));
    }

    @Override
    public Flux<CheckPayment> listCheckPayments(UUID tenantId, UUID organizationId, UUID bankAccountId) {
        return bankAccountId != null
                ? repository.findByBankAccountId(tenantId, bankAccountId)
                        .filter(check -> check.organizationId().equals(organizationId))
                : repository.findByOrganizationId(tenantId, organizationId);
    }

    @Override
    public Mono<CheckPayment> clear(UUID checkPaymentId) {
        return transactionalExecutor.transactional(repository.findById(checkPaymentId)
                .switchIfEmpty(Mono.error(new CheckPaymentNotFoundException(checkPaymentId)))
                .map(CheckPayment::clear)
                .flatMap(repository::save)
                .flatMap(saved -> businessEventPublisher.publish(checkClearedEvent(saved)).thenReturn(saved)));
    }

    private Mono<String> resolveCheckNumber(RegisterCheckPaymentCommand command) {
        if (command.checkNumber() != null && !command.checkNumber().isBlank()) {
            return Mono.just(command.checkNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), null, DocumentTypes.CHECK_PAYMENT));
    }

    private void validateAccountScope(BankAccount bankAccount, UUID organizationId) {
        if (!bankAccount.organizationId().equals(organizationId)) {
            throw new TreasuryConsistencyException("bank account does not belong to the provided organization");
        }
    }

    private BusinessEvent checkIssuedEvent(CheckPayment checkPayment) {
        return BusinessEvent.now(checkPayment.tenantId(), checkPayment.organizationId(), "CHECK_PAYMENT_ISSUED",
                "CHECK_PAYMENT", checkPayment.id(), payload(
                        "bankAccountId", checkPayment.bankAccountId(),
                        "checkNumber", checkPayment.checkNumber(),
                        "amount", checkPayment.amount(),
                        "beneficiary", checkPayment.beneficiary(),
                        "status", checkPayment.status()));
    }

    private BusinessEvent checkClearedEvent(CheckPayment checkPayment) {
        return BusinessEvent.now(checkPayment.tenantId(), checkPayment.organizationId(), "CHECK_PAYMENT_CLEARED",
                "CHECK_PAYMENT", checkPayment.id(), payload(
                        "bankAccountId", checkPayment.bankAccountId(),
                        "checkNumber", checkPayment.checkNumber(),
                        "amount", checkPayment.amount(),
                        "status", checkPayment.status()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
