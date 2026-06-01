package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.TenantOrgPair;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository {

    Mono<Employee> save(Employee employee);

    Mono<Employee> findById(UUID tenantId, UUID employeeId);

    Mono<Boolean> existsByActorIdAndTenantId(UUID actorId, UUID tenantId);

    Mono<Employee> findByActorId(UUID tenantId, UUID actorId);

    Mono<Boolean> existsByNumCnpsAndTenantId(String numCnps, UUID tenantId);

    Flux<Employee> findByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<Employee> findByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);

    Flux<Employee> findActiveByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<Employee> findActiveByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);

    /** Returns all distinct (tenantId, organizationId) pairs that have at least one ACTIVE employee. */
    Flux<TenantOrgPair> findDistinctActiveOrganizations();
}
