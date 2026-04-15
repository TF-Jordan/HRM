package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.inventory.application.port.in.DispatchSalesOrderCommand;
import yowyob.comops.api.inventory.application.port.in.DispatchSalesOrderLineCommand;
import yowyob.comops.api.inventory.application.port.in.DispatchSalesOrderUseCase;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementCommand;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.out.ProductTransformationRepository;
import yowyob.comops.api.inventory.application.port.out.StockMovementRepository;
import yowyob.comops.api.inventory.application.port.out.WarehouseTransferRepository;
import yowyob.comops.api.inventory.domain.InsufficientStockException;
import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import yowyob.comops.api.inventory.domain.model.StockMovement;
import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SalesOrderDispatchApplicationService implements DispatchSalesOrderUseCase {

    private static final String SOURCE_DOCUMENT_TYPE = "SALES_ORDER";

    private final StockMovementRepository stockMovementRepository;
    private final ProductTransformationRepository productTransformationRepository;
    private final WarehouseTransferRepository warehouseTransferRepository;
    private final RecordStockMovementUseCase recordStockMovementUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public SalesOrderDispatchApplicationService(StockMovementRepository stockMovementRepository,
            ProductTransformationRepository productTransformationRepository,
            WarehouseTransferRepository warehouseTransferRepository,
            RecordStockMovementUseCase recordStockMovementUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.stockMovementRepository = stockMovementRepository;
        this.productTransformationRepository = productTransformationRepository;
        this.warehouseTransferRepository = warehouseTransferRepository;
        this.recordStockMovementUseCase = recordStockMovementUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Void> dispatch(DispatchSalesOrderCommand command) {
        Objects.requireNonNull(command, "command is required");
        List<DispatchSalesOrderLineCommand> lines = List.copyOf(command.lines());
        if (lines.isEmpty()) {
            return Mono.error(new IllegalArgumentException("sales order dispatch requires at least one line"));
        }
        Mono<Void> operation = Flux.fromIterable(lines)
                .concatMap(line -> ensureAvailable(command, line))
                .thenMany(Flux.fromIterable(lines)
                        .index()
                        .concatMap(indexedLine -> dispatchLine(command, indexedLine.getT2(), indexedLine.getT1().intValue())))
                .then(businessEventPublisher.publish(salesOrderDispatchedEvent(command)));
        return transactionalExecutor.transactional(operation);
    }

    private Mono<Void> ensureAvailable(DispatchSalesOrderCommand command, DispatchSalesOrderLineCommand line) {
        return currentBalance(command.tenantId(), command.organizationId(), command.agencyId(), line.productId())
                .flatMap(available -> available.compareTo(line.quantity()) >= 0
                        ? Mono.<Void>empty()
                        : Mono.error(new InsufficientStockException(line.productId(), command.agencyId(),
                                line.quantity(), available)));
    }

    private Mono<StockMovement> dispatchLine(DispatchSalesOrderCommand command, DispatchSalesOrderLineCommand line,
            int zeroBasedLineIndex) {
        return recordStockMovementUseCase.recordMovement(new RecordStockMovementCommand(
                command.tenantId(),
                command.organizationId(),
                command.agencyId(),
                line.productId(),
                command.customerThirdPartyId(),
                dispatchReference(command.salesOrderNumber(), zeroBasedLineIndex + 1),
                SOURCE_DOCUMENT_TYPE,
                command.salesOrderNumber(),
                "OUTBOUND",
                line.quantity()));
    }

    private String dispatchReference(String salesOrderNumber, int oneBasedLineIndex) {
        return salesOrderNumber + "-FULFILL-" + String.format("%02d", oneBasedLineIndex);
    }

    private Mono<BigDecimal> currentBalance(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId) {
        return Mono.zip(
                        stockMovementRepository.findByAgencyAndProduct(tenantId, organizationId, agencyId, productId)
                                .map(StockMovement::signedQuantity)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        productTransformationRepository.findByAgency(tenantId, organizationId, agencyId)
                                .map(transformation -> transformationDelta(transformation, productId))
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        warehouseTransferRepository.findByOrganization(tenantId, organizationId)
                                .map(transfer -> transferDelta(transfer, agencyId, productId))
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                .map(tuple -> tuple.getT1().add(tuple.getT2()).add(tuple.getT3()));
    }

    private BigDecimal transformationDelta(ProductTransformation transformation, UUID productId) {
        BigDecimal delta = BigDecimal.ZERO;
        if (transformation.sourceProductId().equals(productId)) {
            delta = delta.subtract(transformation.sourceQuantity());
        }
        if (transformation.targetProductId().equals(productId)) {
            delta = delta.add(transformation.targetQuantity());
        }
        return delta;
    }

    private BigDecimal transferDelta(WarehouseTransfer transfer, UUID agencyId, UUID productId) {
        if (!transfer.completed() || !transfer.productId().equals(productId)) {
            return BigDecimal.ZERO;
        }
        if (transfer.sourceAgencyId().equals(agencyId)) {
            return transfer.quantity().negate();
        }
        if (transfer.targetAgencyId().equals(agencyId)) {
            return transfer.quantity();
        }
        return BigDecimal.ZERO;
    }

    private BusinessEvent salesOrderDispatchedEvent(DispatchSalesOrderCommand command) {
        return BusinessEvent.now(command.tenantId(), command.organizationId(), "SALES_ORDER_STOCK_DISPATCHED",
                "SALES_ORDER", command.salesOrderId(), payload(
                        "salesOrderNumber", command.salesOrderNumber(),
                        "sourceDocumentNumber", command.salesOrderNumber(),
                        "agencyId", command.agencyId(),
                        "customerThirdPartyId", command.customerThirdPartyId(),
                        "lineCount", command.lines().size(),
                        "status", "DISPATCHED"));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
