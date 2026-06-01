package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OrganizationSecondaryStructureRepository;
import yowyob.comops.api.organization.domain.model.AgencyAffiliation;
import yowyob.comops.api.organization.domain.model.AgencyDomain;
import yowyob.comops.api.organization.domain.model.BusinessDomain;
import yowyob.comops.api.organization.domain.model.Certification;
import yowyob.comops.api.organization.domain.model.OrganizationActor;
import yowyob.comops.api.organization.domain.model.OrganizationDomain;
import yowyob.comops.api.organization.domain.model.ProposedActivity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOrganizationSecondaryStructureRepository implements OrganizationSecondaryStructureRepository {

    private final Map<UUID, BusinessDomain> businessDomains = new ConcurrentHashMap<>();
    private final Map<UUID, Certification> certifications = new ConcurrentHashMap<>();
    private final Map<UUID, ProposedActivity> proposedActivities = new ConcurrentHashMap<>();
    private final Map<UUID, OrganizationActor> organizationActors = new ConcurrentHashMap<>();
    private final Map<UUID, OrganizationDomain> organizationDomains = new ConcurrentHashMap<>();
    private final Map<UUID, AgencyDomain> agencyDomains = new ConcurrentHashMap<>();
    private final Map<UUID, AgencyAffiliation> agencyAffiliations = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsBusinessDomainCode(UUID tenantId, String code) {
        return Mono.fromSupplier(() -> businessDomains.values().stream()
                .filter(domain -> domain.tenantId().equals(tenantId))
                .anyMatch(domain -> domain.code().equalsIgnoreCase(code)));
    }

    @Override
    public Mono<BusinessDomain> saveBusinessDomain(BusinessDomain businessDomain) {
        return Mono.fromSupplier(() -> {
            businessDomains.put(businessDomain.id(), businessDomain);
            return businessDomain;
        });
    }

    @Override
    public Flux<BusinessDomain> findBusinessDomains(UUID tenantId) {
        return Flux.fromStream(businessDomains.values().stream()
                .filter(domain -> domain.tenantId().equals(tenantId)));
    }

    @Override
    public Mono<BusinessDomain> findBusinessDomainById(UUID tenantId, UUID domainId) {
        return Mono.justOrEmpty(businessDomains.get(domainId))
                .filter(domain -> domain.tenantId().equals(tenantId));
    }

    @Override
    public Mono<Certification> saveCertification(Certification certification) {
        return Mono.fromSupplier(() -> {
            certifications.put(certification.id(), certification);
            return certification;
        });
    }

    @Override
    public Flux<Certification> findCertifications(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(certifications.values().stream()
                .filter(certification -> certification.tenantId().equals(tenantId))
                .filter(certification -> certification.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<ProposedActivity> saveProposedActivity(ProposedActivity proposedActivity) {
        return Mono.fromSupplier(() -> {
            proposedActivities.put(proposedActivity.id(), proposedActivity);
            return proposedActivity;
        });
    }

    @Override
    public Flux<ProposedActivity> findProposedActivities(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(proposedActivities.values().stream()
                .filter(activity -> activity.tenantId().equals(tenantId))
                .filter(activity -> activity.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<OrganizationActor> saveOrganizationActor(OrganizationActor organizationActor) {
        return Mono.fromSupplier(() -> {
            organizationActors.put(organizationActor.id(), organizationActor);
            return organizationActor;
        });
    }

    @Override
    public Flux<OrganizationActor> findOrganizationActors(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(organizationActors.values().stream()
                .filter(link -> link.tenantId().equals(tenantId))
                .filter(link -> link.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<OrganizationDomain> saveOrganizationDomain(OrganizationDomain organizationDomain) {
        return Mono.fromSupplier(() -> {
            organizationDomains.put(organizationDomain.id(), organizationDomain);
            return organizationDomain;
        });
    }

    @Override
    public Flux<OrganizationDomain> findOrganizationDomains(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(organizationDomains.values().stream()
                .filter(link -> link.tenantId().equals(tenantId))
                .filter(link -> link.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<AgencyDomain> saveAgencyDomain(AgencyDomain agencyDomain) {
        return Mono.fromSupplier(() -> {
            agencyDomains.put(agencyDomain.id(), agencyDomain);
            return agencyDomain;
        });
    }

    @Override
    public Flux<AgencyDomain> findAgencyDomains(UUID tenantId, UUID agencyId) {
        return Flux.fromStream(agencyDomains.values().stream()
                .filter(link -> link.tenantId().equals(tenantId))
                .filter(link -> link.agencyId().equals(agencyId)));
    }

    @Override
    public Mono<AgencyAffiliation> saveAgencyAffiliation(AgencyAffiliation agencyAffiliation) {
        return Mono.fromSupplier(() -> {
            agencyAffiliations.put(agencyAffiliation.id(), agencyAffiliation);
            return agencyAffiliation;
        });
    }

    @Override
    public Flux<AgencyAffiliation> findAgencyAffiliations(UUID tenantId, UUID agencyId) {
        return Flux.fromStream(agencyAffiliations.values().stream()
                .filter(link -> link.tenantId().equals(tenantId))
                .filter(link -> link.agencyId().equals(agencyId)));
    }
}
