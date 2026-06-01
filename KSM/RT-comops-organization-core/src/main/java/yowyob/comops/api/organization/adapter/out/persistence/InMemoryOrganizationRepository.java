package yowyob.comops.api.organization.adapter.out.persistence;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.domain.model.Organization;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOrganizationRepository implements OrganizationRepository {

    private final Map<UUID, Organization> organizations = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, String code) {
        return Mono.fromSupplier(() -> organizations.values().stream()
                .filter(organization -> organization.tenantId().equals(tenantId))
                .anyMatch(organization -> organization.code().equalsIgnoreCase(code)));
    }

    @Override
    public Mono<Boolean> existsByCodeExcludingId(UUID tenantId, String code, UUID organizationId) {
        return Mono.fromSupplier(() -> organizations.values().stream()
                .filter(organization -> organization.tenantId().equals(tenantId))
                .filter(organization -> !organization.id().equals(organizationId))
                .anyMatch(organization -> organization.code().equalsIgnoreCase(code)));
    }

    @Override
    public Mono<Organization> findById(UUID tenantId, UUID organizationId) {
        return Mono.justOrEmpty(organizations.get(organizationId))
                .filter(organization -> organization.tenantId().equals(tenantId));
    }

    @Override
    public Flux<Organization> findByTenantId(UUID tenantId) {
        return Flux.fromStream(organizations.values().stream()
                .filter(organization -> organization.tenantId().equals(tenantId)));
    }

    @Override
    public Flux<Organization> findByBusinessActorId(UUID tenantId, UUID businessActorId) {
        return Flux.fromStream(organizations.values().stream()
                .filter(organization -> organization.tenantId().equals(tenantId))
                .filter(organization -> organization.businessActorId().equals(businessActorId)));
    }

    @Override
    public Flux<Organization> findByCode(String code) {
        return Flux.fromStream(organizations.values().stream()
                .filter(organization -> organization.code().equalsIgnoreCase(code)));
    }

    @Override
    public Mono<Organization> save(Organization organization) {
        return Mono.fromSupplier(() -> {
            organizations.put(organization.id(), organization);
            return organization;
        });
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID organizationId) {
        return Mono.fromRunnable(() -> organizations.computeIfPresent(organizationId,
                (id, organization) -> organization.tenantId().equals(tenantId) ? null : organization));
    }
}
