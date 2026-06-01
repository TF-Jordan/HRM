package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.RhKpiSnapshot;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageRhKpiUseCase {

    Mono<RhKpiSnapshot> createSnapshot(CreateRhKpiSnapshotCommand command);

    Mono<RhKpiSnapshot> getSnapshot(UUID id);

    Flux<RhKpiSnapshot> listSnapshots(UUID orgId);
}
