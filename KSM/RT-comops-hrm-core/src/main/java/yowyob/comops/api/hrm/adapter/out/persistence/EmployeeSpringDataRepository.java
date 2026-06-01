package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeSpringDataRepository extends ReactiveCrudRepository<EmployeeEntity, UUID> {

    Mono<EmployeeEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<Boolean> existsByActorIdAndTenantId(UUID actorId, UUID tenantId);

    Mono<EmployeeEntity> findByActorIdAndTenantId(UUID actorId, UUID tenantId);

    Mono<Boolean> existsByNumCnpsAndTenantId(String numCnps, UUID tenantId);

    Flux<EmployeeEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Flux<EmployeeEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);

    Flux<EmployeeEntity> findAllByTenantIdAndOrganizationIdAndStatus(UUID tenantId, UUID organizationId, String status);

    Flux<EmployeeEntity> findAllByTenantIdAndOrganizationIdAndAgencyIdAndStatus(UUID tenantId, UUID organizationId, UUID agencyId, String status);
}
