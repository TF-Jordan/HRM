package yowyob.comops.api.treasury.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import yowyob.comops.api.treasury.application.port.in.GetInvoiceSettlementUseCase;
import yowyob.comops.api.treasury.application.port.in.ListInvoiceSettlementsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterInvoiceSettlementCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterInvoiceSettlementUseCase;
import yowyob.comops.api.treasury.application.port.out.ApplyInvoiceSettlementGateway;
import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.application.port.out.InvoiceSettlementRepository;
import yowyob.comops.api.treasury.application.port.out.PostedInvoiceSettlementSource;
import yowyob.comops.api.treasury.application.port.out.PostedInvoiceSettlementSourceProvider;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.DuplicateInvoiceSettlementNumberException;
import yowyob.comops.api.treasury.domain.InvoiceSettlementNotFoundException;
import yowyob.comops.api.treasury.domain.TreasuryConsistencyException;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import yowyob.comops.api.treasury.domain.model.InvoiceSettlement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service("treasuryInvoiceSettlementApplicationService")
public class InvoiceSettlementApplicationService implements RegisterInvoiceSettlementUseCase,
        GetInvoiceSettlementUseCase, ListInvoiceSettlementsUseCase {

    private final InvoiceSettlementRepository invoiceSettlementRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PostedInvoiceSettlementSourceProvider postedInvoiceSettlementSourceProvider;
    private final ApplyInvoiceSettlementGateway applyInvoiceSettlementGateway;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public InvoiceSettlementApplicationService(InvoiceSettlementRepository invoiceSettlementRepository,
            BankAccountRepository bankAccountRepository,
            PostedInvoiceSettlementSourceProvider postedInvoiceSettlementSourceProvider,
            ApplyInvoiceSettlementGateway applyInvoiceSettlementGateway,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.invoiceSettlementRepository = invoiceSettlementRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.postedInvoiceSettlementSourceProvider = postedInvoiceSettlementSourceProvider;
        this.applyInvoiceSettlementGateway = applyInvoiceSettlementGateway;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<InvoiceSettlement> registerSettlement(RegisterInvoiceSettlementCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(Mono.zip(resolveSettlementNumber(command), getExistingBankAccount(command.bankAccountId()),
                        postedInvoiceSettlementSourceProvider.getPostedInvoice(command.invoiceId())
                                .switchIfEmpty(Mono.error(new TreasuryConsistencyException(
                                        "Posted invoice not found for settlement: " + command.invoiceId()))))
                .flatMap(tuple -> validateAndSave(command, tuple.getT1(), tuple.getT2(), tuple.getT3())));
    }

    @Override
    public Mono<InvoiceSettlement> getSettlement(UUID settlementId) {
        return invoiceSettlementRepository.findById(settlementId)
                .switchIfEmpty(Mono.error(new InvoiceSettlementNotFoundException(settlementId)));
    }

    @Override
    public Flux<InvoiceSettlement> listSettlements(UUID tenantId, UUID organizationId, UUID invoiceId) {
        return invoiceSettlementRepository.findByOrganizationId(tenantId, organizationId)
                .filter(settlement -> invoiceId == null || settlement.invoiceId().equals(invoiceId));
    }

    private Mono<InvoiceSettlement> validateAndSave(RegisterInvoiceSettlementCommand command, String settlementNumber,
            BankAccount bankAccount, PostedInvoiceSettlementSource invoiceSource) {
        validateConsistency(command, bankAccount, invoiceSource);
        InvoiceSettlement settlement = InvoiceSettlement.register(command.tenantId(), command.organizationId(),
                command.bankAccountId(), command.invoiceId(), settlementNumber, command.paymentMethod(),
                command.amount(), invoiceSource.currency());
        return invoiceSettlementRepository.existsBySettlementNumber(command.tenantId(), command.organizationId(),
                        settlement.settlementNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateInvoiceSettlementNumberException(settlement.settlementNumber()))
                        : invoiceSettlementRepository.save(settlement))
                .flatMap(saved -> applyInvoiceSettlementGateway.apply(saved.invoiceId(), saved.settlementNumber(),
                                saved.amount())
                        .thenReturn(saved))
                .flatMap(saved -> businessEventPublisher.publish(invoiceSettlementRegisteredEvent(saved, invoiceSource))
                        .thenReturn(saved));
    }

    private Mono<BankAccount> getExistingBankAccount(UUID bankAccountId) {
        return bankAccountRepository.findById(bankAccountId)
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(bankAccountId)));
    }

    private Mono<String> resolveSettlementNumber(RegisterInvoiceSettlementCommand command) {
        if (command.settlementNumber() != null && !command.settlementNumber().isBlank()) {
            return Mono.just(command.settlementNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), null, DocumentTypes.INVOICE_SETTLEMENT));
    }

    private void validateConsistency(RegisterInvoiceSettlementCommand command, BankAccount bankAccount,
            PostedInvoiceSettlementSource invoiceSource) {
        if (!bankAccount.tenantId().equals(command.tenantId())) {
            throw new TreasuryConsistencyException("Bank account does not belong to tenant " + command.tenantId());
        }
        if (!invoiceSource.tenantId().equals(command.tenantId())) {
            throw new TreasuryConsistencyException("Invoice does not belong to tenant " + command.tenantId());
        }
        if (!bankAccount.organizationId().equals(command.organizationId())) {
            throw new TreasuryConsistencyException("Bank account does not belong to organization " + command.organizationId());
        }
        if (!invoiceSource.organizationId().equals(command.organizationId())) {
            throw new TreasuryConsistencyException("Invoice does not belong to organization " + command.organizationId());
        }
        if (!bankAccount.currency().equalsIgnoreCase(invoiceSource.currency())) {
            throw new TreasuryConsistencyException("Bank account currency must match invoice currency");
        }
        if (invoiceSource.outstandingAmount().compareTo(command.amount()) < 0) {
            throw new TreasuryConsistencyException("Settlement amount exceeds invoice outstanding amount");
        }
        if (invoiceSource.outstandingAmount().signum() == 0) {
            throw new TreasuryConsistencyException("Invoice is already fully settled");
        }
    }

    private BusinessEvent invoiceSettlementRegisteredEvent(InvoiceSettlement settlement,
            PostedInvoiceSettlementSource invoiceSource) {
        return BusinessEvent.now(settlement.tenantId(), settlement.organizationId(), "INVOICE_SETTLEMENT_REGISTERED",
                "INVOICE_SETTLEMENT", settlement.id(), payload(
                        "settlementNumber", settlement.settlementNumber(),
                        "invoiceId", settlement.invoiceId(),
                        "invoiceNumber", invoiceSource.invoiceNumber(),
                        "bankAccountId", settlement.bankAccountId(),
                        "paymentMethod", settlement.paymentMethod(),
                        "amount", settlement.amount(),
                        "currency", settlement.currency(),
                        "status", settlement.status()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
