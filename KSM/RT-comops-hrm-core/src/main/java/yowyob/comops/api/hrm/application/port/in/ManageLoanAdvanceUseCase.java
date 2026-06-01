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

    /** Admin: list all loan advances for the organization. */
    Flux<LoanAdvance> listAll();

    /** Self-service: list the calling user's own loans (resolved via actorId). */
    Flux<LoanAdvance> getMyLoans();

    /** Self-service: employee requests a loan for themselves. */
    Mono<LoanAdvance> requestMyLoan(java.math.BigDecimal montant, int nbEcheances, String motif);
}
