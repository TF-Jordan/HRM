package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.MedicalCertificate;
import yowyob.comops.api.hrm.domain.model.MedicalVisit;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageMedicalUseCase {

    Mono<MedicalVisit> createVisit(CreateMedicalVisitCommand command);

    Mono<MedicalVisit> getVisit(UUID visitId);

    Flux<MedicalVisit> listVisitsByEmployee(UUID employeeId);

    /** Org-wide medical visit list (médecin du travail / HR director). */
    Flux<MedicalVisit> listVisits(UUID organizationId);

    Mono<MedicalCertificate> createCertificate(CreateMedicalCertificateCommand command);

    Mono<MedicalCertificate> getCertificate(UUID certificateId);

    Flux<MedicalCertificate> listCertificatesByEmployee(UUID employeeId);

    /** Org-wide medical certificate list (for expiry alerts). */
    Flux<MedicalCertificate> listCertificates(UUID organizationId);
}
