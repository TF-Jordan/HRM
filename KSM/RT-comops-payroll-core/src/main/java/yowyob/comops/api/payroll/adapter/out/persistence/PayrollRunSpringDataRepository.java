package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollRunSpringDataRepository extends ReactiveCrudRepository<PayrollRunEntity, UUID> {

    Mono<PayrollRunEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<PayrollRunEntity> findByTenantIdAndOrganizationIdAndAgencyIdIsNullAndPeriodeAndRunType(
            UUID tenantId, UUID organizationId, String periode, String runType);

    Mono<PayrollRunEntity> findByTenantIdAndOrganizationIdAndAgencyIdAndPeriodeAndRunType(
            UUID tenantId, UUID organizationId, UUID agencyId, String periode, String runType);

    Flux<PayrollRunEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
