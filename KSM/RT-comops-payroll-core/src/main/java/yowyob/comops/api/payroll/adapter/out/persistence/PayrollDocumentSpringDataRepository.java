package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollDocumentSpringDataRepository
        extends ReactiveCrudRepository<PayrollDocumentEntity, UUID> {

    Mono<PayrollDocumentEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<PayrollDocumentEntity> findByTenantIdAndVerificationCode(UUID tenantId, String verificationCode);

    Flux<PayrollDocumentEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<PayrollDocumentEntity> findAllByTenantIdAndSubjectId(UUID tenantId, UUID subjectId);
}
