package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.MissionOrder;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageMissionOrderUseCase {

    Mono<MissionOrder> createMissionOrder(CreateMissionOrderCommand command);

    Mono<MissionOrder> approveMissionOrder(UUID missionOrderId);

    Mono<MissionOrder> startMissionOrder(UUID missionOrderId);

    Mono<MissionOrder> completeMissionOrder(UUID missionOrderId);

    Mono<MissionOrder> cancelMissionOrder(UUID missionOrderId);

    Mono<MissionOrder> getMissionOrder(UUID missionOrderId);

    Flux<MissionOrder> listMissionOrdersByEmployee(UUID employeeId);
}
