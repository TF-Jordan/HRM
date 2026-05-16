package yowyob.comops.api.billing.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommercialDocumentRepository extends ReactiveCrudRepository<CommercialDocumentEntity, UUID> {
    Flux<CommercialDocumentEntity> findByOrganizationIdAndType(UUID organizationId, String type);
    Mono<Long> countByOrganizationIdAndType(UUID organizationId, String type);
}
