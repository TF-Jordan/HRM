package yowyob.comops.api.bootstrap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import yowyob.comops.api.actor.application.port.in.CreateActorCommand;
import yowyob.comops.api.actor.application.port.in.CreateActorUseCase;
import yowyob.comops.api.actor.application.port.in.OnboardBusinessActorCommand;
import yowyob.comops.api.actor.application.port.in.OnboardBusinessActorUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.kernel.config.UserSessionTokenService;
import yowyob.comops.api.organization.application.port.in.CreateOrganizationCommand;
import yowyob.comops.api.organization.application.port.in.CreateOrganizationUseCase;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "iwm.quotas.tenant-requests.enabled=true",
                "iwm.quotas.tenant-requests.limit=1",
                "iwm.quotas.tenant-requests.window=1m",
                "iwm.quotas.tenant-requests.fail-open=false"
        })
@AutoConfigureWebTestClient
@ActiveProfiles("test-memory")
class TenantRequestQuotaIntegrationTests {

    private static final String CLIENT_ID = "test-client";
    private static final String API_KEY = "test-key";
    private static final String TENANT_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserSessionTokenService userSessionTokenService;

    @Autowired
    private CreateActorUseCase createActorUseCase;

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private OnboardBusinessActorUseCase onboardBusinessActorUseCase;

    @Autowired
    private CreateOrganizationUseCase createOrganizationUseCase;

    @Autowired
    private CreateRoleUseCase createRoleUseCase;

    @Autowired
    private AssignRoleToUserUseCase assignRoleToUserUseCase;

    @MockBean
    private ReactiveStringRedisTemplate redisTemplate;

    @MockBean
    private ReactiveValueOperations<String, String> redisValueOperations;

    @BeforeEach
    void configureRedis() {
        reset(redisTemplate, redisValueOperations);
        when(redisTemplate.opsForValue()).thenReturn(redisValueOperations);
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(Mono.just(Boolean.TRUE));
        when(redisValueOperations.increment(anyString())).thenReturn(Mono.just(1L));
    }

    @Test
    void quotaKeyIncludesTenantClientAndServiceAndRejectsSecondRequest() {
        TestUser user = bootstrapUser("quota-sales", Set.of("organizations:write", "sales:write"));
        String organizationId = createOwnedOrganization(user, "ORG-QUOTA-" + UUID.randomUUID().toString().substring(0, 6));
        WebTestClient scopedClient = userClient(user).mutate()
                .defaultHeader("X-Organization-Id", organizationId)
                .build();

        reset(redisTemplate, redisValueOperations);
        when(redisTemplate.opsForValue()).thenReturn(redisValueOperations);
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(Mono.just(Boolean.TRUE));
        when(redisValueOperations.increment(anyString()))
                .thenReturn(Mono.just(1L), Mono.just(2L));

        scopedClient.get()
                .uri("/api/sales/orders/{orderId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().valueEquals("X-IWM-Quota-Scope", "tenant-client-service")
                .expectHeader().valueEquals("X-IWM-Quota-Client-Id", CLIENT_ID)
                .expectHeader().valueEquals("X-IWM-Quota-Service", "SALES")
                .expectHeader().valueEquals("X-IWM-Quota-Limit", "1")
                .expectHeader().valueEquals("X-IWM-Quota-Remaining", "0");

        scopedClient.get()
                .uri("/api/sales/orders/{orderId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectHeader().valueEquals("X-IWM-Quota-Service", "SALES")
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("TENANT_REQUEST_QUOTA_EXCEEDED");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisValueOperations, times(2)).increment(keyCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(keyCaptor.getAllValues())
                .allMatch(key -> key.contains(":" + TENANT_ID + ":" + CLIENT_ID + ":SALES:"));
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

        return new TestUser(userId.toString(), actorId.toString());
    }

    private String createOwnedOrganization(TestUser user, String code) {
        UUID tenantId = UUID.fromString(TENANT_ID);
        UUID userId = UUID.fromString(user.userId());
        UUID businessActorId = Objects.requireNonNull(onboardBusinessActorUseCase.onboard(new OnboardBusinessActorCommand(
                tenantId,
                userId,
                "Owner " + code,
                "BA-" + code,
                "NIU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                null,
                null,
                null,
                null,
                null,
                null)).block()).id();
        return Objects.requireNonNull(createOrganizationUseCase.createOrganization(new CreateOrganizationCommand(
                tenantId,
                businessActorId,
                code,
                "Legal " + code,
                "Display " + code,
                "PRIVATE_COMPANY")).block()).id().toString();
    }

    private WebTestClient userClient(TestUser user) {
        return webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", API_KEY)
                .defaultHeader("X-Tenant-Id", TENANT_ID)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userSessionTokenService.issue(
                        UUID.fromString(TENANT_ID),
                        UUID.fromString(user.userId()),
                        UUID.fromString(user.actorId())))
                .build();
    }

    private record TestUser(String userId, String actorId) {
    }
}
