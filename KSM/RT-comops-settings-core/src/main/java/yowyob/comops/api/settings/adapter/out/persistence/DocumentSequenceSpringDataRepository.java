package yowyob.comops.api.settings.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentSequenceSpringDataRepository extends ReactiveCrudRepository<DocumentSequenceEntity, UUID> {

    Mono<DocumentSequenceEntity> findByTenantIdAndOrganizationIdAndAgencyIdAndDocumentType(UUID tenantId, UUID organizationId, UUID agencyId, String documentType);

    Flux<DocumentSequenceEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId,
            UUID agencyId);

    Flux<DocumentSequenceEntity> findAllByTenantIdAndOrganizationIdAndAgencyIdIsNull(UUID tenantId, UUID organizationId);
}
