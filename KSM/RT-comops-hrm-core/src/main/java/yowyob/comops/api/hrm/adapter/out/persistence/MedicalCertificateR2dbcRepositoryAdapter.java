package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.MedicalCertificateRepository;
import yowyob.comops.api.hrm.domain.model.MedicalCertificate;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class MedicalCertificateR2dbcRepositoryAdapter implements MedicalCertificateRepository {

    private final MedicalCertificateSpringDataRepository repository;

    public MedicalCertificateR2dbcRepositoryAdapter(MedicalCertificateSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<MedicalCertificate> save(MedicalCertificate certificate) {
        return repository.save(toEntity(certificate)).map(this::toDomain);
    }

    @Override
    public Mono<MedicalCertificate> findById(UUID tenantId, UUID certificateId) {
        return repository.findByIdAndTenantId(certificateId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<MedicalCertificate> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    private MedicalCertificateEntity toEntity(MedicalCertificate c) {
        return new MedicalCertificateEntity(c.id(), c.tenantId(), c.createdAt(), c.updatedAt(),
                c.employeeId(), c.typeCertificat(), c.dateEmission(), c.dateExpiration(),
                c.statut(), c.fichierId());
    }

    private MedicalCertificate toDomain(MedicalCertificateEntity e) {
        return MedicalCertificate.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.employeeId(), e.typeCertificat(), e.dateEmission(), e.dateExpiration(),
                e.statut(), e.fichierId());
    }
}
