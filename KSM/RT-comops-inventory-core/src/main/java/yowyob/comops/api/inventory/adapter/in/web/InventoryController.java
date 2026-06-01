package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.inventory.application.port.in.GetStockBalanceUseCase;
import yowyob.comops.api.inventory.application.port.in.GetStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.in.ListStockMovementsUseCase;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementCommand;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.in.ValidateStockMovementUseCase;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/inventory/movements")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'inventory:write')")
public class InventoryController {

    private final RecordStockMovementUseCase recordStockMovementUseCase;
    private final ListStockMovementsUseCase listStockMovementsUseCase;
    private final GetStockBalanceUseCase getStockBalanceUseCase;
    private final GetStockMovementUseCase getStockMovementUseCase;
    private final ValidateStockMovementUseCase validateStockMovementUseCase;

    public InventoryController(RecordStockMovementUseCase recordStockMovementUseCase,
            ListStockMovementsUseCase listStockMovementsUseCase,
            GetStockBalanceUseCase getStockBalanceUseCase,
            GetStockMovementUseCase getStockMovementUseCase,
            ValidateStockMovementUseCase validateStockMovementUseCase) {
        this.recordStockMovementUseCase = recordStockMovementUseCase;
        this.listStockMovementsUseCase = listStockMovementsUseCase;
        this.getStockBalanceUseCase = getStockBalanceUseCase;
        this.getStockMovementUseCase = getStockMovementUseCase;
        this.validateStockMovementUseCase = validateStockMovementUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<StockMovementResponse>>> recordMovement(
            @Valid @RequestBody Mono<RecordStockMovementRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> recordStockMovementUseCase.recordMovement(new RecordStockMovementCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(),
                        tuple.getT1().productId(),
                        tuple.getT1().thirdPartyId(),
                        tuple.getT1().referenceNumber(),
                        tuple.getT1().sourceDocumentType(),
                        tuple.getT1().sourceDocumentNumber(),
                        tuple.getT1().movementType(),
                        tuple.getT1().quantity())))
                .map(StockMovementResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Stock movement recorded.")));
    }

    @GetMapping("/{movementId}")
    public Mono<ResponseEntity<ApiResponse<StockMovementResponse>>> getMovement(@PathVariable UUID movementId) {
        return getStockMovementUseCase.getMovement(movementId)
                .map(StockMovementResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Stock movement retrieved.")));
    }

    @PostMapping("/{movementId}/validate")
    public Mono<ResponseEntity<ApiResponse<StockMovementResponse>>> validateMovement(@PathVariable UUID movementId) {
        return validateStockMovementUseCase.validate(movementId)
                .map(StockMovementResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Stock movement validated.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<StockMovementResponse>>>> listMovements(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam("agencyId") UUID agencyId,
            @RequestParam("productId") UUID productId) {
        return listStockMovementsUseCase.listMovements(organizationId, agencyId, productId)
                .map(StockMovementResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Stock movements retrieved.")));
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<ApiResponse<StockBalanceResponse>>> getBalance(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam("agencyId") UUID agencyId,
            @RequestParam("productId") UUID productId) {
        return getStockBalanceUseCase.getBalance(organizationId, agencyId, productId)
                .map(StockBalanceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Stock balance retrieved.")));
    }
}
