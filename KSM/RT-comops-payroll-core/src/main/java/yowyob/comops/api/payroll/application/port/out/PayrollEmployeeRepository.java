package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayrollEmployee;

import java.util.UUID;

/** Persistence port for payroll-owned employee records (standalone payroll mode). */
public interface PayrollEmployeeRepository {

    Mono<PayrollEmployee> save(PayrollEmployee employee);

    Mono<PayrollEmployee> findById(UUID tenantId, UUID id);

    Mono<PayrollEmployee> findByMatricule(UUID tenantId, UUID organizationId, String matricule);

    Mono<PayrollEmployee> findByActorId(UUID tenantId, UUID actorId);

    Flux<PayrollEmployee> findByOrganization(UUID tenantId, UUID organizationId);

    Flux<PayrollEmployee> findActiveByOrganization(UUID tenantId, UUID organizationId, UUID agencyId);
}
