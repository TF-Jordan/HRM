package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayrollDocumentRepository;
import yowyob.comops.api.payroll.domain.model.PayrollDocument;
import yowyob.comops.api.payroll.domain.model.PayrollDocumentType;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class PayrollDocumentR2dbcRepositoryAdapter implements PayrollDocumentRepository {

    private final PayrollDocumentSpringDataRepository repository;

    public PayrollDocumentR2dbcRepositoryAdapter(PayrollDocumentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayrollDocument> save(PayrollDocument document) {
        return repository.save(toEntity(document)).map(this::toDomain);
    }

    @Override
    public Mono<PayrollDocument> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<PayrollDocument> findByVerificationCode(UUID tenantId, String verificationCode) {
        return repository.findByTenantIdAndVerificationCode(tenantId, verificationCode).map(this::toDomain);
    }

    @Override
    public Flux<PayrollDocument> findByEmployee(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<PayrollDocument> findBySubject(UUID tenantId, UUID subjectId) {
        return repository.findAllByTenantIdAndSubjectId(tenantId, subjectId).map(this::toDomain);
    }

    private PayrollDocumentEntity toEntity(PayrollDocument d) {
        return new PayrollDocumentEntity(d.id(), d.tenantId(), d.createdAt(), d.updatedAt(),
                d.organizationId(), d.employeeId(), d.type().name(), d.subjectId(), d.periode(), d.fileId(),
                d.fileName(), d.canonicalContent(), d.algorithm(), d.contentHashHex(), d.signatureBase64(),
                d.keyId(), d.signedBy(), d.signedAt(), d.verificationCode());
    }

    private PayrollDocument toDomain(PayrollDocumentEntity e) {
        return PayrollDocument.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), PayrollDocumentType.valueOf(e.type()), e.subjectId(),
                e.periode(), e.fileId(), e.fileName(), e.canonicalContent(), e.algorithm(),
                e.contentHashHex(), e.signatureBase64(), e.keyId(), e.signedBy(), e.signedAt(),
                e.verificationCode());
    }
}
