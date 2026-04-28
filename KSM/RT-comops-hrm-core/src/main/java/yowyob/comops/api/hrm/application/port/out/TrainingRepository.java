package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Training;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrainingRepository {

    Mono<Training> save(Training training);

    Mono<Training> findById(UUID tenantId, UUID trainingId);

    Flux<Training> findByOrganizationId(UUID tenantId, UUID organizationId);
}
