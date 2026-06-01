package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryAgencyRepository implements AgencyRepository {

    private final Map<UUID, Agency> agencies = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String code) {
        return Mono.fromSupplier(() -> agencies.values().stream()
                .filter(agency -> agency.tenantId().equals(tenantId))
                .filter(agency -> agency.organizationId().equals(organizationId))
                .anyMatch(agency -> agency.code().equalsIgnoreCase(code)));
    }

    @Override
    public Mono<Agency> findById(UUID tenantId, UUID agencyId) {
        return Mono.justOrEmpty(agencies.get(agencyId))
                .filter(agency -> agency.tenantId().equals(tenantId));
    }

    @Override
    public Flux<Agency> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(agencies.values().stream()
                .filter(agency -> agency.tenantId().equals(tenantId))
                .filter(agency -> agency.organizationId().equals(organizationId)));
    }

    @Override
    public Flux<Agency> findByTenantId(UUID tenantId) {
        return Flux.fromStream(agencies.values().stream()
                .filter(agency -> agency.tenantId().equals(tenantId)));
    }

    @Override
    public Mono<Agency> save(Agency agency) {
        return Mono.fromSupplier(() -> {
            agencies.put(agency.id(), agency);
            return agency;
        });
    }
}
