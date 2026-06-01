package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.InterviewRepository;
import yowyob.comops.api.hrm.domain.model.Interview;
import yowyob.comops.api.hrm.domain.model.InterviewResult;
import yowyob.comops.api.hrm.domain.model.InterviewType;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class InterviewR2dbcRepositoryAdapter implements InterviewRepository {

    private final InterviewSpringDataRepository repository;

    public InterviewR2dbcRepositoryAdapter(InterviewSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Interview> save(Interview interview) {
        return repository.save(toEntity(interview)).map(this::toDomain);
    }

    @Override
    public Mono<Interview> findById(UUID tenantId, UUID interviewId) {
        return repository.findByIdAndTenantId(interviewId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<Interview> findByApplicationId(UUID tenantId, UUID applicationId) {
        return repository.findAllByTenantIdAndApplicationId(tenantId, applicationId).map(this::toDomain);
    }

    private InterviewEntity toEntity(Interview i) {
        return new InterviewEntity(i.id(), i.tenantId(), i.createdAt(), i.updatedAt(),
                i.applicationId(), i.type().name(), i.dateHeure(), i.lieu(),
                i.interviewerPartyId(), i.interviewerDisplayName(), i.notes(),
                i.resultat().name());
    }

    private Interview toDomain(InterviewEntity e) {
        return Interview.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.applicationId(), InterviewType.valueOf(e.type()), e.dateHeure(), e.lieu(),
                e.interviewerPartyId(), e.interviewerDisplayName(), e.notes(),
                InterviewResult.valueOf(e.resultat()));
    }
}
