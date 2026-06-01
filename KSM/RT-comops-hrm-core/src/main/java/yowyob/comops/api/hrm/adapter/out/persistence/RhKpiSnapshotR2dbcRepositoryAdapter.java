package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.RhKpiSnapshotRepository;
import yowyob.comops.api.hrm.domain.model.RhKpiSnapshot;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class RhKpiSnapshotR2dbcRepositoryAdapter implements RhKpiSnapshotRepository {

    private final RhKpiSnapshotSpringDataRepository repository;

    public RhKpiSnapshotR2dbcRepositoryAdapter(RhKpiSnapshotSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<RhKpiSnapshot> save(RhKpiSnapshot snapshot) {
        return repository.save(toEntity(snapshot)).map(this::toDomain);
    }

    @Override
    public Mono<RhKpiSnapshot> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<RhKpiSnapshot> findByOrganizationId(UUID tenantId, UUID orgId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, orgId).map(this::toDomain);
    }

    private RhKpiSnapshotEntity toEntity(RhKpiSnapshot s) {
        return new RhKpiSnapshotEntity(s.id(), s.tenantId(), s.createdAt(), s.updatedAt(),
                s.organizationId(), s.periode(), s.effectifTotal(), s.effectifActif(),
                s.tauxTurnover(), s.tauxAbsenteisme(), s.masseSalariale(), s.couvertureCompetences());
    }

    private RhKpiSnapshot toDomain(RhKpiSnapshotEntity e) {
        return RhKpiSnapshot.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.periode(), e.effectifTotal(), e.effectifActif(),
                e.tauxTurnover(), e.tauxAbsenteisme(), e.masseSalariale(), e.couvertureCompetences());
    }
}
