package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.AddObjectiveCommand;
import yowyob.comops.api.hrm.application.port.in.CreateReviewCommand;
import yowyob.comops.api.hrm.application.port.in.ManageReviewUseCase;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.PerformanceReviewRepository;
import yowyob.comops.api.hrm.application.port.out.ReviewObjectiveRepository;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.PerformanceReview;
import yowyob.comops.api.hrm.domain.model.ReviewObjective;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class ReviewService implements ManageReviewUseCase {

    private final PerformanceReviewRepository reviewRepository;
    private final ReviewObjectiveRepository objectiveRepository;
    private final EmployeeRepository employeeRepository;

    public ReviewService(PerformanceReviewRepository reviewRepository,
                         ReviewObjectiveRepository objectiveRepository,
                         EmployeeRepository employeeRepository) {
        this.reviewRepository = reviewRepository;
        this.objectiveRepository = objectiveRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Mono<PerformanceReview> createReview(CreateReviewCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> {
                            PerformanceReview review = PerformanceReview.create(context.tenantId(),
                                    context.organizationId(), command.employeeId(),
                                    command.evaluateurPartyId(), command.evaluateurDisplayName(),
                                    command.periode());
                            return reviewRepository.save(review);
                        }));
    }

    @Override
    public Mono<PerformanceReview> submitReview(UUID reviewId, BigDecimal noteGlobale,
                                                 String commentaires, String planAction) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> reviewRepository.findById(context.tenantId(), reviewId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found")))
                        .map(r -> r.submit(noteGlobale, commentaires, planAction))
                        .flatMap(reviewRepository::save));
    }

    @Override
    public Mono<PerformanceReview> acknowledgeReview(UUID reviewId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> reviewRepository.findById(context.tenantId(), reviewId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found")))
                        .map(PerformanceReview::acknowledge)
                        .flatMap(reviewRepository::save));
    }

    @Override
    public Mono<PerformanceReview> acknowledgeMyReview(UUID reviewId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findByActorId(context.tenantId(), context.actorId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(context.actorId())))
                        .flatMap(employee -> reviewRepository.findById(context.tenantId(), reviewId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found")))
                                // Ownership guard: a worker can only acknowledge their OWN review.
                                .flatMap(review -> review.employeeId().equals(employee.id())
                                        ? Mono.just(review)
                                        : Mono.error(new IllegalArgumentException("Review not found")))
                                .map(PerformanceReview::acknowledge)
                                .flatMap(reviewRepository::save)));
    }

    @Override
    public Mono<PerformanceReview> finalizeReview(UUID reviewId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> reviewRepository.findById(context.tenantId(), reviewId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found")))
                        .map(PerformanceReview::finalize_)
                        .flatMap(reviewRepository::save));
    }

    @Override
    public Mono<PerformanceReview> getReview(UUID reviewId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> reviewRepository.findById(context.tenantId(), reviewId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found"))));
    }

    @Override
    public Flux<PerformanceReview> listByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> reviewRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Flux<PerformanceReview> listByOrganizationAndPeriode(UUID organizationId, String periode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> reviewRepository.findByOrganizationIdAndPeriode(
                        context.tenantId(), organizationId, periode));
    }

    @Override
    public Mono<ReviewObjective> addObjective(AddObjectiveCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> reviewRepository.findById(context.tenantId(), command.reviewId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found")))
                        .flatMap(review -> {
                            ReviewObjective objective = ReviewObjective.create(context.tenantId(),
                                    command.reviewId(), command.description(), command.poids());
                            return objectiveRepository.save(objective);
                        }));
    }

    @Override
    public Mono<ReviewObjective> evaluateObjective(UUID objectiveId, BigDecimal noteAtteinte, String commentaire) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> objectiveRepository.findById(context.tenantId(), objectiveId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Objective not found")))
                        .map(o -> o.evaluate(noteAtteinte, commentaire))
                        .flatMap(objectiveRepository::save));
    }

    @Override
    public Flux<ReviewObjective> listObjectivesByReview(UUID reviewId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> objectiveRepository.findByReviewId(context.tenantId(), reviewId));
    }
}
