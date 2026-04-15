package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.EmployeeMembershipRepository;
import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class EmployeeMembershipR2dbcRepositoryAdapter implements EmployeeMembershipRepository {

    private final EmployeeMembershipSpringDataRepository repository;

    public EmployeeMembershipR2dbcRepositoryAdapter(EmployeeMembershipSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByOrganizationAndUser(UUID tenantId, UUID organizationId, UUID userId) {
        return repository.existsByTenantIdAndOrganizationIdAndUserIdAndStatusNot(tenantId, organizationId, userId,
                "REMOVED");
    }

    @Override
    public Mono<EmployeeMembership> findById(UUID tenantId, UUID membershipId) {
        return repository.findByIdAndTenantId(membershipId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<EmployeeMembership> findByUserId(UUID tenantId, UUID userId) {
        return repository.findAllByTenantIdAndUserIdAndStatusNot(tenantId, userId, "REMOVED")
                .map(this::toDomain);
    }

    @Override
    public Flux<EmployeeMembership> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<EmployeeMembership> save(EmployeeMembership membership) {
        return repository.save(toEntity(membership)).map(this::toDomain);
    }

    private EmployeeMembershipEntity toEntity(EmployeeMembership membership) {
        return new EmployeeMembershipEntity(membership.id(), membership.tenantId(), membership.createdAt(),
                membership.updatedAt(), membership.organizationId(), membership.userId(), membership.actorId(),
                membership.email(), membership.agencyId(), membership.roleId(), membership.status());
    }

    private EmployeeMembership toDomain(EmployeeMembershipEntity entity) {
        return EmployeeMembership.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.userId(), entity.actorId(), entity.email(), entity.agencyId(),
                entity.roleId(), entity.status());
    }
}
