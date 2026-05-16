package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Application;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {

    Mono<Application> save(Application application);

    Mono<Application> findById(UUID tenantId, UUID applicationId);

    Flux<Application> findByJobOfferId(UUID tenantId, UUID jobOfferId);
}
