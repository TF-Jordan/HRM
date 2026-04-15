package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class AgencyR2dbcRepositoryAdapter implements AgencyRepository {

    private final AgencySpringDataRepository repository;

    public AgencyR2dbcRepositoryAdapter(AgencySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String code) {
        return repository.existsByTenantIdAndOrganizationIdAndCodeIgnoreCase(tenantId, organizationId, code);
    }

    @Override
    public Mono<Agency> findById(UUID tenantId, UUID agencyId) {
        return repository.findByIdAndTenantId(agencyId, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Agency> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Agency> findByTenantId(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Agency> save(Agency agency) {
        return repository.save(new AgencyEntity(agency.id(), agency.tenantId(), agency.createdAt(), agency.updatedAt(),
                agency.organizationId(), agency.governanceStatus().name(), agency.governedByUserId(),
                agency.governedAt(), agency.governanceReason(), agency.code(), agency.ownerId(), agency.managerId(),
                agency.name(), agency.location(), agency.description(), agency.transferable(), agency.active(),
                agency.logoUri(), agency.logoId(), agency.shortName(), agency.longName(),
                agency.isIndividualBusiness(), agency.isHeadquarter(), agency.country(), agency.city(),
                agency.latitude(), agency.longitude(), agency.openTime(), agency.closeTime(), agency.phone(),
                agency.email(), agency.whatsapp(), agency.greetingMessage(), agency.averageRevenue(),
                agency.capitalShare(), agency.registrationNumber(), agency.socialNetwork(), agency.taxNumber(),
                agency.keywords(), agency.isPublic(), agency.isBusiness(), agency.totalAffiliatedCustomers(),
                agency.deletedAt(), agency.agencyType())).map(this::toDomain);
    }

    private Agency toDomain(AgencyEntity entity) {
        return Agency.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.governanceStatus(), entity.governedByUserId(), entity.governedAt(),
                entity.governanceReason(), entity.code(), entity.ownerId(), entity.managerId(), entity.name(),
                entity.location(), entity.description(), entity.transferable(), entity.active(), entity.logoUri(),
                entity.logoId(), entity.shortName(), entity.longName(), entity.isIndividualBusiness(),
                entity.isHeadquarter(), entity.country(), entity.city(), entity.latitude(), entity.longitude(),
                entity.openTime(), entity.closeTime(), entity.phone(), entity.email(), entity.whatsapp(),
                entity.greetingMessage(), entity.averageRevenue(), entity.capitalShare(),
                entity.registrationNumber(), entity.socialNetwork(), entity.taxNumber(),
                entity.keywords() == null ? Set.of() : entity.keywords(), entity.isPublic(), entity.isBusiness(),
                entity.totalAffiliatedCustomers(), entity.deletedAt(), entity.agencyType());
    }
}
