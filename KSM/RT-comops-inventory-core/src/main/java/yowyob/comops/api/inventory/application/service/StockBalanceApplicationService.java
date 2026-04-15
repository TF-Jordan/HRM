package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.inventory.application.port.in.GetStockBalanceUseCase;
import yowyob.comops.api.inventory.application.port.in.ListStockMovementsUseCase;
import yowyob.comops.api.inventory.application.port.in.ListProductTransformationsUseCase;
import yowyob.comops.api.inventory.application.port.in.ListWarehouseTransfersUseCase;
import yowyob.comops.api.inventory.application.port.out.ProductTransformationRepository;
import yowyob.comops.api.inventory.application.port.out.StockMovementRepository;
import yowyob.comops.api.inventory.application.port.out.WarehouseTransferRepository;
import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import yowyob.comops.api.inventory.domain.model.StockBalance;
import yowyob.comops.api.inventory.domain.model.StockMovement;
import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class StockBalanceApplicationService implements ListStockMovementsUseCase, GetStockBalanceUseCase,
        ListProductTransformationsUseCase, ListWarehouseTransfersUseCase {

    private final StockMovementRepository stockMovementRepository;
    private final ProductTransformationRepository productTransformationRepository;
    private final WarehouseTransferRepository warehouseTransferRepository;

    public StockBalanceApplicationService(StockMovementRepository stockMovementRepository,
            ProductTransformationRepository productTransformationRepository,
            WarehouseTransferRepository warehouseTransferRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productTransformationRepository = productTransformationRepository;
        this.warehouseTransferRepository = warehouseTransferRepository;
    }

    @Override
    public Flux<StockMovement> listMovements(UUID organizationId, UUID agencyId, UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> stockMovementRepository.findByAgencyAndProduct(context.tenantId(), organizationId,
                        agencyId, productId));
    }

    @Override
    public Flux<ProductTransformation> listTransformations(UUID organizationId, UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> productTransformationRepository.findByAgency(context.tenantId(), organizationId,
                        agencyId));
    }

    @Override
    public Flux<WarehouseTransfer> listTransfers(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> warehouseTransferRepository.findByOrganization(context.tenantId(), organizationId));
    }

    @Override
    public Mono<StockBalance> getBalance(UUID organizationId, UUID agencyId, UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> Mono.zip(
                        stockMovementRepository.findByAgencyAndProduct(context.tenantId(), organizationId, agencyId,
                                        productId)
                                .map(StockMovement::signedQuantity)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        productTransformationRepository.findByAgency(context.tenantId(), organizationId, agencyId)
                                .map(transformation -> transformationDelta(transformation, productId))
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        warehouseTransferRepository.findByOrganization(context.tenantId(), organizationId)
                                .map(transfer -> transferDelta(transfer, agencyId, productId))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .map(tuple -> tuple.getT1().add(tuple.getT2()).add(tuple.getT3()))
                .map(balance -> new StockBalance(organizationId, agencyId, productId, balance));
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
}
