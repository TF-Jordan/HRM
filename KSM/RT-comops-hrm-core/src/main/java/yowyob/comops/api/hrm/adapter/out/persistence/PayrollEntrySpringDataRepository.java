package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollEntrySpringDataRepository extends ReactiveCrudRepository<PayrollEntryEntity, UUID> {

    Mono<PayrollEntryEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<PayrollEntryEntity> findAllByTenantIdAndPayrollRunId(UUID tenantId, UUID payrollRunId);

    Flux<PayrollEntryEntity> findAllByTenantIdAndPayrollRunIdAndPaymentStatus(
            UUID tenantId, UUID payrollRunId, String paymentStatus);
}
