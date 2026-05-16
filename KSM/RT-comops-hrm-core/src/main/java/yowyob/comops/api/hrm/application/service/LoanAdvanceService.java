package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.ManageLoanAdvanceUseCase;
import yowyob.comops.api.hrm.application.port.in.RequestLoanAdvanceCommand;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class LoanAdvanceService implements ManageLoanAdvanceUseCase {

    private final LoanAdvanceRepository loanAdvanceRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessEventPublisher businessEventPublisher;

    public LoanAdvanceService(LoanAdvanceRepository loanAdvanceRepository,
                              EmployeeRepository employeeRepository,
                              BusinessEventPublisher businessEventPublisher) {
        this.loanAdvanceRepository = loanAdvanceRepository;
        this.employeeRepository = employeeRepository;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<LoanAdvance> requestLoanAdvance(RequestLoanAdvanceCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> loanAdvanceRepository.findActiveByEmployeeId(
                                        context.tenantId(), command.employeeId())
                                .map(LoanAdvance::soldeRestant)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .flatMap(totalActive -> {
                                    LoanAdvance loan = LoanAdvance.request(
                                            context.tenantId(), context.organizationId(), context.agencyId(),
                                            command.employeeId(), command.montant(), command.nbEcheances(),
                                            command.motif());
                                    return loanAdvanceRepository.save(loan);
                                })));
    }

    @Override
    public Mono<LoanAdvance> approveLoanAdvance(UUID loanAdvanceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> loanAdvanceRepository.findById(context.tenantId(), loanAdvanceId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Loan advance not found")))
                        .map(loan -> loan.approve(context.userId()))
                        .flatMap(loanAdvanceRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(
                                BusinessEvent.now(context.tenantId(), context.organizationId(),
                                        "LOAN_APPROVED", "LOAN_ADVANCE", saved.id(),
                                        payload("employeeId", saved.employeeId(),
                                                "montant", saved.montant()))).thenReturn(saved)));
    }

    @Override
    public Mono<LoanAdvance> rejectLoanAdvance(UUID loanAdvanceId, String motif) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> loanAdvanceRepository.findById(context.tenantId(), loanAdvanceId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Loan advance not found")))
                        .map(loan -> loan.reject(context.userId(), motif))
                        .flatMap(loanAdvanceRepository::save));
    }

    @Override
    public Mono<LoanAdvance> getLoanAdvance(UUID loanAdvanceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> loanAdvanceRepository.findById(context.tenantId(), loanAdvanceId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Loan advance not found"))));
    }

    @Override
    public Flux<LoanAdvance> listByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> loanAdvanceRepository.findByEmployeeId(context.tenantId(), employeeId));
    }

    @Override
    public Flux<LoanAdvance> listActiveByEmployee(UUID tenantId, UUID employeeId) {
        return loanAdvanceRepository.findActiveByEmployeeId(tenantId, employeeId);
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            payload.put(entries[i].toString(), entries[i + 1]);
        }
        return payload;
    }
}
