package yowyob.comops.api.auth.adapter.in.web;

import yowyob.comops.api.auth.domain.model.UserAccount;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        UUID id,
        UUID tenantId,
        UUID actorId,
        String username,
        String email,
        String authProvider,
        String status,
        String plan,
        String onboardingStatus,
        int onboardingStep,
        String accessToken,
        String sessionToken,
        String tokenType,
        long expiresInSeconds,
        Set<String> authorities,
        List<UserOrganizationAccessResponse> organizations) {

    public static LoginResponse from(
            UserAccount userAccount,
            String accessToken,
            Duration accessTokenTtl,
            Set<String> authorities,
            List<UserOrganizationAccessResponse> organizations) {
        long expiresInSeconds = accessTokenTtl == null ? 0L : Math.max(0L, accessTokenTtl.getSeconds());
        return new LoginResponse(
                userAccount.id(),
                userAccount.tenantId(),
                userAccount.actorId(),
                userAccount.username(),
                userAccount.email(),
                userAccount.authProvider(),
                userAccount.status(),
                userAccount.plan(),
                userAccount.onboardingStatus(),
                userAccount.onboardingStep(),
                accessToken,
                accessToken,
                "Bearer",
                expiresInSeconds,
                authorities,
                organizations);
    }
}
