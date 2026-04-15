package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.domain.model.Organization;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OrganizationR2dbcRepositoryAdapter implements OrganizationRepository {

    private final OrganizationSpringDataRepository repository;

    public OrganizationR2dbcRepositoryAdapter(OrganizationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, String code) {
        return repository.existsByTenantIdAndCodeIgnoreCase(tenantId, code);
    }

    @Override
    public Mono<Boolean> existsByCodeExcludingId(UUID tenantId, String code, UUID organizationId) {
        return repository.existsByTenantIdAndCodeIgnoreCaseAndIdNot(tenantId, code, organizationId);
    }

    @Override
    public Mono<Organization> findById(UUID tenantId, UUID organizationId) {
        return repository.findByIdAndTenantId(organizationId, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Organization> findByTenantId(UUID tenantId) {
        return repository.findAllByTenantId(tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Organization> findByBusinessActorId(UUID tenantId, UUID businessActorId) {
        return repository.findAllByTenantIdAndBusinessActorId(tenantId, businessActorId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Organization> save(Organization organization) {
        return repository.save(new OrganizationEntity(organization.id(), organization.tenantId(), organization.createdAt(),
                organization.updatedAt(), organization.businessActorId(), organization.governanceStatus().name(),
                organization.governedByUserId(), organization.governedAt(), organization.governanceReason(),
                organization.code(), organization.service(), organization.isIndividualBusiness(), organization.email(),
                organization.shortName(), organization.longName(), organization.description(), organization.logoUri(),
                organization.logoId(), organization.websiteUrl(), organization.socialNetwork(),
                organization.businessRegistrationNumber(), organization.taxNumber(), organization.capitalShare(),
                organization.ceoName(), organization.yearFounded(), organization.keywords(),
                organization.numberOfEmployees(), organization.legalForm(), organization.isActive(),
                organization.status(), organization.deletedAt(), organization.legalName(), organization.displayName(),
                organization.organizationType())).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID organizationId) {
        return repository.deleteByIdAndTenantId(organizationId, tenantId);
    }

    private Organization toDomain(OrganizationEntity entity) {
        return Organization.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.businessActorId(), entity.governanceStatus(), entity.governedByUserId(), entity.governedAt(),
                entity.governanceReason(), entity.code(), firstNonBlank(entity.service(), entity.organizationType()),
                entity.isIndividualBusiness(), entity.email(), firstNonBlank(entity.shortName(), entity.displayName()),
                firstNonBlank(entity.longName(), entity.legalName()), entity.description(), entity.logoUri(),
                entity.logoId(), entity.websiteUrl(), entity.socialNetwork(), entity.businessRegistrationNumber(),
                entity.taxNumber(), entity.capitalShare(), entity.ceoName(), entity.yearFounded(),
                entity.keywords() == null ? Set.of() : entity.keywords(), entity.numberOfEmployees(),
                entity.legalForm(), entity.isActive(), entity.status(), entity.deletedAt());
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
