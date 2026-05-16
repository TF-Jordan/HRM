package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
public class LegacyBankingCatalogController {

    private final LegacyBankingCatalogService service;

    public LegacyBankingCatalogController(LegacyBankingCatalogService service) {
        this.service = service;
    }

    @GetMapping("/api/banks")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.BankView>> listBanks() {
        return organizationId().flatMapMany(service::listBanks).collectList();
    }

    @GetMapping("/api/banks/{id}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<LegacyBankingDtos.BankView> getBank(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.getBank(id, orgId));
    }

    @GetMapping("/api/banks/code/{code}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<LegacyBankingDtos.BankView> getBankByCode(@PathVariable String code) {
        return organizationId().flatMap(orgId -> service.getBankByCode(code, orgId));
    }

    @PostMapping("/api/banks")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<LegacyBankingDtos.BankView>> createBank(@Valid @RequestBody Mono<LegacyBankingDtos.CreateBankRequest> requestMono) {
        return organizationId().zipWith(requestMono)
                .flatMap(tuple -> service.createBank(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/api/banks/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<LegacyBankingDtos.BankView> updateBank(@PathVariable("id") UUID id,
            @Valid @RequestBody Mono<LegacyBankingDtos.UpdateBankRequest> requestMono) {
        return organizationId().zipWith(requestMono)
                .flatMap(tuple -> service.updateBank(id, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping("/api/banks/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<Void>> deleteBank(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.deleteBank(id, orgId)).thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/api/transaction-types")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.TransactionTypeView>> listTransactionTypes() {
        return organizationId().flatMapMany(service::listTransactionTypes).collectList();
    }

    @GetMapping("/api/transaction-types/{id}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<LegacyBankingDtos.TransactionTypeView> getTransactionType(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.getTransactionType(id, orgId));
    }

    @GetMapping("/api/transaction-types/category/{category}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.TransactionTypeView>> listTransactionTypesByCategory(@PathVariable String category) {
        return organizationId().flatMapMany(orgId -> service.listTransactionTypesByCategory(category, orgId)).collectList();
    }

    @PostMapping("/api/transaction-types")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<LegacyBankingDtos.TransactionTypeView>> createTransactionType(@Valid @RequestBody Mono<LegacyBankingDtos.CreateTransactionTypeRequest> requestMono) {
        return organizationId().zipWith(requestMono)
                .flatMap(tuple -> service.createTransactionType(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/api/transaction-types/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<LegacyBankingDtos.TransactionTypeView> updateTransactionType(@PathVariable("id") UUID id,
            @Valid @RequestBody Mono<LegacyBankingDtos.UpdateTransactionTypeRequest> requestMono) {
        return organizationId().zipWith(requestMono)
                .flatMap(tuple -> service.updateTransactionType(id, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping("/api/transaction-types/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<Void>> deleteTransactionType(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.deleteTransactionType(id, orgId)).thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/api/statement-lines/statement/{statementId}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.StatementLineView>> listStatementLines(@PathVariable UUID statementId) {
        return organizationId().flatMapMany(orgId -> service.listStatementLines(statementId, orgId)).collectList();
    }

    @GetMapping("/api/statement-lines/{id}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<LegacyBankingDtos.StatementLineView> getStatementLine(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.getStatementLine(id, orgId));
    }

    @GetMapping("/api/statement-lines/statement/{statementId}/unmatched")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.StatementLineView>> listUnmatchedStatementLines(@PathVariable UUID statementId) {
        return organizationId().flatMapMany(orgId -> service.listUnmatchedStatementLines(statementId, orgId)).collectList();
    }

    @GetMapping("/api/statement-lines/statement/{statementId}/matched")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.StatementLineView>> listMatchedStatementLines(@PathVariable UUID statementId) {
        return organizationId().flatMapMany(orgId -> service.listMatchedStatementLines(statementId, orgId)).collectList();
    }

    @PostMapping("/api/statement-lines")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<LegacyBankingDtos.StatementLineView>> createStatementLine(@Valid @RequestBody Mono<LegacyBankingDtos.CreateStatementLineRequest> requestMono) {
        return organizationId().zipWith(requestMono)
                .flatMap(tuple -> service.createStatementLine(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/api/statement-lines/statement/{statementId}/batch")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<List<LegacyBankingDtos.StatementLineView>> createBatch(@PathVariable UUID statementId,
            @Valid @RequestBody Mono<LegacyBankingDtos.BatchStatementLinesRequest> requestMono) {
        return organizationId().zipWith(requestMono)
                .flatMapMany(tuple -> service.createStatementLinesBatch(statementId, tuple.getT2().lines(), tuple.getT1()))
                .collectList();
    }

    @DeleteMapping("/api/statement-lines/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<Void>> deleteStatementLine(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.deleteStatementLine(id, orgId)).thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/api/statement-lines/{id}/ignore")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<LegacyBankingDtos.StatementLineView> ignoreStatementLine(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.ignoreStatementLine(id, orgId));
    }

    @PostMapping("/api/statement-lines/{id}/reset")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<LegacyBankingDtos.StatementLineView> resetStatementLine(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.resetStatementLine(id, orgId));
    }

    @GetMapping("/api/audit-logs")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.AuditLogView>> listAuditLogs() {
        return organizationId().flatMapMany(service::listAuditLogs).collectList();
    }

    @GetMapping("/api/audit-logs/count")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<Long> countAuditLogs() {
        return organizationId().flatMap(service::countAuditLogs);
    }

    @GetMapping("/api/audit-logs/{id}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<LegacyBankingDtos.AuditLogView> getAuditLog(@PathVariable("id") UUID id) {
        return organizationId().flatMap(orgId -> service.getAuditLog(id, orgId));
    }

    @GetMapping("/api/audit-logs/entity/{entityId}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.AuditLogView>> listByEntity(@PathVariable UUID entityId) {
        return organizationId().flatMapMany(orgId -> service.listAuditLogsByEntity(entityId, orgId)).collectList();
    }

    @GetMapping("/api/audit-logs/today")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<LegacyBankingDtos.AuditLogView>> listToday() {
        return organizationId().flatMapMany(service::listTodayAuditLogs).collectList();
    }

    @GetMapping("/api/audit-logs/modules")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<Set<String>> listModules() {
        return organizationId().flatMap(service::listAuditModules);
    }

    @GetMapping("/api/audit-logs/actions")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<Set<String>> listActions() {
        return organizationId().flatMap(service::listAuditActions);
    }

    private Mono<UUID> organizationId() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .map(context -> context.organizationId())
                .filter(id -> id != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization context is required")));
    }
}
