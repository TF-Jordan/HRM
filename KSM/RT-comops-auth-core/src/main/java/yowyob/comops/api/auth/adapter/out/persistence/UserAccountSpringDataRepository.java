package yowyob.comops.api.auth.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserAccountSpringDataRepository extends ReactiveCrudRepository<UserAccountEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);

    Mono<UserAccountEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<UserAccountEntity> findByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);

    Mono<UserAccountEntity> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email);
}
