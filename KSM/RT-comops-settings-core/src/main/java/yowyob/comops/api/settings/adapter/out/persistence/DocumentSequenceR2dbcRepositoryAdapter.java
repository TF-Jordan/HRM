package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.settings.application.port.out.DocumentSequenceRepository;
import yowyob.comops.api.settings.domain.model.DocumentSequence;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class DocumentSequenceR2dbcRepositoryAdapter implements DocumentSequenceRepository {

    private final DocumentSequenceSpringDataRepository repository;

    public DocumentSequenceR2dbcRepositoryAdapter(DocumentSequenceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<DocumentSequence> save(DocumentSequence documentSequence) {
        DocumentSequenceEntity entity = new DocumentSequenceEntity(documentSequence.id(), documentSequence.tenantId(),
                documentSequence.createdAt(), documentSequence.updatedAt(), documentSequence.organizationId(),
                documentSequence.agencyId(), documentSequence.documentType(), documentSequence.prefix(),
                documentSequence.suffix(), documentSequence.paddingWidth(), documentSequence.nextNumber());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<DocumentSequence> findByScopeAndType(UUID tenantId, UUID organizationId, UUID agencyId, String documentType) {
        return repository.findByTenantIdAndOrganizationIdAndAgencyIdAndDocumentType(tenantId, organizationId, agencyId, documentType)
                .map(this::toDomain);
    }

    @Override
    public Flux<DocumentSequence> findAllByScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        if (agencyId == null) {
            return repository.findAllByTenantIdAndOrganizationIdAndAgencyIdIsNull(tenantId, organizationId)
                    .map(this::toDomain);
        }
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    private DocumentSequence toDomain(DocumentSequenceEntity entity) {
        return DocumentSequence.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.documentType(), entity.prefix(), entity.suffix(),
                entity.paddingWidth(), entity.nextNumber());
    }
}
