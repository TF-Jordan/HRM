package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.TrainingRequest;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingRequestRepository {

    Mono<TrainingRequest> save(TrainingRequest request);

    Mono<TrainingRequest> findById(UUID tenantId, UUID requestId);

    Flux<TrainingRequest> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<TrainingRequest> findByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<TrainingRequest> findByOrganizationIdAndStatus(UUID tenantId, UUID organizationId, String status);
}
