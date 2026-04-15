package yowyob.comops.api.organization.application.port.out;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationRoleGateway {

    Mono<Void> assignRole(UUID tenantId, UUID userId, UUID roleId, String scopeType, UUID scopeId, String scope);

    Mono<RoleRecord> createRole(UUID tenantId, String code, String name, String scopeType, List<String> permissions);

    Flux<RoleRecord> listRoles(UUID tenantId);

    record RoleRecord(UUID id, String code, String name, String scopeType, List<String> permissions) {
    }
}
