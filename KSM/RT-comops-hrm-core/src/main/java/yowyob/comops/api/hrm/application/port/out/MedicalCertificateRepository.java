package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.MedicalCertificate;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MedicalCertificateRepository {

    Mono<MedicalCertificate> save(MedicalCertificate medicalCertificate);

    Mono<MedicalCertificate> findById(UUID tenantId, UUID certificateId);

    Flux<MedicalCertificate> findByEmployeeId(UUID tenantId, UUID employeeId);

    /** Every medical certificate of the tenant, regardless of employee. */
    Flux<MedicalCertificate> findAll(UUID tenantId);
}
