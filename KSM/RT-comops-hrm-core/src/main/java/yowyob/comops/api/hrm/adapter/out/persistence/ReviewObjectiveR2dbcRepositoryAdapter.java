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
        return repository.save(toEntity(objective)).map(this::toDomain);
    }

    @Override
    public Mono<ReviewObjective> findById(UUID tenantId, UUID objectiveId) {
        return repository.findByIdAndTenantId(objectiveId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<ReviewObjective> findByReviewId(UUID tenantId, UUID reviewId) {
        return repository.findAllByTenantIdAndReviewId(tenantId, reviewId).map(this::toDomain);
    }

    private ReviewObjectiveEntity toEntity(ReviewObjective o) {
        return new ReviewObjectiveEntity(o.id(), o.tenantId(), o.reviewId(), o.description(),
                o.poids(), o.noteAtteinte(), o.commentaire());
    }

    private ReviewObjective toDomain(ReviewObjectiveEntity e) {
        return new ReviewObjective(e.id(), e.tenantId(), e.reviewId(), e.description(),
                e.poids(), e.noteAtteinte(), e.commentaire());
    }
}
