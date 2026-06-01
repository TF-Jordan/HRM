package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.TrainingEnrollment;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingEnrollmentRepository {

    Mono<TrainingEnrollment> save(TrainingEnrollment enrollment);

    Mono<TrainingEnrollment> findById(UUID tenantId, UUID enrollmentId);

    Flux<TrainingEnrollment> findByTrainingId(UUID tenantId, UUID trainingId);

    Flux<TrainingEnrollment> findByEmployeeId(UUID tenantId, UUID employeeId);
}
