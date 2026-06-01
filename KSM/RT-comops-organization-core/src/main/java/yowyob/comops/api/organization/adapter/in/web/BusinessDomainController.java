package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.OrganizationSecondaryStructureUseCase;
import yowyob.comops.api.organization.domain.model.BusinessDomain;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/business-domains")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class BusinessDomainController {

    private final OrganizationSecondaryStructureUseCase useCase;

    public BusinessDomainController(OrganizationSecondaryStructureUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<BusinessDomainResponse>>> createDomain(
            @Valid @RequestBody Mono<CreateBusinessDomainRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createBusinessDomain(tuple.getT2().tenantId(), tuple.getT1().code(),
                        tuple.getT1().service(), tuple.getT1().parentId(), tuple.getT1().name(),
                        tuple.getT1().imageUri(), tuple.getT1().imageId(), tuple.getT1().type(),
                        tuple.getT1().typeLabel(), tuple.getT1().description()))
                .map(BusinessDomainResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Business domain created.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<BusinessDomainResponse>>>> listDomains() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listBusinessDomains(context.tenantId()).map(BusinessDomainResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Business domains retrieved.")));
    }

    public record CreateBusinessDomainRequest(
            @NotBlank String code,
            String service,
            UUID parentId,
            @NotBlank String name,
            String imageUri,
            UUID imageId,
            String type,
            String typeLabel,
            String description) {
    }

    public record BusinessDomainResponse(
            UUID id,
            UUID tenantId,
            String code,
            String service,
            UUID parentId,
            String name,
            String imageUri,
            UUID imageId,
            String type,
            String typeLabel,
            String description) {

        static BusinessDomainResponse from(BusinessDomain businessDomain) {
            return new BusinessDomainResponse(businessDomain.id(), businessDomain.tenantId(), businessDomain.code(),
                    businessDomain.service(), businessDomain.parentId(), businessDomain.name(), businessDomain.imageUri(),
                    businessDomain.imageId(), businessDomain.type(), businessDomain.typeLabel(),
                    businessDomain.description());
        }
    }
}
