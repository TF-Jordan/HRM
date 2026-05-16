package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.domain.model.Dependent;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class DependentR2dbcRepositoryAdapter implements DependentRepository {

    private final DependentSpringDataRepository repository;

    public DependentR2dbcRepositoryAdapter(DependentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Dependent> save(Dependent dependent) {
        return repository.save(toEntity(dependent)).map(this::toDomain);
    }

    @Override
    public Flux<Dependent> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID dependentId) {
        return repository.deleteByIdAndTenantId(dependentId, tenantId);
    }

    private DependentEntity toEntity(Dependent d) {
        return new DependentEntity(d.id(), d.tenantId(), d.createdAt(), d.updatedAt(), d.organizationId(),
                d.employeeId(), d.nom(), d.prenom(), d.dateNaissance(), d.lienParente(), d.certificatFileId());
    }

    private Dependent toDomain(DependentEntity e) {
        return Dependent.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.employeeId(), e.nom(), e.prenom(), e.dateNaissance(), e.lienParente(), e.certificatFileId());
    }
}
