package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.PayrollRun;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollRunRepository {

    Mono<PayrollRun> save(PayrollRun payrollRun);

    Mono<PayrollRun> findById(UUID tenantId, UUID payrollRunId);

    Mono<PayrollRun> findByOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode);

    Mono<PayrollRun> findByOrganizationIdAndAgencyIdAndPeriode(UUID tenantId, UUID organizationId,
                                                                UUID agencyId, String periode);

    Flux<PayrollRun> findByOrganizationId(UUID tenantId, UUID organizationId);
}
