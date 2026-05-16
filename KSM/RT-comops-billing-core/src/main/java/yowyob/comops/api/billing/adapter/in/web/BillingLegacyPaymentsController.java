package yowyob.comops.api.billing.adapter.in.web;

import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.billing.application.service.BillingOperationsService;
import yowyob.comops.api.billing.application.service.BillingRequestContext;
import yowyob.comops.api.billing.web.BillingOperationsRequests;
import yowyob.comops.api.billing.web.BillingOperationsViews;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
@RequestMapping("/api/paiement")
public class BillingLegacyPaymentsController {

    private final BillingOperationsService billingOperationsService;

    public BillingLegacyPaymentsController(BillingOperationsService billingOperationsService) {
        this.billingOperationsService = billingOperationsService;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<BillingOperationsViews.PaymentView>> create(
            @Valid @RequestBody Mono<BillingOperationsRequests.CreatePaymentRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> billingOperationsService.createPayment(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<BillingOperationsViews.PaymentView> update(@PathVariable("id") UUID paymentId,
            @Valid @RequestBody Mono<BillingOperationsRequests.UpdatePaymentRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> billingOperationsService.updatePayment(paymentId, tuple.getT2(), tuple.getT1()));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<List<BillingOperationsViews.PaymentView>> list() {
        return context().flatMapMany(billingOperationsService::listPayments).collectList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<BillingOperationsViews.PaymentView> getById(@PathVariable("id") UUID paymentId) {
        return context().flatMap(ctx -> billingOperationsService.getPayment(paymentId, ctx));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<List<BillingOperationsViews.PaymentView>> listByClient(@PathVariable("clientId") UUID customerId) {
        return context().flatMapMany(ctx -> billingOperationsService.listPaymentsByCustomer(customerId, ctx)).collectList();
    }

    @GetMapping("/facture/{factureId}")
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<List<BillingOperationsViews.PaymentView>> listByInvoice(@PathVariable("factureId") UUID invoiceId) {
        return context().flatMapMany(ctx -> billingOperationsService.listPaymentsByInvoice(invoiceId, ctx)).collectList();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") UUID paymentId) {
        return context().flatMap(ctx -> billingOperationsService.deletePayment(paymentId, ctx))
                .thenReturn(ResponseEntity.noContent().build());
    }

    private Mono<BillingRequestContext> context() {
        return ReactiveRequestContextHolder.getRequiredContext().map(BillingRequestContext::from);
    }
}
