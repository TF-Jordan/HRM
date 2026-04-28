package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.CreateMissionOrderCommand;
import yowyob.comops.api.hrm.application.port.in.ManageMissionOrderUseCase;
import yowyob.comops.api.hrm.application.port.out.MissionOrderRepository;
import yowyob.comops.api.hrm.domain.model.MissionOrder;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MissionOrderService implements ManageMissionOrderUseCase {

    private final MissionOrderRepository missionOrderRepository;

    public MissionOrderService(MissionOrderRepository missionOrderRepository) {
        this.missionOrderRepository = missionOrderRepository;
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
    public Mono<MissionOrder> approveMissionOrder(UUID missionOrderId) {
        return updateMissionOrder(missionOrderId, MissionOrder::approve);
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

    private Mono<MissionOrder> updateMissionOrder(UUID id, java.util.function.Function<MissionOrder, MissionOrder> transition) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> missionOrderRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Mission order not found")))
                        .map(transition)
                        .flatMap(missionOrderRepository::save));
    }
}
