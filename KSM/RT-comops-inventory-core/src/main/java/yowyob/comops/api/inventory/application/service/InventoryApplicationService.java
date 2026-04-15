package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.inventory.application.port.in.GetStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementCommand;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.in.ValidateStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.out.StockMovementRepository;
import yowyob.comops.api.inventory.domain.DuplicateStockMovementReferenceException;
import yowyob.comops.api.inventory.domain.StockMovementNotFoundException;
import yowyob.comops.api.inventory.domain.model.StockMovement;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class InventoryApplicationService implements RecordStockMovementUseCase, GetStockMovementUseCase,
        ValidateStockMovementUseCase {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final ThirdPartyRepository thirdPartyRepository;
    private final AgencyRepository agencyRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public InventoryApplicationService(StockMovementRepository stockMovementRepository,
            ProductRepository productRepository,
            ThirdPartyRepository thirdPartyRepository,
            AgencyRepository agencyRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.thirdPartyRepository = thirdPartyRepository;
        this.agencyRepository = agencyRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<StockMovement> recordMovement(RecordStockMovementCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(validateReferences(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.productId(), command.thirdPartyId())
                .then(resolveReferenceNumber(command))
                .map(referenceNumber -> StockMovement.record(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.productId(), command.thirdPartyId(), referenceNumber,
                        command.sourceDocumentType(), command.sourceDocumentNumber(), command.movementType(),
                        command.quantity()))
                .flatMap(stockMovement -> stockMovementRepository.existsByReference(stockMovement.tenantId(),
                        stockMovement.organizationId(), stockMovement.referenceNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateStockMovementReferenceException(stockMovement.referenceNumber()))
                        : stockMovementRepository.save(stockMovement)))
                .flatMap(saved -> businessEventPublisher.publish(stockMovementRecordedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<StockMovement> getMovement(java.util.UUID movementId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> stockMovementRepository.findById(context.tenantId(), movementId))
                .switchIfEmpty(Mono.error(new StockMovementNotFoundException(movementId)));
    }

    @Override
    public Mono<StockMovement> validate(java.util.UUID movementId) {
        return transactionalExecutor.transactional(ReactiveRequestContextHolder
                .getRequiredContext()
                .flatMap(context -> stockMovementRepository.findById(context.tenantId(), movementId))
                .switchIfEmpty(Mono.error(new StockMovementNotFoundException(movementId)))
                .map(StockMovement::validate)
                .flatMap(stockMovementRepository::save));
    }

    private Mono<String> resolveReferenceNumber(RecordStockMovementCommand command) {
        if (command.referenceNumber() != null && !command.referenceNumber().isBlank()) {
            return Mono.just(command.referenceNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), command.agencyId(), DocumentTypes.STOCK_MOVEMENT));
    }

    private BusinessEvent stockMovementRecordedEvent(StockMovement movement) {
        return BusinessEvent.now(movement.tenantId(), movement.organizationId(), "STOCK_MOVEMENT_RECORDED",
                "STOCK_MOVEMENT", movement.id(), payload(
                        "referenceNumber", movement.referenceNumber(),
                        "agencyId", movement.agencyId(),
                        "productId", movement.productId(),
                        "thirdPartyId", movement.thirdPartyId(),
                        "sourceDocumentType", movement.sourceDocumentType(),
                        "sourceDocumentNumber", movement.sourceDocumentNumber(),
                        "movementType", movement.movementType(),
                        "quantity", movement.quantity(),
                        "signedQuantity", movement.signedQuantity()));
    }

    private Mono<Void> validateReferences(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId,
            UUID thirdPartyId) {
        Mono<Void> agencyValidation = agencyRepository.findById(tenantId, agencyId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                .flatMap(agency -> {
                    if (!agency.organizationId().equals(organizationId)) {
                        return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                    }
                    if (!agency.active()) {
                        return Mono.error(new IllegalArgumentException("agency is not active"));
                    }
                    return Mono.empty();
                });
        Mono<Void> productValidation = productRepository.findById(tenantId, productId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("product not found")))
                .flatMap(product -> {
                    if (!product.organizationId().equals(organizationId)) {
                        return Mono.error(new IllegalArgumentException("product does not belong to the organization"));
                    }
                    if (!product.active()) {
                        return Mono.error(new IllegalArgumentException("product is not active"));
                    }
                    return Mono.empty();
                });
        Mono<Void> thirdPartyValidation = thirdPartyId == null
                ? Mono.empty()
                : thirdPartyRepository.findById(tenantId, thirdPartyId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("third party not found")))
                        .flatMap(thirdParty -> {
                            if (!thirdParty.organizationId().equals(organizationId)) {
                                return Mono.error(new IllegalArgumentException("third party does not belong to the organization"));
                            }
                            if (!thirdParty.active()) {
                                return Mono.error(new IllegalArgumentException("third party is not active"));
                            }
                            return Mono.empty();
                        });
        return Mono.when(agencyValidation, productValidation, thirdPartyValidation);
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
