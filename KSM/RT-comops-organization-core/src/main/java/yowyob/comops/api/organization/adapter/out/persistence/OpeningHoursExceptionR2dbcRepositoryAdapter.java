package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OpeningHoursExceptionRepository;
import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OpeningHoursExceptionR2dbcRepositoryAdapter implements OpeningHoursExceptionRepository {

    private final OpeningHoursExceptionSpringDataRepository repository;

    public OpeningHoursExceptionR2dbcRepositoryAdapter(OpeningHoursExceptionSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<OpeningHoursExceptionRule> save(OpeningHoursExceptionRule exceptionRule) {
        return repository.save(toEntity(exceptionRule)).map(this::toDomain);
    }

    @Override
    public Mono<OpeningHoursExceptionRule> findById(UUID tenantId, UUID exceptionId) {
        return repository.findByIdAndTenantId(exceptionId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<OpeningHoursExceptionRule> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<OpeningHoursExceptionRule> findFutureByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId,
            java.time.LocalDate fromDate) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyIdAndExceptionDateGreaterThanEqual(tenantId,
                organizationId, agencyId, fromDate).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID exceptionId) {
        return repository.deleteByIdAndTenantId(exceptionId, tenantId);
    }

    private OpeningHoursExceptionEntity toEntity(OpeningHoursExceptionRule exceptionRule) {
        return new OpeningHoursExceptionEntity(exceptionRule.id(), exceptionRule.tenantId(), exceptionRule.createdAt(),
                exceptionRule.updatedAt(), exceptionRule.organizationId(), exceptionRule.agencyId(),
                exceptionRule.exceptionDate(), exceptionRule.label(), exceptionRule.opensAt(), exceptionRule.closesAt(),
                exceptionRule.closed());
    }

    private OpeningHoursExceptionRule toDomain(OpeningHoursExceptionEntity entity) {
        return OpeningHoursExceptionRule.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.exceptionDate(), entity.label(), entity.opensAt(),
                entity.closesAt(), entity.closed());
    }
}
