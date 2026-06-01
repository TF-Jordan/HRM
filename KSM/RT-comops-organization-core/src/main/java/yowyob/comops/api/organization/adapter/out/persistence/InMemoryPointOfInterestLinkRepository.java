package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.PointOfInterestLinkRepository;
import yowyob.comops.api.organization.domain.model.PointOfInterestLink;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryPointOfInterestLinkRepository implements PointOfInterestLinkRepository {

    private final Map<UUID, PointOfInterestLink> links = new ConcurrentHashMap<>();

    @Override
    public Mono<PointOfInterestLink> save(PointOfInterestLink link) {
        return Mono.fromSupplier(() -> {
            links.put(link.id(), link);
            return link;
        });
    }

    @Override
    public Mono<Boolean> existsByAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId) {
        return Mono.fromSupplier(() -> links.values().stream()
                .filter(link -> link.tenantId().equals(tenantId))
                .filter(link -> link.agencyId().equals(agencyId))
                .anyMatch(link -> link.pointOfInterestId().equals(pointOfInterestId)));
    }

    @Override
    public Flux<PointOfInterestLink> findByAgencyId(UUID tenantId, UUID agencyId) {
        return Flux.fromStream(links.values().stream()
                .filter(link -> link.tenantId().equals(tenantId))
                .filter(link -> link.agencyId().equals(agencyId)));
    }

    @Override
    public Mono<Void> deleteByAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId) {
        return Mono.fromRunnable(() -> links.entrySet().removeIf(entry -> {
            PointOfInterestLink link = entry.getValue();
            return link.tenantId().equals(tenantId)
                    && link.agencyId().equals(agencyId)
                    && link.pointOfInterestId().equals(pointOfInterestId);
        }));
    }
}
