package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryPhysicalSpaceRepository implements PhysicalSpaceRepository {

    private final Map<UUID, PhysicalSpace> spaces = new ConcurrentHashMap<>();

    @Override
    public Mono<PhysicalSpace> save(PhysicalSpace physicalSpace) {
        return Mono.fromSupplier(() -> {
            spaces.put(physicalSpace.id(), physicalSpace);
            return physicalSpace;
        });
    }

    @Override
    public Mono<Boolean> existsByAgencyAndCode(UUID tenantId, UUID organizationId, UUID agencyId, String code) {
        return Mono.fromSupplier(() -> spaces.values().stream()
                .filter(space -> space.tenantId().equals(tenantId))
                .filter(space -> space.organizationId().equals(organizationId))
                .filter(space -> space.agencyId().equals(agencyId))
                .anyMatch(space -> space.code().equalsIgnoreCase(code)));
    }

    @Override
    public Flux<PhysicalSpace> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(spaces.values().stream()
                .filter(space -> space.tenantId().equals(tenantId))
                .filter(space -> space.organizationId().equals(organizationId))
                .filter(space -> space.agencyId().equals(agencyId)));
    }

    @Override
    public Flux<PhysicalSpace> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(spaces.values().stream()
                .filter(space -> space.tenantId().equals(tenantId))
                .filter(space -> space.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<PhysicalSpace> findById(UUID tenantId, UUID physicalSpaceId) {
        return Mono.justOrEmpty(spaces.get(physicalSpaceId))
                .filter(space -> space.tenantId().equals(tenantId));
    }
}
