package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PayslipLineSpringDataRepository extends ReactiveCrudRepository<PayslipLineEntity, UUID> {

    Flux<PayslipLineEntity> findAllByTenantIdAndPayrollEntryIdOrderByOrdreAffichage(
            UUID tenantId, UUID payrollEntryId);
}
