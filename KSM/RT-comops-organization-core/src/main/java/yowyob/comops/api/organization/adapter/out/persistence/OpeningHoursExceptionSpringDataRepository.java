package yowyob.comops.api.organization.adapter.out.persistence;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OpeningHoursExceptionSpringDataRepository extends ReactiveCrudRepository<OpeningHoursExceptionEntity, UUID> {

    Mono<OpeningHoursExceptionEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<OpeningHoursExceptionEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId,
            UUID agencyId);

    Flux<OpeningHoursExceptionEntity> findAllByTenantIdAndOrganizationIdAndAgencyIdAndExceptionDateGreaterThanEqual(
            UUID tenantId, UUID organizationId, UUID agencyId, LocalDate fromDate);

    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
