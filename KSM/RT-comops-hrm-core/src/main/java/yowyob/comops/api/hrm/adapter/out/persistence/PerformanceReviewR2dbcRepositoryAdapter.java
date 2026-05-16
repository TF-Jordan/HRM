package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PerformanceReviewRepository;
import yowyob.comops.api.hrm.domain.model.PerformanceReview;
import yowyob.comops.api.hrm.domain.model.ReviewStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PerformanceReviewR2dbcRepositoryAdapter implements PerformanceReviewRepository {

    private final PerformanceReviewSpringDataRepository repository;

    public PerformanceReviewR2dbcRepositoryAdapter(PerformanceReviewSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PerformanceReview> save(PerformanceReview review) {
        return repository.save(toEntity(review)).map(this::toDomain);
    }

    @Override
    public Mono<PerformanceReview> findById(UUID tenantId, UUID reviewId) {
        return repository.findByIdAndTenantId(reviewId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<PerformanceReview> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<PerformanceReview> findByOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode) {
        return repository.findAllByTenantIdAndOrganizationIdAndPeriode(tenantId, organizationId, periode).map(this::toDomain);
    }

    private PerformanceReviewEntity toEntity(PerformanceReview r) {
        return new PerformanceReviewEntity(r.id(), r.tenantId(), r.createdAt(), r.updatedAt(),
                r.organizationId(), r.employeeId(), r.evaluateurPartyId(), r.evaluateurDisplayName(),
                r.periode(), r.noteGlobale(), r.commentaires(), r.planAction(), r.status().name());
    }

    private PerformanceReview toDomain(PerformanceReviewEntity e) {
        return PerformanceReview.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), e.evaluateurPartyId(), e.evaluateurDisplayName(),
                e.periode(), e.noteGlobale(), e.commentaires(), e.planAction(),
                ReviewStatus.valueOf(e.status()));
    }
}
