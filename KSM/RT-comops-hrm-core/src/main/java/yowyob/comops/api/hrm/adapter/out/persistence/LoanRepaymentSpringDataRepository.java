package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LoanRepaymentSpringDataRepository extends ReactiveCrudRepository<LoanRepaymentEntity, UUID> {

    Flux<LoanRepaymentEntity> findAllByTenantIdAndLoanIdOrderByCreatedAtAsc(UUID tenantId, UUID loanId);

    Flux<LoanRepaymentEntity> findAllByTenantIdAndEmployeeIdOrderByCreatedAtAsc(UUID tenantId, UUID employeeId);
}
