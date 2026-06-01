package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollRunSpringDataRepository extends ReactiveCrudRepository<PayrollRunEntity, UUID> {

    Mono<PayrollRunEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<PayrollRunEntity> findByTenantIdAndOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode);

    Mono<PayrollRunEntity> findByTenantIdAndOrganizationIdAndAgencyIdAndPeriode(
            UUID tenantId, UUID organizationId, UUID agencyId, String periode);

    Flux<PayrollRunEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
