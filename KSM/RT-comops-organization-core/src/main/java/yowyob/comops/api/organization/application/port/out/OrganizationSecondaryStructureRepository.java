package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.AgencyAffiliation;
import yowyob.comops.api.organization.domain.model.AgencyDomain;
import yowyob.comops.api.organization.domain.model.BusinessDomain;
import yowyob.comops.api.organization.domain.model.Certification;
import yowyob.comops.api.organization.domain.model.OrganizationActor;
import yowyob.comops.api.organization.domain.model.OrganizationDomain;
import yowyob.comops.api.organization.domain.model.ProposedActivity;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationSecondaryStructureRepository {

    Mono<Boolean> existsBusinessDomainCode(UUID tenantId, String code);

    Mono<BusinessDomain> saveBusinessDomain(BusinessDomain businessDomain);

    Flux<BusinessDomain> findBusinessDomains(UUID tenantId);

    Mono<BusinessDomain> findBusinessDomainById(UUID tenantId, UUID domainId);

    Mono<Certification> saveCertification(Certification certification);

    Flux<Certification> findCertifications(UUID tenantId, UUID organizationId);

    Mono<ProposedActivity> saveProposedActivity(ProposedActivity proposedActivity);

    Flux<ProposedActivity> findProposedActivities(UUID tenantId, UUID organizationId);

    Mono<OrganizationActor> saveOrganizationActor(OrganizationActor organizationActor);

    Flux<OrganizationActor> findOrganizationActors(UUID tenantId, UUID organizationId);

    Mono<OrganizationDomain> saveOrganizationDomain(OrganizationDomain organizationDomain);

    Flux<OrganizationDomain> findOrganizationDomains(UUID tenantId, UUID organizationId);

    Mono<AgencyDomain> saveAgencyDomain(AgencyDomain agencyDomain);

    Flux<AgencyDomain> findAgencyDomains(UUID tenantId, UUID agencyId);

    Mono<AgencyAffiliation> saveAgencyAffiliation(AgencyAffiliation agencyAffiliation);

    Flux<AgencyAffiliation> findAgencyAffiliations(UUID tenantId, UUID agencyId);
}
