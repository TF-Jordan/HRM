package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Employee;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository {

    Mono<Employee> save(Employee employee);

    Mono<Employee> findById(UUID tenantId, UUID employeeId);

    Mono<Boolean> existsByActorIdAndTenantId(UUID actorId, UUID tenantId);

    Flux<Employee> findByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<Employee> findByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);

    Flux<Employee> findActiveByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<Employee> findActiveByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
}
