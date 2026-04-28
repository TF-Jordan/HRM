package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OnboardingTaskSpringDataRepository extends ReactiveCrudRepository<OnboardingTaskEntity, UUID> {

    Mono<OnboardingTaskEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<OnboardingTaskEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);
}
