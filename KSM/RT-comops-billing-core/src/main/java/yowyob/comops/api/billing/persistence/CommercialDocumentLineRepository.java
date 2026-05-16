package yowyob.comops.api.billing.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommercialDocumentLineRepository extends ReactiveCrudRepository<CommercialDocumentLineEntity, UUID> {
    Flux<CommercialDocumentLineEntity> findByDocumentIdOrderByLineIndexAsc(UUID documentId);
    Mono<Void> deleteByDocumentId(UUID documentId);
}
