package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeMembershipSpringDataRepository extends ReactiveCrudRepository<EmployeeMembershipEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndUserIdAndStatusNot(UUID tenantId, UUID organizationId, UUID userId,
            String excludedStatus);

    Mono<EmployeeMembershipEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<EmployeeMembershipEntity> findAllByTenantIdAndUserIdAndStatusNot(UUID tenantId, UUID userId, String excludedStatus);

    Flux<EmployeeMembershipEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
