package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.TrainingEnrollmentRepository;
import yowyob.comops.api.hrm.domain.model.TrainingEnrollment;
import yowyob.comops.api.hrm.domain.model.TrainingEnrollmentStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class TrainingEnrollmentR2dbcRepositoryAdapter implements TrainingEnrollmentRepository {

    private final TrainingEnrollmentSpringDataRepository repository;

    public TrainingEnrollmentR2dbcRepositoryAdapter(TrainingEnrollmentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<TrainingEnrollment> save(TrainingEnrollment enrollment) {
        return repository.save(toEntity(enrollment)).map(this::toDomain);
    }

    @Override
    public Mono<TrainingEnrollment> findById(UUID tenantId, UUID enrollmentId) {
        return repository.findByIdAndTenantId(enrollmentId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<TrainingEnrollment> findByTrainingId(UUID tenantId, UUID trainingId) {
        return repository.findAllByTenantIdAndTrainingId(tenantId, trainingId).map(this::toDomain);
    }

    @Override
    public Flux<TrainingEnrollment> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    private TrainingEnrollmentEntity toEntity(TrainingEnrollment e) {
        return new TrainingEnrollmentEntity(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.trainingId(), e.employeeId(), e.status().name(), e.noteEvaluation(), e.attestationFileId());
    }

    private TrainingEnrollment toDomain(TrainingEnrollmentEntity e) {
        return TrainingEnrollment.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.trainingId(), e.employeeId(), TrainingEnrollmentStatus.valueOf(e.status()),
                e.noteEvaluation(), e.attestationFileId());
    }
}
