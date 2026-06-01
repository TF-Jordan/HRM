package yowyob.comops.api.bootstrap;

import yowyob.comops.api.administration.application.port.out.AdminAuditRepository;
import yowyob.comops.api.administration.application.port.out.AdministrativePlatformOptionsRepository;
import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import yowyob.comops.api.actor.application.port.in.CreateActorCommand;
import yowyob.comops.api.actor.application.port.in.CreateActorUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.kernel.application.port.in.RelayOutboxEventsUseCase;
import yowyob.comops.api.kernel.application.port.out.DomainEventProjectionRepository;
import yowyob.comops.api.kernel.application.port.out.OutboxEventRepository;
import yowyob.comops.api.kernel.config.UserSessionTokenService;
import yowyob.comops.api.kernel.domain.model.DomainEventProjection;
import yowyob.comops.api.kernel.domain.model.OutboxEvent;
import yowyob.comops.api.kernel.domain.model.OutboxEventStatus;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
@ActiveProfiles("test-memory")
class ApiIntegrationTests {

    private static final String CLIENT_ID = "test-client";
    private static final String API_KEY = "test-key";
    private static final String MANAGEMENT_API_KEY = "test-management-key";
    private static final String TENANT_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private RelayOutboxEventsUseCase relayOutboxEventsUseCase;

    @Autowired
    private DomainEventProjectionRepository domainEventProjectionRepository;

    @Autowired
    private AdministrativePlatformOptionsRepository administrativePlatformOptionsRepository;

    @Autowired
    private AdminAuditRepository adminAuditRepository;

    @Autowired
    private UserSessionTokenService userSessionTokenService;

    @Autowired
    private CreateActorUseCase createActorUseCase;

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private CreateRoleUseCase createRoleUseCase;

    @Autowired
    private AssignRoleToUserUseCase assignRoleToUserUseCase;

    @BeforeEach
    void resetPlatformOptions() {
        administrativePlatformOptionsRepository.save(AdministrativePlatformOptions.defaults(UUID.fromString(TENANT_ID)))
                .block();
    }

    @Test
    void healthEndpointIsPublic() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void jwksEndpointIsPublicWhenJwtModeIsEnabled() {
        webTestClient.get()
                .uri("/.well-known/jwks.json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.keys[0].kty").isEqualTo("RSA")
                .jsonPath("$.keys[0].alg").isEqualTo("RS256")
                .jsonPath("$.keys[0].kid").isEqualTo("iwm-key-1");
    }

    @Test
    void openIdConfigurationEndpointIsPublic() {
        webTestClient.get()
                .uri("/.well-known/openid-configuration")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.jwks_uri").isEqualTo("http://localhost/.well-known/jwks.json")
                .jsonPath("$.token_endpoint").isEqualTo("http://localhost/oauth2/token")
                .jsonPath("$.userinfo_endpoint").isEqualTo("http://localhost/oauth2/userinfo")
                .jsonPath("$.introspection_endpoint").isEqualTo("http://localhost/oauth2/introspect")
                .jsonPath("$.token_endpoint_auth_methods_supported[0]").isEqualTo("client_secret_basic")
                .jsonPath("$.grant_types_supported[0]")
                .isEqualTo("urn:ietf:params:oauth:grant-type:token-exchange");
    }

    @Test
    void authorizationServerMetadataEndpointIsPublic() {
        webTestClient.get()
                .uri("/.well-known/oauth-authorization-server")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token_endpoint").isEqualTo("http://localhost/oauth2/token")
                .jsonPath("$.introspection_endpoint").isEqualTo("http://localhost/oauth2/introspect");
    }

    @Test
    void loginIssuesVerifiableJwtAccessToken() {
        String email = "jwt-login." + UUID.randomUUID() + "@example.com";
        String username = "jwt-" + UUID.randomUUID().toString().substring(0, 8);
        String password = "Password!123";
        UUID tenantId = UUID.fromString(TENANT_ID);
        UUID actorId = Objects.requireNonNull(createActorUseCase.createActor(new CreateActorCommand(
                tenantId,
                "Jwt",
                "Tester",
                null,
                email,
                null,
                null,
                null,
                null,
                null)).block()).id();

        UUID userId = Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                tenantId,
                actorId,
                username,
                email,
                password,
                "LOCAL")).block()).id();

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", username,
                        "password", password))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.tokenType").isEqualTo("Bearer")
                .jsonPath("$.data.expiresInSeconds").isEqualTo(900)
                .jsonPath("$.data.sharedSession.tokenType").isEqualTo("Bearer")
                .jsonPath("$.data.sharedSession.token").exists()
                .jsonPath("$.data.sessionToken").value(value -> {
                    String token = value.toString();
                    org.assertj.core.api.Assertions.assertThat(token.split("\\.")).hasSize(3);
                    yowyob.comops.api.kernel.config.UserSessionTokenClaims claims =
                            userSessionTokenService.verify(token).orElseThrow();
                    org.assertj.core.api.Assertions.assertThat(claims.tenantId())
                            .isEqualTo(UUID.fromString(TENANT_ID));
                    org.assertj.core.api.Assertions.assertThat(claims.userId())
                            .isEqualTo(userId);
                    org.assertj.core.api.Assertions.assertThat(claims.actorId())
                            .isEqualTo(actorId);
                })
                .jsonPath("$.data.accessToken").exists();
    }

    @Test
    void sharedSsoSessionCanDriveUserInfoAndTokenExchange() {
        TestUser user = bootstrapUser("sso-central", Set.of("organizations:write"));
        String organizationId = createOwnedOrganization(user, "ORG-SSO-" + UUID.randomUUID().toString().substring(0, 6));
        AtomicReference<String> sharedSessionToken = new AtomicReference<>();
        AtomicReference<String> contextId = new AtomicReference<>();

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", user.username(),
                        "password", user.password()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.sharedSession.token").value(value -> sharedSessionToken.set(value.toString()));

        webTestClient.get()
                .uri("/oauth2/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + sharedSessionToken.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contexts.length()").isEqualTo(1)
                .jsonPath("$.contexts[0].tenantId").isEqualTo(TENANT_ID)
                .jsonPath("$.contexts[0].organizations[0].organizationId")
                .value(value -> org.assertj.core.api.Assertions.assertThat(String.valueOf(value))
                        .isEqualTo(organizationId))
                .jsonPath("$.contexts[0].contextId").value(value -> contextId.set(value.toString()));

        webTestClient.post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                        .with("client_id", CLIENT_ID)
                        .with("client_secret", API_KEY)
                        .with("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
                        .with("subject_token", sharedSessionToken.get())
                        .with("context_id", contextId.get())
                        .with("organization_id", organizationId)
                        .with("service_code", "ORGANIZATION"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token_type").isEqualTo("Bearer")
                .jsonPath("$.issued_token_type").isEqualTo("urn:ietf:params:oauth:token-type:access_token")
                .jsonPath("$.access_token").value(value -> {
                    String token = value.toString();
                    yowyob.comops.api.kernel.config.UserSessionTokenClaims claims =
                            userSessionTokenService.verify(token).orElseThrow();
                    org.assertj.core.api.Assertions.assertThat(claims.tenantId())
                            .isEqualTo(UUID.fromString(TENANT_ID));
                    org.assertj.core.api.Assertions.assertThat(claims.organizationId())
                            .isEqualTo(UUID.fromString(organizationId));
                    org.assertj.core.api.Assertions.assertThat(claims.userId())
                            .isEqualTo(UUID.fromString(user.userId()));
                    org.assertj.core.api.Assertions.assertThat(claims.actorId())
                            .isEqualTo(UUID.fromString(user.actorId()));
                })
                .jsonPath("$.scope").value(value -> org.assertj.core.api.Assertions
                        .assertThat(value.toString())
                        .contains("organizations:write"));
    }

    @Test
    void sharedSsoSessionSupportsBasicClientAuthIntrospectionAndAccessTokenUserInfo() {
        TestUser user = bootstrapUser("sso-basic", Set.of("organizations:write"));
        String organizationId = createOwnedOrganization(user, "ORG-BASIC-" + UUID.randomUUID().toString().substring(0, 6));
        AtomicReference<String> sharedSessionToken = new AtomicReference<>();
        AtomicReference<String> contextId = new AtomicReference<>();
        AtomicReference<String> accessToken = new AtomicReference<>();

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", user.username(),
                        "password", user.password()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.sharedSession.token").value(value -> sharedSessionToken.set(value.toString()));

        webTestClient.get()
                .uri("/oauth2/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + sharedSessionToken.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.contexts[0].contextId").value(value -> contextId.set(value.toString()));

        webTestClient.post()
                .uri("/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, basicAuthorization(CLIENT_ID, API_KEY))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                        .with("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
                        .with("subject_token", sharedSessionToken.get())
                        .with("context_id", contextId.get())
                        .with("organization_id", organizationId)
                        .with("service_code", "ORGANIZATION"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.issued_token_type").isEqualTo("urn:ietf:params:oauth:token-type:access_token")
                .jsonPath("$.access_token").value(value -> accessToken.set(value.toString()));

        webTestClient.post()
                .uri("/oauth2/introspect")
                .header(HttpHeaders.AUTHORIZATION, basicAuthorization(CLIENT_ID, API_KEY))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("token", accessToken.get())
                        .with("token_type_hint", "access_token"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.active").isEqualTo(true)
                .jsonPath("$.client_id").isEqualTo(CLIENT_ID)
                .jsonPath("$.sub").isEqualTo(user.userId())
                .jsonPath("$.tid").isEqualTo(TENANT_ID)
                .jsonPath("$.oid").isEqualTo(organizationId)
                .jsonPath("$.svc").isEqualTo("ORGANIZATION")
                .jsonPath("$.permissions[0]").isEqualTo("organizations:write");

        webTestClient.get()
                .uri("/oauth2/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sub").isEqualTo(user.userId())
                .jsonPath("$.tenantId").isEqualTo(TENANT_ID)
                .jsonPath("$.organizationId").isEqualTo(organizationId)
                .jsonPath("$.clientId").isEqualTo(CLIENT_ID)
                .jsonPath("$.serviceCode").isEqualTo("ORGANIZATION")
                .jsonPath("$.permissions[0]").isEqualTo("organizations:write")
                .jsonPath("$.sso").isEqualTo(true);
    }

    @Test
    void loginAndUserProfileExposeOrganizationServices() {
        TestUser user = bootstrapUser("service-login", Set.of("organizations:write"));
        String organizationId = createOwnedOrganization(user, "ORG-SVC-" + UUID.randomUUID().toString().substring(0, 6));

        userClient(user).delete()
                .uri("/api/organizations/{organizationId}/services/{serviceCode}", organizationId, "SALES")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.effectiveServices").value(value -> org.assertj.core.api.Assertions
                        .assertThat((java.util.List<String>) (java.util.List<?>) value)
                        .doesNotContain("SALES"));

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", user.username(),
                        "password", user.password()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.organizations.length()").isEqualTo(1)
                .jsonPath("$.data.organizations[0].organizationId").isEqualTo(organizationId)
                .jsonPath("$.data.organizations[0].services").value(value -> org.assertj.core.api.Assertions
                        .assertThat((java.util.List<String>) (java.util.List<?>) value)
                        .contains("COMMERCIAL")
                        .doesNotContain("SALES"));

        userClient(user).get()
                .uri("/api/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.organizations[0].organizationId").isEqualTo(organizationId)
                .jsonPath("$.data.organizations[0].services").value(value -> org.assertj.core.api.Assertions
                        .assertThat((java.util.List<String>) (java.util.List<?>) value)
                        .contains("PRODUCT")
                        .doesNotContain("SALES"));
    }

    @Test
    void discoverContextsAndSelectContextIssueOrganizationScopedJwt() {
        TestUser user = bootstrapUser("context-login", Set.of("organizations:write"));
        String organizationId = createOwnedOrganization(user,
                "ORG-CTX-" + UUID.randomUUID().toString().substring(0, 6));
        AtomicReference<String> selectionToken = new AtomicReference<>();
        AtomicReference<String> contextId = new AtomicReference<>();

        webTestClient.post()
                .uri("/api/auth/discover-contexts")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of(
                        "principal", user.username(),
                        "password", user.password()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.selectionToken").value(value -> selectionToken.set(value.toString()))
                .jsonPath("$.data.expiresInSeconds").isEqualTo(300)
                .jsonPath("$.data.contexts.length()").isEqualTo(1)
                .jsonPath("$.data.contexts[0].tenantId").isEqualTo(TENANT_ID)
                .jsonPath("$.data.contexts[0].organizations[0].organizationId").isEqualTo(organizationId)
                .jsonPath("$.data.contexts[0].contextId").value(value -> contextId.set(value.toString()));

        webTestClient.post()
                .uri("/api/auth/select-context")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of(
                        "selectionToken", selectionToken.get(),
                        "contextId", contextId.get(),
                        "organizationId", organizationId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.selectedTenantId").isEqualTo(TENANT_ID)
                .jsonPath("$.data.selectedOrganizationId").isEqualTo(organizationId)
                .jsonPath("$.data.session.accessToken").value(value -> {
                    String token = value.toString();
                    yowyob.comops.api.kernel.config.UserSessionTokenClaims claims =
                            userSessionTokenService.verify(token).orElseThrow();
                    org.assertj.core.api.Assertions.assertThat(claims.tenantId())
                            .isEqualTo(UUID.fromString(TENANT_ID));
                    org.assertj.core.api.Assertions.assertThat(claims.organizationId())
                            .isEqualTo(UUID.fromString(organizationId));
                    org.assertj.core.api.Assertions.assertThat(claims.userId())
                            .isEqualTo(UUID.fromString(user.userId()));
                })
                .jsonPath("$.data.session.organizations[0].organizationId").isEqualTo(organizationId);
    }

    @Test
    void identifyReturnsPasswordStepForExistingAccount() {
        TestUser user = bootstrapUser("identify-existing", Set.of());

        webTestClient.post()
                .uri("/api/auth/identify")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of("principal", user.username()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.principal").isEqualTo(user.username())
                .jsonPath("$.data.accountExists").isEqualTo(true)
                .jsonPath("$.data.nextStep").isEqualTo("SIGN_IN_PASSWORD")
                .jsonPath("$.data.matchingAccountCount").isEqualTo(1);
    }

    @Test
    void identifyReturnsSignUpForUnknownAccount() {
        String principal = "unknown." + UUID.randomUUID() + "@example.com";

        webTestClient.post()
                .uri("/api/auth/identify")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of("principal", principal))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.principal").isEqualTo(principal)
                .jsonPath("$.data.accountExists").isEqualTo(false)
                .jsonPath("$.data.nextStep").isEqualTo("SIGN_UP")
                .jsonPath("$.data.matchingAccountCount").isEqualTo(0);
    }

    @Test
    void publicSignUpCreatesAccountAndIssuesImmediateJwt() {
        String email = "signup." + UUID.randomUUID() + "@example.com";
        AtomicReference<String> tokenRef = new AtomicReference<>();
        AtomicReference<String> userIdRef = new AtomicReference<>();
        AtomicReference<String> actorIdRef = new AtomicReference<>();

        webTestClient.post()
                .uri("/api/auth/sign-up")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of(
                        "tenantId", TENANT_ID,
                        "firstName", "Google",
                        "lastName", "Style",
                        "email", email,
                        "password", "Password!123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> userIdRef.set(value.toString()))
                .jsonPath("$.data.actorId").value(value -> actorIdRef.set(value.toString()))
                .jsonPath("$.data.email").isEqualTo(email)
                .jsonPath("$.data.username").isEqualTo(email)
                .jsonPath("$.data.tokenType").isEqualTo("Bearer")
                .jsonPath("$.data.organizations.length()").isEqualTo(0)
                .jsonPath("$.data.accessToken").value(value -> {
                    String token = value.toString();
                    tokenRef.set(token);
                    yowyob.comops.api.kernel.config.UserSessionTokenClaims claims =
                            userSessionTokenService.verify(token).orElseThrow();
                    org.assertj.core.api.Assertions.assertThat(claims.tenantId())
                            .isEqualTo(UUID.fromString(TENANT_ID));
                    org.assertj.core.api.Assertions.assertThat(claims.userId())
                            .isEqualTo(UUID.fromString(userIdRef.get()));
                    org.assertj.core.api.Assertions.assertThat(claims.actorId())
                            .isEqualTo(UUID.fromString(actorIdRef.get()));
                    org.assertj.core.api.Assertions.assertThat(claims.organizationId()).isNull();
                });

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", email,
                        "password", "Password!123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(userIdRef.get())
                .jsonPath("$.data.actorId").isEqualTo(actorIdRef.get())
                .jsonPath("$.data.accessToken").exists();
    }

    @Test
    void forgotPasswordCanIssueResetTokenAndApplyNewPassword() {
        TestUser user = bootstrapUser("forgot-password", Set.of());
        AtomicReference<String> selectionToken = new AtomicReference<>();
        AtomicReference<String> contextId = new AtomicReference<>();
        AtomicReference<String> resetToken = new AtomicReference<>();

        webTestClient.post()
                .uri("/api/auth/forgot-password")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of("principal", user.username()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.matchingAccountCount").isEqualTo(1)
                .jsonPath("$.data.selectionToken").value(value -> selectionToken.set(value.toString()))
                .jsonPath("$.data.contexts[0].contextId").value(value -> contextId.set(value.toString()));

        webTestClient.post()
                .uri("/api/auth/password-reset/issue")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of(
                        "selectionToken", selectionToken.get(),
                        "contextId", contextId.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.deliveryMode").isEqualTo("PREVIEW_ONLY")
                .jsonPath("$.data.challengeTokenPreview").value(value -> resetToken.set(value.toString()));

        webTestClient.post()
                .uri("/api/auth/reset-password")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of(
                        "resetToken", resetToken.get(),
                        "newPassword", "NewPassword!123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(user.userId());

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", user.username(),
                        "password", "NewPassword!123"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void emailVerificationCanBeRequestedAndConfirmed() {
        String email = "verify." + UUID.randomUUID() + "@example.com";
        AtomicReference<String> verificationToken = new AtomicReference<>();
        AtomicReference<String> accessToken = new AtomicReference<>();

        webTestClient.post()
                .uri("/api/auth/sign-up")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of(
                        "tenantId", TENANT_ID,
                        "firstName", "Email",
                        "lastName", "Verification",
                        "email", email,
                        "password", "Password!123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.emailVerified").isEqualTo(false)
                .jsonPath("$.data.accessToken").value(value -> accessToken.set(value.toString()));

        webTestClient.post()
                .uri("/api/auth/email-verification/request")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.deliveryMode").isEqualTo("PREVIEW_ONLY")
                .jsonPath("$.data.challengeTokenPreview").value(value -> verificationToken.set(value.toString()));

        webTestClient.post()
                .uri("/api/auth/email-verification/confirm")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of("verificationToken", verificationToken.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.emailVerified").isEqualTo(true)
                .jsonPath("$.data.emailVerifiedAt").exists();
    }

    @Test
    void publicSignUpCanUseDiscoveredOrganizationContextWithoutTenantId() {
        TestUser owner = bootstrapUser("signup-discovery-owner", Set.of("organizations:write"));
        String organizationCode = "ORG-SIGN-" + UUID.randomUUID().toString().substring(0, 6);
        createOwnedOrganization(owner, organizationCode);
        AtomicReference<String> selectionToken = new AtomicReference<>();
        AtomicReference<String> contextId = new AtomicReference<>();
        String email = "signup.discovery." + UUID.randomUUID() + "@example.com";

        webTestClient.post()
                .uri("/api/auth/discover-sign-up-contexts")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of("organizationCode", organizationCode))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.contexts.length()").isEqualTo(1)
                .jsonPath("$.data.selectionToken").value(value -> selectionToken.set(value.toString()))
                .jsonPath("$.data.contexts[0].contextId").value(value -> contextId.set(value.toString()))
                .jsonPath("$.data.contexts[0].organizationCode").isEqualTo(organizationCode.toUpperCase(java.util.Locale.ROOT))
                .jsonPath("$.data.contexts[0].tenantId").isEqualTo(TENANT_ID);

        webTestClient.post()
                .uri("/api/auth/sign-up")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .bodyValue(Map.of(
                        "signUpSelectionToken", selectionToken.get(),
                        "contextId", contextId.get(),
                        "firstName", "Sign",
                        "lastName", "Up",
                        "email", email,
                        "password", "Password!123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.tenantId").isEqualTo(TENANT_ID)
                .jsonPath("$.data.email").isEqualTo(email)
                .jsonPath("$.data.username").isEqualTo(email);
    }

    @Test
    void authCoreCoversCaptchaOtpPhoneMfaAndTypedOnboarding() {
        AtomicReference<String> captchaToken = new AtomicReference<>();
        AtomicReference<String> captchaAnswer = new AtomicReference<>();
        AtomicReference<String> captchaVerificationToken = new AtomicReference<>();
        AtomicReference<String> accessToken = new AtomicReference<>();
        AtomicReference<String> phoneChallengeToken = new AtomicReference<>();
        AtomicReference<String> phoneCode = new AtomicReference<>();
        AtomicReference<String> mfaChallengeToken = new AtomicReference<>();
        AtomicReference<String> mfaCode = new AtomicReference<>();
        AtomicReference<String> loginMfaToken = new AtomicReference<>();
        AtomicReference<String> loginMfaCode = new AtomicReference<>();
        AtomicReference<String> genericOtpToken = new AtomicReference<>();
        AtomicReference<String> genericOtpCode = new AtomicReference<>();
        String email = "full-auth." + UUID.randomUUID() + "@example.com";
        String phone = "+2376" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        systemClient().post()
                .uri("/api/auth/captcha")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.captchaToken").value(value -> captchaToken.set(value.toString()))
                .jsonPath("$.data.answerPreview").value(value -> captchaAnswer.set(value.toString()));

        systemClient().post()
                .uri("/api/auth/captcha/verify")
                .bodyValue(Map.of(
                        "captchaToken", captchaToken.get(),
                        "answer", captchaAnswer.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.captchaVerificationToken")
                .value(value -> captchaVerificationToken.set(value.toString()));

        systemClient().post()
                .uri("/api/auth/sign-up")
                .bodyValue(Map.ofEntries(
                        Map.entry("firstName", "Full"),
                        Map.entry("lastName", "Auth"),
                        Map.entry("tenantId", TENANT_ID),
                        Map.entry("username", "full-auth-" + UUID.randomUUID().toString().substring(0, 8)),
                        Map.entry("email", email),
                        Map.entry("phoneNumber", phone),
                        Map.entry("password", "StrongPass!123"),
                        Map.entry("captchaVerificationToken", captchaVerificationToken.get()),
                        Map.entry("accountType", "BUSINESS"),
                        Map.entry("businessType", "FREELANCE"),
                        Map.entry("onboardingData", Map.of(
                                "businessName", "Full Auth Freelance",
                                "deliveryAddress", "Douala",
                                "niu", "NIU-" + UUID.randomUUID().toString().substring(0, 8)))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.phoneNumber").isEqualTo(phone)
                .jsonPath("$.data.accountType").isEqualTo("BUSINESS")
                .jsonPath("$.data.businessType").isEqualTo("FREELANCE")
                .jsonPath("$.data.onboardingStatus").isEqualTo("IN_PROGRESS")
                .jsonPath("$.data.accessToken").value(value -> accessToken.set(value.toString()));

        WebTestClient signedInClient = webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", API_KEY)
                .defaultHeader("X-Tenant-Id", TENANT_ID)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.get())
                .build();

        signedInClient.post()
                .uri("/api/auth/phone-verification/request")
                .bodyValue(Map.of(
                        "channel", "SMS",
                        "recipient", phone))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.challengeToken").value(value -> phoneChallengeToken.set(value.toString()))
                .jsonPath("$.data.codePreview").value(value -> phoneCode.set(value.toString()));

        signedInClient.post()
                .uri("/api/auth/phone-verification/confirm")
                .bodyValue(Map.of(
                        "challengeToken", phoneChallengeToken.get(),
                        "code", phoneCode.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.phoneVerified").isEqualTo(true);

        systemClient().post()
                .uri("/api/auth/otp")
                .bodyValue(Map.of(
                        "channel", "WHATSAPP",
                        "recipient", phone,
                        "purpose", "SIGN_UP"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.challengeToken").value(value -> genericOtpToken.set(value.toString()))
                .jsonPath("$.data.codePreview").value(value -> genericOtpCode.set(value.toString()));

        systemClient().post()
                .uri("/api/auth/otp/verify")
                .bodyValue(Map.of(
                        "challengeToken", genericOtpToken.get(),
                        "code", genericOtpCode.get(),
                        "purpose", "SIGN_UP"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.verified").isEqualTo(true)
                .jsonPath("$.data.channel").isEqualTo("WHATSAPP");

        signedInClient.post()
                .uri("/api/auth/mfa/enable")
                .bodyValue(Map.of("channel", "SMS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.challengeToken").value(value -> mfaChallengeToken.set(value.toString()))
                .jsonPath("$.data.codePreview").value(value -> mfaCode.set(value.toString()));

        signedInClient.post()
                .uri("/api/auth/mfa/confirm")
                .bodyValue(Map.of(
                        "challengeToken", mfaChallengeToken.get(),
                        "code", mfaCode.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.mfaEnabled").isEqualTo(true)
                .jsonPath("$.data.mfaChannel").isEqualTo("SMS");

        systemClient().post()
                .uri("/api/auth/login")
                .bodyValue(Map.of(
                        "principal", phone,
                        "password", "StrongPass!123"))
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.data.nextStep").isEqualTo("CONFIRM_MFA")
                .jsonPath("$.data.mfaToken").value(value -> loginMfaToken.set(value.toString()))
                .jsonPath("$.data.codePreview").value(value -> loginMfaCode.set(value.toString()));

        systemClient().post()
                .uri("/api/auth/login/mfa/confirm")
                .bodyValue(Map.of(
                        "mfaToken", loginMfaToken.get(),
                        "code", loginMfaCode.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.accessToken").exists()
                .jsonPath("$.data.mfaEnabled").isEqualTo(true);

        signedInClient.put()
                .uri("/api/users/me/identity-onboarding")
                .bodyValue(Map.of(
                        "accountType", "BUSINESS",
                        "businessType", "ORGANIZATION",
                        "step", 4,
                        "status", "COMPLETED",
                        "data", Map.of(
                                "businessName", "Full Organization",
                                "businessOwnerName", "Full Auth",
                                "niu", "NIU-FULL",
                                "logo", "logo.png",
                                "headerPhoto", "header.png")))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.accountType").isEqualTo("BUSINESS")
                .jsonPath("$.data.businessType").isEqualTo("ORGANIZATION")
                .jsonPath("$.data.onboardingStep").isEqualTo(4)
                .jsonPath("$.data.onboardingStatus").isEqualTo("COMPLETED")
                .jsonPath("$.data.onboardingPayload").value(value -> org.assertj.core.api.Assertions
                        .assertThat(value.toString()).contains("Full Organization"));
    }

    @Test
    void unsubscribedBusinessServiceIsForbiddenUntilResubscribed() {
        TestUser user = bootstrapUser("sales-subscription", Set.of("organizations:write", "sales:write"));
        String organizationId = createOwnedOrganization(user, "ORG-ENT-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = userClient(user).mutate()
                .defaultHeader("X-Organization-Id", organizationId)
                .build();

        scopedClient.delete()
                .uri("/api/organizations/{organizationId}/services/{serviceCode}", organizationId, "SALES")
                .exchange()
                .expectStatus().isOk();

        scopedClient.get()
                .uri("/api/sales/orders/{orderId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("ORGANIZATION_SERVICE_NOT_SUBSCRIBED");

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/services", organizationId)
                .bodyValue(Map.of("serviceCode", "SALES"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.effectiveServices").value(value -> org.assertj.core.api.Assertions
                        .assertThat((java.util.List<String>) (java.util.List<?>) value)
                        .contains("SALES"));

        scopedClient.get()
                .uri("/api/sales/orders/{orderId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void organizationServiceQuotaCanBeViewedAndUpdated() {
        TestUser user = bootstrapUser("service-quota-admin", Set.of("organizations:write"));
        String organizationId = createOwnedOrganization(user, "ORG-QTY-" + UUID.randomUUID().toString().substring(0, 6));

        userClient(user).get()
                .uri("/api/organizations/{organizationId}/services", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.serviceQuotas[?(@.serviceCode=='SALES')].requestQuotaLimit").isEqualTo(10000)
                .jsonPath("$.data.serviceQuotas[?(@.serviceCode=='SALES')].requestQuotaWindowSeconds").isEqualTo(60);

        userClient(user).patch()
                .uri("/api/organizations/{organizationId}/services/{serviceCode}/quota", organizationId, "SALES")
                .bodyValue(Map.of(
                        "requestQuotaLimit", 25,
                        "requestQuotaWindowSeconds", 120))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.serviceQuotas[?(@.serviceCode=='SALES')].requestQuotaLimit").isEqualTo(25)
                .jsonPath("$.data.serviceQuotas[?(@.serviceCode=='SALES')].requestQuotaWindowSeconds").isEqualTo(120);
    }

    @Test
    void organizationGovernanceLifecycleCascadesToAgencies() {
        TestUser user = bootstrapUser("organization-governance", Set.of(
                "organizations:write",
                "administration:govern:organizations",
                "administration:govern:agencies",
                "administration:audit:read"));
        String organizationId = createOwnedOrganization(user, "ORG-GOV-" + UUID.randomUUID().toString().substring(0, 6));
        String agencyId = createAgency(user, organizationId, "AGY-GOV-" + UUID.randomUUID().toString().substring(0, 6));

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/suspend", organizationId)
                .bodyValue(Map.of("reason", "compliance-hold"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("SUSPENDED")
                .jsonPath("$.data.isActive").isEqualTo(false)
                .jsonPath("$.data.governanceReason").isEqualTo("compliance-hold");

        userClient(user).get()
                .uri("/api/organizations/{organizationId}/agencies", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[?(@.id=='" + agencyId + "')].governanceStatus").isEqualTo("SUSPENDED")
                .jsonPath("$.data[?(@.id=='" + agencyId + "')].active").isEqualTo(false);

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/close", organizationId)
                .bodyValue(Map.of("reason", "shutdown"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("CLOSED")
                .jsonPath("$.data.isActive").isEqualTo(false)
                .jsonPath("$.data.governanceReason").isEqualTo("shutdown");

        userClient(user).get()
                .uri("/api/organizations/{organizationId}/agencies", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[?(@.id=='" + agencyId + "')].governanceStatus").isEqualTo("CLOSED")
                .jsonPath("$.data[?(@.id=='" + agencyId + "')].active").isEqualTo(false);

        java.util.List<String> actions = adminAuditRepository.findByTenantId(UUID.fromString(TENANT_ID), 20)
                .map(entry -> entry.action())
                .collectList()
                .blockOptional()
                .orElse(List.of());
        org.assertj.core.api.Assertions.assertThat(actions)
                .contains("ORGANIZATION_SUSPENDED", "ORGANIZATION_CLOSED", "AGENCY_SUSPENDED", "AGENCY_CLOSED");
    }

    @Test
    void organizationGovernanceSupportsRejectApproveAndReopen() {
        TestUser user = bootstrapUser("organization-governance-advanced", Set.of(
                "organizations:write",
                "administration:govern:organizations",
                "administration:audit:read"));
        String organizationId = createOwnedOrganization(user, "ORG-GOV-ADV-" + UUID.randomUUID().toString().substring(0, 6));

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/reject", organizationId)
                .bodyValue(Map.of("reason", "missing-docs"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("REJECTED")
                .jsonPath("$.data.isActive").isEqualTo(false)
                .jsonPath("$.data.governanceReason").isEqualTo("missing-docs");

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/approve", organizationId)
                .bodyValue(Map.of("reason", "validated"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED")
                .jsonPath("$.data.isActive").isEqualTo(true)
                .jsonPath("$.data.governanceReason").isEqualTo("validated");

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/close", organizationId)
                .bodyValue(Map.of("reason", "merge-test"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("CLOSED");

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/reopen", organizationId)
                .bodyValue(Map.of("reason", "restored"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED")
                .jsonPath("$.data.isActive").isEqualTo(true)
                .jsonPath("$.data.governanceReason").isEqualTo("restored");
    }

    @Test
    void agencyGovernanceLifecycleCanBeManagedDirectly() {
        TestUser user = bootstrapUser("agency-governance", Set.of(
                "organizations:write",
                "administration:govern:agencies",
                "administration:audit:read"));
        String organizationId = createOwnedOrganization(user, "ORG-AGOV-" + UUID.randomUUID().toString().substring(0, 6));
        String agencyId = createAgency(user, organizationId, "AGY-AGOV-" + UUID.randomUUID().toString().substring(0, 6));

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/suspend", organizationId, agencyId)
                .bodyValue(Map.of("reason", "maintenance"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(agencyId)
                .jsonPath("$.data.governanceStatus").isEqualTo("SUSPENDED")
                .jsonPath("$.data.active").isEqualTo(false)
                .jsonPath("$.data.governanceReason").isEqualTo("maintenance");

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/activate", organizationId, agencyId)
                .bodyValue(Map.of("reason", "back-online"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(agencyId)
                .jsonPath("$.data.governanceStatus").isEqualTo("ACTIVE")
                .jsonPath("$.data.active").isEqualTo(true)
                .jsonPath("$.data.governanceReason").isEqualTo("back-online");

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/close", organizationId, agencyId)
                .bodyValue(Map.of("reason", "retired"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(agencyId)
                .jsonPath("$.data.governanceStatus").isEqualTo("CLOSED")
                .jsonPath("$.data.active").isEqualTo(false)
                .jsonPath("$.data.governanceReason").isEqualTo("retired");
    }

    @Test
    void organizationWritersCannotUseGovernanceEndpointsWithoutGovernPermission() {
        TestUser user = bootstrapUser("org-writer-no-govern", Set.of("organizations:write"));
        String organizationId = createOwnedOrganization(user, "ORG-WRITE-" + UUID.randomUUID().toString().substring(0, 6));

        userClient(user).post()
                .uri("/api/organizations/{organizationId}/suspend", organizationId)
                .bodyValue(Map.of("reason", "should-fail"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void clientApplicationAllowedServicesRestrictMappedModules() {
        TestUser adminUser = bootstrapUser("client-service-scope",
                Set.of("iam:admin", "organizations:write", "sales:write"));
        String organizationId = createOwnedOrganization(adminUser,
                "ORG-SCOPE-" + UUID.randomUUID().toString().substring(0, 6));

        AtomicReference<String> scopedSecret = new AtomicReference<>();
        userClient(adminUser).post()
                .uri("/api/client-applications")
                .bodyValue(Map.of(
                        "clientId", "sales-only-backend",
                        "name", "Sales Only Backend",
                        "description", "Dedicated backend limited to sales endpoints.",
                        "allowedServices", List.of("SALES")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.clientApplication.allowedServices").value(value -> org.assertj.core.api.Assertions
                        .assertThat((java.util.List<String>) (java.util.List<?>) value)
                        .containsExactly("SALES"))
                .jsonPath("$.data.clientSecret").value(value -> scopedSecret.set(value.toString()));

        WebTestClient salesOnlyClient = userClient(adminUser, "sales-only-backend", scopedSecret.get()).mutate()
                .defaultHeader("X-Organization-Id", organizationId)
                .build();

        salesOnlyClient.get()
                .uri("/api/organizations/my")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("CLIENT_APPLICATION_SERVICE_NOT_ALLOWED");

        salesOnlyClient.get()
                .uri("/api/sales/orders/{orderId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void clientApplicationsCanBeCreatedRotatedAndRevokedByIamAdmin() {
        TestUser adminUser = bootstrapUser("client-admin", Set.of("iam:admin"));
        AtomicReference<String> actorId = new AtomicReference<>();
        String username = "client-login-" + UUID.randomUUID().toString().substring(0, 8);
        String password = "Password!789";

        systemClient().post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Client",
                        "lastName", "Login",
                        "email", "client.login." + UUID.randomUUID() + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> actorId.set(value.toString()));

        registerUserUseCase.register(new RegisterUserCommand(
                UUID.fromString(TENANT_ID),
                UUID.fromString(actorId.get()),
                username,
                "client.login." + UUID.randomUUID() + "@example.com",
                password,
                "LOCAL")).block();

        AtomicReference<String> clientApplicationId = new AtomicReference<>();
        AtomicReference<String> firstSecret = new AtomicReference<>();

        userClient(adminUser).post()
                .uri("/api/client-applications")
                .bodyValue(Map.of(
                        "clientId", "erp-backend",
                        "name", "ERP Backend",
                        "description", "ERP integration backend"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.clientApplication.id").value(value -> clientApplicationId.set(value.toString()))
                .jsonPath("$.data.clientApplication.clientId").isEqualTo("erp-backend")
                .jsonPath("$.data.clientApplication.allowedServices").value(value -> org.assertj.core.api.Assertions
                        .assertThat((java.util.List<String>) (java.util.List<?>) value)
                        .contains("ORGANIZATION", "SALES", "RESOURCE"))
                .jsonPath("$.data.clientSecret").value(value -> firstSecret.set(value.toString()));

        userClient(adminUser).get()
                .uri("/api/client-applications")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[?(@.clientId=='erp-backend')]").exists();

        userClient(adminUser).patch()
                .uri("/api/client-applications/{clientApplicationId}", clientApplicationId.get())
                .bodyValue(Map.of(
                        "name", "ERP Backend",
                        "description", "ERP integration backend",
                        "allowedServices", List.of("COMMERCIAL", "SALES")))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.allowedServices").value(value -> org.assertj.core.api.Assertions
                        .assertThat((java.util.List<String>) (java.util.List<?>) value)
                        .containsExactly("COMMERCIAL", "SALES"));

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", "erp-backend")
                .header("X-Api-Key", firstSecret.get())
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", username,
                        "password", password))
                .exchange()
                .expectStatus().isOk();

        AtomicReference<String> rotatedSecret = new AtomicReference<>();
        userClient(adminUser).post()
                .uri("/api/client-applications/{clientApplicationId}/rotate-secret", clientApplicationId.get())
                .bodyValue(Map.of())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.clientSecret").value(value -> rotatedSecret.set(value.toString()));

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", "erp-backend")
                .header("X-Api-Key", firstSecret.get())
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", username,
                        "password", password))
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", "erp-backend")
                .header("X-Api-Key", rotatedSecret.get())
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", username,
                        "password", password))
                .exchange()
                .expectStatus().isOk();

        userClient(adminUser).post()
                .uri("/api/client-applications/{clientApplicationId}/revoke", clientApplicationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("REVOKED");

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", "erp-backend")
                .header("X-Api-Key", rotatedSecret.get())
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", username,
                        "password", password))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void actorEndpointRequiresApiKey() {
        webTestClient.post()
                .uri("/api/actors")
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "firstName", "Ada",
                        "lastName", "Lovelace",
                        "email", "ada@example.com"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void actorCanBeCreatedWhenSecurityAndTenantHeadersArePresent() {
        systemClient().post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Ada",
                        "lastName", "Lovelace",
                        "email", "ada.lovelace@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.email").isEqualTo("ada.lovelace@example.com");
    }

    @Test
    void externalIdentityHeadersCannotOverrideAuthenticatedUserContext() {
        TestUser privilegedUser = bootstrapUser("spoof-admin", Set.of("organizations:write"));
        TestUser limitedUser = bootstrapUser("spoof-limited", Set.of());

        userClient(limitedUser).mutate()
                .defaultHeader("X-User-Id", privilegedUser.userId())
                .build()
                .post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", UUID.randomUUID().toString(),
                        "code", "SPOOF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        "legalName", "Spoof Attempt",
                        "displayName", "Spoof Attempt",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void authBusinessActorEmployeesAndWarehousesSupportLegacyParityFlow() {
        String loginEmail = "parity.login." + UUID.randomUUID() + "@example.com";
        String loginPassword = "Password!123";
        AtomicReference<String> managerActorId = new AtomicReference<>();
        AtomicReference<String> managerUserId = new AtomicReference<>();
        AtomicReference<String> businessActorId = new AtomicReference<>();
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> roleId = new AtomicReference<>();
        AtomicReference<String> invitedActorId = new AtomicReference<>();
        AtomicReference<String> invitedUserId = new AtomicReference<>();
        AtomicReference<String> invitedEmail = new AtomicReference<>();

        systemClient().post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Parity",
                        "lastName", "Manager",
                        "email", loginEmail))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> managerActorId.set(value.toString()));

        UUID managerUserUuid = Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                UUID.fromString(TENANT_ID),
                UUID.fromString(managerActorId.get()),
                "parity-manager",
                loginEmail,
                loginPassword,
                "LOCAL")).block()).id();
        managerUserId.set(managerUserUuid.toString());

        UUID roleUuid = Objects.requireNonNull(createRoleUseCase.createRole(new CreateRoleCommand(
                UUID.fromString(TENANT_ID),
                "PARITY-ORG-ADMIN",
                "Parity Org Admin",
                Set.of("organizations:write"))).block()).id();
        roleId.set(roleUuid.toString());
        assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
                UUID.fromString(TENANT_ID),
                managerUserUuid,
                roleUuid,
                "GLOBAL")).block();

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", API_KEY)
                .header("X-Tenant-Id", TENANT_ID)
                .bodyValue(Map.of(
                        "principal", "parity-manager",
                        "password", loginPassword))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(managerUserId.get());

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).post()
                .uri("/api/actors/onboarding")
                .bodyValue(Map.of(
                        "name", "Parity Business Actor",
                        "businessId", "BA-PARITY-01",
                        "niu", "NIU-001"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> businessActorId.set(value.toString()));

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).get()
                .uri("/api/actors/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(businessActorId.get());

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", businessActorId.get(),
                        "code", "ORG-PARITY-01",
                        "legalName", "Parity Legal",
                        "displayName", "Parity Display",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).mutate()
                .defaultHeader("X-Organization-Id", organizationId.get())
                .build()
                .post()
                .uri("/api/warehouses")
                .bodyValue(Map.of(
                        "code", "WH-PARITY-01",
                        "name", "Parity Warehouse",
                        "agencyType", "WAREHOUSE"))
                .exchange()
                .expectStatus().isCreated();

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).mutate()
                .defaultHeader("X-Organization-Id", organizationId.get())
                .build()
                .get()
                .uri("/api/warehouses")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        systemClient().post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Parity",
                        "lastName", "Employee",
                        "email", "parity.employee." + UUID.randomUUID() + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> invitedActorId.set(value.toString()))
                .jsonPath("$.data.email").value(value -> invitedEmail.set(value.toString()));

        invitedUserId.set(Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                UUID.fromString(TENANT_ID),
                UUID.fromString(invitedActorId.get()),
                "parity-employee-" + UUID.randomUUID().toString().substring(0, 8),
                invitedEmail.get(),
                "Password!456",
                "LOCAL")).block()).id().toString());

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).post()
                .uri(uriBuilder -> uriBuilder.path("/api/employees/invite")
                        .queryParam("organizationId", organizationId.get())
                        .build())
                .bodyValue(Map.of(
                        "email", invitedEmail.get(),
                        "roleId", roleId.get()))
                .exchange()
                .expectStatus().isCreated();

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).get()
                .uri(uriBuilder -> uriBuilder.path("/api/employees")
                        .queryParam("organizationId", organizationId.get())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        userClient(new TestUser(managerUserId.get(), managerActorId.get(), Set.of("organizations:write"))).get()
                .uri("/api/employees/roles")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);
    }

    @Test
    void businessEndpointRequiresUserPermissionBeyondApiKey() {
        systemClient().post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", UUID.randomUUID().toString(),
                        "code", "ORG-NO-PERM",
                        "legalName", "Legal ORG-NO-PERM",
                        "displayName", "Display ORG-NO-PERM",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isForbidden();

        TestUser limitedUser = bootstrapUser("limited", Set.of("products:write"));
        userClient(limitedUser).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", UUID.randomUUID().toString(),
                        "code", "ORG-LIMITED",
                        "legalName", "Legal ORG-LIMITED",
                        "displayName", "Display ORG-LIMITED",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void organizationModuleSupportsAgenciesOpeningHoursAndPointsOfInterest() {
        TestUser orgUser = bootstrapUser("org-module", Set.of("organizations:write"));
        String organizationId = createOrganization(orgUser, "ORG-MODULE-01");
        String agencyId = createAgency(orgUser, organizationId, "AGY-MODULE-01");

        userClient(orgUser).post()
                .uri("/api/organizations/opening-hours")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", agencyId,
                        "dayOfWeek", "MONDAY",
                        "opensAt", "08:00:00",
                        "closesAt", "17:00:00",
                        "closed", false))
                .exchange()
                .expectStatus().isOk();

        userClient(orgUser).post()
                .uri("/api/organizations/points-of-interest")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", agencyId,
                        "name", "Accueil principal",
                        "poiType", "FRONT_DESK",
                        "latitude", 48.8566,
                        "longitude", 2.3522))
                .exchange()
                .expectStatus().isCreated();

        userClient(orgUser).get()
                .uri("/api/organizations/{organizationId}/agencies", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        userClient(orgUser).get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/organizations/opening-hours/{organizationId}/agencies/{agencyId}")
                        .build(organizationId, agencyId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        userClient(orgUser).get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/organizations/points-of-interest/{organizationId}/agencies/{agencyId}")
                        .build(organizationId, agencyId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);
    }

    @Test
    void outboxEventsAreProjectedAcrossInventorySalesAccountingAndTreasury() {
        TestUser user = bootstrapUser("evt-flow", Set.of(
                "organizations:write",
                "products:write",
                "inventory:write",
                "third-parties:write",
                "sales:write",
                "accounting:write",
                "settings:write",
                "treasury:manage",
                "system:observe"));
        String organizationId = createOrganization(user, "ORG-EVT-01");
        String sourceAgencyId = createWarehouse(user, organizationId, "WH-EVT-01");
        String targetAgencyId = createWarehouse(user, organizationId, "WH-EVT-02");
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> customerActorId = new AtomicReference<>();
        AtomicReference<String> customerThirdPartyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> transferId = new AtomicReference<>();
        AtomicReference<String> orderId = new AtomicReference<>();
        AtomicReference<String> bankAccountId = new AtomicReference<>();
        AtomicReference<String> statementId = new AtomicReference<>();
        AtomicReference<String> reconciliationId = new AtomicReference<>();

        configureSequence(user, organizationId, "SALES_ORDER", "ORD-EVT-", 4);
        configureSequence(user, organizationId, "SALES_INVOICE", "INV-EVT-", 4);
        configureSequence(user, organizationId, "STOCK_MOVEMENT", "MOV-EVT-", 4);
        configureSequence(user, organizationId, "WAREHOUSE_TRANSFER", "WHT-EVT-", 4);
        configureSequence(user, organizationId, "BANK_STATEMENT", "STM-EVT-", 4);
        configureSequence(user, organizationId, "RECONCILIATION", "REC-EVT-", 4);


        scopedClient.post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Customer",
                        "lastName", "Event",
                        "email", "customer.evt-01@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> customerActorId.set(value.toString()));

        scopedClient.post()
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ACTOR",
                        "partyId", customerActorId.get(),
                        "referenceCode", "TP-EVT-01",
                        "displayName", "Event Customer",
                        "roles", List.of("CLIENT"),
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> customerThirdPartyId.set(value.toString()));

        scopedClient.post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sku", "SKU-EVT-01",
                        "name", "Event Product",
                        "familyCode", "FAMILY-EVT",
                        "variantLabel", "STANDARD",
                        "unitPrice", 14.25,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        scopedClient.post()
                .uri("/api/inventory/movements")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", sourceAgencyId,
                        "thirdPartyId", customerThirdPartyId.get(),
                        "productId", productId.get(),
                        "movementType", "INBOUND",
                        "quantity", 10.0))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/inventory/transfers")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sourceAgencyId", sourceAgencyId,
                        "targetAgencyId", targetAgencyId,
                        "productId", productId.get(),
                        "quantity", 3.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> transferId.set(value.toString()));

        scopedClient.post()
                .uri("/api/inventory/transfers/{transferId}/complete", transferId.get())
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/sales/orders")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", sourceAgencyId,
                        "customerThirdPartyId", customerThirdPartyId.get(),
                        "productId", productId.get(),
                        "quantity", 2.0,
                        "unitPrice", 14.25,
                        "currency", "eur"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> orderId.set(value.toString()));

        scopedClient.post()
                .uri("/api/sales/orders/{orderId}/confirm", orderId.get())
                .exchange()
                .expectStatus().isOk();

        scopedClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/inventory/movements/balance")
                        .queryParam("organizationId", organizationId)
                        .queryParam("agencyId", sourceAgencyId)
                        .queryParam("productId", productId.get())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.onHandQuantity").isEqualTo(5);

        org.assertj.core.api.Assertions.assertThat(outboxEvents(TENANT_ID))
                .filteredOn(event -> "STOCK_MOVEMENT_RECORDED".equals(event.eventType()))
                .anySatisfy(event -> {
                    org.assertj.core.api.Assertions.assertThat(event.payload().get("sourceDocumentType"))
                            .isEqualTo("SALES_ORDER");
                    org.assertj.core.api.Assertions.assertThat(event.payload().get("sourceDocumentNumber"))
                            .isEqualTo("ORD-EVT-0001");
                });

        scopedClient.post()
                .uri("/api/accounting/invoices/from-orders/{orderId}", orderId.get())
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/treasury/bank-accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankName", "Event Bank",
                        "accountNumber", "ACC-EVT-01",
                        "iban", "FR7630006000011234567890188",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        scopedClient.post()
                .uri("/api/treasury/statements")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "statementDate", "2026-03-08",
                        "openingBalance", 100.0,
                        "closingBalance", 150.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> statementId.set(value.toString()));

        scopedClient.post()
                .uri("/api/treasury/bank-accounts/reconciliations")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "statementId", statementId.get()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> reconciliationId.set(value.toString()));

        scopedClient.post()
                .uri("/api/treasury/bank-accounts/reconciliations/{reconciliationId}/close", reconciliationId.get())
                .exchange()
                .expectStatus().isOk();

        List<String> eventTypes = outboxEvents(TENANT_ID).stream()
                .map(OutboxEvent::eventType)
                .toList();

        org.assertj.core.api.Assertions.assertThat(eventTypes).contains(
                "STOCK_MOVEMENT_RECORDED",
                "SALES_ORDER_STOCK_DISPATCHED",
                "WAREHOUSE_TRANSFER_CREATED",
                "WAREHOUSE_TRANSFER_COMPLETED",
                "SALES_ORDER_CREATED",
                "SALES_ORDER_CONFIRMED",
                "INVOICE_CREATED",
                "BANK_STATEMENT_REGISTERED",
                "RECONCILIATION_OPENED",
                "RECONCILIATION_CLOSED");

        Integer relayedCount = relayOutboxUntilIdle(1000, 5);

        org.assertj.core.api.Assertions.assertThat(relayedCount).isGreaterThanOrEqualTo(9);
        org.assertj.core.api.Assertions.assertThat(outboxEvents(TENANT_ID))
                .allMatch(event -> event.status() == OutboxEventStatus.PUBLISHED)
                .allMatch(event -> event.publishedAt() != null);
        int publishedEventCount = outboxEvents(TENANT_ID).size();
        org.assertj.core.api.Assertions.assertThat(domainEventProjections(TENANT_ID))
                .extracting(DomainEventProjection::domainType)
                .contains("INVENTORY", "SALES", "ACCOUNTING", "TREASURY");

        userClient(user).get()
                .uri(uriBuilder -> uriBuilder.path("/api/observability/outbox/summary")
                        .queryParam("tenantId", TENANT_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.publishedCount").isEqualTo(publishedEventCount)
                .jsonPath("$.data.pendingCount").isEqualTo(0)
                .jsonPath("$.data.deadLetterCount").isEqualTo(0);

        userClient(user).get()
                .uri(uriBuilder -> uriBuilder.path("/api/observability/projections/summary")
                        .queryParam("tenantId", TENANT_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.totalCount").isEqualTo(domainEventProjections(TENANT_ID).size())
                .jsonPath("$.data.perDomainCount.INVENTORY").isNotEmpty()
                .jsonPath("$.data.perDomainCount.SALES").isNotEmpty();

        userClient(user).get()
                .uri("/api/observability/runtime")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.persistenceMode").isEqualTo("memory")
                .jsonPath("$.data.outboxDeliveryType").isEqualTo("recording")
                .jsonPath("$.data.outboxRelayEnabled").isEqualTo(false)
                .jsonPath("$.data.bootstrapClientEnabled").isEqualTo(true);

        managementClient().get()
                .uri("/actuator/health/operations")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isNotEmpty();

        managementClient().get()
                .uri("/actuator/metrics/iwm.outbox.pending")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("iwm.outbox.pending");
    }

    @Test
    void salesOrderConfirmationFailsWhenInventoryIsInsufficient() {
        TestUser user = bootstrapUser("sales-stock", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "sales:write"));
        String organizationId = createOrganization(user, "ORG-SALES-STOCK-01");
        String agencyId = createAgency(user, organizationId, "AGY-SALES-STOCK-01");
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> customerActorId = new AtomicReference<>();
        AtomicReference<String> customerThirdPartyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> orderId = new AtomicReference<>();

        scopedClient.post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Customer",
                        "lastName", "NoStock",
                        "email", "customer.no-stock@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> customerActorId.set(value.toString()));

        scopedClient.post()
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ACTOR",
                        "partyId", customerActorId.get(),
                        "referenceCode", "TP-STK-01",
                        "displayName", "No Stock Customer",
                        "roles", List.of("CLIENT"),
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> customerThirdPartyId.set(value.toString()));

        scopedClient.post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sku", "SKU-STK-01",
                        "name", "No Stock Product",
                        "familyCode", "FAMILY-STK",
                        "variantLabel", "STANDARD",
                        "unitPrice", 20.0,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        scopedClient.post()
                .uri("/api/sales/orders")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", agencyId,
                        "customerThirdPartyId", customerThirdPartyId.get(),
                        "orderNumber", "ORD-STK-0001",
                        "productId", productId.get(),
                        "quantity", 1.0,
                        "unitPrice", 20.0,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> orderId.set(value.toString()));

        scopedClient.post()
                .uri("/api/sales/orders/{orderId}/confirm", orderId.get())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("SALES_ORDER_INSUFFICIENT_STOCK");
    }

    @Test
    void treasurySupportsReadClearAndConsistencyChecks() {
        TestUser treasuryUser = bootstrapUser("treasury-module", Set.of(
                "organizations:write",
                "treasury:manage"));
        String organizationId = createOrganization(treasuryUser, "ORG-TRS-01");
        String secondOrganizationId = createOrganization(treasuryUser, "ORG-TRS-02");
        WebTestClient firstOrganizationClient = organizationUserClient(treasuryUser, organizationId);
        WebTestClient secondOrganizationClient = organizationUserClient(treasuryUser, secondOrganizationId);
        AtomicReference<String> bankAccountId = new AtomicReference<>();
        AtomicReference<String> secondBankAccountId = new AtomicReference<>();
        AtomicReference<String> statementId = new AtomicReference<>();
        AtomicReference<String> checkId = new AtomicReference<>();
        AtomicReference<String> reconciliationId = new AtomicReference<>();

        firstOrganizationClient.post()
                .uri("/api/treasury/bank-accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankName", "Treasury Bank",
                        "accountNumber", "ACC-TRS-01",
                        "iban", "FR7630006000011234567890189",
                        "currency", "eur"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        secondOrganizationClient.post()
                .uri("/api/treasury/bank-accounts")
                .bodyValue(Map.of(
                        "organizationId", secondOrganizationId,
                        "bankName", "Treasury Bank 2",
                        "accountNumber", "ACC-TRS-02",
                        "iban", "FR7630006000011234567890190",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> secondBankAccountId.set(value.toString()));

        firstOrganizationClient.post()
                .uri("/api/treasury/statements")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "statementNumber", "STM-TRS-01",
                        "statementDate", "2026-03-08",
                        "openingBalance", 100.0,
                        "closingBalance", 120.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> statementId.set(value.toString()));

        firstOrganizationClient.post()
                .uri("/api/treasury/bank-accounts/checks")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "checkNumber", "CHK-TRS-01",
                        "amount", 55.0,
                        "beneficiary", "Treasury Supplier"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> checkId.set(value.toString()));

        firstOrganizationClient.post()
                .uri("/api/treasury/bank-accounts/checks/{checkId}/clear", checkId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("CLEARED");

        firstOrganizationClient.post()
                .uri("/api/treasury/bank-accounts/reconciliations")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "statementId", statementId.get(),
                        "referenceNumber", "REC-TRS-01"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> reconciliationId.set(value.toString()));

        firstOrganizationClient.post()
                .uri("/api/treasury/bank-accounts/reconciliations/{reconciliationId}/close", reconciliationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("CLOSED");

        firstOrganizationClient.get()
                .uri("/api/treasury/bank-accounts/{bankAccountId}", bankAccountId.get())
                .exchange()
                .expectStatus().isOk();

        firstOrganizationClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/treasury/statements")
                        .queryParam("organizationId", organizationId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        firstOrganizationClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/treasury/bank-accounts/checks")
                        .queryParam("organizationId", organizationId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        firstOrganizationClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/treasury/bank-accounts/reconciliations")
                        .queryParam("organizationId", organizationId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        firstOrganizationClient.post()
                .uri("/api/treasury/bank-accounts/reconciliations")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", secondBankAccountId.get(),
                        "statementId", statementId.get(),
                        "referenceNumber", "REC-TRS-ERR"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void invoiceSettlementCycleUpdatesAccountingAndTreasury() {
        TestUser user = bootstrapUser("invoice-settlement", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "inventory:write",
                "sales:write",
                "accounting:write",
                "settings:write",
                "treasury:manage"));
        String organizationId = createOrganization(user, "ORG-SETTLE-01");
        String agencyId = createAgency(user, organizationId, "AGY-SETTLE-01");
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> customerActorId = new AtomicReference<>();
        AtomicReference<String> customerThirdPartyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> orderId = new AtomicReference<>();
        AtomicReference<String> invoiceId = new AtomicReference<>();
        AtomicReference<String> bankAccountId = new AtomicReference<>();
        String customerSuffix = UUID.randomUUID().toString().substring(0, 8);
        String productSuffix = UUID.randomUUID().toString().substring(0, 8);

        configureSequence(user, organizationId, "SALES_ORDER", "ORD-SET-", 4);
        configureSequence(user, organizationId, "SALES_INVOICE", "INV-SET-", 4);
        configureSequence(user, organizationId, "STOCK_MOVEMENT", "MOV-SET-", 4);
        configureSequence(user, organizationId, "INVOICE_SETTLEMENT", "SET-INV-", 4);

        scopedClient.post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Customer",
                        "lastName", "Settlement",
                        "email", "customer.settlement." + customerSuffix + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> customerActorId.set(value.toString()));

        scopedClient.post()
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ACTOR",
                        "partyId", customerActorId.get(),
                        "referenceCode", "TP-SET-" + customerSuffix,
                        "displayName", "Settlement Customer",
                        "roles", List.of("CLIENT"),
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> customerThirdPartyId.set(value.toString()));

        scopedClient.post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sku", "SKU-SET-" + productSuffix,
                        "name", "Settlement Product",
                        "familyCode", "FAMILY-SET",
                        "variantLabel", "STANDARD",
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        scopedClient.post()
                .uri("/api/inventory/movements")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", agencyId,
                        "productId", productId.get(),
                        "movementType", "INBOUND",
                        "quantity", 5.0))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/sales/orders")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", agencyId,
                        "customerThirdPartyId", customerThirdPartyId.get(),
                        "productId", productId.get(),
                        "quantity", 2.0,
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> orderId.set(value.toString()));

        scopedClient.post()
                .uri("/api/sales/orders/{orderId}/confirm", orderId.get())
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/accounting/invoices/from-orders/{orderId}", orderId.get())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> invoiceId.set(value.toString()))
                .jsonPath("$.data.paymentStatus").isEqualTo("UNPAID")
                .jsonPath("$.data.outstandingAmount").isEqualTo(25);

        scopedClient.post()
                .uri("/api/accounting/invoices/{invoiceId}/post", invoiceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("POSTED");

        scopedClient.post()
                .uri("/api/treasury/bank-accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankName", "Settlement Bank",
                        "accountNumber", "ACC-SET-01",
                        "iban", "FR7630006000011234567890191",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        scopedClient.post()
                .uri("/api/treasury/bank-accounts/invoice-settlements")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "invoiceId", invoiceId.get(),
                        "paymentMethod", "BANK_TRANSFER",
                        "amount", 25.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.settlementNumber").isEqualTo("SET-INV-0001");

        scopedClient.get()
                .uri("/api/accounting/invoices/{invoiceId}", invoiceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.paymentStatus").isEqualTo("PAID")
                .jsonPath("$.data.settledAmount").isEqualTo(25)
                .jsonPath("$.data.outstandingAmount").isEqualTo(0);

        List<String> eventTypes = outboxEvents(TENANT_ID).stream()
                .map(OutboxEvent::eventType)
                .toList();

        org.assertj.core.api.Assertions.assertThat(eventTypes).contains(
                "INVOICE_POSTED",
                "INVOICE_SETTLEMENT_REGISTERED",
                "INVOICE_SETTLEMENT_APPLIED");
    }

    @Test
    void billingDocumentCanBePaidThroughCashierAndSettledInAccounting() {
        TestUser user = bootstrapUser("billing-cashier", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "accounting:write",
                "cashier:write",
                "treasury:manage"));
        String organizationId = createOrganization(user, "ORG-BIL-CASH-" + UUID.randomUUID().toString().substring(0, 6));
        String agencyId = createAgency(user, organizationId, "AGY-BIL-CASH-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        enableOrganizationServices(scopedClient, organizationId, List.of(
                "COMMERCIAL", "PRODUCT", "BILLING", "ACCOUNTING", "CASHIER"));

        CustomerFixture customer = createCustomerFixture(scopedClient, organizationId,
                UUID.randomUUID().toString().substring(0, 8));
        String productId = createProductFixture(scopedClient, organizationId,
                UUID.randomUUID().toString().substring(0, 8));
        CashierSessionFixture cashierSession = openCashierSessionFixture(scopedClient, agencyId,
                UUID.randomUUID().toString().substring(0, 8));
        AtomicReference<String> documentId = new AtomicReference<>();
        AtomicReference<String> accountingInvoiceId = new AtomicReference<>();
        AtomicReference<String> cashierBillId = new AtomicReference<>();

        scopedClient.post()
                .uri("/api/factures-proforma")
                .bodyValue(Map.of(
                        "counterpartyThirdPartyId", customer.thirdPartyId(),
                        "currency", "EUR",
                        "lines", List.of(Map.of(
                                "productId", productId,
                                "quantity", 2.0,
                                "unitPrice", 12.50))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(value -> documentId.set(value.toString()))
                .jsonPath("$.totalAmount").isEqualTo(25);

        scopedClient.post()
                .uri("/api/factures-proforma/{documentId}/sync/accounting-invoice", documentId.get())
                .bodyValue(Map.of(
                        "invoiceNumber", "BIL-CASH-" + UUID.randomUUID().toString().substring(0, 8),
                        "postInvoice", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.linkedAccountingInvoiceId").value(value -> accountingInvoiceId.set(value.toString()))
                .jsonPath("$.linkedCashierBillId").doesNotExist();

        scopedClient.post()
                .uri("/api/factures-proforma/{documentId}/sync/cashier-bill", documentId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.linkedAccountingInvoiceId").isEqualTo(accountingInvoiceId.get())
                .jsonPath("$.linkedCashierBillId").value(value -> cashierBillId.set(value.toString()));

        scopedClient.post()
                .uri("/api/factures-proforma/{documentId}/payments/cashier", documentId.get())
                .bodyValue(Map.of(
                        "amount", 25.0,
                        "sessionId", cashierSession.sessionId(),
                        "registerId", cashierSession.registerId(),
                        "reference", "PAY-CASH-" + UUID.randomUUID().toString().substring(0, 6),
                        "syncAccountingSettlement", true))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.invoiceId").isEqualTo(accountingInvoiceId.get())
                .jsonPath("$.billingDocumentId").isEqualTo(documentId.get())
                .jsonPath("$.linkedServiceCode").isEqualTo("CASHIER")
                .jsonPath("$.linkedDocumentType").isEqualTo("BILL")
                .jsonPath("$.linkedDocumentId").isEqualTo(cashierBillId.get())
                .jsonPath("$.status").isEqualTo("SETTLED")
                .jsonPath("$.amount").isEqualTo(25);

        scopedClient.get()
                .uri("/api/accounting/invoices/{invoiceId}", accountingInvoiceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("POSTED")
                .jsonPath("$.data.paymentStatus").isEqualTo("PAID")
                .jsonPath("$.data.settledAmount").isEqualTo(25)
                .jsonPath("$.data.outstandingAmount").isEqualTo(0);
    }

    @Test
    void billingDocumentCanBePaidThroughBankSettlementAndSettledInAccounting() {
        TestUser user = bootstrapUser("billing-bank", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "accounting:write",
                "treasury:manage"));
        String organizationId = createOrganization(user, "ORG-BIL-BNK-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        enableOrganizationServices(scopedClient, organizationId, List.of(
                "COMMERCIAL", "PRODUCT", "BILLING", "ACCOUNTING", "TREASURY"));

        CustomerFixture customer = createCustomerFixture(scopedClient, organizationId,
                UUID.randomUUID().toString().substring(0, 8));
        String productId = createProductFixture(scopedClient, organizationId,
                UUID.randomUUID().toString().substring(0, 8));
        AtomicReference<String> documentId = new AtomicReference<>();
        AtomicReference<String> accountingInvoiceId = new AtomicReference<>();
        AtomicReference<String> bankAccountId = new AtomicReference<>();

        scopedClient.post()
                .uri("/api/factures-proforma")
                .bodyValue(Map.of(
                        "counterpartyThirdPartyId", customer.thirdPartyId(),
                        "currency", "EUR",
                        "lines", List.of(Map.of(
                                "productId", productId,
                                "quantity", 4.0,
                                "unitPrice", 10.00))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(value -> documentId.set(value.toString()))
                .jsonPath("$.totalAmount").isEqualTo(40);

        scopedClient.post()
                .uri("/api/factures-proforma/{documentId}/sync/accounting-invoice", documentId.get())
                .bodyValue(Map.of(
                        "invoiceNumber", "BIL-BNK-" + UUID.randomUUID().toString().substring(0, 8),
                        "postInvoice", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.linkedAccountingInvoiceId").value(value -> accountingInvoiceId.set(value.toString()));

        scopedClient.post()
                .uri("/api/treasury/bank-accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankName", "Billing Settlement Bank",
                        "accountNumber", "ACC-BIL-" + UUID.randomUUID().toString().substring(0, 8),
                        "iban", "FR7630006000011234567890199",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        scopedClient.post()
                .uri("/api/factures-proforma/{documentId}/payments/bank", documentId.get())
                .bodyValue(Map.of(
                        "bankAccountId", bankAccountId.get(),
                        "paymentMethod", "BANK_TRANSFER",
                        "amount", 40.0,
                        "reference", "PAY-BANK-" + UUID.randomUUID().toString().substring(0, 6)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.invoiceId").isEqualTo(accountingInvoiceId.get())
                .jsonPath("$.billingDocumentId").isEqualTo(documentId.get())
                .jsonPath("$.linkedServiceCode").isEqualTo("TREASURY")
                .jsonPath("$.linkedDocumentType").isEqualTo("INVOICE_SETTLEMENT")
                .jsonPath("$.status").isEqualTo("SETTLED")
                .jsonPath("$.amount").isEqualTo(40);

        scopedClient.get()
                .uri("/api/accounting/invoices/{invoiceId}", accountingInvoiceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("POSTED")
                .jsonPath("$.data.paymentStatus").isEqualTo("PAID")
                .jsonPath("$.data.settledAmount").isEqualTo(40)
                .jsonPath("$.data.outstandingAmount").isEqualTo(0);
    }

    @Test
    void resourceReservationLifecycleSupportsReleaseAssignmentUnassignmentAndDisposal() {
        TestUser user = bootstrapUser("resource-lifecycle", Set.of(
                "organizations:write",
                "resources:write"));
        String organizationId = createOrganization(user, "ORG-RES-LIFE-01");
        String agencyId = createAgency(user, organizationId, "AGY-RES-LIFE-01");
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> resourceId = new AtomicReference<>();
        AtomicReference<String> reservationId = new AtomicReference<>();
        String resourceSuffix = UUID.randomUUID().toString().substring(0, 8);

        scopedClient.post()
                .uri("/api/resources")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", agencyId,
                        "resourceCode", "RES-LIFE-" + resourceSuffix,
                        "name", "Lifecycle Laptop",
                        "category", "it",
                        "serialNumber", "SN-LIFE-" + resourceSuffix))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> resourceId.set(value.toString()))
                .jsonPath("$.data.status").isEqualTo("AVAILABLE");

        scopedClient.post()
                .uri("/api/resources/{resourceId}/reservations", resourceId.get())
                .bodyValue(Map.of(
                        "reserveeType", "AGENCY",
                        "reserveeId", agencyId,
                        "reason", "Deployment"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("RESERVED");

        scopedClient.get()
                .uri("/api/resources/{resourceId}/reservations", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].id").value(value -> reservationId.set(value.toString()))
                .jsonPath("$.data[0].status").isEqualTo("ACTIVE");

        scopedClient.post()
                .uri("/api/resources/{resourceId}/reservations/{reservationId}/release", resourceId.get(),
                        reservationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("AVAILABLE");

        scopedClient.post()
                .uri("/api/resources/{resourceId}/reservations", resourceId.get())
                .bodyValue(Map.of(
                        "reserveeType", "AGENCY",
                        "reserveeId", agencyId,
                        "reason", "Assigned deployment"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("RESERVED");

        scopedClient.post()
                .uri("/api/resources/{resourceId}/assignments", resourceId.get())
                .bodyValue(Map.of(
                        "assigneeType", "AGENCY",
                        "assigneeId", agencyId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("ASSIGNED");

        scopedClient.post()
                .uri("/api/resources/{resourceId}/unassign", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("AVAILABLE");

        scopedClient.get()
                .uri("/api/resources/{resourceId}/assignments", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("CLOSED");

        scopedClient.post()
                .uri("/api/resources/{resourceId}/dispose", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("DISPOSED");

        org.assertj.core.api.Assertions.assertThat(outboxEvents(TENANT_ID).stream()
                        .map(OutboxEvent::eventType)
                        .toList())
                .contains("RESOURCE_RESERVED", "RESOURCE_ASSIGNED", "RESOURCE_UNASSIGNED", "RESOURCE_DISPOSED");
    }

    @Test
    void nestedAddressBooksBridgeCommonCoreAcrossActorOrganizationThirdPartyAndResource() {
        TestUser user = bootstrapUser("address-book-bridge", Set.of(
                "organizations:write",
                "third-parties:write",
                "resources:write"));
        String organizationId = createOrganization(user, "ORG-ADDR-" + UUID.randomUUID().toString().substring(0, 6));
        String agencyId = createAgency(user, organizationId, "AGY-ADDR-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> actorId = new AtomicReference<>();
        AtomicReference<String> thirdPartyId = new AtomicReference<>();
        AtomicReference<String> resourceId = new AtomicReference<>();

        systemClient().post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Address",
                        "lastName", "Book",
                        "email", "address.book." + UUID.randomUUID() + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> actorId.set(value.toString()));

        scopedClient.post()
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ACTOR",
                        "partyId", actorId.get(),
                        "referenceCode", "TP-ADDR-" + UUID.randomUUID().toString().substring(0, 6),
                        "displayName", "Address Book Third Party",
                        "roles", List.of("CLIENT"),
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> thirdPartyId.set(value.toString()));

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/resources", organizationId, agencyId)
                .bodyValue(Map.of(
                        "resourceCode", "RES-ADDR-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Address Book Terminal",
                        "category", "IT",
                        "serialNumber", "SN-ADDR-" + UUID.randomUUID().toString().substring(0, 6),
                        "ipAddress", "10.0.0.10"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> resourceId.set(value.toString()));

        userClient(user).post()
                .uri("/api/actors/{actorId}/addresses", actorId.get())
                .bodyValue(Map.of(
                        "type", "HOME",
                        "addressLine1", "12 Actor Street",
                        "city", "Douala",
                        "isDefault", true))
                .exchange()
                .expectStatus().isCreated();

        userClient(user).post()
                .uri("/api/actors/{actorId}/contacts", actorId.get())
                .bodyValue(Map.of(
                        "firstName", "Address",
                        "lastName", "Book",
                        "phoneNumber", "+237600000001",
                        "email", "actor.contact@example.com"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/addresses", organizationId)
                .bodyValue(Map.of(
                        "type", "HEADQUARTER",
                        "addressLine1", "1 Organization Avenue",
                        "city", "Yaounde",
                        "isDefault", true))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/agencies/{agencyId}/contacts", agencyId)
                .bodyValue(Map.of(
                        "firstName", "Agency",
                        "lastName", "Desk",
                        "phoneNumber", "+237600000002",
                        "email", "agency.contact@example.com"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/third-parties/{thirdPartyId}/addresses", thirdPartyId.get())
                .bodyValue(Map.of(
                        "type", "BILLING",
                        "addressLine1", "44 Third Party Road",
                        "city", "Bafoussam",
                        "isDefault", true))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/resources/{resourceId}/contacts", resourceId.get())
                .bodyValue(Map.of(
                        "firstName", "Resource",
                        "lastName", "Support",
                        "phoneNumber", "+237600000003",
                        "email", "resource.contact@example.com"))
                .exchange()
                .expectStatus().isCreated();

        userClient(user).get()
                .uri("/api/actors/{actorId}/addresses", actorId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        userClient(user).get()
                .uri("/api/actors/{actorId}/contacts", actorId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/addresses", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        scopedClient.get()
                .uri("/api/agencies/{agencyId}/contacts", agencyId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        scopedClient.get()
                .uri("/api/third-parties/{thirdPartyId}/addresses", thirdPartyId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        scopedClient.get()
                .uri("/api/resources/{resourceId}/contacts", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);
    }

    @Test
    void scopedResourceEndpointsBridgeOrganizationsAgenciesAndWarehouses() {
        TestUser user = bootstrapUser("scoped-resource", Set.of(
                "organizations:write",
                "resources:write"));
        String organizationId = createOrganization(user, "ORG-SCP-" + UUID.randomUUID().toString().substring(0, 6));
        String branchAgencyId = createAgency(user, organizationId, "AGY-SCP-" + UUID.randomUUID().toString().substring(0, 6));
        String warehouseId = createWarehouse(user, organizationId, "WH-SCP-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = organizationUserClient(user, organizationId);

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/resources", organizationId)
                .bodyValue(Map.of(
                        "agencyId", branchAgencyId,
                        "resourceCode", "RES-ORG-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Organization Router",
                        "category", "NETWORK",
                        "serialNumber", "SN-ORG-" + UUID.randomUUID().toString().substring(0, 6),
                        "ipAddress", "10.0.1.10"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/resources", organizationId, branchAgencyId)
                .bodyValue(Map.of(
                        "resourceCode", "RES-AGY-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Agency Tablet",
                        "category", "IT",
                        "serialNumber", "SN-AGY-" + UUID.randomUUID().toString().substring(0, 6)))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/warehouses/{warehouseId}/resources", warehouseId)
                .bodyValue(Map.of(
                        "resourceCode", "RES-WH-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Warehouse Forklift",
                        "category", "MACHINE",
                        "serialNumber", "SN-WH-" + UUID.randomUUID().toString().substring(0, 6)))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/resources", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(3);

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/resources", organizationId, branchAgencyId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(2);

        scopedClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/warehouses/{warehouseId}/resources/search")
                        .queryParam("q", "Forklift")
                        .build(warehouseId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].name").isEqualTo("Warehouse Forklift");
    }

    @Test
    void resourceAssignmentsAndReservationsCanBeTrackedByActorAgencyAndOrganizationTargets() {
        TestUser user = bootstrapUser("resource-targets", Set.of(
                "organizations:write",
                "resources:write"));
        String organizationId = createOrganization(user, "ORG-TGT-" + UUID.randomUUID().toString().substring(0, 6));
        String agencyId = createAgency(user, organizationId, "AGY-TGT-" + UUID.randomUUID().toString().substring(0, 6));
        String warehouseId = createWarehouse(user, organizationId, "WH-TGT-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> actorId = new AtomicReference<>();
        AtomicReference<String> actorResourceId = new AtomicReference<>();
        AtomicReference<String> agencyResourceId = new AtomicReference<>();
        AtomicReference<String> organizationResourceId = new AtomicReference<>();

        systemClient().post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Target",
                        "lastName", "Actor",
                        "email", "target.actor." + UUID.randomUUID() + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> actorId.set(value.toString()));

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/resources", organizationId, agencyId)
                .bodyValue(Map.of(
                        "resourceCode", "RES-ACT-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Actor Laptop",
                        "category", "IT",
                        "serialNumber", "SN-ACT-" + UUID.randomUUID().toString().substring(0, 6)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> actorResourceId.set(value.toString()));

        scopedClient.post()
                .uri("/api/warehouses/{warehouseId}/resources", warehouseId)
                .bodyValue(Map.of(
                        "resourceCode", "RES-AGY-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Agency Forklift",
                        "category", "MACHINE",
                        "serialNumber", "SN-AGY-" + UUID.randomUUID().toString().substring(0, 6)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> agencyResourceId.set(value.toString()));

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/resources", organizationId)
                .bodyValue(Map.of(
                        "agencyId", agencyId,
                        "resourceCode", "RES-ORG-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Organization Server",
                        "category", "NETWORK",
                        "serialNumber", "SN-ORG-" + UUID.randomUUID().toString().substring(0, 6),
                        "ipAddress", "10.10.0.10"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationResourceId.set(value.toString()));

        scopedClient.post()
                .uri("/api/resources/{resourceId}/reservations", actorResourceId.get())
                .bodyValue(Map.of(
                        "reserveeType", "ACTOR",
                        "reserveeId", actorId.get(),
                        "reason", "Assigned workstation"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/resources/{resourceId}/assignments", actorResourceId.get())
                .bodyValue(Map.of(
                        "assigneeType", "ACTOR",
                        "assigneeId", actorId.get()))
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/resources/{resourceId}/reservations", agencyResourceId.get())
                .bodyValue(Map.of(
                        "reserveeType", "AGENCY",
                        "reserveeId", warehouseId,
                        "reason", "Warehouse equipment pool"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/resources/{resourceId}/assignments", agencyResourceId.get())
                .bodyValue(Map.of(
                        "assigneeType", "AGENCY",
                        "assigneeId", warehouseId))
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/resources/{resourceId}/reservations", organizationResourceId.get())
                .bodyValue(Map.of(
                        "reserveeType", "ORGANIZATION",
                        "reserveeId", organizationId,
                        "reason", "Shared organization infrastructure"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/resources/{resourceId}/assignments", organizationResourceId.get())
                .bodyValue(Map.of(
                        "assigneeType", "ORGANIZATION",
                        "assigneeId", organizationId))
                .exchange()
                .expectStatus().isOk();

        userClient(user).get()
                .uri("/api/actors/{actorId}/resources/assignments", actorId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].assigneeType").isEqualTo("ACTOR");

        userClient(user).get()
                .uri("/api/actors/{actorId}/resources/reservations", actorId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].status").isEqualTo("FULFILLED");

        scopedClient.get()
                .uri("/api/agencies/{agencyId}/resources/assignments", warehouseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].assigneeType").isEqualTo("AGENCY");

        scopedClient.get()
                .uri("/api/agencies/{agencyId}/resources/reservations", warehouseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].status").isEqualTo("FULFILLED");

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/resources/assignments", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].assigneeType").isEqualTo("ORGANIZATION");

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/resources/reservations", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].status").isEqualTo("FULFILLED");
    }

    @Test
    void operationalSiteGeneralizedInventoryAndServiceWorkspaceExposeTransverseViews() {
        TestUser user = bootstrapUser("workspace-views", Set.of(
                "organizations:write",
                "products:write",
                "inventory:write",
                "resources:write",
                "settings:write"));
        String organizationId = createOrganization(user, "ORG-OPS-" + UUID.randomUUID().toString().substring(0, 6));
        String branchAgencyId = createAgency(user, organizationId, "AGY-OPS-" + UUID.randomUUID().toString().substring(0, 6));
        String warehouseId = createWarehouse(user, organizationId, "WH-OPS-" + UUID.randomUUID().toString().substring(0, 6));
        String secondWarehouseId = createWarehouse(user, organizationId, "WH-OPS-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> fileId = new AtomicReference<>();
        AtomicReference<String> resourceId = new AtomicReference<>();

        configureSequence(user, organizationId, "STOCK_MOVEMENT", "MOV-OPS-", 4);
        configureSequence(user, organizationId, "INVENTORY_SESSION", "INV-OPS-", 4);
        configureSequence(user, organizationId, "WAREHOUSE_TRANSFER", "WHT-OPS-", 4);

        for (String serviceCode : List.of("COMMERCIAL", "PRODUCT", "INVENTORY", "SALES", "ACCOUNTING", "TREASURY",
                "RESOURCE")) {
            scopedClient.post()
                    .uri("/api/organizations/{organizationId}/services", organizationId)
                    .bodyValue(Map.of("serviceCode", serviceCode))
                    .exchange()
                    .expectStatus().isOk();
        }

        scopedClient.post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sku", "SKU-OPS-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Operational Product",
                        "familyCode", "FAMILY-OPS",
                        "variantLabel", "STANDARD",
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        scopedClient.post()
                .uri("/api/warehouses/{warehouseId}/resources", warehouseId)
                .bodyValue(Map.of(
                        "resourceCode", "RES-OPS-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Warehouse Scanner",
                        "category", "IT",
                        "serialNumber", "SN-OPS-" + UUID.randomUUID().toString().substring(0, 6),
                        "ipAddress", "10.20.30.40"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> resourceId.set(value.toString()));

        String buildingId = createPhysicalSpace(user, organizationId, warehouseId, null,
                "BLD-OPS-" + UUID.randomUUID().toString().substring(0, 4), "Warehouse Building", "BUILDING", 0, 100);
        String roomId = createPhysicalSpace(user, organizationId, warehouseId, buildingId,
                "ROM-OPS-" + UUID.randomUUID().toString().substring(0, 4), "Control Room", "ROOM", 1, 10);
        String workstationId = createPhysicalSpace(user, organizationId, warehouseId, roomId,
                "WKS-OPS-" + UUID.randomUUID().toString().substring(0, 4), "Scanner Station", "WORKSTATION", 2, 1);

        scopedClient.post()
                .uri("/api/resources/{resourceId}/reservations", resourceId.get())
                .bodyValue(Map.of(
                        "reserveeType", "PHYSICAL_SPACE",
                        "reserveeId", workstationId,
                        "reason", "Scanner docking point"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/resources/{resourceId}/assignments", resourceId.get())
                .bodyValue(Map.of(
                        "assigneeType", "PHYSICAL_SPACE",
                        "assigneeId", workstationId))
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/inventory/movements")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", warehouseId,
                        "productId", productId.get(),
                        "movementType", "INBOUND",
                        "quantity", 8.0))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/inventory/sessions")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", warehouseId,
                        "productId", productId.get(),
                        "countedQuantity", 8.0))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/inventory/transfers")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sourceAgencyId", warehouseId,
                        "targetAgencyId", secondWarehouseId,
                        "productId", productId.get(),
                        "quantity", 2.0))
                .exchange()
                .expectStatus().isCreated();

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new ByteArrayResource("ops-document".getBytes()) {
                    @Override
                    public String getFilename() {
                        return "ops-note.txt";
                    }
                })
                .contentType(MediaType.TEXT_PLAIN);

        scopedClient.post()
                .uri("/api/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> fileId.set(value.toString()));

        scopedClient.post()
                .uri("/api/document-hub/links")
                .bodyValue(Map.of(
                        "targetType", "AGENCY",
                        "targetId", warehouseId,
                        "fileId", fileId.get(),
                        "documentCategory", "CHECKLIST",
                        "label", "Warehouse checklist"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/document-hub/links")
                .bodyValue(Map.of(
                        "targetType", "PHYSICAL_SPACE",
                        "targetId", workstationId,
                        "fileId", fileId.get(),
                        "documentCategory", "LAYOUT",
                        "label", "Scanner station map"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.get()
                .uri("/api/physical-spaces/{spaceId}/resources/assignments", workstationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].assigneeType").isEqualTo("PHYSICAL_SPACE");

        scopedClient.get()
                .uri("/api/physical-spaces/{spaceId}/resources/reservations", workstationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].status").isEqualTo("FULFILLED");

        scopedClient.get()
                .uri("/api/warehouses/{warehouseId}/operational-site", warehouseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.scopeType").isEqualTo("WAREHOUSE")
                .jsonPath("$.data.physicalLayout.totalSpaces").isEqualTo(3)
                .jsonPath("$.data.physicalLayout.rootSpaceCount").isEqualTo(1)
                .jsonPath("$.data.physicalLayout.tree[0].children[0].children[0].assignedResources").isEqualTo(1)
                .jsonPath("$.data.physicalLayout.tree[0].children[0].children[0].documentCount").isEqualTo(1)
                .jsonPath("$.data.assetPortfolio.totalResources").isEqualTo(1)
                .jsonPath("$.data.documents.totalDocuments").isEqualTo(1)
                .jsonPath("$.data.inventory.validatedStockMovementCount").isEqualTo(1)
                .jsonPath("$.data.inventory.inventorySessionCount").isEqualTo(1)
                .jsonPath("$.data.capabilities").value(value -> org.assertj.core.api.Assertions
                        .assertThat((List<String>) (List<?>) value)
                        .contains("PHYSICAL_LAYOUT", "ASSET_MANAGEMENT", "GENERALIZED_INVENTORY", "DOCUMENT_HUB"));

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/generalized-inventory", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.catalogProductCount").isEqualTo(1)
                .jsonPath("$.data.scopedProductCount").isEqualTo(1)
                .jsonPath("$.data.physicalSpaceCount").isEqualTo(3)
                .jsonPath("$.data.resourceCount").isEqualTo(1)
                .jsonPath("$.data.warehouseTransferCount").isEqualTo(1)
                .jsonPath("$.data.documentCount").isEqualTo(2);

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/service-workspaces/{workspaceCode}", organizationId, "CASHIER")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.workspaceCode").isEqualTo("CASHIER")
                .jsonPath("$.data.activeOperationalAgencyCount").isEqualTo(1)
                .jsonPath("$.data.activeWarehouseCount").isEqualTo(2)
                .jsonPath("$.data.assets.totalResources").isEqualTo(1)
                .jsonPath("$.data.inventory.catalogProductCount").isEqualTo(1)
                .jsonPath("$.data.physicalLayout.totalSpaces").isEqualTo(3)
                .jsonPath("$.data.documents.totalDocuments").isEqualTo(2)
                .jsonPath("$.data.readiness.servicesReady").isEqualTo(true)
                .jsonPath("$.data.readiness.physicalLayoutReady").isEqualTo(true)
                .jsonPath("$.data.readiness.ready").isEqualTo(true);
    }

    @Test
    void operationalExcellenceAxesCoverPolicySiteAssetDocumentCampaignPilotageComplianceAndTimeline() {
        TestUser user = bootstrapUser("ops-excellence", Set.of(
                "organizations:write",
                "settings:read",
                "settings:write",
                "resources:write",
                "inventory:write",
                "administration:read",
                "administration:write",
                "administration:audit:read",
                "administration:govern:agencies"));
        String organizationId = createOrganization(user, "ORG-OPEX-" + UUID.randomUUID().toString().substring(0, 6));
        String warehouseId = createWarehouse(user, organizationId, "WH-OPEX-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = organizationUserClient(user, organizationId);
        AtomicReference<String> resourceId = new AtomicReference<>();
        AtomicReference<String> fileId = new AtomicReference<>();
        AtomicReference<String> governedDocumentLinkId = new AtomicReference<>();
        AtomicReference<String> campaignId = new AtomicReference<>();

        for (String serviceCode : List.of("INVENTORY", "RESOURCE")) {
            scopedClient.post()
                    .uri("/api/organizations/{organizationId}/services", organizationId)
                    .bodyValue(Map.of("serviceCode", serviceCode))
                    .exchange()
                    .expectStatus().isOk();
        }

        Map<String, Object> operationalPolicyBody = new java.util.LinkedHashMap<>();
        operationalPolicyBody.put("assignmentRequiresApproval", true);
        operationalPolicyBody.put("allowCrossAgencyAssetAssignment", false);
        operationalPolicyBody.put("siteOpeningChecklistRequired", true);
        operationalPolicyBody.put("mandatoryDocumentApproval", true);
        operationalPolicyBody.put("inventoryVarianceTolerancePercent", 3);
        operationalPolicyBody.put("maintenanceAlertThresholdDays", 21);
        operationalPolicyBody.put("lowUtilizationThresholdPercent", 15);
        operationalPolicyBody.put("maxOpenInventoryCampaigns", 2);
        operationalPolicyBody.put("requireInventorySupervisorApproval", true);
        operationalPolicyBody.put("automaticLifecycleEvents", true);
        operationalPolicyBody.put("strictDocumentExpiry", true);

        scopedClient.put()
                .uri("/api/settings/organizations/{organizationId}/agencies/{agencyId}/operational-policy",
                        organizationId, warehouseId)
                .bodyValue(operationalPolicyBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.assignmentRequiresApproval").isEqualTo(true)
                .jsonPath("$.data.requireInventorySupervisorApproval").isEqualTo(true)
                .jsonPath("$.data.maxOpenInventoryCampaigns").isEqualTo(2);

        String buildingId = createPhysicalSpace(user, organizationId, warehouseId, null,
                "BLD-OPEX-" + UUID.randomUUID().toString().substring(0, 4),
                "Operations Building", "BUILDING", 0, 150);
        String roomId = createPhysicalSpace(user, organizationId, warehouseId, buildingId,
                "ROM-OPEX-" + UUID.randomUUID().toString().substring(0, 4),
                "Control Room", "ROOM", 1, 12);
        String workstationId = createPhysicalSpace(user, organizationId, warehouseId, roomId,
                "WKS-OPEX-" + UUID.randomUUID().toString().substring(0, 4),
                "Supervisor Desk", "WORKSTATION", 2, 1);

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/operational-responsibilities",
                        organizationId, warehouseId)
                .bodyValue(Map.of(
                        "physicalSpaceId", workstationId,
                        "actorId", user.actorId(),
                        "responsibilityType", "SITE_MANAGER",
                        "primaryResponsibility", true,
                        "active", true,
                        "notes", "Go-live owner"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.actorId").isEqualTo(user.actorId())
                .jsonPath("$.data.primaryResponsibility").isEqualTo(true);

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/operational-site-readiness",
                        organizationId, warehouseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.totalPhysicalSpaces").isEqualTo(3)
                .jsonPath("$.data.primaryResponsibilities").isEqualTo(1)
                .jsonPath("$.data.ready").isEqualTo(true)
                .jsonPath("$.data.readinessStatus").isEqualTo("SITE_READY");

        scopedClient.post()
                .uri("/api/administration/operational-excellence/organizations/{organizationId}/agencies/{agencyId}/commission-site",
                        organizationId, warehouseId)
                .bodyValue(Map.of(
                        "siteCategory", "WAREHOUSE",
                        "operatingModel", "STAFFED",
                        "cashEnabled", false,
                        "warehouseEnabled", true,
                        "maintenanceEnabled", true,
                        "inventoryEnabled", true,
                        "documentComplianceRequired", true,
                        "defaultPhysicalSpaceId", workstationId,
                        "readinessNotes", "go-live ready"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.siteProfile.siteCategory").isEqualTo("WAREHOUSE")
                .jsonPath("$.data.siteProfile.openingStatus").isEqualTo("ACTIVE")
                .jsonPath("$.data.siteReadiness.ready").isEqualTo(true);

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/operational-site-profile",
                        organizationId, warehouseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.siteCategory").isEqualTo("WAREHOUSE")
                .jsonPath("$.data.defaultPhysicalSpaceId").isEqualTo(workstationId);

        scopedClient.post()
                .uri("/api/warehouses/{warehouseId}/resources", warehouseId)
                .bodyValue(Map.of(
                        "resourceCode", "RES-OPEX-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Operations Terminal",
                        "category", "IT",
                        "serialNumber", "SN-OPEX-" + UUID.randomUUID().toString().substring(0, 6),
                        "ipAddress", "10.10.40.12"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> resourceId.set(value.toString()));

        scopedClient.post()
                .uri("/api/resources/{resourceId}/assignments", resourceId.get())
                .bodyValue(Map.of(
                        "assigneeType", "PHYSICAL_SPACE",
                        "assigneeId", workstationId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("ASSIGNED");

        Map<String, Object> commissionAssetBody = new java.util.LinkedHashMap<>();
        commissionAssetBody.put("physicalSpaceId", workstationId);
        commissionAssetBody.put("ownerActorId", user.actorId());
        commissionAssetBody.put("assetClass", "POS_TERMINAL");
        commissionAssetBody.put("criticality", "HIGH");
        commissionAssetBody.put("complianceStatus", "NON_COMPLIANT");
        commissionAssetBody.put("acquisitionCost", 2200.00);
        commissionAssetBody.put("currentValue", 1800.00);
        commissionAssetBody.put("depreciationMethod", "STRAIGHT_LINE");
        commissionAssetBody.put("acquisitionDate", Instant.now().minusSeconds(86400L * 45).toString());
        commissionAssetBody.put("warrantyUntil", Instant.now().plusSeconds(86400L * 180).toString());
        commissionAssetBody.put("expectedRenewalDate", Instant.now().plusSeconds(86400L * 365).toString());
        commissionAssetBody.put("lastComplianceCheckAt", Instant.now().minusSeconds(86400L * 10).toString());
        commissionAssetBody.put("nextComplianceCheckAt", Instant.now().plusSeconds(86400L * 20).toString());
        commissionAssetBody.put("maintenanceContractReference", "MC-OPEX-01");
        commissionAssetBody.put("notes", "Commissioned terminal");

        scopedClient.post()
                .uri("/api/administration/operational-excellence/resources/{resourceId}/commission", resourceId.get())
                .bodyValue(commissionAssetBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.resourceId").isEqualTo(resourceId.get())
                .jsonPath("$.data.lifecyclePhase").isEqualTo("IN_SERVICE")
                .jsonPath("$.data.complianceStatus").isEqualTo("NON_COMPLIANT")
                .jsonPath("$.data.physicalSpaceId").isEqualTo(workstationId);

        scopedClient.get()
                .uri("/api/organizations/{organizationId}/advanced-assets/overview", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.totalAssets").isEqualTo(1)
                .jsonPath("$.data.nonCompliantAssets").isEqualTo(1);

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new ByteArrayResource("operational-excellence-document".getBytes()) {
                    @Override
                    public String getFilename() {
                        return "operational-excellence.txt";
                    }
                })
                .contentType(MediaType.TEXT_PLAIN);

        scopedClient.post()
                .uri("/api/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> fileId.set(value.toString()));

        scopedClient.post()
                .uri("/api/document-hub/links")
                .bodyValue(Map.of(
                        "targetType", "AGENCY",
                        "targetId", warehouseId,
                        "fileId", fileId.get(),
                        "documentCategory", "CHECKLIST",
                        "label", "Agency opening checklist"))
                .exchange()
                .expectStatus().isCreated();

        scopedClient.post()
                .uri("/api/document-hub/links")
                .bodyValue(Map.of(
                        "targetType", "PHYSICAL_SPACE",
                        "targetId", workstationId,
                        "fileId", fileId.get(),
                        "documentCategory", "LAYOUT",
                        "label", "Supervisor desk layout"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> governedDocumentLinkId.set(value.toString()));

        scopedClient.put()
                .uri("/api/document-governance/organizations/{organizationId}/policies/{targetType}/{documentCategory}",
                        organizationId, "PHYSICAL_SPACE", "LAYOUT")
                .bodyValue(Map.of(
                        "mandatory", true,
                        "approvalRequired", true,
                        "expiryDays", 30,
                        "reviewerResponsibilityType", "SITE_MANAGER"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.mandatory").isEqualTo(true)
                .jsonPath("$.data.approvalRequired").isEqualTo(true)
                .jsonPath("$.data.reviewerResponsibilityType").isEqualTo("SITE_MANAGER");

        scopedClient.get()
                .uri("/api/document-governance/targets/{targetType}/{targetId}", "PHYSICAL_SPACE", workstationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].documentCategory").isEqualTo("LAYOUT")
                .jsonPath("$.data[0].mandatory").isEqualTo(true)
                .jsonPath("$.data[0].reviewStatus").isEqualTo("PENDING");

        scopedClient.post()
                .uri("/api/administration/operational-excellence/documents/{documentLinkId}/approve",
                        governedDocumentLinkId.get())
                .bodyValue(Map.of(
                        "expiresAt", Instant.now().plusSeconds(86400L * 30).toString(),
                        "notes", "Approved for go-live"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.reviewStatus").isEqualTo("APPROVED");

        scopedClient.get()
                .uri("/api/document-governance/organizations/{organizationId}/overview", organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.policyCount").isEqualTo(1)
                .jsonPath("$.data.documentCount").isEqualTo(2)
                .jsonPath("$.data.approvedDocuments").isEqualTo(1)
                .jsonPath("$.data.pendingDocuments").isEqualTo(1);

        scopedClient.post()
                .uri("/api/administration/operational-excellence/organizations/{organizationId}/inventory-campaigns/prepare",
                        organizationId)
                .bodyValue(Map.of(
                        "agencyId", warehouseId,
                        "warehouseId", warehouseId,
                        "physicalSpaceId", workstationId,
                        "supervisorActorId", user.actorId(),
                        "campaignCode", "GIC-" + UUID.randomUUID().toString().substring(0, 6),
                        "campaignType", "FULL",
                        "scopeType", "SITE",
                        "scheduledAt", Instant.now().plusSeconds(3600).toString(),
                        "notes", "Quarterly count"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").value(value -> campaignId.set(value.toString()))
                .jsonPath("$.data.status").isEqualTo("PLANNED")
                .jsonPath("$.data.approvalRequired").isEqualTo(true);

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/generalized-inventory-campaigns/{campaignId}/start",
                        organizationId, campaignId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("IN_PROGRESS");

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/generalized-inventory-campaigns/{campaignId}/submit",
                        organizationId, campaignId.get())
                .bodyValue(Map.of("variancePercent", 1.5))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("PENDING_APPROVAL");

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/generalized-inventory-campaigns/{campaignId}/approve",
                        organizationId, campaignId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("COMPLETED");

        scopedClient.get()
                .uri("/api/administration/operational-excellence/organizations/{organizationId}/pilotage",
                        organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.assetPortfolio.totalResources").isEqualTo(1)
                .jsonPath("$.data.advancedAssetOverview.totalAssets").isEqualTo(1)
                .jsonPath("$.data.advancedAssetOverview.nonCompliantAssets").isEqualTo(1)
                .jsonPath("$.data.documentHubOverview.totalDocuments").isEqualTo(2)
                .jsonPath("$.data.documentGovernanceOverview.approvedDocuments").isEqualTo(1)
                .jsonPath("$.data.campaignSummary.totalCampaigns").isEqualTo(1)
                .jsonPath("$.data.siteSnapshots[0].ready").isEqualTo(true);

        scopedClient.get()
                .uri("/api/administration/operational-excellence/organizations/{organizationId}/agencies/{agencyId}/pilotage",
                        organizationId, warehouseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.siteProfile.siteCategory").isEqualTo("WAREHOUSE")
                .jsonPath("$.data.siteProfile.openingStatus").isEqualTo("ACTIVE")
                .jsonPath("$.data.siteReadiness.ready").isEqualTo(true)
                .jsonPath("$.data.assetPortfolio.totalResources").isEqualTo(1)
                .jsonPath("$.data.campaignSummary.totalCampaigns").isEqualTo(1)
                .jsonPath("$.data.agencyDocumentCount").isEqualTo(1);

        scopedClient.get()
                .uri("/api/administration/operational-excellence/organizations/{organizationId}/compliance",
                        organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.totalAssets").isEqualTo(1)
                .jsonPath("$.data.nonCompliantAssets").isEqualTo(1)
                .jsonPath("$.data.retiredAssets").isEqualTo(0)
                .jsonPath("$.data.pendingDocuments").isEqualTo(1)
                .jsonPath("$.data.pendingInventoryCampaigns").isEqualTo(0)
                .jsonPath("$.data.unreadySites").isEqualTo(0);

        scopedClient.post()
                .uri("/api/administration/operational-excellence/resources/{resourceId}/retire", resourceId.get())
                .bodyValue(Map.of("notes", "Retired after go-live rehearsal"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.lifecyclePhase").isEqualTo("RETIRED");

        scopedClient.get()
                .uri("/api/administration/operational-excellence/organizations/{organizationId}/compliance",
                        organizationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.retiredAssets").isEqualTo(1);

        scopedClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/administration/operational-excellence/organizations/{organizationId}/timeline")
                        .queryParam("limit", 20)
                        .build(organizationId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[?(@.action=='SITE_COMMISSIONED')]").exists()
                .jsonPath("$.data[?(@.action=='ASSET_COMMISSIONED')]").exists()
                .jsonPath("$.data[?(@.action=='DOCUMENT_APPROVED')]").exists()
                .jsonPath("$.data[?(@.action=='INVENTORY_CAMPAIGN_PREPARED')]").exists()
                .jsonPath("$.data[?(@.action=='ASSET_RETIRED')]").exists();
    }

    @Test
    void administrationCoreSupportsRolePermissionAssignmentSettingsAndAudit() {
        TestUser adminUser = bootstrapUser("admin-core", Set.of(
                "administration:read",
                "administration:write",
                "administration:roles:clone",
                "administration:permissions:read",
                "administration:assignments:write",
                "administration:settings:write",
                "administration:audit:read",
                "administration:govern:business-actors",
                "administration:govern:organizations",
                "administration:govern:agencies",
                "organizations:write",
                "settings:write"));
        TestUser targetUser = bootstrapUser("admin-target", Set.of());
        AtomicReference<String> businessActorId = new AtomicReference<>();
        userClient(targetUser).post()
                .uri("/api/actors/onboarding")
                .bodyValue(Map.of(
                        "name", "Target Business Actor",
                        "businessId", "BA-" + UUID.randomUUID().toString().substring(0, 8)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> businessActorId.set(value.toString()));
        String organizationId = createOrganization(adminUser, "ORG-ADM-" + UUID.randomUUID().toString().substring(0, 6));
        String agencyId = createAgency(adminUser, organizationId, "AGY-ADM-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = userClient(adminUser).mutate()
                .defaultHeader("X-Organization-Id", organizationId)
                .build();
        AtomicReference<String> roleId = new AtomicReference<>();
        AtomicReference<String> clonedRoleId = new AtomicReference<>();
        AtomicReference<String> assignmentId = new AtomicReference<>();

        scopedClient.get()
                .uri("/api/administration/permissions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[?(@.code == 'administration:write')]").exists()
                .jsonPath("$.data[?(@.code == 'settings:read')]").exists()
                .jsonPath("$.data[?(@.code == 'cashier:write')]").exists()
                .jsonPath("$.data[?(@.code == 'hrm:employee:read')]").exists()
                .jsonPath("$.data[?(@.code == 'hrm:payroll:run')]").exists()
                .jsonPath("$.data[?(@.code == 'blockchain:wallet:create')]").exists()
                .jsonPath("$.data[?(@.code == 'blockchain:chain:validate')]").exists();

        scopedClient.get()
                .uri("/api/administration/role-templates")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[?(@.code == 'ORGANIZATION_ADMIN')]").exists()
                .jsonPath("$.data[?(@.code == 'AGENCY_ADMIN')]").exists()
                .jsonPath("$.data[?(@.code == 'HR_MANAGER')]").exists()
                .jsonPath("$.data[?(@.code == 'PAYROLL_MANAGER')]").exists()
                .jsonPath("$.data[?(@.code == 'BLOCKCHAIN_OPERATOR')]").exists();

        scopedClient.post()
                .uri("/api/administration/roles/defaults")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.length()").value(value -> org.assertj.core.api.Assertions.assertThat((Integer) value)
                        .isGreaterThanOrEqualTo(5));

        scopedClient.post()
                .uri("/api/administration/roles")
                .bodyValue(Map.of(
                        "code", "OPS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        "name", "Operations Admin",
                        "scopeType", "ORGANIZATION",
                        "permissions", List.of("products:write", "inventory:write", "hrm:employee:read",
                                "blockchain:block:read")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> roleId.set(value.toString()));

        scopedClient.patch()
                .uri("/api/administration/roles/{roleId}", roleId.get())
                .bodyValue(Map.of("name", "Operations Supervisor"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("Operations Supervisor");

        scopedClient.post()
                .uri("/api/administration/roles/{roleId}/clone", roleId.get())
                .bodyValue(Map.of(
                        "code", "OPS-CLONE-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "name", "Operations Supervisor Clone"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> clonedRoleId.set(value.toString()));

        scopedClient.put()
                .uri("/api/administration/roles/{roleId}/permissions", roleId.get())
                .bodyValue(Map.of("permissions", List.of("products:write", "inventory:write", "sales:write",
                        "hrm:payroll:read", "blockchain:chain:validate", "settings:read")))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.permissions.length()").isEqualTo(6);

        scopedClient.post()
                .uri("/api/administration/users/{userId}/roles", targetUser.userId())
                .bodyValue(Map.of(
                        "roleId", roleId.get(),
                        "scope", "ORGANIZATION"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> assignmentId.set(value.toString()));

        scopedClient.get()
                .uri("/api/administration/users/{userId}/roles", targetUser.userId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        scopedClient.get()
                .uri("/api/administration/settings/platform-options")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.allowRoleCloning").isEqualTo(true);

        scopedClient.put()
                .uri("/api/administration/settings/platform-options")
                .bodyValue(Map.of(
                        "requireBusinessActorApproval", true,
                        "requireOrganizationApproval", true,
                        "allowOrganizationSelfServiceCreation", true,
                        "allowAgencySelfServiceCreation", false,
                        "allowRoleCloning", true,
                        "allowAgencyScopedCustomRoles", true,
                        "allowOrganizationAdminsToGovernAgencies", true,
                        "allowBusinessActorSelfReactivation", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.allowOrganizationAdminsToGovernAgencies").isEqualTo(true);

        scopedClient.get()
                .uri("/api/administration/settings/general-options")
                .exchange()
                .expectStatus().isOk();

        scopedClient.put()
                .uri("/api/administration/settings/general-options")
                .bodyValue(Map.of(
                        "negotiateSellingPrice", true,
                        "sellingPriceIncludeVat", false,
                        "authorizeExceptionalDiscount", true,
                        "grantableDiscountRate", 7.5,
                        "printLogo", true,
                        "paperFormat", "A5",
                        "lengthOfVatInvoiceNumber", 12,
                        "prefixOfVatInvoiceNumber", "ADM",
                        "lowStockAlert", true,
                        "preventiveMaintenanceAlert", true))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.paperFormat").isEqualTo("A5");

        scopedClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/administration/governance/business-actors")
                        .queryParam("status", "PENDING_REVIEW")
                        .build())
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/administration/governance/business-actors/{businessActorId}", businessActorId.get())
                .bodyValue(Map.of(
                        "action", "approve",
                        "reason", "kyc approved"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED");

        scopedClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/administration/governance/organizations")
                        .queryParam("status", "PENDING_APPROVAL")
                        .build())
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/administration/governance/organizations/{organizationId}", organizationId)
                .bodyValue(Map.of(
                        "action", "approve",
                        "reason", "governance approved"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED");

        scopedClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/administration/governance/agencies")
                        .queryParam("organizationId", organizationId)
                        .queryParam("status", "ACTIVE")
                        .build())
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/administration/governance/agencies/{agencyId}", agencyId)
                .bodyValue(Map.of(
                        "action", "suspend",
                        "reason", "maintenance"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("SUSPENDED");

        scopedClient.get()
                .uri("/api/administration/audit")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").value(value -> org.assertj.core.api.Assertions.assertThat((Integer) value).isGreaterThanOrEqualTo(7));

        scopedClient.delete()
                .uri("/api/administration/users/{userId}/roles/{assignmentId}", targetUser.userId(), assignmentId.get())
                .exchange()
                .expectStatus().isOk();

        scopedClient.delete()
                .uri("/api/administration/roles/{roleId}", clonedRoleId.get())
                .exchange()
                .expectStatus().isOk();

        scopedClient.delete()
                .uri("/api/administration/roles/{roleId}", roleId.get())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void platformOptionsDriveRemainingSelfServiceWorkflows() {
        TestUser adminUser = bootstrapUser("admin-self-service", Set.of(
                "administration:read",
                "administration:write",
                "administration:settings:write",
                "administration:govern:business-actors",
                "organizations:write"));
        TestUser businessUser = bootstrapUser("self-service-user", Set.of("organizations:write"));
        AtomicReference<String> businessActorId = new AtomicReference<>();
        AtomicReference<String> organizationId = new AtomicReference<>();

        userClient(businessUser).post()
                .uri("/api/actors/onboarding")
                .bodyValue(Map.of(
                        "name", "Self Service Business Actor",
                        "businessId", "SS-BA-" + UUID.randomUUID().toString().substring(0, 8)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> businessActorId.set(value.toString()));

        userClient(businessUser).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", businessActorId.get(),
                        "code", "ORG-SS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "legalName", "Self Service Org",
                        "displayName", "Self Service Org",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));

        WebTestClient adminScopedClient = userClient(adminUser).mutate()
                .defaultHeader("X-Organization-Id", organizationId.get())
                .build();

        adminScopedClient.put()
                .uri("/api/administration/settings/platform-options")
                .bodyValue(Map.of(
                        "requireBusinessActorApproval", true,
                        "requireOrganizationApproval", true,
                        "allowOrganizationSelfServiceCreation", false,
                        "allowAgencySelfServiceCreation", false,
                        "allowRoleCloning", true,
                        "allowAgencyScopedCustomRoles", true,
                        "allowOrganizationAdminsToGovernAgencies", true,
                        "allowBusinessActorSelfReactivation", false))
                .exchange()
                .expectStatus().isOk();

        userClient(businessUser).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", businessActorId.get(),
                        "code", "ORG-DIS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "legalName", "Disabled Org",
                        "displayName", "Disabled Org",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("ORGANIZATION_SELF_SERVICE_DISABLED");

        userClient(businessUser).post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .bodyValue(Map.of(
                        "code", "AGY-DIS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "name", "Disabled Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("AGENCY_SELF_SERVICE_DISABLED");

        adminScopedClient.post()
                .uri("/api/administration/governance/business-actors/{businessActorId}", businessActorId.get())
                .bodyValue(Map.of(
                        "action", "suspend",
                        "reason", "manual review"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("SUSPENDED");

        userClient(businessUser).post()
                .uri("/api/actors/me/reactivate")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("BUSINESS_ACTOR_SELF_REACTIVATION_DISABLED");

        adminScopedClient.put()
                .uri("/api/administration/settings/platform-options")
                .bodyValue(Map.of(
                        "requireBusinessActorApproval", true,
                        "requireOrganizationApproval", true,
                        "allowOrganizationSelfServiceCreation", false,
                        "allowAgencySelfServiceCreation", false,
                        "allowRoleCloning", true,
                        "allowAgencyScopedCustomRoles", true,
                        "allowOrganizationAdminsToGovernAgencies", true,
                        "allowBusinessActorSelfReactivation", true))
                .exchange()
                .expectStatus().isOk();

        userClient(businessUser).post()
                .uri("/api/actors/me/reactivate")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED");
    }

    @Test
    void legacyParityGeneralOptionsInventorySessionsAndBankingRoutesAreAvailable() {
        TestUser user = bootstrapUser("legacy-final", Set.of(
                "organizations:write",
                "products:write",
                "settings:write",
                "inventory:write",
                "treasury:manage"));
        String organizationId = createOrganization(user, "ORG-LEG-" + UUID.randomUUID().toString().substring(0, 6));
        String agencyId = createAgency(user, organizationId, "AGY-LEG-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = userClient(user).mutate()
                .defaultHeader("X-Organization-Id", organizationId)
                .build();
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> inventorySessionId = new AtomicReference<>();
        AtomicReference<String> bankAccountId = new AtomicReference<>();
        AtomicReference<String> statementId = new AtomicReference<>();
        AtomicReference<String> transactionId = new AtomicReference<>();
        AtomicReference<String> checkId = new AtomicReference<>();

        scopedClient.get()
                .uri("/api/generalOptions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.organizationId").isEqualTo(organizationId);

        scopedClient.put()
                .uri("/api/generalOptions")
                .bodyValue(Map.ofEntries(
                        Map.entry("agencyId", agencyId),
                        Map.entry("negotiateSellingPrice", true),
                        Map.entry("sellingPriceIncludeVat", true),
                        Map.entry("authorizeExceptionalDiscount", true),
                        Map.entry("grantableDiscountRate", 7.5),
                        Map.entry("printLogo", true),
                        Map.entry("paperFormat", "A5"),
                        Map.entry("lengthOfVatInvoiceNumber", 10),
                        Map.entry("prefixOfVatInvoiceNumber", "VAT-LEG"),
                        Map.entry("lowStockAlert", true),
                        Map.entry("preventiveMaintenanceAlert", true)))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.paperFormat").isEqualTo("A5");

        scopedClient.post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sku", "SKU-LEG-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Legacy Product",
                        "familyCode", "LEGACY",
                        "variantLabel", "STD",
                        "unitPrice", 12.5,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        scopedClient.post()
                .uri("/api/inventories")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "agencyId", agencyId,
                        "productId", productId.get(),
                        "referenceNumber", "INV-LEG-" + UUID.randomUUID().toString().substring(0, 6),
                        "countedQuantity", 9.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> inventorySessionId.set(value.toString()))
                .jsonPath("$.data.status").isEqualTo("DRAFT");

        scopedClient.post()
                .uri("/api/inventories/{inventoryId}/validate", inventorySessionId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("VALIDATED");

        scopedClient.post()
                .uri("/api/banking/accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankName", "Legacy Bank",
                        "accountNumber", "ACC-" + UUID.randomUUID().toString().substring(0, 8),
                        "iban", "FR7630006000011234567890189",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        scopedClient.post()
                .uri("/api/banking/statements/import")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "statementNumber", "STM-LEG-" + UUID.randomUUID().toString().substring(0, 6),
                        "statementDate", "2026-03-08",
                        "openingBalance", 100.0,
                        "closingBalance", 90.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> statementId.set(value.toString()));

        scopedClient.post()
                .uri("/api/banking/transactions")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "referenceNumber", "TRX-LEG-" + UUID.randomUUID().toString().substring(0, 6),
                        "transactionType", "FEE",
                        "transactionDate", "2026-03-08",
                        "amount", -10.0,
                        "description", "Legacy fee"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> transactionId.set(value.toString()))
                .jsonPath("$.data.status").isEqualTo("RECORDED");

        scopedClient.get()
                .uri("/api/banking/accounts/{bankAccountId}/transactions?limit=10", bankAccountId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);

        scopedClient.post()
                .uri("/api/banking/reconciliation/manual")
                .bodyValue(Map.of(
                        "transactionId", transactionId.get(),
                        "statementLineId", statementId.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("RECONCILED");

        scopedClient.post()
                .uri("/api/banking/checks")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "bankAccountId", bankAccountId.get(),
                        "checkNumber", "CHK-LEG-" + UUID.randomUUID().toString().substring(0, 6),
                        "amount", 25.0,
                        "beneficiary", "Legacy Supplier"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> checkId.set(value.toString()));

        scopedClient.post()
                .uri("/api/banking/checks/{checkId}/deposit?accountId={accountId}", checkId.get(), bankAccountId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("CLEARED");
    }

    private WebTestClient systemClient() {
        return webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", API_KEY)
                .defaultHeader("X-Tenant-Id", TENANT_ID)
                .build();
    }

    private WebTestClient managementClient() {
        return webTestClient.mutate()
                .defaultHeader("X-Management-Api-Key", MANAGEMENT_API_KEY)
                .build();
    }

    private String basicAuthorization(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private TestUser bootstrapUser(String alias, Set<String> permissions) {
        String email = alias + "." + UUID.randomUUID() + "@example.com";
        String password = "Password!123";
        UUID tenantId = UUID.fromString(TENANT_ID);
        UUID actorId = Objects.requireNonNull(createActorUseCase.createActor(new CreateActorCommand(
                tenantId,
                "User",
                alias,
                null,
                email,
                null,
                null,
                null,
                null,
                null)).block()).id();
        UUID userId = Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                tenantId,
                actorId,
                alias,
                email,
                password,
                "LOCAL")).block()).id();

        for (String permission : permissions) {
            String roleCode = (alias + "-" + permission).replace(':', '-').toUpperCase();
            UUID roleId = Objects.requireNonNull(createRoleUseCase.createRole(new CreateRoleCommand(
                    tenantId,
                    roleCode,
                    "Role " + roleCode,
                    Set.of(permission))).block()).id();
            assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
                    tenantId,
                    userId,
                    roleId,
                    "GLOBAL")).block();
        }

        return new TestUser(userId.toString(), actorId.toString(), alias, password, permissions);
    }

    private WebTestClient userClient(TestUser user) {
        WebTestClient.Builder builder = systemClient().mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userSessionTokenService.issue(
                        UUID.fromString(TENANT_ID),
                        UUID.fromString(user.userId()),
                        UUID.fromString(user.actorId())));
        return builder.build();
    }

    private WebTestClient organizationUserClient(TestUser user, String organizationId) {
        return userClient(user).mutate()
                .defaultHeader("X-Organization-Id", organizationId)
                .build();
    }

    private WebTestClient userClient(TestUser user, String clientId, String apiKey) {
        return webTestClient.mutate()
                .defaultHeader("X-Client-Id", clientId)
                .defaultHeader("X-Api-Key", apiKey)
                .defaultHeader("X-Tenant-Id", TENANT_ID)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userSessionTokenService.issue(
                        UUID.fromString(TENANT_ID),
                        UUID.fromString(user.userId()),
                        UUID.fromString(user.actorId())))
                .build();
    }

    private String createOrganization(TestUser user, String code) {
        AtomicReference<String> organizationId = new AtomicReference<>();
        userClient(user).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", UUID.randomUUID().toString(),
                        "code", code,
                        "legalName", "Legal " + code,
                        "displayName", "Display " + code,
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));
        return organizationId.get();
    }

    private String createOwnedOrganization(TestUser user, String code) {
        AtomicReference<String> businessActorId = new AtomicReference<>();
        userClient(user).post()
                .uri("/api/actors/onboarding")
                .bodyValue(Map.of(
                        "name", "Owner " + code,
                        "businessId", "BA-" + code,
                        "niu", "NIU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> businessActorId.set(value.toString()));
        AtomicReference<String> organizationId = new AtomicReference<>();
        userClient(user).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", businessActorId.get(),
                        "code", code,
                        "legalName", "Legal " + code,
                        "displayName", "Display " + code,
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));
        return organizationId.get();
    }

    private String createAgency(TestUser user, String organizationId, String code) {
        AtomicReference<String> agencyId = new AtomicReference<>();
        userClient(user).post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId)
                .bodyValue(Map.of(
                        "code", code,
                        "name", "Agency " + code,
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> agencyId.set(value.toString()));
        return agencyId.get();
    }

    private String createWarehouse(TestUser user, String organizationId, String code) {
        AtomicReference<String> warehouseId = new AtomicReference<>();
        organizationUserClient(user, organizationId).post()
                .uri("/api/warehouses")
                .bodyValue(Map.of(
                        "code", code,
                        "name", "Warehouse " + code))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> warehouseId.set(value.toString()));
        return warehouseId.get();
    }

    private String createPhysicalSpace(TestUser user, String organizationId, String agencyId, String parentSpaceId,
            String code, String name, String spaceType, int levelNumber, int capacity) {
        AtomicReference<String> physicalSpaceId = new AtomicReference<>();
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("code", code);
        body.put("name", name);
        body.put("spaceType", spaceType);
        body.put("levelNumber", levelNumber);
        body.put("capacity", capacity);
        if (parentSpaceId != null) {
            body.put("parentSpaceId", parentSpaceId);
        }
        userClient(user).post()
                .uri("/api/organizations/{organizationId}/agencies/{agencyId}/physical-spaces", organizationId,
                        agencyId)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> physicalSpaceId.set(value.toString()));
        return physicalSpaceId.get();
    }

    private void configureSequence(TestUser user, String organizationId, String documentType, String prefix, int paddingWidth) {
        userClient(user).post()
                .uri("/api/settings/document-sequences")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "documentType", documentType,
                        "prefix", prefix,
                        "suffix", "",
                        "paddingWidth", paddingWidth,
                        "nextNumber", 1))
                .exchange()
                .expectStatus().isOk();
    }

    private void enableOrganizationServices(WebTestClient scopedClient, String organizationId, List<String> serviceCodes) {
        for (String serviceCode : serviceCodes) {
            scopedClient.post()
                    .uri("/api/organizations/{organizationId}/services", organizationId)
                    .bodyValue(Map.of("serviceCode", serviceCode))
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    private CustomerFixture createCustomerFixture(WebTestClient scopedClient, String organizationId, String suffix) {
        AtomicReference<String> actorId = new AtomicReference<>();
        AtomicReference<String> thirdPartyId = new AtomicReference<>();
        scopedClient.post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Billing",
                        "lastName", "Customer",
                        "email", "billing.customer." + suffix + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> actorId.set(value.toString()));

        scopedClient.post()
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ACTOR",
                        "partyId", actorId.get(),
                        "referenceCode", "TP-BIL-" + suffix,
                        "displayName", "Billing Customer " + suffix,
                        "roles", List.of("CLIENT"),
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> thirdPartyId.set(value.toString()));
        return new CustomerFixture(actorId.get(), thirdPartyId.get());
    }

    private String createProductFixture(WebTestClient scopedClient, String organizationId, String suffix) {
        AtomicReference<String> productId = new AtomicReference<>();
        scopedClient.post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "sku", "SKU-BIL-" + suffix,
                        "name", "Billing Product " + suffix,
                        "familyCode", "BILLING",
                        "variantLabel", "STANDARD",
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));
        return productId.get();
    }

    private CashierSessionFixture openCashierSessionFixture(WebTestClient scopedClient, String agencyId, String suffix) {
        AtomicReference<String> registerId = new AtomicReference<>();
        AtomicReference<String> cashierId = new AtomicReference<>();
        AtomicReference<String> sessionId = new AtomicReference<>();

        scopedClient.post()
                .uri("/api/cash-registers")
                .bodyValue(Map.of(
                        "code", "REG-" + suffix,
                        "label", "Register " + suffix,
                        "agencyId", agencyId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(value -> registerId.set(value.toString()));

        scopedClient.post()
                .uri("/api/cashiers")
                .bodyValue(Map.of(
                        "email", "cashier." + suffix + "@example.com",
                        "fullName", "Cashier " + suffix,
                        "agencyId", agencyId,
                        "kind", "CASHIER"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(value -> cashierId.set(value.toString()));

        scopedClient.post()
                .uri("/api/sessions")
                .bodyValue(Map.of(
                        "registerId", registerId.get(),
                        "cashierId", cashierId.get(),
                        "openingAmount", 100.0,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(value -> sessionId.set(value.toString()));

        return new CashierSessionFixture(registerId.get(), cashierId.get(), sessionId.get());
    }

    private List<OutboxEvent> outboxEvents(String tenantId) {
        return outboxEventRepository.findByTenantId(UUID.fromString(tenantId))
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private int relayOutboxUntilIdle(int batchSize, int maxPasses) {
        int total = 0;
        for (int pass = 0; pass < maxPasses; pass++) {
            int relayed = relayOutboxEventsUseCase.relayBatch(batchSize)
                    .blockOptional()
                    .orElse(0);
            total += relayed;
            if (relayed == 0) {
                return total;
            }
        }
        return total;
    }

    private List<DomainEventProjection> domainEventProjections(String tenantId) {
        return domainEventProjectionRepository.findByTenantId(UUID.fromString(tenantId))
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private record TestUser(String userId, String actorId, String username, String password, Set<String> permissions) {
        private TestUser(String userId, String actorId, Set<String> permissions) {
            this(userId, actorId, "bootstrap", "Password!123", permissions);
        }
    }

    private record CustomerFixture(String actorId, String thirdPartyId) {
    }

    private record CashierSessionFixture(String registerId, String cashierId, String sessionId) {
    }
}
