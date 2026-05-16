package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.AgencyAffiliation;
import yowyob.comops.api.organization.domain.model.AgencyDomain;
import yowyob.comops.api.organization.domain.model.BusinessDomain;
import yowyob.comops.api.organization.domain.model.Certification;
import yowyob.comops.api.organization.domain.model.OrganizationActor;
import yowyob.comops.api.organization.domain.model.OrganizationDomain;
import yowyob.comops.api.organization.domain.model.ProposedActivity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationSecondaryStructureUseCase {

    Mono<BusinessDomain> createBusinessDomain(UUID tenantId, String code, String service, UUID parentId, String name,
            String imageUri, UUID imageId, String type, String typeLabel, String description);

    Flux<BusinessDomain> listBusinessDomains(UUID tenantId);

    Mono<Certification> createCertification(UUID tenantId, UUID organizationId, String type, String name,
            String description, Instant obtainmentDate);

    Flux<Certification> listCertifications(UUID tenantId, UUID organizationId);

    Mono<ProposedActivity> createProposedActivity(UUID tenantId, UUID organizationId, String type, String name,
            BigDecimal rate, String description);

    Flux<ProposedActivity> listProposedActivities(UUID tenantId, UUID organizationId);

    Mono<OrganizationActor> linkOrganizationActor(UUID tenantId, UUID organizationId, UUID actorId, String type);

    Flux<OrganizationActor> listOrganizationActors(UUID tenantId, UUID organizationId);

    Mono<OrganizationDomain> linkOrganizationDomain(UUID tenantId, UUID organizationId, UUID domainId);

    Flux<OrganizationDomain> listOrganizationDomains(UUID tenantId, UUID organizationId);

    Mono<AgencyDomain> linkAgencyDomain(UUID tenantId, UUID organizationId, UUID agencyId, UUID domainId);

    Flux<AgencyDomain> listAgencyDomains(UUID tenantId, UUID agencyId);

    Mono<AgencyAffiliation> createAgencyAffiliation(UUID tenantId, UUID organizationId, UUID agencyId, UUID actorId,
            String type, boolean active);

    Flux<AgencyAffiliation> listAgencyAffiliations(UUID tenantId, UUID agencyId);
}
