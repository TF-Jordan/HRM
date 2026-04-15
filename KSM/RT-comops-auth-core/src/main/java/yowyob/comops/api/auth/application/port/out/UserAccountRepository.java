package yowyob.comops.api.auth.application.port.out;

import yowyob.comops.api.auth.domain.model.UserAccount;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface UserAccountRepository {

    Mono<Boolean> existsByUsername(UUID tenantId, String username);

    Mono<UserAccount> findById(UUID tenantId, UUID userId);

    Mono<UserAccount> findByPrincipal(UUID tenantId, String principal);

    Mono<UserAccount> save(UserAccount userAccount);
}
