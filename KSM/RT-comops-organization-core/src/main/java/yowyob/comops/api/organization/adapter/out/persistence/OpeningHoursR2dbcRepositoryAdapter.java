package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OpeningHoursRepository;
import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.time.DayOfWeek;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OpeningHoursR2dbcRepositoryAdapter implements OpeningHoursRepository {

    private final OpeningHoursSpringDataRepository repository;

    public OpeningHoursR2dbcRepositoryAdapter(OpeningHoursSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<OpeningHoursRule> save(OpeningHoursRule openingHoursRule) {
        return repository.save(toEntity(openingHoursRule)).map(this::toDomain);
    }

    @Override
    public Mono<OpeningHoursRule> findByScopeAndDay(UUID tenantId, UUID organizationId, UUID agencyId, DayOfWeek dayOfWeek) {
        return repository.findByTenantIdAndOrganizationIdAndAgencyIdAndDayOfWeek(tenantId, organizationId, agencyId,
                dayOfWeek.name()).map(this::toDomain);
    }

    @Override
    public Flux<OpeningHoursRule> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteAllByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.deleteAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId);
    }

    private OpeningHoursRuleEntity toEntity(OpeningHoursRule openingHoursRule) {
        return new OpeningHoursRuleEntity(openingHoursRule.id(), openingHoursRule.tenantId(), openingHoursRule.createdAt(),
                openingHoursRule.updatedAt(), openingHoursRule.organizationId(), openingHoursRule.agencyId(),
                openingHoursRule.dayOfWeek().name(), openingHoursRule.opensAt(), openingHoursRule.closesAt(),
                openingHoursRule.closed());
    }

    private OpeningHoursRule toDomain(OpeningHoursRuleEntity entity) {
        return OpeningHoursRule.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), DayOfWeek.valueOf(entity.dayOfWeek()), entity.opensAt(),
                entity.closesAt(), entity.closed());
    }
}
