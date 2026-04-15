package yowyob.comops.api.treasury.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.treasury.adapter.out.persistence.BankStatementSpringDataRepository;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyAuditLogEntity;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyAuditLogRepository;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyBankEntity;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyBankRepository;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyStatementLineEntity;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyStatementLineRepository;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyTransactionTypeEntity;
import yowyob.comops.api.treasury.adapter.out.persistence.LegacyTransactionTypeRepository;

@Service
public class LegacyBankingCatalogService {

    private final LegacyBankRepository bankRepository;
    private final LegacyTransactionTypeRepository transactionTypeRepository;
    private final LegacyStatementLineRepository statementLineRepository;
    private final LegacyAuditLogRepository auditLogRepository;
    private final BankStatementSpringDataRepository bankStatementRepository;

    public LegacyBankingCatalogService(LegacyBankRepository bankRepository,
            LegacyTransactionTypeRepository transactionTypeRepository,
            LegacyStatementLineRepository statementLineRepository,
            LegacyAuditLogRepository auditLogRepository,
            BankStatementSpringDataRepository bankStatementRepository) {
        this.bankRepository = bankRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.statementLineRepository = statementLineRepository;
        this.auditLogRepository = auditLogRepository;
        this.bankStatementRepository = bankStatementRepository;
    }

    public Mono<LegacyBankingDtos.BankView> createBank(LegacyBankingDtos.CreateBankRequest request, UUID organizationId) {
        LegacyBankEntity entity = new LegacyBankEntity(UUID.randomUUID(), organizationId, request.code().trim(), request.name().trim(), true, Instant.now());
        return bankRepository.save(entity)
                .flatMap(saved -> audit(saved.organizationId(), "BANK_CREATED", "BANK", saved.id(), saved.code()).thenReturn(saved))
                .map(this::toView);
    }

    public Flux<LegacyBankingDtos.BankView> listBanks(UUID organizationId) {
        return bankRepository.findAll()
                .filter(bank -> organizationId.equals(bank.organizationId()))
                .sort(Comparator.comparing(LegacyBankEntity::createdAt).reversed())
                .map(this::toView);
    }

    public Mono<LegacyBankingDtos.BankView> getBank(UUID bankId, UUID organizationId) {
        return requireBank(bankId, organizationId).map(this::toView);
    }

    public Mono<LegacyBankingDtos.BankView> getBankByCode(String code, UUID organizationId) {
        return listBanks(organizationId)
                .filter(bank -> bank.code().equalsIgnoreCase(code.trim()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("bank not found")));
    }

    public Mono<LegacyBankingDtos.BankView> updateBank(UUID bankId, LegacyBankingDtos.UpdateBankRequest request, UUID organizationId) {
        return requireBank(bankId, organizationId)
                .map(bank -> new LegacyBankEntity(bank.id(), bank.organizationId(), request.code().trim(), request.name().trim(), request.active(), bank.createdAt()))
                .flatMap(bankRepository::save)
                .flatMap(saved -> audit(saved.organizationId(), "BANK_UPDATED", "BANK", saved.id(), saved.code()).thenReturn(saved))
                .map(this::toView);
    }

    public Mono<Void> deleteBank(UUID bankId, UUID organizationId) {
        return requireBank(bankId, organizationId)
                .flatMap(bank -> bankRepository.deleteById(bank.id())
                        .then(audit(bank.organizationId(), "BANK_DELETED", "BANK", bank.id(), "deleted")));
    }

    public Mono<LegacyBankingDtos.TransactionTypeView> createTransactionType(LegacyBankingDtos.CreateTransactionTypeRequest request,
            UUID organizationId) {
        LegacyTransactionTypeEntity entity = new LegacyTransactionTypeEntity(UUID.randomUUID(), organizationId,
                request.code().trim(), request.label().trim(), request.inbound(), Instant.now());
        return transactionTypeRepository.save(entity)
                .flatMap(saved -> audit(saved.organizationId(), "TRANSACTION_TYPE_CREATED", "TRANSACTION_TYPE", saved.id(), saved.code()).thenReturn(saved))
                .map(this::toView);
    }

    public Flux<LegacyBankingDtos.TransactionTypeView> listTransactionTypes(UUID organizationId) {
        return transactionTypeRepository.findAll()
                .filter(type -> organizationId.equals(type.organizationId()))
                .sort(Comparator.comparing(LegacyTransactionTypeEntity::createdAt).reversed())
                .map(this::toView);
    }

    public Mono<LegacyBankingDtos.TransactionTypeView> getTransactionType(UUID id, UUID organizationId) {
        return requireTransactionType(id, organizationId).map(this::toView);
    }

    public Flux<LegacyBankingDtos.TransactionTypeView> listTransactionTypesByCategory(String category, UUID organizationId) {
        String normalized = category.trim().toUpperCase(java.util.Locale.ROOT);
        return listTransactionTypes(organizationId)
                .filter(type -> switch (normalized) {
                    case "INBOUND", "CREDIT" -> type.inbound();
                    case "OUTBOUND", "DEBIT" -> !type.inbound();
                    default -> true;
                });
    }

    public Mono<LegacyBankingDtos.TransactionTypeView> updateTransactionType(UUID id,
            LegacyBankingDtos.UpdateTransactionTypeRequest request,
            UUID organizationId) {
        return requireTransactionType(id, organizationId)
                .map(type -> new LegacyTransactionTypeEntity(type.id(), type.organizationId(), request.code().trim(),
                        request.label().trim(), request.inbound(), type.createdAt()))
                .flatMap(transactionTypeRepository::save)
                .flatMap(saved -> audit(saved.organizationId(), "TRANSACTION_TYPE_UPDATED", "TRANSACTION_TYPE", saved.id(), saved.code()).thenReturn(saved))
                .map(this::toView);
    }

    public Mono<Void> deleteTransactionType(UUID id, UUID organizationId) {
        return requireTransactionType(id, organizationId)
                .flatMap(type -> transactionTypeRepository.deleteById(type.id())
                        .then(audit(type.organizationId(), "TRANSACTION_TYPE_DELETED", "TRANSACTION_TYPE", type.id(), "deleted")));
    }

    public Mono<LegacyBankingDtos.StatementLineView> createStatementLine(LegacyBankingDtos.CreateStatementLineRequest request,
            UUID organizationId) {
        return ensureOwnedStatement(request.statementId(), organizationId)
                .then(Mono.defer(() -> statementLineRepository.save(new LegacyStatementLineEntity(
                        UUID.randomUUID(),
                        request.statementId(),
                        request.reference().trim(),
                        request.amount(),
                        request.currency().trim(),
                        request.direction().trim().toUpperCase(java.util.Locale.ROOT),
                        "NEW",
                        false,
                        Instant.now()))))
                .flatMap(saved -> audit(organizationId, "STATEMENT_LINE_CREATED", "STATEMENT_LINE", saved.id(), saved.reference()).thenReturn(saved))
                .map(this::toView);
    }

    public Flux<LegacyBankingDtos.StatementLineView> createStatementLinesBatch(UUID statementId,
            List<LegacyBankingDtos.CreateStatementLineRequest> lines,
            UUID organizationId) {
        return ensureOwnedStatement(statementId, organizationId)
                .thenMany(Flux.fromIterable(lines)
                        .concatMap(line -> createStatementLine(new LegacyBankingDtos.CreateStatementLineRequest(
                                statementId,
                                line.reference(),
                                line.amount(),
                                line.currency(),
                                line.direction()), organizationId)));
    }

    public Flux<LegacyBankingDtos.StatementLineView> listStatementLines(UUID statementId, UUID organizationId) {
        return ensureOwnedStatement(statementId, organizationId)
                .thenMany(statementLineRepository.findAll()
                        .filter(line -> statementId.equals(line.statementId()))
                        .sort(Comparator.comparing(LegacyStatementLineEntity::createdAt))
                        .map(this::toView));
    }

    public Mono<LegacyBankingDtos.StatementLineView> getStatementLine(UUID lineId, UUID organizationId) {
        return requireStatementLine(lineId, organizationId).map(this::toView);
    }

    public Flux<LegacyBankingDtos.StatementLineView> listMatchedStatementLines(UUID statementId, UUID organizationId) {
        return listStatementLines(statementId, organizationId).filter(LegacyBankingDtos.StatementLineView::reconciled);
    }

    public Flux<LegacyBankingDtos.StatementLineView> listUnmatchedStatementLines(UUID statementId, UUID organizationId) {
        return listStatementLines(statementId, organizationId).filter(line -> !line.reconciled());
    }

    public Mono<Void> deleteStatementLine(UUID lineId, UUID organizationId) {
        return requireStatementLine(lineId, organizationId)
                .flatMap(line -> statementLineRepository.deleteById(line.id())
                        .then(audit(organizationId, "STATEMENT_LINE_DELETED", "STATEMENT_LINE", line.id(), "deleted")));
    }

    public Mono<LegacyBankingDtos.StatementLineView> ignoreStatementLine(UUID lineId, UUID organizationId) {
        return updateStatementLineStatus(lineId, organizationId, "IGNORED", false, "STATEMENT_LINE_IGNORED");
    }

    public Mono<LegacyBankingDtos.StatementLineView> resetStatementLine(UUID lineId, UUID organizationId) {
        return updateStatementLineStatus(lineId, organizationId, "NEW", false, "STATEMENT_LINE_RESET");
    }

    public Flux<LegacyBankingDtos.AuditLogView> listAuditLogs(UUID organizationId) {
        return auditLogRepository.findAll()
                .filter(log -> organizationId.equals(log.organizationId()))
                .sort(Comparator.comparing(LegacyAuditLogEntity::createdAt).reversed())
                .map(this::toView);
    }

    public Mono<LegacyBankingDtos.AuditLogView> getAuditLog(UUID auditId, UUID organizationId) {
        return requireAuditLog(auditId, organizationId).map(this::toView);
    }

    public Mono<Long> countAuditLogs(UUID organizationId) {
        return listAuditLogs(organizationId).count();
    }

    public Flux<LegacyBankingDtos.AuditLogView> listAuditLogsByEntity(UUID entityId, UUID organizationId) {
        return listAuditLogs(organizationId).filter(log -> entityId.equals(log.targetId()));
    }

    public Flux<LegacyBankingDtos.AuditLogView> listTodayAuditLogs(UUID organizationId) {
        Instant cutoff = Instant.now().minusSeconds(24 * 3600);
        return listAuditLogs(organizationId).filter(log -> log.createdAt().isAfter(cutoff));
    }

    public Mono<Set<String>> listAuditModules(UUID organizationId) {
        return listAuditLogs(organizationId).map(LegacyBankingDtos.AuditLogView::targetType).collect(Collectors.toSet());
    }

    public Mono<Set<String>> listAuditActions(UUID organizationId) {
        return listAuditLogs(organizationId).map(LegacyBankingDtos.AuditLogView::action).collect(Collectors.toSet());
    }

    private Mono<LegacyBankingDtos.StatementLineView> updateStatementLineStatus(UUID lineId,
            UUID organizationId,
            String status,
            boolean reconciled,
            String auditAction) {
        return requireStatementLine(lineId, organizationId)
                .map(line -> new LegacyStatementLineEntity(line.id(), line.statementId(), line.reference(), line.amount(),
                        line.currency(), line.direction(), status, reconciled, line.createdAt()))
                .flatMap(statementLineRepository::save)
                .flatMap(saved -> audit(organizationId, auditAction, "STATEMENT_LINE", saved.id(), saved.reference()).thenReturn(saved))
                .map(this::toView);
    }

    private Mono<LegacyBankEntity> requireBank(UUID bankId, UUID organizationId) {
        return bankRepository.findById(bankId)
                .filter(bank -> organizationId.equals(bank.organizationId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("bank not found")));
    }

    private Mono<LegacyTransactionTypeEntity> requireTransactionType(UUID id, UUID organizationId) {
        return transactionTypeRepository.findById(id)
                .filter(type -> organizationId.equals(type.organizationId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("transaction type not found")));
    }

    private Mono<LegacyStatementLineEntity> requireStatementLine(UUID lineId, UUID organizationId) {
        return statementLineRepository.findById(lineId)
                .flatMap(line -> ensureOwnedStatement(line.statementId(), organizationId).thenReturn(line))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("statement line not found")));
    }

    private Mono<LegacyAuditLogEntity> requireAuditLog(UUID auditId, UUID organizationId) {
        return auditLogRepository.findById(auditId)
                .filter(log -> organizationId.equals(log.organizationId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("audit log not found")));
    }

    private Mono<Void> ensureOwnedStatement(UUID statementId, UUID organizationId) {
        return bankStatementRepository.findById(statementId)
                .filter(statement -> organizationId.equals(statement.organizationId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("statement not found")))
                .then();
    }

    private Mono<Void> audit(UUID organizationId, String action, String targetType, UUID targetId, String details) {
        return auditLogRepository.save(new LegacyAuditLogEntity(UUID.randomUUID(), organizationId, action, targetType, targetId, details, Instant.now()))
                .then();
    }

    private LegacyBankingDtos.BankView toView(LegacyBankEntity entity) {
        return new LegacyBankingDtos.BankView(entity.id(), entity.organizationId(), entity.code(), entity.name(), entity.active(), entity.createdAt());
    }

    private LegacyBankingDtos.TransactionTypeView toView(LegacyTransactionTypeEntity entity) {
        return new LegacyBankingDtos.TransactionTypeView(entity.id(), entity.organizationId(), entity.code(), entity.label(), entity.inbound(), entity.createdAt());
    }

    private LegacyBankingDtos.StatementLineView toView(LegacyStatementLineEntity entity) {
        return new LegacyBankingDtos.StatementLineView(entity.id(), entity.statementId(), entity.reference(), entity.amount(), entity.currency(), entity.direction(), entity.status(), entity.reconciled(), entity.createdAt());
    }

    private LegacyBankingDtos.AuditLogView toView(LegacyAuditLogEntity entity) {
        return new LegacyBankingDtos.AuditLogView(entity.id(), entity.organizationId(), entity.action(), entity.targetType(), entity.targetId(), entity.details(), entity.createdAt());
    }
}
