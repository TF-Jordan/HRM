package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.AmendMissionOrderCommand;
import yowyob.comops.api.hrm.application.port.in.CreateMissionOrderCommand;
import yowyob.comops.api.hrm.application.port.in.ManageMissionOrderUseCase;
import yowyob.comops.api.hrm.application.port.out.MissionOrderRepository;
import yowyob.comops.api.hrm.domain.model.MissionOrder;
import yowyob.comops.api.hrm.domain.model.MissionOrderStatus;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class MissionOrderService implements ManageMissionOrderUseCase {

    private final MissionOrderRepository missionOrderRepository;
    private final BusinessEventPublisher businessEventPublisher;

    public MissionOrderService(MissionOrderRepository missionOrderRepository,
                               BusinessEventPublisher businessEventPublisher) {
        this.missionOrderRepository = missionOrderRepository;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<MissionOrder> createMissionOrder(CreateMissionOrderCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    MissionOrder order = MissionOrder.create(ctx.tenantId(), command.employeeId(),
                            command.destination(), command.objet(), command.dateDebut(),
                            command.dateFin(), command.montantAvance(), command.centreCout());
                    return missionOrderRepository.save(order);
                });
    }

    @Override
    public Mono<MissionOrder> issueMissionOrder(UUID missionOrderId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> missionOrderRepository.findById(ctx.tenantId(), missionOrderId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Mission order not found")))
                        .map(MissionOrder::issueForAcceptance)
                        .flatMap(missionOrderRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(
                                BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                                        "MISSION_PENDING_ACCEPTANCE", "MISSION_ORDER", saved.id(),
                                        payload("employeeId", saved.employeeId(),
                                                "destination", saved.destination(),
                                                "dateDebut", saved.dateDebut(),
                                                "dateFin", saved.dateFin())))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<MissionOrder> acceptMissionOrder(UUID missionOrderId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> missionOrderRepository.findById(ctx.tenantId(), missionOrderId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Mission order not found")))
                        .map(MissionOrder::acceptByEmployee)
                        .flatMap(missionOrderRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(
                                BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                                        "MISSION_ACCEPTED", "MISSION_ORDER", saved.id(),
                                        payload("employeeId", saved.employeeId(),
                                                "decidedAt", saved.decidedAt())))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<MissionOrder> declineMissionOrder(UUID missionOrderId, String reason) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> missionOrderRepository.findById(ctx.tenantId(), missionOrderId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Mission order not found")))
                        .map(order -> order.declineByEmployee(reason))
                        .flatMap(missionOrderRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(
                                BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                                        "MISSION_DECLINED", "MISSION_ORDER", saved.id(),
                                        payload("employeeId", saved.employeeId(),
                                                "reason", saved.decisionReason(),
                                                "decidedAt", saved.decidedAt())))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<MissionOrder> amendMissionOrder(AmendMissionOrderCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> missionOrderRepository.findById(ctx.tenantId(), command.parentMissionOrderId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Parent mission order not found")))
                        .flatMap(parent -> {
                            if (parent.status() != MissionOrderStatus.DECLINED) {
                                return Mono.error(new IllegalStateException(
                                        "Can only amend a DECLINED mission order (was " + parent.status() + ")"));
                            }
                            MissionOrder avenant = MissionOrder.create(ctx.tenantId(), parent.employeeId(),
                                    command.destination(), command.objet(), command.dateDebut(),
                                    command.dateFin(), command.montantAvance(), command.centreCout(),
                                    parent.id());
                            return missionOrderRepository.save(avenant)
                                    .flatMap(saved -> businessEventPublisher.publish(
                                            BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                                                    "MISSION_AMENDED", "MISSION_ORDER", saved.id(),
                                                    payload("employeeId", saved.employeeId(),
                                                            "parentOrderId", saved.parentOrderId())))
                                            .thenReturn(saved));
                        }));
    }

    @Override
    public Mono<MissionOrder> startMissionOrder(UUID missionOrderId) {
        return updateMissionOrder(missionOrderId, MissionOrder::start);
    }

    @Override
    public Mono<MissionOrder> completeMissionOrder(UUID missionOrderId) {
        return updateMissionOrder(missionOrderId, MissionOrder::complete);
    }

    @Override
    public Mono<MissionOrder> cancelMissionOrder(UUID missionOrderId) {
        return updateMissionOrder(missionOrderId, MissionOrder::cancel);
    }

    @Override
    public Mono<MissionOrder> getMissionOrder(UUID missionOrderId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> missionOrderRepository.findById(ctx.tenantId(), missionOrderId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Mission order not found"))));
    }

    @Override
    public Flux<MissionOrder> listMissionOrdersByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> missionOrderRepository.findByEmployeeId(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<MissionOrder> listPendingAcceptance(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> missionOrderRepository.findByStatus(ctx.tenantId(),
                        MissionOrderStatus.PENDING_ACCEPTANCE.name()));
    }

    @Override
    public Flux<MissionOrder> listDeclined(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> missionOrderRepository.findByStatus(ctx.tenantId(),
                        MissionOrderStatus.DECLINED.name()));
    }

    private Mono<MissionOrder> updateMissionOrder(UUID id, java.util.function.Function<MissionOrder, MissionOrder> transition) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> missionOrderRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Mission order not found")))
                        .map(transition)
                        .flatMap(missionOrderRepository::save));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            payload.put(entries[i].toString(), entries[i + 1]);
        }
        return payload;
    }
}
