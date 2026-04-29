package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.RhKpiSnapshot;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RhKpiSnapshotRepository {

    Mono<RhKpiSnapshot> save(RhKpiSnapshot snapshot);

    Mono<RhKpiSnapshot> findById(UUID tenantId, UUID id);

    Flux<RhKpiSnapshot> findByOrganizationId(UUID tenantId, UUID orgId);
}
