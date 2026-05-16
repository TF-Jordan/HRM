package yowyob.comops.api.bootstrap;

import yowyob.comops.api.actor.application.port.in.CreateActorCommand;
import yowyob.comops.api.actor.application.port.in.CreateActorUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.r2dbc.url=r2dbc:postgresql://localhost:54329/iwm",
                "spring.r2dbc.username=iwm",
                "spring.r2dbc.password=iwm",
                "spring.liquibase.url=jdbc:postgresql://localhost:54329/iwm",
                "spring.liquibase.user=iwm",
                "spring.liquibase.password=iwm",
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline"
        })
@AutoConfigureWebTestClient
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.r2dbc.enabled", matches = "true")
class LegacyTiersExpansionR2dbcIntegrationTests extends AbstractContractR2dbcIntegrationTest {

    private static final String CLIENT_ID = "test-client";

    @Autowired
    private CreateActorUseCase createActorUseCase;

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private CreateRoleUseCase createRoleUseCase;

    @Autowired
    private AssignRoleToUserUseCase assignRoleToUserUseCase;

    @Test
    void tiersAndUserSelfServiceWorkAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "tiers-r2dbc", Set.of("organizations:write", "third-parties:write"));
        String organizationId = createOrganization(tenantId, user, "ORG-R2DBC-" + UUID.randomUUID().toString().substring(0, 6));
        AtomicReference<String> prospectId = new AtomicReference<>();

        userClient(tenantId, user.userId()).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, bearer(tenantId, user))
                .defaultHeader("X-Organization-Id", organizationId)
                .build()
                .post()
                .uri("/api/prospects")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ACTOR",
                        "partyId", UUID.randomUUID().toString(),
                        "referenceCode", "PR2-" + UUID.randomUUID().toString().substring(0, 8),
                        "displayName", "Prospect PG",
                        "accountingAccount", "411PG",
                        "prospect", true))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> prospectId.set(value.toString()));

        userClient(tenantId, user.userId()).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, bearer(tenantId, user))
                .defaultHeader("X-Organization-Id", organizationId)
                .build()
                .post()
                .uri("/api/prospects/{id}/convert", prospectId.get())
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user.userId()).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, bearer(tenantId, user))
                .build()
                .put()
                .uri("/api/users/me/plan")
                .bodyValue(Map.of("plan", "PROFESSIONAL"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.plan").isEqualTo("PROFESSIONAL");

        userClient(tenantId, user.userId()).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, bearer(tenantId, user))
                .defaultHeader("X-Organization-Id", organizationId)
                .build()
                .patch()
                .uri("/api/prospects/{id}/qualification", prospectId.get())
                .bodyValue(Map.of("segment", "B2B_PREMIUM", "qualificationScore", 91))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.segment").isEqualTo("B2B_PREMIUM")
                .jsonPath("$.data.qualificationScore").isEqualTo(91);

        userClient(tenantId, user.userId()).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, bearer(tenantId, user))
                .defaultHeader("X-Organization-Id", organizationId)
                .build()
                .patch()
                .uri("/api/prospects/{id}/follow-up/schedule", prospectId.get())
                .bodyValue(Map.of("nextFollowUpAt", "2031-03-01T09:00:00Z"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.followUpStatus").isEqualTo("SCHEDULED")
                .jsonPath("$.data.nextFollowUpAt").isEqualTo("2031-03-01T09:00:00Z");

        userClient(tenantId, user.userId()).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, bearer(tenantId, user))
                .defaultHeader("X-Organization-Id", organizationId)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/system-audits/organization").queryParam("limit", 20).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
.jsonPath("$.data.length()").value(Integer.class, length -> org.assertj.core.api.Assertions.assertThat(length).isGreaterThan(0));
    }

    private TestUser bootstrapUser(String tenantId, String alias, Set<String> permissions) {
        String email = alias + "." + UUID.randomUUID() + "@example.com";
        UUID tenant = UUID.fromString(tenantId);
        UUID actorId = Objects.requireNonNull(createActorUseCase.createActor(new CreateActorCommand(
                tenant,
                "Pg",
                alias,
                null,
                email,
                null,
                null,
                null,
                null,
                null)).block()).id();
        UUID userId = Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                tenant,
                actorId,
                alias,
                email,
                "Password!123",
                "LOCAL")).block()).id();
        UUID roleId = Objects.requireNonNull(createRoleUseCase.createRole(new CreateRoleCommand(
                tenant,
                "ROLE-" + alias.toUpperCase(),
                alias + " role",
                permissions)).block()).id();
        assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
                tenant,
                userId,
                roleId,
                "GLOBAL")).block();

        return new TestUser(userId.toString(), actorId.toString());
    }

    private WebTestClient systemClient(String tenantId) {
        return webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", API_KEY)
                .defaultHeader("X-Tenant-Id", tenantId)
                .build();
    }

    private String createOrganization(String tenantId, TestUser user, String code) {
        AtomicReference<String> organizationId = new AtomicReference<>();
        userClient(tenantId, user.userId()).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, bearer(tenantId, user))
                .build()
                .post()
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

    private String bearer(String tenantId, TestUser user) {
        return "Bearer " + userSessionTokenService.issue(UUID.fromString(tenantId), UUID.fromString(user.userId()),
                UUID.fromString(user.actorId()));
    }

    private record TestUser(String userId, String actorId) {
    }
}
