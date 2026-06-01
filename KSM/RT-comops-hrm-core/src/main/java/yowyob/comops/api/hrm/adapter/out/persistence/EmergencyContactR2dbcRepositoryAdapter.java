package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.EmergencyContactRepository;
import yowyob.comops.api.hrm.domain.model.EmergencyContact;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class EmergencyContactR2dbcRepositoryAdapter implements EmergencyContactRepository {

    private final EmergencyContactSpringDataRepository repository;

    public EmergencyContactR2dbcRepositoryAdapter(EmergencyContactSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<EmergencyContact> save(EmergencyContact contact) {
        return repository.save(toEntity(contact)).map(this::toDomain);
    }

    @Override
    public Flux<EmergencyContact> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeIdOrderByPrioriteAsc(tenantId, employeeId)
                .map(this::toDomain);
    }

    @Override
    public Mono<EmergencyContact> findById(UUID tenantId, UUID contactId) {
        return repository.findByIdAndTenantId(contactId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID contactId) {
        return repository.deleteByIdAndTenantId(contactId, tenantId);
    }

    private EmergencyContactEntity toEntity(EmergencyContact c) {
        return new EmergencyContactEntity(
                c.id(), c.tenantId(), c.createdAt(), c.updatedAt(),
                c.employeeId(), c.nom(), c.prenom(), c.relation(),
                c.telephone(), c.email(), c.priorite());
    }

    private EmergencyContact toDomain(EmergencyContactEntity e) {
        return EmergencyContact.rehydrate(
                e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.employeeId(), e.nom(), e.prenom(), e.relation(),
                e.telephone(), e.email(), e.priorite());
    }
}
