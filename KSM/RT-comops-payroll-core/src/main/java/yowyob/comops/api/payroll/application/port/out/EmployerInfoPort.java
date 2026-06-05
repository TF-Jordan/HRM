package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

/** Outbound port to fetch employer header info from organization-core (via a bootstrap adapter). */
public interface EmployerInfoPort {

    Mono<EmployerInfo> find(UUID tenantId, UUID organizationId);
}
