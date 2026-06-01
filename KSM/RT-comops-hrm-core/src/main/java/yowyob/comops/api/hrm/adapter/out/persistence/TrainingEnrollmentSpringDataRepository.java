package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingEnrollmentSpringDataRepository extends ReactiveCrudRepository<TrainingEnrollmentEntity, UUID> {

    Mono<TrainingEnrollmentEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<TrainingEnrollmentEntity> findAllByTenantIdAndTrainingId(UUID tenantId, UUID trainingId);

    Flux<TrainingEnrollmentEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);
}
