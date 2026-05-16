package yowyob.comops.api.settings.adapter.out.persistence;
import yowyob.comops.api.settings.application.port.out.DocumentSequenceRepository;
import yowyob.comops.api.settings.domain.model.DocumentSequence;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryDocumentSequenceRepository implements DocumentSequenceRepository {

    private final Map<String, DocumentSequence> documentSequences = new ConcurrentHashMap<>();

    @Override
    public Mono<DocumentSequence> save(DocumentSequence documentSequence) {
        return Mono.fromSupplier(() -> {
            documentSequences.put(key(documentSequence.tenantId(), documentSequence.organizationId(),
                    documentSequence.agencyId(), documentSequence.documentType()), documentSequence);
            return documentSequence;
        });
    }

    @Override
    public Mono<DocumentSequence> findByScopeAndType(UUID tenantId, UUID organizationId, UUID agencyId, String documentType) {
        return Mono.justOrEmpty(documentSequences.get(key(tenantId, organizationId, agencyId, documentType)));
    }

    @Override
    public Flux<DocumentSequence> findAllByScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(documentSequences.values().stream()
                .filter(sequence -> sequence.tenantId().equals(tenantId))
                .filter(sequence -> sequence.organizationId().equals(organizationId))
                .filter(sequence -> java.util.Objects.equals(sequence.agencyId(), agencyId))
                .sorted(Comparator.comparing(DocumentSequence::documentType)));
    }

    private static String key(UUID tenantId, UUID organizationId, UUID agencyId, String documentType) {
        return tenantId + ":" + organizationId + ":" + agencyId + ":" + documentType;
    }
}
