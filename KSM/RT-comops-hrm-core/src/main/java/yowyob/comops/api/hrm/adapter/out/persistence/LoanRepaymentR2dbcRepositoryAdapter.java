package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.LoanRepaymentRepository;
import yowyob.comops.api.hrm.domain.model.LoanRepayment;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class LoanRepaymentR2dbcRepositoryAdapter implements LoanRepaymentRepository {

    private final LoanRepaymentSpringDataRepository repository;

    public LoanRepaymentR2dbcRepositoryAdapter(LoanRepaymentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LoanRepayment> save(LoanRepayment loanRepayment) {
        return repository.save(toEntity(loanRepayment)).map(this::toDomain);
    }

    @Override
    public Flux<LoanRepayment> findByLoanId(UUID tenantId, UUID loanId) {
        return repository.findAllByTenantIdAndLoanIdOrderByCreatedAtAsc(tenantId, loanId).map(this::toDomain);
    }

    @Override
    public Flux<LoanRepayment> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeIdOrderByCreatedAtAsc(tenantId, employeeId).map(this::toDomain);
    }

    private LoanRepaymentEntity toEntity(LoanRepayment r) {
        return new LoanRepaymentEntity(r.id(), r.tenantId(), r.createdAt(), r.updatedAt(),
                r.organizationId(), r.loanId(), r.employeeId(), r.runId(), r.period(),
                r.payrollEntryId(), r.montant(), r.soldeApres());
    }

    private LoanRepayment toDomain(LoanRepaymentEntity e) {
        return LoanRepayment.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.loanId(), e.employeeId(), e.runId(), e.period(),
                e.payrollEntryId(), e.montant(), e.soldeApres());
    }
}
