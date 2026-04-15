package yowyob.comops.api.auth.adapter.out.persistence;

import yowyob.comops.api.auth.application.port.out.UserAccountRepository;
import yowyob.comops.api.auth.domain.model.UserAccount;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class UserAccountR2dbcRepositoryAdapter implements UserAccountRepository {

    private final UserAccountSpringDataRepository repository;

    public UserAccountR2dbcRepositoryAdapter(UserAccountSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByUsername(java.util.UUID tenantId, String username) {
        return repository.existsByTenantIdAndUsernameIgnoreCase(tenantId, username);
    }

    @Override
    public Mono<UserAccount> findById(java.util.UUID tenantId, java.util.UUID userId) {
        return repository.findByIdAndTenantId(userId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<UserAccount> findByPrincipal(java.util.UUID tenantId, String principal) {
        return repository.findByTenantIdAndUsernameIgnoreCase(tenantId, principal)
                .switchIfEmpty(repository.findByTenantIdAndEmailIgnoreCase(tenantId, principal))
                .map(this::toDomain);
    }

    @Override
    public Mono<UserAccount> save(UserAccount userAccount) {
        return repository.save(toEntity(userAccount)).map(this::toDomain);
    }

    private UserAccountEntity toEntity(UserAccount userAccount) {
        return new UserAccountEntity(userAccount.id(), userAccount.tenantId(), userAccount.createdAt(),
                userAccount.updatedAt(), userAccount.actorId(), userAccount.username(), userAccount.email(),
                userAccount.passwordHash(), userAccount.authProvider(), userAccount.status(), userAccount.plan(),
                userAccount.onboardingStatus(), userAccount.onboardingStep());
    }

    private UserAccount toDomain(UserAccountEntity entity) {
        return UserAccount.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.actorId(), entity.username(), entity.email(), entity.passwordHash(), entity.authProvider(),
                entity.status(), entity.plan(), entity.onboardingStatus(), entity.onboardingStep());
    }
}
