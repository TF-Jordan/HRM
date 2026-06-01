package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.JobOfferRepository;
import yowyob.comops.api.hrm.domain.model.JobOffer;
import yowyob.comops.api.hrm.domain.model.JobOfferStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class JobOfferR2dbcRepositoryAdapter implements JobOfferRepository {

    private final JobOfferSpringDataRepository repository;

    public JobOfferR2dbcRepositoryAdapter(JobOfferSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<JobOffer> save(JobOffer jobOffer) {
        return repository.save(toEntity(jobOffer)).map(this::toDomain);
    }

    @Override
    public Mono<JobOffer> findById(UUID tenantId, UUID jobOfferId) {
        return repository.findByIdAndTenantId(jobOfferId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<JobOffer> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private JobOfferEntity toEntity(JobOffer o) {
        return new JobOfferEntity(o.id(), o.tenantId(), o.createdAt(), o.updatedAt(),
                o.organizationId(), o.agencyId(), o.poste(), o.departement(),
                o.localisation(), o.competencesRequises(), o.dateLimite(),
                o.packageSalarial(), o.status().name());
    }

    private JobOffer toDomain(JobOfferEntity e) {
        return JobOffer.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.poste(), e.departement(),
                e.localisation(), e.competencesRequises(), e.dateLimite(),
                e.packageSalarial(), JobOfferStatus.valueOf(e.status()));
    }
}
