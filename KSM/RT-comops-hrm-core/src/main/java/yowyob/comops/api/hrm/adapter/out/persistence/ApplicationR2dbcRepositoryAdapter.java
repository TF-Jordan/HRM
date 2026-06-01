package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.ApplicationRepository;
import yowyob.comops.api.hrm.domain.model.Application;
import yowyob.comops.api.hrm.domain.model.ApplicationStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ApplicationR2dbcRepositoryAdapter implements ApplicationRepository {

    private final ApplicationSpringDataRepository repository;

    public ApplicationR2dbcRepositoryAdapter(ApplicationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Application> save(Application application) {
        return repository.save(toEntity(application)).map(this::toDomain);
    }

    @Override
    public Mono<Application> findById(UUID tenantId, UUID applicationId) {
        return repository.findByIdAndTenantId(applicationId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<Application> findByJobOfferId(UUID tenantId, UUID jobOfferId) {
        return repository.findAllByTenantIdAndJobOfferId(tenantId, jobOfferId).map(this::toDomain);
    }

    private ApplicationEntity toEntity(Application a) {
        return new ApplicationEntity(a.id(), a.tenantId(), a.createdAt(), a.updatedAt(),
                a.jobOfferId(), a.candidatNom(), a.candidatPrenom(), a.candidatEmail(),
                a.candidatTelephone(), a.cvFileId(), a.lettreMotivationFileId(),
                a.status().name());
    }

    private Application toDomain(ApplicationEntity e) {
        return Application.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.jobOfferId(), e.candidatNom(), e.candidatPrenom(), e.candidatEmail(),
                e.candidatTelephone(), e.cvFileId(), e.lettreMotivationFileId(),
                ApplicationStatus.valueOf(e.status()));
    }
}
