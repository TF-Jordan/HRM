package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.TrainingRequestRepository;
import yowyob.comops.api.hrm.domain.model.TrainingRequest;
import yowyob.comops.api.hrm.domain.model.TrainingRequestStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class TrainingRequestR2dbcRepositoryAdapter implements TrainingRequestRepository {

    private final TrainingRequestSpringDataRepository repository;

    public TrainingRequestR2dbcRepositoryAdapter(TrainingRequestSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<TrainingRequest> save(TrainingRequest request) {
        return repository.save(toEntity(request)).map(this::toDomain);
    }

    @Override
    public Mono<TrainingRequest> findById(UUID tenantId, UUID requestId) {
        return repository.findByIdAndTenantId(requestId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<TrainingRequest> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<TrainingRequest> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Flux<TrainingRequest> findByOrganizationIdAndStatus(UUID tenantId, UUID organizationId, String status) {
        return repository.findAllByTenantIdAndOrganizationIdAndStatus(tenantId, organizationId, status)
                .map(this::toDomain);
    }

    private TrainingRequestEntity toEntity(TrainingRequest r) {
        return new TrainingRequestEntity(r.id(), r.tenantId(), r.createdAt(), r.updatedAt(),
                r.organizationId(), r.employeeId(), r.trainingId(), r.motivation(), r.status().name(),
                r.decisionReason(), r.enrollmentId(), r.decidedAt());
    }

    private TrainingRequest toDomain(TrainingRequestEntity e) {
        return TrainingRequest.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), e.trainingId(), e.motivation(),
                TrainingRequestStatus.valueOf(e.status()), e.decisionReason(), e.enrollmentId(), e.decidedAt());
    }
}
