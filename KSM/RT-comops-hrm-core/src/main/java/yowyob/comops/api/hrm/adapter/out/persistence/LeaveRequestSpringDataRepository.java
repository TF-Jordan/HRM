package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRequestSpringDataRepository extends ReactiveCrudRepository<LeaveRequestEntity, UUID> {

    Mono<LeaveRequestEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<LeaveRequestEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LeaveRequestEntity> findAllByTenantIdAndOrganizationIdAndStatus(UUID tenantId, UUID organizationId, String status);

    Flux<LeaveRequestEntity> findAllByTenantIdAndOrganizationIdAndAgencyIdAndStatus(
            UUID tenantId, UUID organizationId, UUID agencyId, String status);
}
