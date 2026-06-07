package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.ReviewObjectiveRepository;
import yowyob.comops.api.hrm.domain.model.ReviewObjective;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ReviewObjectiveR2dbcRepositoryAdapter implements ReviewObjectiveRepository {

    private final ReviewObjectiveSpringDataRepository repository;

    public ReviewObjectiveR2dbcRepositoryAdapter(ReviewObjectiveSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ReviewObjective> save(ReviewObjective objective) {
        // The id is domain-assigned, so we must tell Spring Data whether this is an
        // INSERT (create) or UPDATE (evaluate) — otherwise it always inserts and an
        // evaluation hits the primary-key constraint.
        return repository.findByIdAndTenantId(objective.id(), objective.tenantId())
                .map(existing -> Boolean.FALSE)
                .defaultIfEmpty(Boolean.TRUE)
                .flatMap(isNew -> repository.save(toEntity(objective, isNew)))
                .map(this::toDomain);
    }

    @Override
    public Mono<ReviewObjective> findById(UUID tenantId, UUID objectiveId) {
        return repository.findByIdAndTenantId(objectiveId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<ReviewObjective> findByReviewId(UUID tenantId, UUID reviewId) {
        return repository.findAllByTenantIdAndReviewId(tenantId, reviewId).map(this::toDomain);
    }

    private ReviewObjectiveEntity toEntity(ReviewObjective o, boolean isNew) {
        return ReviewObjectiveEntity.of(o.id(), o.tenantId(), o.reviewId(), o.description(),
                o.poids(), o.noteAtteinte(), o.commentaire(), isNew);
    }

    private ReviewObjective toDomain(ReviewObjectiveEntity e) {
        return new ReviewObjective(e.id(), e.tenantId(), e.reviewId(), e.description(),
                e.poids(), e.noteAtteinte(), e.commentaire());
    }
}
