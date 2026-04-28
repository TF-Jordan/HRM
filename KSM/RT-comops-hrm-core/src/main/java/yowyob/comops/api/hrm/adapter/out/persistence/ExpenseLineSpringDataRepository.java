package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ExpenseLineSpringDataRepository extends ReactiveCrudRepository<ExpenseLineEntity, UUID> {

    Flux<ExpenseLineEntity> findAllByTenantIdAndExpenseReportId(UUID tenantId, UUID expenseReportId);
}
