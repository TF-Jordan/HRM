package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollEmployeeSpringDataRepository
        extends ReactiveCrudRepository<PayrollEmployeeEntity, UUID> {

    Mono<PayrollEmployeeEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<PayrollEmployeeEntity> findByTenantIdAndOrganizationIdAndMatricule(
            UUID tenantId, UUID organizationId, String matricule);

    Mono<PayrollEmployeeEntity> findFirstByTenantIdAndActorId(UUID tenantId, UUID actorId);

    Flux<PayrollEmployeeEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Flux<PayrollEmployeeEntity> findAllByTenantIdAndOrganizationIdAndActiveTrue(
            UUID tenantId, UUID organizationId);

    Flux<PayrollEmployeeEntity> findAllByTenantIdAndOrganizationIdAndAgencyIdAndActiveTrue(
            UUID tenantId, UUID organizationId, UUID agencyId);
}
