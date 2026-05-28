package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.auth.domain.model.UserAccount;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListTenantUsersUseCase {
    Flux<UserAccount> listTenantUsers(UUID tenantId);
}
