package yowyob.comops.api.auth.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "auth_core", name = "user_account")
public record UserAccountEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID actorId,
        String username,
        String email,
        String passwordHash,
        String authProvider,
        String status,
        String plan,
        String onboardingStatus,
        int onboardingStep) implements PersistableEntity {
}
