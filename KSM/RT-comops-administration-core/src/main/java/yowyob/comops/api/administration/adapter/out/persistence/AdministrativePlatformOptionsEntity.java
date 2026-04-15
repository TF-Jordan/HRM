package yowyob.comops.api.administration.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "administration", name = "administrative_platform_options")
public record AdministrativePlatformOptionsEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        boolean requireBusinessActorApproval,
        boolean requireOrganizationApproval,
        boolean allowOrganizationSelfServiceCreation,
        boolean allowAgencySelfServiceCreation,
        boolean allowRoleCloning,
        boolean allowAgencyScopedCustomRoles,
        boolean allowOrganizationAdminsToGovernAgencies,
        boolean allowBusinessActorSelfReactivation) implements PersistableEntity {
}
