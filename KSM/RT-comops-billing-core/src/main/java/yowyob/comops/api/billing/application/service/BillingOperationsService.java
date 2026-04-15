package yowyob.comops.api.billing.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.billing.persistence.CommercialDocumentEntity;
import yowyob.comops.api.billing.persistence.CommercialDocumentLineEntity;
import yowyob.comops.api.billing.persistence.CommercialDocumentLineRepository;
import yowyob.comops.api.billing.persistence.CommercialDocumentRepository;
import yowyob.comops.api.billing.persistence.PaymentEntity;
import yowyob.comops.api.billing.persistence.PaymentRepository;
import yowyob.comops.api.billing.web.BillingInvoiceLineRequest;
import yowyob.comops.api.billing.web.BillingOperationsRequests;
import yowyob.comops.api.billing.web.BillingOperationsViews;
import yowyob.comops.api.product.application.port.in.GetProductUseCase;
import yowyob.comops.api.product.domain.model.Product;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.domain.model.ThirdParty;

@Service
public class BillingOperationsService {

    private final GetThirdPartyUseCase getThirdPartyUseCase;
    private final GetProductUseCase getProductUseCase;
    private final CommercialDocumentRepository commercialDocumentRepository;
    private final CommercialDocumentLineRepository commercialDocumentLineRepository;
    private final PaymentRepository paymentRepository;

    public BillingOperationsService(GetThirdPartyUseCase getThirdPartyUseCase,
            GetProductUseCase getProductUseCase,
            CommercialDocumentRepository commercialDocumentRepository,
            CommercialDocumentLineRepository commercialDocumentLineRepository,
            PaymentRepository paymentRepository) {
        this.getThirdPartyUseCase = getThirdPartyUseCase;
        this.getProductUseCase = getProductUseCase;
        this.commercialDocumentRepository = commercialDocumentRepository;
        this.commercialDocumentLineRepository = commercialDocumentLineRepository;
        this.paymentRepository = paymentRepository;
    }

    public Mono<BillingOperationsViews.CommercialDocumentView> createDocument(DocumentType type,
            BillingOperationsRequests.CreateCommercialDocumentRequest request,
            BillingRequestContext context) {
        return nextNumber(context.requireOrganizationId(), type, request.documentNumber())
                .flatMap(number -> {
                    CommercialDocumentEntity entity = new CommercialDocumentEntity(
                            UUID.randomUUID(),
                            context.requireOrganizationId(),
                            type.name(),
                            number,
                            request.counterpartyThirdPartyId(),
                            request.currency().trim(),
                            DocumentStatus.DRAFT.name(),
                            Instant.now());
                    return commercialDocumentRepository.save(entity)
                            .flatMap(saved -> replaceLines(saved.id(), request.lines())
                                    .then(buildDocumentView(saved)));
                });
    }

    public Flux<BillingOperationsViews.CommercialDocumentView> listDocuments(DocumentType type, BillingRequestContext context) {
        return commercialDocumentRepository.findByOrganizationIdAndType(context.requireOrganizationId(), type.name())
                .sort(Comparator.comparing(CommercialDocumentEntity::createdAt).reversed())
                .flatMap(this::buildDocumentView);
    }

    public Mono<BillingOperationsViews.CommercialDocumentView> getDocument(DocumentType type,
            UUID documentId,
            BillingRequestContext context) {
        return requireDocument(type, documentId, context.requireOrganizationId()).flatMap(this::buildDocumentView);
    }

    public Mono<BillingOperationsViews.CommercialDocumentView> updateDocument(DocumentType type,
            UUID documentId,
            BillingOperationsRequests.CreateCommercialDocumentRequest request,
            BillingRequestContext context) {
        return requireDocument(type, documentId, context.requireOrganizationId())
                .flatMap(existing -> commercialDocumentRepository.save(new CommercialDocumentEntity(
                                existing.id(),
                                existing.organizationId(),
                                existing.type(),
                                request.documentNumber() == null || request.documentNumber().isBlank()
                                        ? existing.documentNumber()
                                        : request.documentNumber().trim(),
                                request.counterpartyThirdPartyId(),
                                request.currency().trim(),
                                existing.status(),
                                existing.createdAt()))
                        .flatMap(saved -> replaceLines(saved.id(), request.lines())
                                .then(buildDocumentView(saved))));
    }

    public Flux<BillingOperationsViews.CommercialDocumentView> listDocumentsByCounterparty(DocumentType type,
            UUID counterpartyId,
            BillingRequestContext context) {
        return listDocuments(type, context)
                .filter(document -> document.counterparty() != null && document.counterparty().id().equals(counterpartyId));
    }

    public Mono<Void> deleteDocument(DocumentType type, UUID documentId, BillingRequestContext context) {
        return requireDocument(type, documentId, context.requireOrganizationId())
                .flatMap(entity -> commercialDocumentLineRepository.deleteByDocumentId(entity.id())
                        .then(commercialDocumentRepository.deleteById(entity.id())));
    }

    public Mono<BillingOperationsViews.CommercialDocumentView> transitionDocument(DocumentType type,
            UUID documentId,
            DocumentStatus nextStatus,
            BillingRequestContext context) {
        return requireDocument(type, documentId, context.requireOrganizationId())
                .flatMap(existing -> commercialDocumentRepository.save(new CommercialDocumentEntity(
                                existing.id(),
                                existing.organizationId(),
                                existing.type(),
                                existing.documentNumber(),
                                existing.counterpartyThirdPartyId(),
                                existing.currency(),
                                nextStatus.name(),
                                existing.createdAt()))
                        .flatMap(this::buildDocumentView));
    }

    public Mono<BillingOperationsViews.PaymentView> createPayment(BillingOperationsRequests.CreatePaymentRequest request,
            BillingRequestContext context) {
        return paymentRepository.save(new PaymentEntity(
                        UUID.randomUUID(),
                        context.requireOrganizationId(),
                        request.invoiceId(),
                        request.supplierInvoiceId(),
                        request.counterpartyThirdPartyId(),
                        request.reference().trim(),
                        request.amount(),
                        request.currency().trim(),
                        "RECORDED",
                        request.paidAt() == null ? Instant.now() : request.paidAt()))
                .flatMap(this::buildPaymentView);
    }

    public Flux<BillingOperationsViews.PaymentView> listPayments(BillingRequestContext context) {
        return paymentRepository.findByOrganizationId(context.requireOrganizationId())
                .sort(Comparator.comparing(PaymentEntity::paidAt).reversed())
                .flatMap(this::buildPaymentView);
    }

    public Mono<BillingOperationsViews.PaymentView> getPayment(UUID paymentId, BillingRequestContext context) {
        return requirePayment(paymentId, context.requireOrganizationId()).flatMap(this::buildPaymentView);
    }

    public Mono<BillingOperationsViews.PaymentView> updatePayment(UUID paymentId,
            BillingOperationsRequests.UpdatePaymentRequest request,
            BillingRequestContext context) {
        return requirePayment(paymentId, context.requireOrganizationId())
                .flatMap(existing -> paymentRepository.save(new PaymentEntity(
                                existing.id(),
                                existing.organizationId(),
                                request.invoiceId(),
                                request.supplierInvoiceId(),
                                request.counterpartyThirdPartyId(),
                                request.reference().trim(),
                                request.amount(),
                                request.currency().trim(),
                                request.status() == null || request.status().isBlank() ? existing.status() : request.status().trim(),
                                request.paidAt() == null ? existing.paidAt() : request.paidAt()))
                        .flatMap(this::buildPaymentView));
    }

    public Flux<BillingOperationsViews.PaymentView> listPaymentsByCustomer(UUID customerId, BillingRequestContext context) {
        return listPayments(context)
                .filter(payment -> payment.counterparty() != null && payment.counterparty().id().equals(customerId));
    }

    public Flux<BillingOperationsViews.PaymentView> listPaymentsByInvoice(UUID invoiceId, BillingRequestContext context) {
        return listPayments(context).filter(payment -> invoiceId.equals(payment.invoiceId()));
    }

    public Mono<Void> deletePayment(UUID paymentId, BillingRequestContext context) {
        return requirePayment(paymentId, context.requireOrganizationId())
                .flatMap(entity -> paymentRepository.deleteById(entity.id()));
    }

    private Mono<CommercialDocumentEntity> requireDocument(DocumentType type, UUID documentId, UUID organizationId) {
        return commercialDocumentRepository.findById(documentId)
                .filter(document -> document.organizationId().equals(organizationId) && document.type().equals(type.name()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("document not found for organization")));
    }

    private Mono<PaymentEntity> requirePayment(UUID paymentId, UUID organizationId) {
        return paymentRepository.findById(paymentId)
                .filter(payment -> payment.organizationId().equals(organizationId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("payment not found for organization")));
    }

    private Mono<String> nextNumber(UUID organizationId, DocumentType type, String requested) {
        if (requested != null && !requested.isBlank()) {
            return Mono.just(requested.trim());
        }
        return commercialDocumentRepository.countByOrganizationIdAndType(organizationId, type.name())
                .map(count -> type.prefix + "-" + String.format("%05d", count + 1));
    }

    private Mono<Void> replaceLines(UUID documentId, List<BillingInvoiceLineRequest> lines) {
        List<CommercialDocumentLineEntity> entities = java.util.stream.IntStream.range(0, lines.size())
                .mapToObj(index -> {
                    BillingInvoiceLineRequest line = lines.get(index);
                    return new CommercialDocumentLineEntity(
                            UUID.randomUUID(),
                            documentId,
                            index,
                            line.productId(),
                            line.quantity(),
                            line.unitPrice());
                })
                .toList();
        return commercialDocumentLineRepository.deleteByDocumentId(documentId)
                .thenMany(commercialDocumentLineRepository.saveAll(entities))
                .then();
    }

    private Mono<BillingOperationsViews.CommercialDocumentView> buildDocumentView(CommercialDocumentEntity entity) {
        return Mono.zip(
                        loadCounterparty(entity.counterpartyThirdPartyId()),
                        commercialDocumentLineRepository.findByDocumentIdOrderByLineIndexAsc(entity.id())
                                .flatMap(this::buildLineView)
                                .collectList())
                .map(tuple -> {
                    List<BillingOperationsViews.CommercialDocumentLineView> lines = tuple.getT2();
                    BigDecimal totalQuantity = lines.stream()
                            .map(BillingOperationsViews.CommercialDocumentLineView::quantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalAmount = lines.stream()
                            .map(BillingOperationsViews.CommercialDocumentLineView::lineAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new BillingOperationsViews.CommercialDocumentView(
                            entity.id(),
                            entity.type(),
                            entity.organizationId(),
                            entity.documentNumber(),
                            entity.status(),
                            tuple.getT1(),
                            entity.currency(),
                            lines,
                            totalQuantity,
                            totalAmount,
                            entity.createdAt());
                });
    }

    private Mono<BillingOperationsViews.CommercialDocumentLineView> buildLineView(CommercialDocumentLineEntity entity) {
        return getProductUseCase.getProduct(entity.productId())
                .map(this::toProductView)
                .onErrorReturn(new BillingOperationsViews.ProductSummaryView(
                        entity.productId(),
                        entity.productId().toString(),
                        entity.productId().toString()))
                .map(product -> new BillingOperationsViews.CommercialDocumentLineView(
                        entity.productId(),
                        product,
                        entity.quantity(),
                        entity.unitPrice(),
                        entity.quantity().multiply(entity.unitPrice())));
    }

    private Mono<BillingOperationsViews.PaymentView> buildPaymentView(PaymentEntity entity) {
        if (entity.counterpartyThirdPartyId() == null) {
            return Mono.just(new BillingOperationsViews.PaymentView(
                    entity.id(),
                    entity.organizationId(),
                    entity.invoiceId(),
                    entity.supplierInvoiceId(),
                    null,
                    entity.reference(),
                    entity.amount(),
                    entity.currency(),
                    entity.status(),
                    entity.paidAt()));
        }
        return loadCounterparty(entity.counterpartyThirdPartyId())
                .map(counterparty -> new BillingOperationsViews.PaymentView(
                        entity.id(),
                        entity.organizationId(),
                        entity.invoiceId(),
                        entity.supplierInvoiceId(),
                        counterparty,
                        entity.reference(),
                        entity.amount(),
                        entity.currency(),
                        entity.status(),
                        entity.paidAt()));
    }

    private Mono<BillingOperationsViews.CounterpartySummaryView> loadCounterparty(UUID thirdPartyId) {
        return getThirdPartyUseCase.getThirdParty(thirdPartyId)
                .map(this::toCounterpartyView)
                .onErrorReturn(new BillingOperationsViews.CounterpartySummaryView(
                        thirdPartyId,
                        thirdPartyId.toString(),
                        thirdPartyId.toString(),
                        "THIRD_PARTY",
                        true));
    }

    private BillingOperationsViews.CounterpartySummaryView toCounterpartyView(ThirdParty thirdParty) {
        return new BillingOperationsViews.CounterpartySummaryView(
                thirdParty.id(),
                thirdParty.code(),
                thirdParty.name(),
                thirdParty.roles().stream().findFirst().orElse("THIRD_PARTY"),
                thirdParty.active());
    }

    private BillingOperationsViews.ProductSummaryView toProductView(Product product) {
        return new BillingOperationsViews.ProductSummaryView(product.id(), product.sku(), product.name());
    }

    public enum DocumentType {
        PURCHASE_VOUCHER("BA"),
        PURCHASE_ORDER("PO"),
        PURCHASE_RECEIPT("BR"),
        DELIVERY_NOTE("BL"),
        PROFORMA_INVOICE("PF"),
        SUPPLIER_INVOICE("SF"),
        CREDIT_NOTE("CN");

        private final String prefix;

        DocumentType(String prefix) {
            this.prefix = prefix;
        }
    }

    public enum DocumentStatus {
        DRAFT,
        FULFILLED
    }
}
