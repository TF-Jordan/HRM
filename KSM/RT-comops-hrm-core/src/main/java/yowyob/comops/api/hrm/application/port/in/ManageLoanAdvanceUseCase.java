package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.LoanAdvance;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageLoanAdvanceUseCase {

    Mono<LoanAdvance> requestLoanAdvance(RequestLoanAdvanceCommand command);

    Mono<LoanAdvance> approveLoanAdvance(UUID loanAdvanceId);

    Mono<LoanAdvance> rejectLoanAdvance(UUID loanAdvanceId, String motif);

    Mono<LoanAdvance> getLoanAdvance(UUID loanAdvanceId);

    Flux<LoanAdvance> listByEmployee(UUID employeeId);

    Flux<LoanAdvance> listActiveByEmployee(UUID tenantId, UUID employeeId);
}
