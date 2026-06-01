package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EmployeePersonalInfoSpringDataRepository
        extends ReactiveCrudRepository<EmployeePersonalInfoEntity, UUID> {

    Mono<EmployeePersonalInfoEntity> findByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Mono<Void> deleteByEmployeeIdAndTenantId(UUID employeeId, UUID tenantId);
}
