package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MedicalCertificateSpringDataRepository extends ReactiveCrudRepository<MedicalCertificateEntity, UUID> {

    Mono<MedicalCertificateEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<MedicalCertificateEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<MedicalCertificateEntity> findAllByTenantId(UUID tenantId);
}
