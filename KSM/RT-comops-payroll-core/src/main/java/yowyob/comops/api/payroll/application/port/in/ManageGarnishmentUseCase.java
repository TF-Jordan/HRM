package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.GarnishmentOrder;

import java.util.UUID;

/** Management of wage-garnishment orders. */
public interface ManageGarnishmentUseCase {

    Mono<GarnishmentOrder> create(CreateGarnishmentOrderCommand command);

    Mono<GarnishmentOrder> cancel(UUID orderId);

    Mono<GarnishmentOrder> get(UUID orderId);

    Flux<GarnishmentOrder> listForEmployee(UUID employeeId);

    Flux<GarnishmentOrder> listForOrganization(UUID organizationId);
}
