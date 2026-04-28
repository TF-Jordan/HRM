package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.OnboardingTaskRepository;
import yowyob.comops.api.hrm.domain.model.OnboardingTask;
import yowyob.comops.api.hrm.domain.model.OnboardingTaskStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OnboardingTaskR2dbcRepositoryAdapter implements OnboardingTaskRepository {

    private final OnboardingTaskSpringDataRepository repository;

    public OnboardingTaskR2dbcRepositoryAdapter(OnboardingTaskSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<OnboardingTask> save(OnboardingTask task) {
        return repository.save(toEntity(task)).map(this::toDomain);
    }

    @Override
    public Mono<OnboardingTask> findById(UUID tenantId, UUID taskId) {
        return repository.findByIdAndTenantId(taskId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<OnboardingTask> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    private OnboardingTaskEntity toEntity(OnboardingTask t) {
        return new OnboardingTaskEntity(t.id(), t.tenantId(), t.createdAt(), t.updatedAt(),
                t.employeeId(), t.titre(), t.description(), t.assignedToPartyId(),
                t.echeance(), t.status().name());
    }

    private OnboardingTask toDomain(OnboardingTaskEntity e) {
        return OnboardingTask.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.employeeId(), e.titre(), e.description(), e.assignedToPartyId(),
                e.echeance(), OnboardingTaskStatus.valueOf(e.status()));
    }
}
