package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.OrganizationSecondaryStructureUseCase;
import yowyob.comops.api.organization.domain.model.AgencyAffiliation;
import yowyob.comops.api.organization.domain.model.AgencyDomain;
import yowyob.comops.api.organization.domain.model.Certification;
import yowyob.comops.api.organization.domain.model.OrganizationActor;
import yowyob.comops.api.organization.domain.model.OrganizationDomain;
import yowyob.comops.api.organization.domain.model.ProposedActivity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
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
@RequestMapping("/api")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class OrganizationStructureController {

    private final OrganizationSecondaryStructureUseCase useCase;

    public OrganizationStructureController(OrganizationSecondaryStructureUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/organizations/{organizationId}/certifications")
    public Mono<ResponseEntity<ApiResponse<CertificationResponse>>> createCertification(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<CreateCertificationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createCertification(tuple.getT2().tenantId(), organizationId, tuple.getT1().type(),
                        tuple.getT1().name(), tuple.getT1().description(), tuple.getT1().obtainmentDate()))
                .map(CertificationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Certification created.")));
    }

    @GetMapping("/organizations/{organizationId}/certifications")
    public Mono<ResponseEntity<ApiResponse<List<CertificationResponse>>>> listCertifications(
            @PathVariable("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listCertifications(context.tenantId(), organizationId)
                        .map(CertificationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Certifications retrieved.")));
    }

    @PostMapping("/organizations/{organizationId}/activities")
    public Mono<ResponseEntity<ApiResponse<ProposedActivityResponse>>> createActivity(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<CreateProposedActivityRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createProposedActivity(tuple.getT2().tenantId(), organizationId,
                        tuple.getT1().type(), tuple.getT1().name(), tuple.getT1().rate(), tuple.getT1().description()))
                .map(ProposedActivityResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Proposed activity created.")));
    }

    @GetMapping("/organizations/{organizationId}/activities")
    public Mono<ResponseEntity<ApiResponse<List<ProposedActivityResponse>>>> listActivities(
            @PathVariable("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listProposedActivities(context.tenantId(), organizationId)
                        .map(ProposedActivityResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Proposed activities retrieved.")));
    }

    @PostMapping("/organizations/{organizationId}/actors")
    public Mono<ResponseEntity<ApiResponse<OrganizationActorResponse>>> linkOrganizationActor(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<LinkOrganizationActorRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.linkOrganizationActor(tuple.getT2().tenantId(), organizationId,
                        tuple.getT1().actorId(), tuple.getT1().type()))
                .map(OrganizationActorResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Organization actor linked.")));
    }

    @GetMapping("/organizations/{organizationId}/actors")
    public Mono<ResponseEntity<ApiResponse<List<OrganizationActorResponse>>>> listOrganizationActors(
            @PathVariable("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listOrganizationActors(context.tenantId(), organizationId)
                        .map(OrganizationActorResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization actors retrieved.")));
    }

    @PostMapping("/organizations/{organizationId}/domains")
    public Mono<ResponseEntity<ApiResponse<OrganizationDomainResponse>>> linkOrganizationDomain(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<LinkOrganizationDomainRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.linkOrganizationDomain(tuple.getT2().tenantId(), organizationId,
                        tuple.getT1().domainId()))
                .map(OrganizationDomainResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Organization domain linked.")));
    }

    @GetMapping("/organizations/{organizationId}/domains")
    public Mono<ResponseEntity<ApiResponse<List<OrganizationDomainResponse>>>> listOrganizationDomains(
            @PathVariable("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listOrganizationDomains(context.tenantId(), organizationId)
                        .map(OrganizationDomainResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization domains retrieved.")));
    }

    @PostMapping("/agencies/{agencyId}/domains")
    public Mono<ResponseEntity<ApiResponse<AgencyDomainResponse>>> linkAgencyDomain(
            @PathVariable("agencyId") UUID agencyId,
            @Valid @RequestBody Mono<LinkAgencyDomainRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.linkAgencyDomain(tuple.getT2().tenantId(), tuple.getT1().organizationId(),
                        agencyId, tuple.getT1().domainId()))
                .map(AgencyDomainResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Agency domain linked.")));
    }

    @GetMapping("/agencies/{agencyId}/domains")
    public Mono<ResponseEntity<ApiResponse<List<AgencyDomainResponse>>>> listAgencyDomains(
            @PathVariable("agencyId") UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listAgencyDomains(context.tenantId(), agencyId)
                        .map(AgencyDomainResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency domains retrieved.")));
    }

    @PostMapping("/agencies/{agencyId}/affiliations")
    public Mono<ResponseEntity<ApiResponse<AgencyAffiliationResponse>>> createAgencyAffiliation(
            @PathVariable("agencyId") UUID agencyId,
            @Valid @RequestBody Mono<CreateAgencyAffiliationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> useCase.createAgencyAffiliation(tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(), agencyId, tuple.getT1().actorId(), tuple.getT1().type(),
                        tuple.getT1().active()))
                .map(AgencyAffiliationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Agency affiliation created.")));
    }

    @GetMapping("/agencies/{agencyId}/affiliations")
    public Mono<ResponseEntity<ApiResponse<List<AgencyAffiliationResponse>>>> listAgencyAffiliations(
            @PathVariable("agencyId") UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> useCase.listAgencyAffiliations(context.tenantId(), agencyId)
                        .map(AgencyAffiliationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency affiliations retrieved.")));
    }

    public record CreateCertificationRequest(String type, @NotBlank String name, String description,
            Instant obtainmentDate) {
    }

    public record CertificationResponse(UUID id, UUID organizationId, String type, String name, String description,
            Instant obtainmentDate) {
        static CertificationResponse from(Certification certification) {
            return new CertificationResponse(certification.id(), certification.organizationId(), certification.type(),
                    certification.name(), certification.description(), certification.obtainmentDate());
        }
    }

    public record CreateProposedActivityRequest(String type, @NotBlank String name, BigDecimal rate, String description) {
    }

    public record ProposedActivityResponse(UUID id, UUID organizationId, String type, String name, BigDecimal rate,
            String description) {
        static ProposedActivityResponse from(ProposedActivity proposedActivity) {
            return new ProposedActivityResponse(proposedActivity.id(), proposedActivity.organizationId(),
                    proposedActivity.type(), proposedActivity.name(), proposedActivity.rate(),
                    proposedActivity.description());
        }
    }

    public record LinkOrganizationActorRequest(@NotNull UUID actorId, String type) {
    }

    public record OrganizationActorResponse(UUID id, UUID organizationId, UUID actorId, String type) {
        static OrganizationActorResponse from(OrganizationActor organizationActor) {
            return new OrganizationActorResponse(organizationActor.id(), organizationActor.organizationId(),
                    organizationActor.actorId(), organizationActor.type());
        }
    }

    public record LinkOrganizationDomainRequest(@NotNull UUID domainId) {
    }

    public record OrganizationDomainResponse(UUID id, UUID organizationId, UUID domainId) {
        static OrganizationDomainResponse from(OrganizationDomain organizationDomain) {
            return new OrganizationDomainResponse(organizationDomain.id(), organizationDomain.organizationId(),
                    organizationDomain.domainId());
        }
    }

    public record LinkAgencyDomainRequest(@NotNull UUID organizationId, @NotNull UUID domainId) {
    }

    public record AgencyDomainResponse(UUID id, UUID organizationId, UUID agencyId, UUID domainId) {
        static AgencyDomainResponse from(AgencyDomain agencyDomain) {
            return new AgencyDomainResponse(agencyDomain.id(), agencyDomain.organizationId(), agencyDomain.agencyId(),
                    agencyDomain.domainId());
        }
    }

    public record CreateAgencyAffiliationRequest(@NotNull UUID organizationId, @NotNull UUID actorId, String type,
            boolean active) {
    }

    public record AgencyAffiliationResponse(UUID id, UUID organizationId, UUID agencyId, UUID actorId, String type,
            boolean active) {
        static AgencyAffiliationResponse from(AgencyAffiliation agencyAffiliation) {
            return new AgencyAffiliationResponse(agencyAffiliation.id(), agencyAffiliation.organizationId(),
                    agencyAffiliation.agencyId(), agencyAffiliation.actorId(), agencyAffiliation.type(),
                    agencyAffiliation.isActive());
        }
    }
}
