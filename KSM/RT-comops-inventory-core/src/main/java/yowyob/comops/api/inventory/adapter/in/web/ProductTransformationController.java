package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.inventory.application.port.in.ListProductTransformationsUseCase;
import yowyob.comops.api.inventory.application.port.in.RecordTransformationCommand;
import yowyob.comops.api.inventory.application.port.in.RecordTransformationUseCase;
import yowyob.comops.api.inventory.application.port.in.ValidateProductTransformationUseCase;
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
@RequestMapping("/api/inventory/transformations")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'inventory:write')")
public class ProductTransformationController {

    private final RecordTransformationUseCase recordTransformationUseCase;
    private final ListProductTransformationsUseCase listProductTransformationsUseCase;
    private final ValidateProductTransformationUseCase validateProductTransformationUseCase;

    public ProductTransformationController(RecordTransformationUseCase recordTransformationUseCase,
            ListProductTransformationsUseCase listProductTransformationsUseCase,
            ValidateProductTransformationUseCase validateProductTransformationUseCase) {
        this.recordTransformationUseCase = recordTransformationUseCase;
        this.listProductTransformationsUseCase = listProductTransformationsUseCase;
        this.validateProductTransformationUseCase = validateProductTransformationUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<ProductTransformationResponse>>>> list(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam("agencyId") UUID agencyId) {
        return listProductTransformationsUseCase.listTransformations(organizationId, agencyId)
                .map(ProductTransformationResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Transformations retrieved.")));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<ProductTransformationResponse>>> create(@Valid @RequestBody Mono<RecordTransformationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> recordTransformationUseCase.recordTransformation(new RecordTransformationCommand(tuple.getT2().tenantId(), tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(), tuple.getT1().sourceProductId(), tuple.getT1().targetProductId(), tuple.getT1().referenceNumber(),
                        tuple.getT1().sourceQuantity(), tuple.getT1().targetQuantity())))
                .map(ProductTransformationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Transformation recorded.")));
    }

    @PostMapping("/{transformationId}/validate")
    public Mono<ResponseEntity<ApiResponse<ProductTransformationResponse>>> validate(
            @PathVariable UUID transformationId) {
        return validateProductTransformationUseCase.validateTransformation(transformationId)
                .map(ProductTransformationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Transformation validated.")));
    }
}
