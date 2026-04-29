package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.SocialDeclarationRepository;
import yowyob.comops.api.hrm.domain.model.DeclarationStatus;
import yowyob.comops.api.hrm.domain.model.DeclarationType;
import yowyob.comops.api.hrm.domain.model.SocialDeclaration;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class SocialDeclarationR2dbcRepositoryAdapter implements SocialDeclarationRepository {

    private final SocialDeclarationSpringDataRepository repository;

    public SocialDeclarationR2dbcRepositoryAdapter(SocialDeclarationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<SocialDeclaration> save(SocialDeclaration declaration) {
        return repository.save(toEntity(declaration)).map(this::toDomain);
    }

    @Override
    public Mono<SocialDeclaration> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<SocialDeclaration> findByOrganizationId(UUID tenantId, UUID orgId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, orgId).map(this::toDomain);
    }

    private SocialDeclarationEntity toEntity(SocialDeclaration d) {
        return new SocialDeclarationEntity(d.id(), d.tenantId(), d.createdAt(), d.updatedAt(),
                d.organizationId(), d.type().name(), d.periode(), d.format(), d.statut().name(),
                d.fichierId(), d.generatedAt(), d.submittedAt());
    }

    private SocialDeclaration toDomain(SocialDeclarationEntity e) {
        return SocialDeclaration.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), DeclarationType.valueOf(e.type()), e.periode(), e.format(),
                DeclarationStatus.valueOf(e.statut()), e.fichierId(), e.generatedAt(), e.submittedAt());
    }
}
