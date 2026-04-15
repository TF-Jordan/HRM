package yowyob.comops.api.bootstrap.integration.organization;

import yowyob.comops.api.auth.application.port.out.UserAccountRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationUserDirectory;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthCoreOrganizationUserDirectory implements OrganizationUserDirectory {

    private final UserAccountRepository userAccountRepository;

    public AuthCoreOrganizationUserDirectory(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Mono<UserRecord> findByEmail(UUID tenantId, String email) {
        return userAccountRepository.findByPrincipal(tenantId, email)
                .map(userAccount -> new UserRecord(userAccount.id(), userAccount.actorId(), userAccount.username(),
                        userAccount.email()));
    }
}
