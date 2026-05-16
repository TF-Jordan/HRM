package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.LoanAdvance;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanAdvanceRepository {

    Mono<LoanAdvance> save(LoanAdvance loanAdvance);

    Mono<LoanAdvance> findById(UUID tenantId, UUID loanAdvanceId);

    Flux<LoanAdvance> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LoanAdvance> findActiveByEmployeeId(UUID tenantId, UUID employeeId);
}
