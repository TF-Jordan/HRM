package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.MedicalVisitRepository;
import yowyob.comops.api.hrm.domain.model.AptitudeResult;
import yowyob.comops.api.hrm.domain.model.MedicalVisit;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class MedicalVisitR2dbcRepositoryAdapter implements MedicalVisitRepository {

    private final MedicalVisitSpringDataRepository repository;

    public MedicalVisitR2dbcRepositoryAdapter(MedicalVisitSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<MedicalVisit> save(MedicalVisit visit) {
        return repository.save(toEntity(visit)).map(this::toDomain);
    }

    @Override
    public Mono<MedicalVisit> findById(UUID tenantId, UUID visitId) {
        return repository.findByIdAndTenantId(visitId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<MedicalVisit> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    private MedicalVisitEntity toEntity(MedicalVisit v) {
        return new MedicalVisitEntity(v.id(), v.tenantId(), v.createdAt(), v.updatedAt(),
                v.employeeId(), v.dateVisite(), v.medecin(), v.resultatAptitude().name(),
                v.restrictions(), v.prochaineEcheance(), v.certificatFileId());
    }

    private MedicalVisit toDomain(MedicalVisitEntity e) {
        return MedicalVisit.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.employeeId(), e.dateVisite(), e.medecin(), AptitudeResult.valueOf(e.resultatAptitude()),
                e.restrictions(), e.prochaineEcheance(), e.certificatFileId());
    }
}
