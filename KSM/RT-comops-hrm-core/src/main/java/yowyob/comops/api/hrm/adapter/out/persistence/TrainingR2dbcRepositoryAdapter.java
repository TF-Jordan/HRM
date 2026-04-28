package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.TrainingRepository;
import yowyob.comops.api.hrm.domain.model.Training;
import yowyob.comops.api.hrm.domain.model.TrainingStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class TrainingR2dbcRepositoryAdapter implements TrainingRepository {

    private final TrainingSpringDataRepository repository;

    public TrainingR2dbcRepositoryAdapter(TrainingSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Training> save(Training training) {
        return repository.save(toEntity(training)).map(this::toDomain);
    }

    @Override
    public Mono<Training> findById(UUID tenantId, UUID trainingId) {
        return repository.findByIdAndTenantId(trainingId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<Training> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private TrainingEntity toEntity(Training t) {
        return new TrainingEntity(t.id(), t.tenantId(), t.createdAt(), t.updatedAt(),
                t.organizationId(), t.agencyId(), t.intitule(), t.organisme(),
                t.dateDebut(), t.dateFin(), t.cout(), t.nbPlaces(), t.lieu(), t.status().name());
    }

    private Training toDomain(TrainingEntity e) {
        return Training.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.intitule(), e.organisme(),
                e.dateDebut(), e.dateFin(), e.cout(), e.nbPlaces(), e.lieu(),
                TrainingStatus.valueOf(e.status()));
    }
}
