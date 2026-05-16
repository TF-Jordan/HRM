package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.EmployeeMembershipRepository;
import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryEmployeeMembershipRepository implements EmployeeMembershipRepository {

    private final Map<UUID, EmployeeMembership> memberships = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByOrganizationAndUser(UUID tenantId, UUID organizationId, UUID userId) {
        return Mono.fromSupplier(() -> memberships.values().stream()
                .filter(membership -> membership.tenantId().equals(tenantId))
                .filter(membership -> membership.organizationId().equals(organizationId))
                .filter(membership -> membership.userId().equals(userId))
                .anyMatch(membership -> !"REMOVED".equals(membership.status())));
    }

    @Override
    public Mono<EmployeeMembership> findById(UUID tenantId, UUID membershipId) {
        return Mono.justOrEmpty(memberships.get(membershipId))
                .filter(membership -> membership.tenantId().equals(tenantId));
    }

    @Override
    public Flux<EmployeeMembership> findByUserId(UUID tenantId, UUID userId) {
        return Flux.fromStream(memberships.values().stream()
                .filter(membership -> membership.tenantId().equals(tenantId))
                .filter(membership -> membership.userId().equals(userId))
                .filter(membership -> !"REMOVED".equals(membership.status())));
    }

    @Override
    public Flux<EmployeeMembership> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(memberships.values().stream()
                .filter(membership -> membership.tenantId().equals(tenantId))
                .filter(membership -> membership.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<EmployeeMembership> save(EmployeeMembership membership) {
        return Mono.fromSupplier(() -> {
            memberships.put(membership.id(), membership);
            return membership;
        });
    }
}
