package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.MissionOrder;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageMissionOrderUseCase {

    Mono<MissionOrder> createMissionOrder(CreateMissionOrderCommand command);

    /** Manager issues a DRAFT order to the employee for acceptance. */
    Mono<MissionOrder> issueMissionOrder(UUID missionOrderId);

    /** Employee accepts a PENDING_ACCEPTANCE order. */
    Mono<MissionOrder> acceptMissionOrder(UUID missionOrderId);

    /** Employee declines a PENDING_ACCEPTANCE order with a mandatory reason. */
    Mono<MissionOrder> declineMissionOrder(UUID missionOrderId, String reason);

    /** Manager rebuilds a corrected order from a DECLINED parent. */
    Mono<MissionOrder> amendMissionOrder(AmendMissionOrderCommand command);

    Mono<MissionOrder> startMissionOrder(UUID missionOrderId);

    Mono<MissionOrder> completeMissionOrder(UUID missionOrderId);

    Mono<MissionOrder> cancelMissionOrder(UUID missionOrderId);

    Mono<MissionOrder> getMissionOrder(UUID missionOrderId);

    Flux<MissionOrder> listMissionOrdersByEmployee(UUID employeeId);

    /**
     * Org-wide list of every mission order in the tenant. When {@code status}
     * is null all statuses are returned, otherwise only the matching ones.
     */
    Flux<MissionOrder> listMissionOrders(UUID organizationId, String status);

    /**
     * Returns every mission order in the tenant currently awaiting employee
     * acceptance — used by the frontend to build in-app notifications.
     */
    Flux<MissionOrder> listPendingAcceptance(UUID organizationId);

    /**
     * Returns every mission order in the tenant that has been DECLINED so the
     * manager can review and emit an amendment.
     */
    Flux<MissionOrder> listDeclined(UUID organizationId);
}
