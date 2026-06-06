package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.LoanRepayment;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanRepaymentRepository {

    Mono<LoanRepayment> save(LoanRepayment loanRepayment);

    /** Repayment history of a single loan, oldest first. */
    Flux<LoanRepayment> findByLoanId(UUID tenantId, UUID loanId);

    /** Repayment history across every loan of an employee, oldest first. */
    Flux<LoanRepayment> findByEmployeeId(UUID tenantId, UUID employeeId);
}
