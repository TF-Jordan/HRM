package yowyob.comops.api.auth.adapter.in.web;

import yowyob.comops.api.auth.domain.model.UserAccount;
import java.util.List;
import java.util.UUID;

public record UserAccountResponse(
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
        List<UserOrganizationAccessResponse> organizations) {

    public static UserAccountResponse from(UserAccount userAccount, List<UserOrganizationAccessResponse> organizations) {
        return new UserAccountResponse(
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
                organizations);
    }
}
