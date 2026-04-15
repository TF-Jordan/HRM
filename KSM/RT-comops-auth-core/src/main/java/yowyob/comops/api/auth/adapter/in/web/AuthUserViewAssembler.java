package yowyob.comops.api.auth.adapter.in.web;

import yowyob.comops.api.auth.application.port.out.UserOrganizationAccessDirectory;
import yowyob.comops.api.auth.domain.model.UserAccount;
import java.time.Duration;
import java.util.Set;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthUserViewAssembler {

    private final UserOrganizationAccessDirectory userOrganizationAccessDirectory;

    public AuthUserViewAssembler(UserOrganizationAccessDirectory userOrganizationAccessDirectory) {
        this.userOrganizationAccessDirectory = userOrganizationAccessDirectory;
    }

    public Mono<LoginResponse> toLoginResponse(UserAccount userAccount, String accessToken, Duration accessTokenTtl,
            Set<String> authorities) {
        return organizations(userAccount)
                .map(organizations -> LoginResponse.from(userAccount, accessToken, accessTokenTtl, authorities,
                        organizations));
    }

    public Mono<UserAccountResponse> toUserAccountResponse(UserAccount userAccount) {
        return organizations(userAccount)
                .map(organizations -> UserAccountResponse.from(userAccount, organizations));
    }

    private Mono<java.util.List<UserOrganizationAccessResponse>> organizations(UserAccount userAccount) {
        return userOrganizationAccessDirectory.listUserOrganizations(userAccount.tenantId(), userAccount.id())
                .map(UserOrganizationAccessResponse::from)
                .collectList();
    }
}
