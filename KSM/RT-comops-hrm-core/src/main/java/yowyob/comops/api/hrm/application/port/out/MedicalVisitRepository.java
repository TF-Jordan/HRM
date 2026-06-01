package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.MedicalVisit;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MedicalVisitRepository {

    Mono<MedicalVisit> save(MedicalVisit medicalVisit);

    Mono<MedicalVisit> findById(UUID tenantId, UUID visitId);

    Flux<MedicalVisit> findByEmployeeId(UUID tenantId, UUID employeeId);

    /** Every medical visit of the tenant, regardless of employee. */
    Flux<MedicalVisit> findAll(UUID tenantId);
}
