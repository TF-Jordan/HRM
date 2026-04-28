package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.OnboardingTask;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OnboardingTaskRepository {

    Mono<OnboardingTask> save(OnboardingTask task);

    Mono<OnboardingTask> findById(UUID tenantId, UUID taskId);

    Flux<OnboardingTask> findByEmployeeId(UUID tenantId, UUID employeeId);
}
