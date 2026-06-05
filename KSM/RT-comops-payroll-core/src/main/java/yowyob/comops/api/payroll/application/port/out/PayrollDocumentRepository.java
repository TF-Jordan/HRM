package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayrollDocument;

import java.util.UUID;

/** Persistence port for the generated/signed {@link PayrollDocument} registry. */
public interface PayrollDocumentRepository {

    Mono<PayrollDocument> save(PayrollDocument document);

    Mono<PayrollDocument> findById(UUID tenantId, UUID id);

    Mono<PayrollDocument> findByVerificationCode(UUID tenantId, String verificationCode);

    Flux<PayrollDocument> findByEmployee(UUID tenantId, UUID employeeId);

    Flux<PayrollDocument> findBySubject(UUID tenantId, UUID subjectId);
}
