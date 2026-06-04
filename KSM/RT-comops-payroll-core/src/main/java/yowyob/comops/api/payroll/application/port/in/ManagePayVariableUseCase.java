package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayVariable;

import java.util.UUID;

/** Capture and consultation of per-employee monthly variable inputs. */
public interface ManagePayVariableUseCase {

    Mono<PayVariable> capture(CapturePayVariableCommand command);

    Mono<PayVariable> getForEmployee(UUID employeeId, String period);

    Flux<PayVariable> listForOrganization(UUID organizationId, String period);
}
