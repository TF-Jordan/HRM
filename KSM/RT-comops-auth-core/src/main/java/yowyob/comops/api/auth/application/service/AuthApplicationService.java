package yowyob.comops.api.auth.application.service;

import yowyob.comops.api.auth.application.port.in.GetCurrentUserProfileUseCase;
import yowyob.comops.api.auth.application.port.in.LoginCommand;
import yowyob.comops.api.auth.application.port.in.LoginUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.auth.application.port.in.UpdateCurrentUserOnboardingUseCase;
import yowyob.comops.api.auth.application.port.in.UpdateCurrentUserPlanUseCase;
import yowyob.comops.api.auth.application.port.out.UserAccountRepository;
import yowyob.comops.api.auth.domain.DuplicateUsernameException;
import yowyob.comops.api.auth.domain.InvalidLoginCredentialsException;
import yowyob.comops.api.auth.domain.model.UserAccount;
import yowyob.comops.api.kernel.application.port.in.RecordSystemAuditUseCase;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthApplicationService implements RegisterUserUseCase, LoginUseCase,
        GetCurrentUserProfileUseCase, UpdateCurrentUserPlanUseCase, UpdateCurrentUserOnboardingUseCase {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecordSystemAuditUseCase recordSystemAuditUseCase;

    public AuthApplicationService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder,
            RecordSystemAuditUseCase recordSystemAuditUseCase) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.recordSystemAuditUseCase = recordSystemAuditUseCase;
    }

    @Override
    public Mono<UserAccount> register(RegisterUserCommand command) {
        Objects.requireNonNull(command, "command is required");
        UserAccount userAccount = UserAccount.register(
                command.tenantId(),
                command.actorId(),
                command.username(),
                command.email(),
                passwordEncoder.encode(resolvePassword(command.password())),
                command.authProvider());

        return userAccountRepository.existsByUsername(userAccount.tenantId(), userAccount.username())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateUsernameException(userAccount.username()))
                        : userAccountRepository.save(userAccount)
                                .flatMap(saved -> recordSystemAuditUseCase.record(saved.tenantId(), null, null,
                                                "USER_REGISTERED", "USER_ACCOUNT", saved.id().toString(),
                                                saved.username())
                                        .thenReturn(saved)));
    }

    @Override
    public Mono<UserAccount> login(LoginCommand command) {
        Objects.requireNonNull(command, "command is required");
        return userAccountRepository.findByPrincipal(command.tenantId(), command.principal())
                .filter(userAccount -> passwordEncoder.matches(command.password(), userAccount.passwordHash()))
                .switchIfEmpty(Mono.error(new InvalidLoginCredentialsException()))
                .flatMap(userAccount -> recordSystemAuditUseCase.record(userAccount.tenantId(), null, userAccount.id(),
                                "USER_LOGIN", "USER_ACCOUNT", userAccount.id().toString(), userAccount.username())
                        .thenReturn(userAccount));
    }

    @Override
    public Mono<UserAccount> getCurrentUserProfile() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> userAccountRepository.findById(context.tenantId(), context.userId()));
    }

    @Override
    public Mono<UserAccount> updateCurrentUserPlan(String plan) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> userAccountRepository.findById(context.tenantId(), context.userId())
                        .flatMap(existing -> userAccountRepository.save(existing.updatePlan(plan))
                                .flatMap(saved -> recordSystemAuditUseCase.record(saved.tenantId(),
                                                context.organizationId(), context.userId(), "USER_PLAN_UPDATED",
                                                "USER_ACCOUNT", saved.id().toString(), saved.plan())
                                        .thenReturn(saved))));
    }

    @Override
    public Mono<UserAccount> updateCurrentUserOnboarding(int step, String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> userAccountRepository.findById(context.tenantId(), context.userId())
                        .flatMap(existing -> userAccountRepository.save(existing.updateOnboarding(step, status))
                                .flatMap(saved -> recordSystemAuditUseCase.record(saved.tenantId(),
                                                context.organizationId(), context.userId(),
                                                "USER_ONBOARDING_UPDATED", "USER_ACCOUNT", saved.id().toString(),
                                                saved.onboardingStatus() + "@" + saved.onboardingStep())
                                        .thenReturn(saved))));
    }

    private String resolvePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return "bootstrap-" + UUID.randomUUID();
        }
        return rawPassword.trim();
    }
}
