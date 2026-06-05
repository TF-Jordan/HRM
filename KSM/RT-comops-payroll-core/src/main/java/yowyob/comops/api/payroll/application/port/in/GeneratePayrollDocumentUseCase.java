package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayrollDocument;

import java.time.Instant;
import java.util.UUID;

/** Generates electronically-sealed payroll documents and verifies their seals. */
public interface GeneratePayrollDocumentUseCase {

    Mono<PayrollDocument> generatePayslip(UUID payrollEntryId);

    Mono<PayrollDocument> generateFinalSettlement(UUID finalSettlementId);

    Mono<PayrollDocument> generateWorkCertificate(UUID employeeId, String position);

    Mono<PayrollDocument> getDocument(UUID documentId);

    Flux<PayrollDocument> listForEmployee(UUID employeeId);

    /** Recomputes the seal over the stored canonical content and checks the signature. */
    Mono<DocumentVerification> verify(UUID documentId);

    record DocumentVerification(boolean valid, String verificationCode, String contentHashHex,
            String algorithm, Instant signedAt) {
    }
}
