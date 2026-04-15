package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.PointOfInterestRepository;
import yowyob.comops.api.organization.domain.model.PointOfInterest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryPointOfInterestRepository implements PointOfInterestRepository {

    private final Map<UUID, PointOfInterest> pois = new ConcurrentHashMap<>();

    @Override
    public Mono<PointOfInterest> save(PointOfInterest pointOfInterest) {
        return Mono.fromSupplier(() -> {
            pois.put(pointOfInterest.id(), pointOfInterest);
            return pointOfInterest;
        });
    }

    @Override
    public Mono<Boolean> existsByAgencyAndName(UUID tenantId, UUID organizationId, UUID agencyId, String name) {
        return Mono.fromSupplier(() -> pois.values().stream()
                .filter(pointOfInterest -> pointOfInterest.tenantId().equals(tenantId))
                .filter(pointOfInterest -> pointOfInterest.organizationId().equals(organizationId))
                .filter(pointOfInterest -> pointOfInterest.agencyId().equals(agencyId))
                .anyMatch(pointOfInterest -> pointOfInterest.name().equalsIgnoreCase(name)));
    }

    @Override
    public Flux<PointOfInterest> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(pois.values().stream()
                .filter(pointOfInterest -> pointOfInterest.tenantId().equals(tenantId))
                .filter(pointOfInterest -> pointOfInterest.organizationId().equals(organizationId))
                .filter(pointOfInterest -> pointOfInterest.agencyId().equals(agencyId)));
    }

    @Override
    public Flux<PointOfInterest> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(pois.values().stream()
                .filter(pointOfInterest -> pointOfInterest.tenantId().equals(tenantId))
                .filter(pointOfInterest -> pointOfInterest.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<PointOfInterest> findById(UUID tenantId, UUID pointOfInterestId) {
        return Mono.justOrEmpty(pois.get(pointOfInterestId))
                .filter(pointOfInterest -> pointOfInterest.tenantId().equals(tenantId));
    }
}
