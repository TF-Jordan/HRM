package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.auth.domain.model.UserAccount;
import java.time.Instant;
import java.util.UUID;

public record AdministrationUserResponse(
        UUID id,
        UUID tenantId,
        UUID actorId,
        String username,
        String email,
        String phoneNumber,
        String status,
        String plan,
        String onboardingStatus,
        int onboardingStep,
        boolean emailVerified,
        boolean phoneVerified,
        boolean mfaEnabled,
        boolean forcePasswordChange,
        Instant createdAt,
        Instant updatedAt) {

    public static AdministrationUserResponse from(UserAccount user) {
        return new AdministrationUserResponse(
                user.id(),
                user.tenantId(),
                user.actorId(),
                user.username(),
                user.email(),
                user.phoneNumber(),
                user.status(),
                user.plan(),
                user.onboardingStatus(),
                user.onboardingStep(),
                user.emailVerified(),
                user.phoneVerified(),
                user.mfaEnabled(),
                user.forcePasswordChange(),
                user.createdAt(),
                user.updatedAt());
    }
}
