package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeSkillSpringDataRepository extends ReactiveCrudRepository<EmployeeSkillEntity, UUID> {

    Mono<EmployeeSkillEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<EmployeeSkillEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<EmployeeSkillEntity> findAllByTenantIdAndSkillId(UUID tenantId, UUID skillId);
}
