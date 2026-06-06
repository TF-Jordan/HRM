package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.EnrollTrainingCommand;
import yowyob.comops.api.hrm.application.port.in.ManageTrainingUseCase;
import yowyob.comops.api.hrm.application.port.in.PlanTrainingCommand;
import yowyob.comops.api.hrm.application.port.in.RequestTrainingCommand;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.TrainingEnrollmentRepository;
import yowyob.comops.api.hrm.application.port.out.TrainingRepository;
import yowyob.comops.api.hrm.application.port.out.TrainingRequestRepository;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.Training;
import yowyob.comops.api.hrm.domain.model.TrainingEnrollment;
import yowyob.comops.api.hrm.domain.model.TrainingRequest;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class TrainingService implements ManageTrainingUseCase {

    private final TrainingRepository trainingRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final TrainingRequestRepository requestRepository;
    private final EmployeeRepository employeeRepository;

    public TrainingService(TrainingRepository trainingRepository,
                           TrainingEnrollmentRepository enrollmentRepository,
                           TrainingRequestRepository requestRepository,
                           EmployeeRepository employeeRepository) {
        this.trainingRepository = trainingRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.requestRepository = requestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Mono<Training> planTraining(PlanTrainingCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> {
                    Training training = Training.create(context.tenantId(), context.organizationId(),
                            command.agencyId(), command.intitule(), command.organisme(),
                            command.dateDebut(), command.dateFin(), command.cout(),
                            command.nbPlaces(), command.lieu());
                    return trainingRepository.save(training);
                });
    }

    @Override
    public Mono<Training> startTraining(UUID trainingId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> trainingRepository.findById(context.tenantId(), trainingId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found")))
                        .map(Training::start)
                        .flatMap(trainingRepository::save));
    }

    @Override
    public Mono<Training> completeTraining(UUID trainingId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> trainingRepository.findById(context.tenantId(), trainingId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found")))
                        .map(Training::complete)
                        .flatMap(trainingRepository::save));
    }

    @Override
    public Mono<Training> cancelTraining(UUID trainingId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> trainingRepository.findById(context.tenantId(), trainingId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found")))
                        .map(Training::cancel)
                        .flatMap(trainingRepository::save));
    }

    @Override
    public Mono<Training> getTraining(UUID trainingId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> trainingRepository.findById(context.tenantId(), trainingId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found"))));
    }

    @Override
    public Flux<Training> listByOrganization(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> trainingRepository.findByOrganizationId(context.tenantId(), organizationId));
    }

    @Override
    public Mono<TrainingEnrollment> enrollEmployee(EnrollTrainingCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .then(trainingRepository.findById(context.tenantId(), command.trainingId())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found"))))
                        .flatMap(training -> {
                            TrainingEnrollment enrollment = TrainingEnrollment.enroll(
                                    context.tenantId(), command.trainingId(), command.employeeId());
                            return enrollmentRepository.save(enrollment);
                        }));
    }

    @Override
    public Mono<TrainingEnrollment> completeEnrollment(UUID enrollmentId, BigDecimal note, UUID attestationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> enrollmentRepository.findById(context.tenantId(), enrollmentId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Enrollment not found")))
                        .map(e -> e.complete(note, attestationId))
                        .flatMap(enrollmentRepository::save));
    }

    @Override
    public Mono<TrainingEnrollment> cancelEnrollment(UUID enrollmentId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> enrollmentRepository.findById(context.tenantId(), enrollmentId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Enrollment not found")))
                        .map(TrainingEnrollment::cancel)
                        .flatMap(enrollmentRepository::save));
    }

    @Override
    public Flux<TrainingEnrollment> listEnrollmentsByTraining(UUID trainingId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> enrollmentRepository.findByTrainingId(context.tenantId(), trainingId));
    }

    @Override
    public Flux<TrainingEnrollment> listEnrollmentsByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> enrollmentRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Mono<TrainingRequest> requestTraining(RequestTrainingCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .then(trainingRepository.findById(context.tenantId(), command.trainingId())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found"))))
                        .flatMap(training -> requestRepository.save(TrainingRequest.create(
                                context.tenantId(), training.organizationId(), command.employeeId(),
                                command.trainingId(), command.motivation()))));
    }

    @Override
    public Mono<TrainingRequest> approveTrainingRequest(UUID requestId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> requestRepository.findById(context.tenantId(), requestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training request not found")))
                        .flatMap(request -> enrollmentRepository.save(TrainingEnrollment.enroll(
                                        context.tenantId(), request.trainingId(), request.employeeId()))
                                .flatMap(enrollment -> requestRepository.save(request.approve(enrollment.id())))));
    }

    @Override
    public Mono<TrainingRequest> rejectTrainingRequest(UUID requestId, String reason) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> requestRepository.findById(context.tenantId(), requestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training request not found")))
                        .map(request -> request.reject(reason))
                        .flatMap(requestRepository::save));
    }

    @Override
    public Mono<TrainingRequest> cancelTrainingRequest(UUID requestId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> requestRepository.findById(context.tenantId(), requestId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training request not found")))
                        .map(TrainingRequest::cancel)
                        .flatMap(requestRepository::save));
    }

    @Override
    public Flux<TrainingRequest> listTrainingRequestsByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> requestRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Flux<TrainingRequest> listTrainingRequestsByOrganization(UUID organizationId, String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> status == null || status.isBlank()
                        ? requestRepository.findByOrganizationId(context.tenantId(), organizationId)
                        : requestRepository.findByOrganizationIdAndStatus(context.tenantId(), organizationId, status));
    }
}
