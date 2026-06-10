package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PayrollDataSourceSpringDataRepository
        extends ReactiveCrudRepository<PayrollDataSourceEntity, UUID> {

    Mono<PayrollDataSourceEntity> findByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
