package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Training;
import yowyob.comops.api.hrm.domain.model.TrainingEnrollment;

import java.math.BigDecimal;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageTrainingUseCase {

    Mono<Training> planTraining(PlanTrainingCommand command);

    Mono<Training> startTraining(UUID trainingId);

    Mono<Training> completeTraining(UUID trainingId);

    Mono<Training> cancelTraining(UUID trainingId);

    Mono<Training> getTraining(UUID trainingId);

    Flux<Training> listByOrganization(UUID organizationId);

    Mono<TrainingEnrollment> enrollEmployee(EnrollTrainingCommand command);

    Mono<TrainingEnrollment> completeEnrollment(UUID enrollmentId, BigDecimal note, UUID attestationId);

    Mono<TrainingEnrollment> cancelEnrollment(UUID enrollmentId);

    Flux<TrainingEnrollment> listEnrollmentsByTraining(UUID trainingId);

    Flux<TrainingEnrollment> listEnrollmentsByEmployee(UUID employeeId);
}
