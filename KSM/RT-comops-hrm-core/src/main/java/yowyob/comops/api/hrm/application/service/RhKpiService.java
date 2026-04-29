package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.CreateRhKpiSnapshotCommand;
import yowyob.comops.api.hrm.application.port.in.ManageRhKpiUseCase;
import yowyob.comops.api.hrm.application.port.out.RhKpiSnapshotRepository;
import yowyob.comops.api.hrm.domain.model.RhKpiSnapshot;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RhKpiService implements ManageRhKpiUseCase {

    private final RhKpiSnapshotRepository rhKpiSnapshotRepository;

    public RhKpiService(RhKpiSnapshotRepository rhKpiSnapshotRepository) {
        this.rhKpiSnapshotRepository = rhKpiSnapshotRepository;
    }

    @Override
    public Mono<RhKpiSnapshot> createSnapshot(CreateRhKpiSnapshotCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    RhKpiSnapshot snapshot = RhKpiSnapshot.create(ctx.tenantId(),
                            command.organizationId(), command.periode(),
                            command.effectifTotal(), command.effectifActif(),
                            command.tauxTurnover(), command.tauxAbsenteisme(),
                            command.masseSalariale(), command.couvertureCompetences());
                    return rhKpiSnapshotRepository.save(snapshot);
                });
    }

    @Override
    public Mono<RhKpiSnapshot> getSnapshot(UUID id) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> rhKpiSnapshotRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("RH KPI snapshot not found"))));
    }

    @Override
    public Flux<RhKpiSnapshot> listSnapshots(UUID orgId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> rhKpiSnapshotRepository.findByOrganizationId(ctx.tenantId(), orgId));
    }
}
