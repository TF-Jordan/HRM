package yowyob.comops.api.sales.adapter.out.persistence;

import yowyob.comops.api.sales.application.port.out.SalesOrderRepository;
import yowyob.comops.api.sales.domain.model.SalesOrder;
import yowyob.comops.api.sales.domain.model.SalesOrderLine;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class SalesOrderR2dbcRepositoryAdapter implements SalesOrderRepository {

    private final SalesOrderSpringDataRepository repository;
    private final SalesOrderLineSpringDataRepository lineRepository;

    public SalesOrderR2dbcRepositoryAdapter(SalesOrderSpringDataRepository repository,
            SalesOrderLineSpringDataRepository lineRepository) {
        this.repository = repository;
        this.lineRepository = lineRepository;
    }

    @Override
    public Mono<Boolean> existsByOrderNumber(UUID tenantId, UUID organizationId, String orderNumber) {
        return repository.existsByTenantIdAndOrganizationIdAndOrderNumberIgnoreCase(tenantId, organizationId,
                orderNumber);
    }

    @Override
    public Mono<SalesOrder> findById(UUID orderId) {
        return repository.findById(orderId)
                .flatMap(this::toDomain);
    }

    @Override
    public Flux<SalesOrder> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByOrderNumberExcludingId(UUID tenantId, UUID organizationId, String orderNumber,
            UUID orderId) {
        return repository.existsByTenantIdAndOrganizationIdAndOrderNumberIgnoreCaseAndIdNot(tenantId, organizationId,
                orderNumber, orderId);
    }

    @Override
    public Mono<SalesOrder> save(SalesOrder salesOrder) {
        SalesOrderEntity entity = new SalesOrderEntity(salesOrder.id(), salesOrder.tenantId(), salesOrder.createdAt(),
                salesOrder.updatedAt(), salesOrder.organizationId(), salesOrder.agencyId(),
                salesOrder.customerThirdPartyId(), salesOrder.productId(), salesOrder.orderNumber(),
                salesOrder.quantity(), salesOrder.unitPrice(), salesOrder.totalQuantity(),
                salesOrder.subtotalAmount(), salesOrder.totalAmount(), salesOrder.currency(), salesOrder.status());
        java.time.Instant lineTimestamp = salesOrder.updatedAt();
        List<SalesOrderLineEntity> lineEntities = salesOrder.lines().stream()
                .map(line -> new SalesOrderLineEntity(UUID.randomUUID(), salesOrder.id(), salesOrder.tenantId(),
                        lineTimestamp, lineTimestamp, line.productId(), line.quantity(),
                        line.unitPrice(), line.lineAmount()))
                .toList();
        return repository.save(entity)
                .flatMap(saved -> lineRepository.deleteAllBySalesOrderId(saved.id())
                        .thenMany(Flux.fromIterable(lineEntities))
                        .flatMap(lineRepository::save)
                        .then(Mono.just(saved)))
                .flatMap(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID orderId) {
        return lineRepository.deleteAllBySalesOrderId(orderId)
                .then(repository.deleteById(orderId));
    }

    private Mono<SalesOrder> toDomain(SalesOrderEntity entity) {
        return lineRepository.findAllBySalesOrderIdOrderByCreatedAtAsc(entity.id())
                .map(this::toDomainLine)
                .collectList()
                .map(lines -> SalesOrder.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                        entity.updatedAt(), entity.organizationId(), entity.agencyId(), entity.customerThirdPartyId(),
                        entity.orderNumber(), entity.currency(), entity.status(), lines, entity.totalQuantity(),
                        entity.subtotalAmount(), entity.totalAmount()));
    }

    private SalesOrderLine toDomainLine(SalesOrderLineEntity entity) {
        return new SalesOrderLine(entity.productId(), entity.quantity(), entity.unitPrice(), entity.lineAmount());
    }
}
