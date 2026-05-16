package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.inventory.application.port.in.CreateInventorySessionCommand;
import yowyob.comops.api.inventory.application.port.in.CreateInventorySessionUseCase;
import yowyob.comops.api.inventory.application.port.in.ListInventorySessionsUseCase;
import yowyob.comops.api.inventory.application.port.in.ValidateInventorySessionUseCase;
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
@RequestMapping({"/api/inventory/sessions", "/api/inventories"})
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'inventory:write')")
public class InventorySessionController {

    private final CreateInventorySessionUseCase createInventorySessionUseCase;
    private final ListInventorySessionsUseCase listInventorySessionsUseCase;
    private final ValidateInventorySessionUseCase validateInventorySessionUseCase;

    public InventorySessionController(CreateInventorySessionUseCase createInventorySessionUseCase,
            ListInventorySessionsUseCase listInventorySessionsUseCase,
            ValidateInventorySessionUseCase validateInventorySessionUseCase) {
        this.createInventorySessionUseCase = createInventorySessionUseCase;
        this.listInventorySessionsUseCase = listInventorySessionsUseCase;
        this.validateInventorySessionUseCase = validateInventorySessionUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<InventorySessionResponse>>>> list(
            @RequestParam("organizationId") UUID organizationId) {
        return listInventorySessionsUseCase.listSessions(organizationId)
                .map(InventorySessionResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Inventory sessions retrieved.")));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<InventorySessionResponse>>> create(
            @Valid @RequestBody Mono<CreateInventorySessionRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createInventorySessionUseCase.createSession(new CreateInventorySessionCommand(
                        tuple.getT2().tenantId(), tuple.getT1().organizationId(), tuple.getT1().agencyId(),
                        tuple.getT1().productId(), tuple.getT1().referenceNumber(), tuple.getT1().countedQuantity())))
                .map(InventorySessionResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Inventory session created.")));
    }

    @PostMapping("/{inventoryId}/validate")
    public Mono<ResponseEntity<ApiResponse<InventorySessionResponse>>> validate(@PathVariable("inventoryId") UUID inventoryId) {
        return validateInventorySessionUseCase.validateSession(inventoryId)
                .map(InventorySessionResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Inventory session validated.")));
    }
}
