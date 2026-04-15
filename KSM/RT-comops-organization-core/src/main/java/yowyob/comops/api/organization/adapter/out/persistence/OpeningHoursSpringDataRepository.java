package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OpeningHoursSpringDataRepository extends ReactiveCrudRepository<OpeningHoursRuleEntity, UUID> {

    Mono<OpeningHoursRuleEntity> findByTenantIdAndOrganizationIdAndAgencyIdAndDayOfWeek(
            UUID tenantId, UUID organizationId, UUID agencyId, String dayOfWeek);

    Flux<OpeningHoursRuleEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId,
            UUID agencyId);

    Mono<Void> deleteAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
}
