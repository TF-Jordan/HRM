package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OrganizationSecondaryStructureRepository;
import yowyob.comops.api.organization.domain.model.AgencyAffiliation;
import yowyob.comops.api.organization.domain.model.AgencyDomain;
import yowyob.comops.api.organization.domain.model.BusinessDomain;
import yowyob.comops.api.organization.domain.model.Certification;
import yowyob.comops.api.organization.domain.model.OrganizationActor;
import yowyob.comops.api.organization.domain.model.OrganizationDomain;
import yowyob.comops.api.organization.domain.model.ProposedActivity;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OrganizationSecondaryStructureR2dbcRepositoryAdapter implements OrganizationSecondaryStructureRepository {

    private final BusinessDomainSpringDataRepository businessDomains;
    private final CertificationSpringDataRepository certifications;
    private final ProposedActivitySpringDataRepository proposedActivities;
    private final OrganizationActorSpringDataRepository organizationActors;
    private final OrganizationDomainSpringDataRepository organizationDomains;
    private final AgencyDomainSpringDataRepository agencyDomains;
    private final AgencyAffiliationSpringDataRepository agencyAffiliations;

    public OrganizationSecondaryStructureR2dbcRepositoryAdapter(
            BusinessDomainSpringDataRepository businessDomains,
            CertificationSpringDataRepository certifications,
            ProposedActivitySpringDataRepository proposedActivities,
            OrganizationActorSpringDataRepository organizationActors,
            OrganizationDomainSpringDataRepository organizationDomains,
            AgencyDomainSpringDataRepository agencyDomains,
            AgencyAffiliationSpringDataRepository agencyAffiliations) {
        this.businessDomains = businessDomains;
        this.certifications = certifications;
        this.proposedActivities = proposedActivities;
        this.organizationActors = organizationActors;
        this.organizationDomains = organizationDomains;
        this.agencyDomains = agencyDomains;
        this.agencyAffiliations = agencyAffiliations;
    }

    @Override
    public Mono<Boolean> existsBusinessDomainCode(UUID tenantId, String code) {
        return businessDomains.existsByTenantIdAndCodeIgnoreCase(tenantId, code);
    }

    @Override
    public Mono<BusinessDomain> saveBusinessDomain(BusinessDomain businessDomain) {
        return businessDomains.save(new BusinessDomainEntity(
                businessDomain.id(), businessDomain.tenantId(), businessDomain.createdAt(), businessDomain.updatedAt(),
                businessDomain.code(), businessDomain.service(), businessDomain.parentId(),
                businessDomain.name(), businessDomain.imageUri(), businessDomain.imageId(), businessDomain.type(),
                businessDomain.typeLabel(), businessDomain.description(), businessDomain.deletedAt())).map(this::toDomain);
    }

    @Override
    public Flux<BusinessDomain> findBusinessDomains(UUID tenantId) {
        return businessDomains.findAllByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    public Mono<BusinessDomain> findBusinessDomainById(UUID tenantId, UUID domainId) {
        return businessDomains.findByIdAndTenantId(domainId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Certification> saveCertification(Certification certification) {
        return certifications.save(new CertificationEntity(
                certification.id(), certification.tenantId(), certification.createdAt(), certification.updatedAt(),
                certification.organizationId(), certification.type(), certification.name(), certification.description(),
                certification.obtainmentDate(), certification.deletedAt())).map(this::toDomain);
    }

    @Override
    public Flux<Certification> findCertifications(UUID tenantId, UUID organizationId) {
        return certifications.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<ProposedActivity> saveProposedActivity(ProposedActivity proposedActivity) {
        return proposedActivities.save(new ProposedActivityEntity(
                proposedActivity.id(), proposedActivity.tenantId(), proposedActivity.createdAt(),
                proposedActivity.updatedAt(), proposedActivity.organizationId(), proposedActivity.type(),
                proposedActivity.name(), proposedActivity.rate(), proposedActivity.description(),
                proposedActivity.deletedAt())).map(this::toDomain);
    }

    @Override
    public Flux<ProposedActivity> findProposedActivities(UUID tenantId, UUID organizationId) {
        return proposedActivities.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<OrganizationActor> saveOrganizationActor(OrganizationActor organizationActor) {
        return organizationActors.save(new OrganizationActorEntity(
                organizationActor.id(), organizationActor.tenantId(), organizationActor.createdAt(),
                organizationActor.updatedAt(), organizationActor.organizationId(), organizationActor.actorId(),
                organizationActor.type(), organizationActor.deletedAt())).map(this::toDomain);
    }

    @Override
    public Flux<OrganizationActor> findOrganizationActors(UUID tenantId, UUID organizationId) {
        return organizationActors.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<OrganizationDomain> saveOrganizationDomain(OrganizationDomain organizationDomain) {
        return organizationDomains.save(new OrganizationDomainEntity(
                organizationDomain.id(), organizationDomain.tenantId(), organizationDomain.createdAt(),
                organizationDomain.updatedAt(), organizationDomain.organizationId(), organizationDomain.domainId(),
                organizationDomain.deletedAt())).map(this::toDomain);
    }

    @Override
    public Flux<OrganizationDomain> findOrganizationDomains(UUID tenantId, UUID organizationId) {
        return organizationDomains.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<AgencyDomain> saveAgencyDomain(AgencyDomain agencyDomain) {
        return agencyDomains.save(new AgencyDomainEntity(
                agencyDomain.id(), agencyDomain.tenantId(), agencyDomain.createdAt(), agencyDomain.updatedAt(),
                agencyDomain.organizationId(), agencyDomain.agencyId(), agencyDomain.domainId(),
                agencyDomain.deletedAt())).map(this::toDomain);
    }

    @Override
    public Flux<AgencyDomain> findAgencyDomains(UUID tenantId, UUID agencyId) {
        return agencyDomains.findAllByTenantIdAndAgencyId(tenantId, agencyId).map(this::toDomain);
    }

    @Override
    public Mono<AgencyAffiliation> saveAgencyAffiliation(AgencyAffiliation agencyAffiliation) {
        return agencyAffiliations.save(new AgencyAffiliationEntity(
                agencyAffiliation.id(), agencyAffiliation.tenantId(), agencyAffiliation.createdAt(),
                agencyAffiliation.updatedAt(), agencyAffiliation.organizationId(), agencyAffiliation.agencyId(),
                agencyAffiliation.actorId(), agencyAffiliation.type(), agencyAffiliation.isActive(),
                agencyAffiliation.deletedAt())).map(this::toDomain);
    }

    @Override
    public Flux<AgencyAffiliation> findAgencyAffiliations(UUID tenantId, UUID agencyId) {
        return agencyAffiliations.findAllByTenantIdAndAgencyId(tenantId, agencyId).map(this::toDomain);
    }

    private BusinessDomain toDomain(BusinessDomainEntity entity) {
        return BusinessDomain.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.code(), entity.service(), entity.parentId(), entity.name(), entity.imageUri(), entity.imageId(),
                entity.type(), entity.typeLabel(), entity.description(), entity.deletedAt());
    }

    private Certification toDomain(CertificationEntity entity) {
        return Certification.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.type(), entity.name(), entity.description(), entity.obtainmentDate(),
                entity.deletedAt());
    }

    private ProposedActivity toDomain(ProposedActivityEntity entity) {
        return ProposedActivity.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.type(), entity.name(), entity.rate(), entity.description(),
                entity.deletedAt());
    }

    private OrganizationActor toDomain(OrganizationActorEntity entity) {
        return OrganizationActor.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.actorId(), entity.type(), entity.deletedAt());
    }

    private OrganizationDomain toDomain(OrganizationDomainEntity entity) {
        return OrganizationDomain.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.domainId(), entity.deletedAt());
    }

    private AgencyDomain toDomain(AgencyDomainEntity entity) {
        return AgencyDomain.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.domainId(), entity.deletedAt());
    }

    private AgencyAffiliation toDomain(AgencyAffiliationEntity entity) {
        return AgencyAffiliation.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.actorId(), entity.type(), entity.isActive(),
                entity.deletedAt());
    }
}
