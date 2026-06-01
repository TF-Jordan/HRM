package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.MissionOrder;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MissionOrderRepository {

    Mono<MissionOrder> save(MissionOrder missionOrder);

    Mono<MissionOrder> findById(UUID tenantId, UUID missionOrderId);

    Flux<MissionOrder> findByEmployeeId(UUID tenantId, UUID employeeId);

    /** Every mission order of the tenant whose status matches the given value. */
    Flux<MissionOrder> findByStatus(UUID tenantId, String status);

    /** Every mission order of the tenant, regardless of status. */
    Flux<MissionOrder> findAll(UUID tenantId);
}
