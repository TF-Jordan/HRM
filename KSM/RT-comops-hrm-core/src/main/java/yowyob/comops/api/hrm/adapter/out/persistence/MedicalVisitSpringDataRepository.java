package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MedicalVisitSpringDataRepository extends ReactiveCrudRepository<MedicalVisitEntity, UUID> {

    Mono<MedicalVisitEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<MedicalVisitEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<MedicalVisitEntity> findAllByTenantId(UUID tenantId);
}
