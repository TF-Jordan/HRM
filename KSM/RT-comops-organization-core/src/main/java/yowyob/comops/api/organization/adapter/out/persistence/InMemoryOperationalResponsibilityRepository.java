package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OperationalResponsibilityRepository;
import yowyob.comops.api.organization.domain.model.OperationalResponsibility;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOperationalResponsibilityRepository implements OperationalResponsibilityRepository {

    private final Map<UUID, OperationalResponsibility> store = new ConcurrentHashMap<>();

    @Override
    public Mono<OperationalResponsibility> save(OperationalResponsibility responsibility) {
        return Mono.fromSupplier(() -> {
            store.put(responsibility.id(), responsibility);
            return responsibility;
        });
    }

    @Override
    public Flux<OperationalResponsibility> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(store.values().stream()
                .filter(value -> value.tenantId().equals(tenantId))
                .filter(value -> value.organizationId().equals(organizationId)));
    }

    @Override
    public Flux<OperationalResponsibility> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(store.values().stream()
                .filter(value -> value.tenantId().equals(tenantId))
                .filter(value -> value.organizationId().equals(organizationId))
                .filter(value -> value.agencyId().equals(agencyId)));
    }

    @Override
    public Flux<OperationalResponsibility> findByPhysicalSpaceId(UUID tenantId, UUID physicalSpaceId) {
        return Flux.fromStream(store.values().stream()
                .filter(value -> value.tenantId().equals(tenantId))
                .filter(value -> physicalSpaceId.equals(value.physicalSpaceId())));
    }
}
