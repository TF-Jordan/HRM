package yowyob.comops.api.billing.adapter.in.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.billing.application.service.BillingOperationsService;
import yowyob.comops.api.billing.application.service.BillingRequestContext;
import yowyob.comops.api.billing.web.BillingOperationsRequests;
import yowyob.comops.api.billing.web.BillingOperationsViews;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
public class BillingLegacyDocumentsController {

    private final BillingOperationsService billingOperationsService;

    public BillingLegacyDocumentsController(BillingOperationsService billingOperationsService) {
        this.billingOperationsService = billingOperationsService;
    }

    @PostMapping({
            "/api/bons-achat",
            "/api/bon-commande",
            "/api/bons-livraison",
            "/api/v1/facturation/bon-receptions",
            "/api/facture-fournisseurs",
            "/api/factures-proforma",
            "/api/v1/facturation/note-credits"
    })
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<BillingOperationsViews.CommercialDocumentView>> create(
            ServerHttpRequest request,
            @Valid @RequestBody Mono<BillingOperationsRequests.CreateCommercialDocumentRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> billingOperationsService.createDocument(resolveType(request), tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({
            "/api/bons-achat",
            "/api/bon-commande",
            "/api/bons-livraison",
            "/api/v1/facturation/bon-receptions",
            "/api/facture-fournisseurs",
            "/api/factures-proforma",
            "/api/v1/facturation/note-credits"
    })
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<List<BillingOperationsViews.CommercialDocumentView>> list(ServerHttpRequest request) {
        return context()
                .flatMapMany(ctx -> billingOperationsService.listDocuments(resolveType(request), ctx))
                .collectList();
    }

    @GetMapping({
            "/api/bons-achat/{id}",
            "/api/bon-commande/{id}",
            "/api/bons-livraison/{id}",
            "/api/v1/facturation/bon-receptions/{id}",
            "/api/facture-fournisseurs/{id}",
            "/api/factures-proforma/{id}",
            "/api/v1/facturation/note-credits/{id}"
    })
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<BillingOperationsViews.CommercialDocumentView> get(ServerHttpRequest request, @PathVariable("id") UUID documentId) {
        return context().flatMap(ctx -> billingOperationsService.getDocument(resolveType(request), documentId, ctx));
    }

    @PutMapping({
            "/api/bons-achat/{id}",
            "/api/bon-commande/{id}",
            "/api/bons-livraison/{id}",
            "/api/v1/facturation/bon-receptions/{id}",
            "/api/facture-fournisseurs/{id}",
            "/api/factures-proforma/{id}",
            "/api/v1/facturation/note-credits/{id}"
    })
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<BillingOperationsViews.CommercialDocumentView> update(ServerHttpRequest request,
            @PathVariable("id") UUID documentId,
            @Valid @RequestBody Mono<BillingOperationsRequests.CreateCommercialDocumentRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> billingOperationsService.updateDocument(resolveType(request), documentId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping({
            "/api/bons-achat/{id}",
            "/api/bons-livraison/{id}",
            "/api/v1/facturation/bon-receptions/{id}",
            "/api/factures-proforma/{id}",
            "/api/v1/facturation/note-credits/{id}"
    })
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<ResponseEntity<Void>> delete(ServerHttpRequest request, @PathVariable("id") UUID documentId) {
        return context().flatMap(ctx -> billingOperationsService.deleteDocument(resolveType(request), documentId, ctx))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping({
            "/api/bons-livraison/client/{idClient}",
            "/api/factures-proforma/client/{idClient}"
    })
    @PreAuthorize("@businessAccessPolicy.canReadAccounting(authentication)")
    public Mono<List<BillingOperationsViews.CommercialDocumentView>> listByClient(ServerHttpRequest request,
            @PathVariable("idClient") UUID counterpartyId) {
        return context()
                .flatMapMany(ctx -> billingOperationsService.listDocumentsByCounterparty(resolveType(request), counterpartyId, ctx))
                .collectList();
    }

    @PostMapping("/api/bons-livraison/{id}/effectuer")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<BillingOperationsViews.CommercialDocumentView> performDelivery(@PathVariable("id") UUID documentId) {
        return context().flatMap(ctx -> billingOperationsService.transitionDocument(
                BillingOperationsService.DocumentType.DELIVERY_NOTE,
                documentId,
                BillingOperationsService.DocumentStatus.FULFILLED,
                ctx));
    }

    @PostMapping({
            "/api/bons-achat/{id}/sync/accounting-invoice",
            "/api/bon-commande/{id}/sync/accounting-invoice",
            "/api/bons-livraison/{id}/sync/accounting-invoice",
            "/api/v1/facturation/bon-receptions/{id}/sync/accounting-invoice",
            "/api/facture-fournisseurs/{id}/sync/accounting-invoice",
            "/api/factures-proforma/{id}/sync/accounting-invoice",
            "/api/v1/facturation/note-credits/{id}/sync/accounting-invoice"
    })
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'accounting:write')")
    public Mono<BillingOperationsViews.CommercialDocumentView> syncToAccounting(ServerHttpRequest request,
            @PathVariable("id") UUID documentId,
            @RequestBody(required = false) Mono<BillingOperationsRequests.SyncDocumentToAccountingRequest> requestMono) {
        Mono<BillingOperationsRequests.SyncDocumentToAccountingRequest> safeRequest = requestMono == null
                ? Mono.just(new BillingOperationsRequests.SyncDocumentToAccountingRequest(null, true))
                : requestMono.defaultIfEmpty(new BillingOperationsRequests.SyncDocumentToAccountingRequest(null, true));
        return context().zipWith(safeRequest)
                .flatMap(tuple -> billingOperationsService.syncDocumentToAccounting(resolveType(request), documentId,
                        tuple.getT2(), tuple.getT1()));
    }

    @PostMapping({
            "/api/bons-achat/{id}/sync/cashier-bill",
            "/api/bon-commande/{id}/sync/cashier-bill",
            "/api/bons-livraison/{id}/sync/cashier-bill",
            "/api/v1/facturation/bon-receptions/{id}/sync/cashier-bill",
            "/api/facture-fournisseurs/{id}/sync/cashier-bill",
            "/api/factures-proforma/{id}/sync/cashier-bill",
            "/api/v1/facturation/note-credits/{id}/sync/cashier-bill"
    })
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'cashier:write')")
    public Mono<BillingOperationsViews.CommercialDocumentView> syncToCashier(ServerHttpRequest request,
            @PathVariable("id") UUID documentId) {
        return context().flatMap(ctx -> billingOperationsService.syncDocumentToCashier(resolveType(request), documentId, ctx));
    }

    @PostMapping({
            "/api/bons-achat/{id}/payments/cashier",
            "/api/bon-commande/{id}/payments/cashier",
            "/api/bons-livraison/{id}/payments/cashier",
            "/api/v1/facturation/bon-receptions/{id}/payments/cashier",
            "/api/facture-fournisseurs/{id}/payments/cashier",
            "/api/factures-proforma/{id}/payments/cashier",
            "/api/v1/facturation/note-credits/{id}/payments/cashier"
    })
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'cashier:write')")
    public Mono<ResponseEntity<BillingOperationsViews.PaymentView>> payViaCashier(ServerHttpRequest request,
            @PathVariable("id") UUID documentId,
            @Valid @RequestBody Mono<BillingOperationsRequests.RecordCashierPaymentRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> billingOperationsService.recordDocumentCashierPayment(resolveType(request), documentId,
                        tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping({
            "/api/bons-achat/{id}/payments/bank",
            "/api/bon-commande/{id}/payments/bank",
            "/api/bons-livraison/{id}/payments/bank",
            "/api/v1/facturation/bon-receptions/{id}/payments/bank",
            "/api/facture-fournisseurs/{id}/payments/bank",
            "/api/factures-proforma/{id}/payments/bank",
            "/api/v1/facturation/note-credits/{id}/payments/bank"
    })
    @PreAuthorize("@businessAccessPolicy.canRegisterInvoiceSettlement(authentication)")
    public Mono<ResponseEntity<BillingOperationsViews.PaymentView>> payViaBank(ServerHttpRequest request,
            @PathVariable("id") UUID documentId,
            @Valid @RequestBody Mono<BillingOperationsRequests.RecordBankSettlementRequest> requestMono) {
        return context().zipWith(requestMono)
                .flatMap(tuple -> billingOperationsService.recordDocumentBankSettlement(resolveType(request), documentId,
                        tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    private Mono<BillingRequestContext> context() {
        return ReactiveRequestContextHolder.getRequiredContext().map(BillingRequestContext::from);
    }

    private BillingOperationsService.DocumentType resolveType(ServerHttpRequest request) {
        String path = request.getPath().pathWithinApplication().value();
        if (path.contains("/bons-achat")) {
            return BillingOperationsService.DocumentType.PURCHASE_VOUCHER;
        }
        if (path.contains("/bon-commande")) {
            return BillingOperationsService.DocumentType.PURCHASE_ORDER;
        }
        if (path.contains("/bons-livraison")) {
            return BillingOperationsService.DocumentType.DELIVERY_NOTE;
        }
        if (path.contains("/bon-receptions")) {
            return BillingOperationsService.DocumentType.PURCHASE_RECEIPT;
        }
        if (path.contains("/facture-fournisseurs")) {
            return BillingOperationsService.DocumentType.SUPPLIER_INVOICE;
        }
        if (path.contains("/factures-proforma")) {
            return BillingOperationsService.DocumentType.PROFORMA_INVOICE;
        }
        if (path.contains("/note-credits")) {
            return BillingOperationsService.DocumentType.CREDIT_NOTE;
        }
        throw new IllegalArgumentException("unsupported billing document path " + path);
    }
}
