package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.organization.application.port.in.OrganizationSecondaryStructureUseCase;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationSecondaryStructureRepository;
import yowyob.comops.api.organization.domain.OrganizationNotFoundException;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.model.AgencyAffiliation;
import yowyob.comops.api.organization.domain.model.AgencyDomain;
import yowyob.comops.api.organization.domain.model.BusinessDomain;
import yowyob.comops.api.organization.domain.model.Certification;
import yowyob.comops.api.organization.domain.model.OrganizationActor;
import yowyob.comops.api.organization.domain.model.OrganizationDomain;
import yowyob.comops.api.organization.domain.model.ProposedActivity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrganizationSecondaryStructureApplicationService implements OrganizationSecondaryStructureUseCase {

    private final OrganizationSecondaryStructureRepository repository;
    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;

    public OrganizationSecondaryStructureApplicationService(OrganizationSecondaryStructureRepository repository,
            OrganizationRepository organizationRepository, AgencyRepository agencyRepository) {
        this.repository = repository;
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
    }

    @Override
    public Mono<BusinessDomain> createBusinessDomain(UUID tenantId, String code, String service, UUID parentId, String name,
            String imageUri, UUID imageId, String type, String typeLabel, String description) {
        return repository.existsBusinessDomainCode(tenantId, code)
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalArgumentException("business domain code already exists"))
                        : repository.saveBusinessDomain(BusinessDomain.create(tenantId, code, service, parentId, name,
                                imageUri, imageId, type, typeLabel, description)));
    }

    @Override
    public Flux<BusinessDomain> listBusinessDomains(UUID tenantId) {
        return repository.findBusinessDomains(tenantId);
    }

    @Override
    public Mono<Certification> createCertification(UUID tenantId, UUID organizationId, String type, String name,
            String description, Instant obtainmentDate) {
        return ensureOrganizationExists(tenantId, organizationId)
                .then(repository.saveCertification(Certification.create(tenantId, organizationId, type, name,
                        description, obtainmentDate)));
    }

    @Override
    public Flux<Certification> listCertifications(UUID tenantId, UUID organizationId) {
        return repository.findCertifications(tenantId, organizationId);
    }

    @Override
    public Mono<ProposedActivity> createProposedActivity(UUID tenantId, UUID organizationId, String type, String name,
            BigDecimal rate, String description) {
        return ensureOrganizationExists(tenantId, organizationId)
                .then(repository.saveProposedActivity(ProposedActivity.create(tenantId, organizationId, type, name,
                        rate, description)));
    }

    @Override
    public Flux<ProposedActivity> listProposedActivities(UUID tenantId, UUID organizationId) {
        return repository.findProposedActivities(tenantId, organizationId);
    }

    @Override
    public Mono<OrganizationActor> linkOrganizationActor(UUID tenantId, UUID organizationId, UUID actorId, String type) {
        return ensureOrganizationExists(tenantId, organizationId)
                .then(repository.saveOrganizationActor(OrganizationActor.create(tenantId, organizationId, actorId, type)));
    }

    @Override
    public Flux<OrganizationActor> listOrganizationActors(UUID tenantId, UUID organizationId) {
        return repository.findOrganizationActors(tenantId, organizationId);
    }

    @Override
    public Mono<OrganizationDomain> linkOrganizationDomain(UUID tenantId, UUID organizationId, UUID domainId) {
        return ensureOrganizationExists(tenantId, organizationId)
                .then(ensureBusinessDomainExists(tenantId, domainId))
                .then(repository.saveOrganizationDomain(OrganizationDomain.create(tenantId, organizationId, domainId)));
    }

    @Override
    public Flux<OrganizationDomain> listOrganizationDomains(UUID tenantId, UUID organizationId) {
        return repository.findOrganizationDomains(tenantId, organizationId);
    }

    @Override
    public Mono<AgencyDomain> linkAgencyDomain(UUID tenantId, UUID organizationId, UUID agencyId, UUID domainId) {
        return ensureOrganizationExists(tenantId, organizationId)
                .then(ensureAgencyExists(tenantId, agencyId))
                .then(ensureBusinessDomainExists(tenantId, domainId))
                .then(repository.saveAgencyDomain(AgencyDomain.create(tenantId, organizationId, agencyId, domainId)));
    }

    @Override
    public Flux<AgencyDomain> listAgencyDomains(UUID tenantId, UUID agencyId) {
        return repository.findAgencyDomains(tenantId, agencyId);
    }

    @Override
    public Mono<AgencyAffiliation> createAgencyAffiliation(UUID tenantId, UUID organizationId, UUID agencyId, UUID actorId,
            String type, boolean active) {
        return ensureOrganizationExists(tenantId, organizationId)
                .then(ensureAgencyExists(tenantId, agencyId))
                .then(repository.saveAgencyAffiliation(
                        AgencyAffiliation.create(tenantId, organizationId, agencyId, actorId, type, active)));
    }

    @Override
    public Flux<AgencyAffiliation> listAgencyAffiliations(UUID tenantId, UUID agencyId) {
        return repository.findAgencyAffiliations(tenantId, agencyId);
    }

    private Mono<Void> ensureOrganizationExists(UUID tenantId, UUID organizationId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(organizationId)))
                .then();
    }

    private Mono<Void> ensureAgencyExists(UUID tenantId, UUID agencyId) {
        return agencyRepository.findById(tenantId, agencyId)
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                .then();
    }

    private Mono<Void> ensureBusinessDomainExists(UUID tenantId, UUID domainId) {
        return repository.findBusinessDomainById(tenantId, domainId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("business domain not found")))
                .then();
    }
}
