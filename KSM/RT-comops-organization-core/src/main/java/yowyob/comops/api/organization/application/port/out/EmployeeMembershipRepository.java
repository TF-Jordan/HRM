package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeMembershipRepository {

    Mono<Boolean> existsByOrganizationAndUser(UUID tenantId, UUID organizationId, UUID userId);

    Mono<EmployeeMembership> findById(UUID tenantId, UUID membershipId);

    Flux<EmployeeMembership> findByUserId(UUID tenantId, UUID userId);

    Flux<EmployeeMembership> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<EmployeeMembership> save(EmployeeMembership membership);
}
