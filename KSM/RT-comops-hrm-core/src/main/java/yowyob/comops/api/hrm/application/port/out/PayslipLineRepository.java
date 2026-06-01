package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.PayslipLine;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayslipLineRepository {

    Mono<PayslipLine> save(PayslipLine payslipLine);

    Flux<PayslipLine> findByPayrollEntryId(UUID tenantId, UUID payrollEntryId);
}
