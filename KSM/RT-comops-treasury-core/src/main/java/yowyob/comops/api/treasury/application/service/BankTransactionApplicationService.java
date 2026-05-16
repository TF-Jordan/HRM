package yowyob.comops.api.treasury.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.settings.domain.DocumentSequenceNotFoundException;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import yowyob.comops.api.treasury.application.port.in.AutoReconcileBankTransactionsUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankTransactionsUseCase;
import yowyob.comops.api.treasury.application.port.in.ManualReconcileBankTransactionCommand;
import yowyob.comops.api.treasury.application.port.in.ManualReconcileBankTransactionUseCase;
import yowyob.comops.api.treasury.application.port.in.ReconciliationRunResult;
import yowyob.comops.api.treasury.application.port.in.RegisterBankTransactionCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankTransactionUseCase;
import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.application.port.out.BankStatementRepository;
import yowyob.comops.api.treasury.application.port.out.BankTransactionRepository;
import yowyob.comops.api.treasury.application.port.out.ReconciliationRepository;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.BankStatementNotFoundException;
import yowyob.comops.api.treasury.domain.BankTransactionNotFoundException;
import yowyob.comops.api.treasury.domain.DuplicateBankTransactionReferenceException;
import yowyob.comops.api.treasury.domain.TreasuryConsistencyException;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import yowyob.comops.api.treasury.domain.model.BankStatement;
import yowyob.comops.api.treasury.domain.model.BankTransaction;
import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BankTransactionApplicationService implements RegisterBankTransactionUseCase, ListBankTransactionsUseCase,
        AutoReconcileBankTransactionsUseCase, ManualReconcileBankTransactionUseCase {

    private final BankTransactionRepository bankTransactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankStatementRepository bankStatementRepository;
    private final ReconciliationRepository reconciliationRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public BankTransactionApplicationService(BankTransactionRepository bankTransactionRepository,
            BankAccountRepository bankAccountRepository,
            BankStatementRepository bankStatementRepository,
            ReconciliationRepository reconciliationRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.bankStatementRepository = bankStatementRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<BankTransaction> registerTransaction(RegisterBankTransactionCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(bankAccountRepository.findById(command.bankAccountId())
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(command.bankAccountId())))
                .doOnNext(account -> validateAccountScope(account, command.organizationId()))
                .flatMap(account -> resolveReferenceNumber(command)
                        .flatMap(referenceNumber -> bankTransactionRepository.existsByReferenceNumber(command.tenantId(),
                                        command.bankAccountId(), referenceNumber)
                                .flatMap(exists -> exists
                                        ? Mono.error(new DuplicateBankTransactionReferenceException(referenceNumber))
                                        : Mono.just(BankTransaction.record(command.tenantId(), command.organizationId(),
                                                command.bankAccountId(), referenceNumber, command.transactionType(),
                                                command.transactionDate(), command.amount(), command.description(),
                                                command.createdBy())))))
                .flatMap(bankTransactionRepository::save)
                .flatMap(saved -> businessEventPublisher.publish(transactionRecordedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Flux<BankTransaction> listBankTransactions(UUID bankAccountId, int limit) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> bankTransactionRepository.findByBankAccountId(context.tenantId(), bankAccountId)
                        .take(Math.max(1, limit)));
    }

    @Override
    public Mono<ReconciliationRunResult> autoReconcile(UUID bankAccountId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> transactionalExecutor.transactional(bankAccountRepository.findById(bankAccountId)
                        .switchIfEmpty(Mono.error(new BankAccountNotFoundException(bankAccountId)))
                        .flatMap(account -> bankStatementRepository.findLatestByBankAccountId(context.tenantId(),
                                        bankAccountId)
                                .switchIfEmpty(Mono.error(new TreasuryConsistencyException(
                                        "no bank statement available for automatic reconciliation")))
                                .flatMap(statement -> ensureOpenReconciliation(context.tenantId(),
                                                account.organizationId(), bankAccountId, statement.id())
                                        .flatMap(reconciliation -> bankTransactionRepository
                                                .findUnreconciledByBankAccountId(context.tenantId(), bankAccountId)
                                                .filter(transaction -> !transaction.transactionDate()
                                                        .isAfter(statement.statementDate()))
                                                .concatMap(transaction -> bankTransactionRepository
                                                        .save(transaction.reconcile(statement.id()))
                                                        .flatMap(saved -> businessEventPublisher
                                                                .publish(transactionReconciledEvent(saved))
                                                                .thenReturn(saved)))
                                                .count()
                                                .flatMap(count -> businessEventPublisher.publish(
                                                                autoReconciledEvent(reconciliation, count.intValue()))
                                                        .thenReturn(new ReconciliationRunResult(reconciliation.id(),
                                                                count.intValue()))))))));
    }

    @Override
    public Mono<BankTransaction> reconcile(ManualReconcileBankTransactionCommand command) {
        Objects.requireNonNull(command, "command is required");
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> transactionalExecutor.transactional(bankTransactionRepository.findById(
                                        command.transactionId())
                                .switchIfEmpty(Mono.error(new BankTransactionNotFoundException(command.transactionId())))
                                .zipWith(bankStatementRepository.findById(command.statementId())
                                        .switchIfEmpty(Mono.error(new BankStatementNotFoundException(command.statementId()))))
                                .flatMap(tuple -> {
                                    BankTransaction transaction = tuple.getT1();
                                    BankStatement statement = tuple.getT2();
                                    validateManualReconciliation(context.tenantId(), transaction, statement);
                                    return ensureOpenReconciliation(context.tenantId(), transaction.organizationId(),
                                            transaction.bankAccountId(), statement.id())
                                            .flatMap(reconciliation -> bankTransactionRepository
                                                    .save(transaction.reconcile(statement.id()))
                                                    .flatMap(saved -> businessEventPublisher.publish(
                                                                    transactionReconciledEvent(saved))
                                                            .then(businessEventPublisher.publish(
                                                                    manualReconciledEvent(reconciliation, saved)))
                                                            .thenReturn(saved)));
                                })));
    }

    private Mono<String> resolveReferenceNumber(RegisterBankTransactionCommand command) {
        if (command.referenceNumber() != null && !command.referenceNumber().isBlank()) {
            return Mono.just(command.referenceNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), null, DocumentTypes.BANK_TRANSACTION));
    }

    private Mono<Reconciliation> ensureOpenReconciliation(UUID tenantId, UUID organizationId, UUID bankAccountId,
            UUID statementId) {
        return reconciliationRepository.findOpenByBankAccountIdAndStatementId(tenantId, bankAccountId, statementId)
                .switchIfEmpty(resolveReconciliationReference(tenantId, organizationId)
                        .map(referenceNumber -> Reconciliation.open(tenantId, organizationId, bankAccountId,
                                statementId, referenceNumber))
                        .flatMap(reconciliationRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(reconciliationOpenedEvent(saved))
                                .thenReturn(saved)));
    }

    private void validateAccountScope(BankAccount bankAccount, UUID organizationId) {
        if (!bankAccount.organizationId().equals(organizationId)) {
            throw new TreasuryConsistencyException("bank account does not belong to the provided organization");
        }
    }

    private void validateManualReconciliation(UUID tenantId, BankTransaction transaction, BankStatement statement) {
        if (!transaction.tenantId().equals(tenantId) || !statement.tenantId().equals(tenantId)) {
            throw new TreasuryConsistencyException("transaction and statement must belong to the current tenant");
        }
        if (!transaction.organizationId().equals(statement.organizationId())) {
            throw new TreasuryConsistencyException("transaction and statement must belong to the same organization");
        }
        if (!transaction.bankAccountId().equals(statement.bankAccountId())) {
            throw new TreasuryConsistencyException("transaction and statement must belong to the same bank account");
        }
    }

    private BusinessEvent transactionRecordedEvent(BankTransaction transaction) {
        return BusinessEvent.now(transaction.tenantId(), transaction.organizationId(), "BANK_TRANSACTION_RECORDED",
                "BANK_TRANSACTION", transaction.id(), payload(
                        "bankAccountId", transaction.bankAccountId(),
                        "referenceNumber", transaction.referenceNumber(),
                        "transactionType", transaction.transactionType(),
                        "transactionDate", transaction.transactionDate(),
                        "amount", transaction.amount(),
                        "status", transaction.status()));
    }

    private BusinessEvent transactionReconciledEvent(BankTransaction transaction) {
        return BusinessEvent.now(transaction.tenantId(), transaction.organizationId(), "BANK_TRANSACTION_RECONCILED",
                "BANK_TRANSACTION", transaction.id(), payload(
                        "bankAccountId", transaction.bankAccountId(),
                        "statementId", transaction.statementId(),
                        "referenceNumber", transaction.referenceNumber(),
                        "status", transaction.status(),
                        "reconciledAt", transaction.reconciledAt()));
    }

    private BusinessEvent reconciliationOpenedEvent(Reconciliation reconciliation) {
        return BusinessEvent.now(reconciliation.tenantId(), reconciliation.organizationId(),
                "RECONCILIATION_OPENED", "RECONCILIATION", reconciliation.id(), payload(
                        "referenceNumber", reconciliation.referenceNumber(),
                        "bankAccountId", reconciliation.bankAccountId(),
                        "statementId", reconciliation.statementId(),
                        "status", reconciliation.status()));
    }

    private BusinessEvent autoReconciledEvent(Reconciliation reconciliation, int matchedTransactions) {
        return BusinessEvent.now(reconciliation.tenantId(), reconciliation.organizationId(),
                "RECONCILIATION_AUTO_MATCHED", "RECONCILIATION", reconciliation.id(), payload(
                        "referenceNumber", reconciliation.referenceNumber(),
                        "bankAccountId", reconciliation.bankAccountId(),
                        "statementId", reconciliation.statementId(),
                        "matchedTransactions", matchedTransactions));
    }

    private BusinessEvent manualReconciledEvent(Reconciliation reconciliation, BankTransaction transaction) {
        return BusinessEvent.now(reconciliation.tenantId(), reconciliation.organizationId(),
                "RECONCILIATION_MANUAL_MATCHED", "RECONCILIATION", reconciliation.id(), payload(
                        "referenceNumber", reconciliation.referenceNumber(),
                        "bankAccountId", reconciliation.bankAccountId(),
                        "statementId", reconciliation.statementId(),
                        "transactionId", transaction.id()));
    }

    private Mono<String> resolveReconciliationReference(UUID tenantId, UUID organizationId) {
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(tenantId, organizationId, null,
                        DocumentTypes.RECONCILIATION))
                .onErrorResume(DocumentSequenceNotFoundException.class,
                        exception -> Mono.just("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
