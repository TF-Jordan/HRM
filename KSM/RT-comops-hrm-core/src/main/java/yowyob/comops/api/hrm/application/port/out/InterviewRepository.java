package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Interview;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InterviewRepository {

    Mono<Interview> save(Interview interview);

    Mono<Interview> findById(UUID tenantId, UUID interviewId);

    Flux<Interview> findByApplicationId(UUID tenantId, UUID applicationId);
}
