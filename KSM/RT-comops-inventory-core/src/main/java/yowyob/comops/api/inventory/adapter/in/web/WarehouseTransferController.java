package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.inventory.application.port.in.CompleteWarehouseTransferUseCase;
import yowyob.comops.api.inventory.application.port.in.CreateWarehouseTransferCommand;
import yowyob.comops.api.inventory.application.port.in.CreateWarehouseTransferUseCase;
import yowyob.comops.api.inventory.application.port.in.ListWarehouseTransfersUseCase;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/inventory/transfers")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'inventory:write')")
public class WarehouseTransferController {
    private final CreateWarehouseTransferUseCase createWarehouseTransferUseCase;
    private final CompleteWarehouseTransferUseCase completeWarehouseTransferUseCase;
    private final ListWarehouseTransfersUseCase listWarehouseTransfersUseCase;

    public WarehouseTransferController(CreateWarehouseTransferUseCase createWarehouseTransferUseCase,
            CompleteWarehouseTransferUseCase completeWarehouseTransferUseCase,
            ListWarehouseTransfersUseCase listWarehouseTransfersUseCase) {
        this.createWarehouseTransferUseCase = createWarehouseTransferUseCase;
        this.completeWarehouseTransferUseCase = completeWarehouseTransferUseCase;
        this.listWarehouseTransfersUseCase = listWarehouseTransfersUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<WarehouseTransferResponse>>>> list(
            @RequestParam("organizationId") UUID organizationId) {
        return listWarehouseTransfersUseCase.listTransfers(organizationId)
                .map(WarehouseTransferResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouse transfers retrieved.")));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<WarehouseTransferResponse>>> create(@Valid @RequestBody Mono<CreateWarehouseTransferRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createWarehouseTransferUseCase.createTransfer(new CreateWarehouseTransferCommand(tuple.getT2().tenantId(), tuple.getT1().organizationId(),
                        tuple.getT1().sourceAgencyId(), tuple.getT1().targetAgencyId(), tuple.getT1().productId(), tuple.getT1().referenceNumber(), tuple.getT1().quantity())))
                .map(WarehouseTransferResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Warehouse transfer created.")));
    }

    @PostMapping("/{transferId}/complete")
    public Mono<ResponseEntity<ApiResponse<WarehouseTransferResponse>>> complete(
            @PathVariable("transferId") UUID transferId) {
        return completeWarehouseTransferUseCase.complete(transferId)
                .map(WarehouseTransferResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Warehouse transfer completed.")));
    }
}
