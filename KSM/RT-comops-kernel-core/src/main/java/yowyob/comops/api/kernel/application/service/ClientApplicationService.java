package yowyob.comops.api.kernel.application.service;

import yowyob.comops.api.common.domain.model.PlatformServiceCode;
import yowyob.comops.api.kernel.application.port.in.AuthenticateClientApplicationUseCase;
import yowyob.comops.api.kernel.application.port.in.ListClientApplicationsUseCase;
import yowyob.comops.api.kernel.application.port.in.RegisterClientApplicationCommand;
import yowyob.comops.api.kernel.application.port.in.RegisterClientApplicationUseCase;
import yowyob.comops.api.kernel.application.port.in.RevokeClientApplicationUseCase;
import yowyob.comops.api.kernel.application.port.in.RotateClientApplicationSecretCommand;
import yowyob.comops.api.kernel.application.port.in.RotateClientApplicationSecretUseCase;
import yowyob.comops.api.kernel.application.port.in.UpdateClientApplicationCommand;
import yowyob.comops.api.kernel.application.port.in.UpdateClientApplicationUseCase;
import yowyob.comops.api.kernel.application.port.out.ClientApplicationRepository;
import yowyob.comops.api.kernel.config.SecurityRuntimeProperties;
import yowyob.comops.api.kernel.domain.ClientApplicationNotFoundException;
import yowyob.comops.api.kernel.domain.DuplicateClientApplicationIdException;
import yowyob.comops.api.kernel.domain.model.ClientApplication;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientApplicationService implements AuthenticateClientApplicationUseCase, ListClientApplicationsUseCase,
        RegisterClientApplicationUseCase, RotateClientApplicationSecretUseCase, RevokeClientApplicationUseCase,
        UpdateClientApplicationUseCase {

    private static final SecureRandom SECRET_RANDOM = new SecureRandom();

    private final ClientApplicationRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ClientApplicationService(ClientApplicationRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<ClientApplication> authenticate(String clientId, String clientSecret) {
        String normalizedClientId = normalizeClientId(clientId);
        String normalizedSecret = requireSecret(clientSecret);
        return repository.findByClientId(normalizedClientId)
                .filter(ClientApplication::isActive)
                .filter(clientApplication -> passwordEncoder.matches(normalizedSecret, clientApplication.secretHash()))
                .flatMap(clientApplication -> repository.save(clientApplication.markAuthenticated()));
    }

    @Override
    public Flux<ClientApplication> list() {
        return repository.findAll()
                .sort(Comparator.comparing(ClientApplication::clientId));
    }

    @Override
    public Mono<ProvisionedClientApplication> register(RegisterClientApplicationCommand command) {
        String normalizedClientId = normalizeClientId(command.clientId());
        String rawSecret = resolveSecret(command.clientSecret());
        return repository.existsByClientId(normalizedClientId)
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateClientApplicationIdException(normalizedClientId))
                        : repository.save(ClientApplication.register(normalizedClientId, command.name(),
                                command.description(), passwordEncoder.encode(rawSecret),
                                normalizeAllowedServices(command.allowedServices()), command.systemManaged()))
                                .map(clientApplication -> new ProvisionedClientApplication(clientApplication, rawSecret)));
    }

    @Override
    public Mono<ClientApplication> update(UpdateClientApplicationCommand command) {
        return repository.findById(command.clientApplicationId())
                .switchIfEmpty(Mono.error(new ClientApplicationNotFoundException(command.clientApplicationId())))
                .flatMap(existing -> repository.save(existing.updateDefinition(
                        command.name(),
                        command.description(),
                        normalizeAllowedServices(command.allowedServices()),
                        existing.systemManaged())));
    }

    @Override
    public Mono<ProvisionedClientApplication> rotateSecret(RotateClientApplicationSecretCommand command) {
        String rawSecret = resolveSecret(command.clientSecret());
        return repository.findById(command.clientApplicationId())
                .switchIfEmpty(Mono.error(new ClientApplicationNotFoundException(command.clientApplicationId())))
                .flatMap(existing -> repository.save(existing.rotateSecret(passwordEncoder.encode(rawSecret)))
                        .map(clientApplication -> new ProvisionedClientApplication(clientApplication, rawSecret)));
    }

    @Override
    public Mono<ClientApplication> revoke(UUID clientApplicationId) {
        return repository.findById(clientApplicationId)
                .switchIfEmpty(Mono.error(new ClientApplicationNotFoundException(clientApplicationId)))
                .flatMap(existing -> repository.save(existing.revoke()));
    }

    public Mono<ClientApplication> ensureBootstrapClient(SecurityRuntimeProperties.BootstrapClientProperties bootstrap) {
        if (bootstrap == null || !bootstrap.isEnabled()) {
            return Mono.empty();
        }
        String normalizedClientId = normalizeClientId(bootstrap.getClientId());
        String normalizedSecret = requireSecret(bootstrap.getSecret());
        String encodedSecret = passwordEncoder.encode(normalizedSecret);
        return repository.findByClientId(normalizedClientId)
                .flatMap(existing -> {
                    ClientApplication updated = existing.updateDefinition(
                            bootstrap.getName(),
                            bootstrap.getDescription(),
                            normalizeAllowedServices(bootstrap.getAllowedServices()),
                            true);
                    if (!existing.isActive()) {
                        updated = updated.activate();
                    }
                    if (!passwordEncoder.matches(normalizedSecret, existing.secretHash())) {
                        updated = updated.rotateSecret(encodedSecret);
                    }
                    return repository.save(updated);
                })
                .switchIfEmpty(repository.save(ClientApplication.register(normalizedClientId, bootstrap.getName(),
                        bootstrap.getDescription(), encodedSecret,
                        normalizeAllowedServices(bootstrap.getAllowedServices()), true)));
    }

    private String normalizeClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId is required");
        }
        return clientId.trim().toLowerCase(Locale.ROOT);
    }

    private String requireSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("clientSecret is required");
        }
        return secret.trim();
    }

    private String resolveSecret(String secret) {
        if (secret != null && !secret.isBlank()) {
            return secret.trim();
        }
        byte[] randomBytes = new byte[32];
        SECRET_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private Set<String> normalizeAllowedServices(java.util.List<String> requestedServices) {
        if (requestedServices == null || requestedServices.isEmpty()) {
            return new LinkedHashSet<>(PlatformServiceCode.catalog().stream().map(PlatformServiceCode::code).toList());
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        requestedServices.stream()
                .map(PlatformServiceCode::from)
                .map(PlatformServiceCode::code)
                .forEach(normalized::add);
        return Set.copyOf(normalized);
    }
}
