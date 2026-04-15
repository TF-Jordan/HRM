package yowyob.comops.api.cashier.adapter.in.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
import yowyob.comops.api.cashier.application.service.CashierOperationsService;
import yowyob.comops.api.cashier.application.service.CashierRequestContext;
import yowyob.comops.api.cashier.web.CashierRequests;
import yowyob.comops.api.cashier.web.CashierViews;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
public class CashierOperationsController {

    private final CashierOperationsService cashierOperationsService;

    public CashierOperationsController(CashierOperationsService cashierOperationsService) {
        this.cashierOperationsService = cashierOperationsService;
    }

    @GetMapping("/api/admin/accounts")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.WalletAccountView>> adminAccounts() {
        return context().flatMapMany(cashierOperationsService::listAccounts).collectList();
    }

    @GetMapping("/api/cashier/accounts")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.WalletAccountView>> cashierAccounts() {
        return adminAccounts();
    }

    @PostMapping("/api/accounts/transfer")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.WalletAccountView> transfer(@Valid @RequestBody Mono<CashierRequests.TransferRequest> requestMono) {
        return context().zipWith(requestMono).flatMap(tuple -> cashierOperationsService.transfer(tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/api/accounts/withdraw")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.WalletAccountView> withdraw(@Valid @RequestBody Mono<CashierRequests.WithdrawRequest> requestMono) {
        return context().zipWith(requestMono).flatMap(tuple -> cashierOperationsService.withdraw(tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/api/accounts/transfer-p2p")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.WalletAccountView> transferP2P(@Valid @RequestBody Mono<CashierRequests.P2PTransferRequest> requestMono) {
        return context().zipWith(requestMono).flatMap(tuple -> cashierOperationsService.transferP2P(tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/api/cashier/fund-requests")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.FundRequestView>> createFundRequest(@Valid @RequestBody Mono<CashierRequests.CreateFundRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.createFundRequest(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/cashier/bills")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.BillView>> cashierBills() {
        return context().flatMapMany(cashierOperationsService::listBills).collectList();
    }

    @GetMapping("/api/cashier/bills/{id}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<CashierViews.BillView> cashierBill(@PathVariable("id") UUID billId) {
        return context().flatMap(ctx -> cashierOperationsService.getBill(billId, ctx));
    }

    @GetMapping("/api/bills")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.BillView>> bills() {
        return cashierBills();
    }

    @PostMapping("/api/bills")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.BillView>> createBill(@Valid @RequestBody Mono<CashierRequests.CreateBillRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.createBill(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/api/bills/pay")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.BillView> payBill(@RequestParam("billId") UUID billId,
            @Valid @RequestBody Mono<CashierRequests.PayBillRequest> requestMono) {
        return context().zipWith(requestMono).flatMap(tuple -> cashierOperationsService.payBill(billId, tuple.getT2(), tuple.getT1()));
    }

    @GetMapping("/api/cash-registers")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashRegisterView>> cashRegisters() {
        return context().flatMapMany(cashierOperationsService::listCashRegisters).collectList();
    }

    @PostMapping("/api/cash-registers")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.CashRegisterView>> createCashRegister(
            @Valid @RequestBody Mono<CashierRequests.CreateCashRegisterRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.createCashRegister(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/cash-registers/{registerId}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<CashierViews.CashRegisterView> cashRegister(@PathVariable UUID registerId) {
        return context().flatMap(ctx -> cashierOperationsService.getCashRegister(registerId, ctx));
    }

    @PutMapping("/api/cash-registers/{registerId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashRegisterView> updateCashRegister(@PathVariable UUID registerId,
            @Valid @RequestBody Mono<CashierRequests.UpdateCashRegisterRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.updateCashRegister(registerId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping("/api/cash-registers/{registerId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<Void>> deleteCashRegister(@PathVariable UUID registerId) {
        return context().flatMap(ctx -> cashierOperationsService.deleteCashRegister(registerId, ctx))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/api/cash-registers/{registerId}/assign")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashRegisterView> assignCashRegister(@PathVariable UUID registerId,
            @Valid @RequestBody Mono<CashierRequests.AssignCashRegisterRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.assignCashRegister(registerId, tuple.getT2().cashierId(), tuple.getT1()));
    }

    @GetMapping("/api/cashiers")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashierProfileView>> cashiers() {
        return context().flatMapMany(cashierOperationsService::listCashiers).collectList();
    }

    @GetMapping("/api/cashiers/available")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashierProfileView>> availableCashiers() {
        return context().flatMapMany(cashierOperationsService::listAvailableCashiers).collectList();
    }

    @GetMapping("/api/cashiers/with-profile")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashierProfileView>> cashiersWithProfile() {
        return context().flatMapMany(cashierOperationsService::listCashiersWithProfile).collectList();
    }

    @GetMapping("/api/cashiers/self-profile")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<CashierViews.CashierProfileView> selfProfile(@RequestParam("principalEmail") String principalEmail) {
        return context().flatMap(ctx -> cashierOperationsService.findProfileByEmail(principalEmail, ctx));
    }

    @PutMapping("/api/cashiers/self-profile")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<CashierViews.CashierProfileView> updateSelfProfile(
            @RequestParam("principalEmail") String principalEmail,
            @Valid @RequestBody Mono<CashierRequests.UpdateMyProfileRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.upsertSelfProfile(principalEmail, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/api/cashiers")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.CashierProfileView>> createCashier(
            @Valid @RequestBody Mono<CashierRequests.CreateCashierProfileRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.createProfile(tuple.getT2(), CashierOperationsService.KIND_CASHIER, tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/api/cashiers/{cashierId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashierProfileView> updateCashier(@PathVariable UUID cashierId,
            @Valid @RequestBody Mono<CashierRequests.UpdateCashierProfileRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.updateProfile(cashierId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping("/api/cashiers/{cashierId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<Void>> deleteCashier(@PathVariable UUID cashierId) {
        return context().flatMap(ctx -> cashierOperationsService.deleteProfile(cashierId, ctx))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/api/cashier/sessions")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashierSessionView>> cashierSessions() {
        return context().flatMapMany(cashierOperationsService::listCashierSessions).collectList();
    }

    @GetMapping({"/api/dashboard/stats", "/api/dashboard/stat"})
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<CashierViews.CashDashboardView> dashboard() {
        return context().flatMap(cashierOperationsService::dashboard);
    }

    @GetMapping("/api/admin/documents")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashDocumentView>> documents() {
        return context().flatMapMany(cashierOperationsService::listDocuments).collectList();
    }

    @GetMapping("/api/config/denominations")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.DenominationView>> denominations() {
        return Mono.just(cashierOperationsService.denominations());
    }

    @GetMapping("/api/cashier/movements")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashMovementView>> movements() {
        return context().flatMapMany(cashierOperationsService::listMovements).collectList();
    }

    @PostMapping("/api/movements/transfer")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashMovementView> transferMovement(@Valid @RequestBody Mono<CashierRequests.CreateMovementRequest> requestMono) {
        return context().zipWith(requestMono).flatMap(tuple -> cashierOperationsService.createMovement(tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/api/movements/{movementId}/account")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashMovementView> attachMovementAccount(@PathVariable UUID movementId,
            @RequestParam("accountId") UUID accountId) {
        return context().flatMap(ctx -> cashierOperationsService.attachMovementAccount(movementId, accountId, ctx));
    }

    @GetMapping("/api/transactions")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashMovementView>> transactions() {
        return movements();
    }

    @GetMapping("/api/transactions/recent")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashMovementView>> recentTransactions() {
        return context().flatMapMany(cashierOperationsService::listRecentTransactions).collectList();
    }

    @GetMapping("/api/notifications")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashNotificationView>> notifications() {
        return context().flatMapMany(cashierOperationsService::listNotifications).collectList();
    }

    @PostMapping("/api/notifications/test")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.CashNotificationView>> testNotification(
            @Valid @RequestBody Mono<CashierRequests.CreateNotificationRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.createNotification(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/audit")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashAuditEntryView>> audit() {
        return context().flatMapMany(cashierOperationsService::listAuditEntries).collectList();
    }

    @PostMapping("/api/audit")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.CashAuditEntryView>> manualAudit(
            @Valid @RequestBody Mono<CashierRequests.CreateAuditEntryRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.createAuditEntry(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/admin/reconciliations")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashReconciliationView>> adminReconciliations() {
        return context().flatMapMany(cashierOperationsService::listReconciliations).collectList();
    }

    @GetMapping("/api/cashier/reconciliations")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashReconciliationView>> cashierReconciliations() {
        return adminReconciliations();
    }

    @PostMapping("/api/reconciliations/{reconciliationId}/review")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashReconciliationView> review(@PathVariable UUID reconciliationId,
            @Valid @RequestBody Mono<CashierRequests.ReviewReconciliationRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.reviewReconciliation(reconciliationId, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/api/reconciliations/{reconciliationId}/justify")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashReconciliationView> justify(@PathVariable UUID reconciliationId,
            @Valid @RequestBody Mono<CashierRequests.JustifyReconciliationRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.justifyReconciliation(reconciliationId, tuple.getT2(), tuple.getT1()));
    }

    @GetMapping("/api/reports/transactions")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<CashierViews.CashReportView> reportTransactions() {
        return context().flatMap(cashierOperationsService::reportTransactions);
    }

    @PostMapping("/api/reports/register/{registerId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashReportView> reportRegister(@PathVariable UUID registerId) {
        return context().flatMap(ctx -> cashierOperationsService.reportRegister(registerId, ctx));
    }

    @GetMapping("/api/reports/session/{sessionId}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<CashierViews.CashReportView> reportSession(@PathVariable UUID sessionId) {
        return context().flatMap(ctx -> cashierOperationsService.reportSession(sessionId, ctx));
    }

    @GetMapping("/api/reports/audit")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<CashierViews.CashReportView> reportAudit() {
        return context().flatMap(cashierOperationsService::reportAudit);
    }

    @GetMapping("/api/sessions")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<List<CashierViews.CashierSessionView>> sessions() {
        return context().flatMapMany(cashierOperationsService::listSessions).collectList();
    }

    @PostMapping("/api/sessions")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.CashierSessionView>> createSession(
            @Valid @RequestBody Mono<CashierRequests.CreateSessionRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.openSession(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/api/sessions/{sessionId}/close")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashierSessionView> closeSession(@PathVariable UUID sessionId,
            @Valid @RequestBody Mono<CashierRequests.CloseSessionRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.closeSession(sessionId, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/api/sessions/{sessionId}/lock")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashierSessionView> lockSession(@PathVariable UUID sessionId) {
        return context().flatMap(ctx -> cashierOperationsService.lockSession(sessionId, ctx));
    }

    @DeleteMapping("/api/sessions/{sessionId}/lock")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<CashierViews.CashierSessionView> unlockSession(@PathVariable UUID sessionId) {
        return context().flatMap(ctx -> cashierOperationsService.unlockSession(sessionId, ctx));
    }

    @PostMapping("/api/notify-unauthorized")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
    public Mono<ResponseEntity<CashierViews.CashNotificationView>> notifyUnauthorized(
            @Valid @RequestBody Mono<CashierRequests.NotifyUnauthorizedRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> cashierOperationsService.notifyUnauthorized(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    private Mono<CashierRequestContext> context() {
        return ReactiveRequestContextHolder.getRequiredContext().map(CashierRequestContext::from);
    }
}
