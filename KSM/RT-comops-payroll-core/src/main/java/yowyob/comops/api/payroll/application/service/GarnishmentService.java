package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.payroll.application.port.in.CreateGarnishmentOrderCommand;
import yowyob.comops.api.payroll.application.port.in.ManageGarnishmentUseCase;
import yowyob.comops.api.payroll.application.port.out.GarnishmentOrderRepository;
import yowyob.comops.api.payroll.domain.model.GarnishmentOrder;

import java.util.UUID;

/** CRUD for wage-garnishment orders, scoped to the request-context tenant. */
@Service
@Profile("!test-memory")
public class GarnishmentService implements ManageGarnishmentUseCase {

    private final GarnishmentOrderRepository repository;

    public GarnishmentService(GarnishmentOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<GarnishmentOrder> create(CreateGarnishmentOrderCommand c) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx -> repository.save(
                GarnishmentOrder.create(ctx.tenantId(), c.organizationId(), c.employeeId(), c.type(),
                        c.beneficiary(), c.reference(), c.totalAmount(), c.monthlyAmount())));
    }

    @Override
    public Mono<GarnishmentOrder> cancel(UUID orderId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), orderId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Garnishment not found")))
                        .map(GarnishmentOrder::cancel)
                        .flatMap(repository::save));
    }

    @Override
    public Mono<GarnishmentOrder> get(UUID orderId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), orderId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Garnishment not found"))));
    }

    @Override
    public Flux<GarnishmentOrder> listForEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByEmployee(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<GarnishmentOrder> listForOrganization(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByOrganization(ctx.tenantId(), organizationId));
    }
}
