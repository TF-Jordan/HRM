package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Training;
import yowyob.comops.api.hrm.domain.model.TrainingEnrollment;
import yowyob.comops.api.hrm.domain.model.TrainingRequest;

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

    // --- Self-service training requests (employee → manager/DRH approval) ---

    Mono<TrainingRequest> requestTraining(RequestTrainingCommand command);

    /** Approve a pending request → creates the enrollment and links it back. */
    Mono<TrainingRequest> approveTrainingRequest(UUID requestId);

    Mono<TrainingRequest> rejectTrainingRequest(UUID requestId, String reason);

    /** Employee withdraws their own still-pending request. */
    Mono<TrainingRequest> cancelTrainingRequest(UUID requestId);

    Flux<TrainingRequest> listTrainingRequestsByEmployee(UUID employeeId);

    /** Approval queue for managers/DRH; {@code status} null returns every request. */
    Flux<TrainingRequest> listTrainingRequestsByOrganization(UUID organizationId, String status);
}
