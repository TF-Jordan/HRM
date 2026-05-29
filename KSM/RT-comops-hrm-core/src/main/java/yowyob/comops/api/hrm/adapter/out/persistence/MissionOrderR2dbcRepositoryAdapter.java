package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.MissionOrderRepository;
import yowyob.comops.api.hrm.domain.model.MissionOrder;
import yowyob.comops.api.hrm.domain.model.MissionOrderStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class MissionOrderR2dbcRepositoryAdapter implements MissionOrderRepository {

    private final MissionOrderSpringDataRepository repository;

    public MissionOrderR2dbcRepositoryAdapter(MissionOrderSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<MissionOrder> save(MissionOrder order) {
        return repository.save(toEntity(order)).map(this::toDomain);
    }

    @Override
    public Mono<MissionOrder> findById(UUID tenantId, UUID missionOrderId) {
        return repository.findByIdAndTenantId(missionOrderId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<MissionOrder> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<MissionOrder> findByStatus(UUID tenantId, String status) {
        return repository.findAllByTenantIdAndStatus(tenantId, status).map(this::toDomain);
    }

    @Override
    public Flux<MissionOrder> findAll(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).map(this::toDomain);
    }

    private MissionOrderEntity toEntity(MissionOrder o) {
        return new MissionOrderEntity(o.id(), o.tenantId(), o.createdAt(), o.updatedAt(),
                o.employeeId(), o.destination(), o.objet(), o.dateDebut(), o.dateFin(),
                o.montantAvance(), o.centreCout(), o.status().name(),
                o.parentOrderId(), o.decisionReason(), o.decidedAt());
    }

    private MissionOrder toDomain(MissionOrderEntity e) {
        return MissionOrder.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.employeeId(), e.destination(), e.objet(), e.dateDebut(), e.dateFin(),
                e.montantAvance(), e.centreCout(), MissionOrderStatus.valueOf(e.status()),
                e.parentOrderId(), e.decisionReason(), e.decidedAt());
    }
}
