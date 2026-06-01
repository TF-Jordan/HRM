package yowyob.comops.api.bootstrap;

import yowyob.comops.api.actor.application.port.in.CreateActorCommand;
import yowyob.comops.api.actor.application.port.in.CreateActorUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.kernel.config.UserSessionTokenService;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"iwm.file.storage.allowed-content-types[0]=text/plain"})
@AutoConfigureWebTestClient
@ActiveProfiles("test-memory")
class LegacyTiersExpansionIntegrationTests {

    private static final String CLIENT_ID = "test-client";
    private static final String API_KEY = "test-key";
    private static final String TENANT_ID = "22222222-2222-2222-2222-222222222222";

    @Autowired
    private WebTestClient webTestClient;

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

    @Test
    void tiersUsersSystemAuditAndFilesAreSupportedInMemory() {
        TestUser user = bootstrapUser("tiers-expansion", Set.of("organizations:write", "third-parties:write"));
        String organizationId = createOrganization(user, "ORG-TP-" + UUID.randomUUID().toString().substring(0, 6));
        AtomicReference<String> prospectId = new AtomicReference<>();
        AtomicReference<String> customerId = new AtomicReference<>();
        AtomicReference<String> fileId = new AtomicReference<>();

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().post()
                .uri("/api/prospects")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ACTOR",
                        "partyId", UUID.randomUUID().toString(),
                        "referenceCode", "PROS-" + UUID.randomUUID().toString().substring(0, 8),
                        "displayName", "Prospect Memory",
                        "accountingAccount", "411MEM",
                        "prospect", true))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> prospectId.set(value.toString()));

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().post()
                .uri("/api/prospects/{id}/bank-accounts", prospectId.get())
                .bodyValue(Map.of(
                        "label", "Primary",
                        "bankName", "BICEC",
                        "iban", "CM21MEM" + UUID.randomUUID().toString().substring(0, 10).toUpperCase(),
                        "swiftBic", "BICECMCX",
                        "currency", "XAF",
                        "primary", true))
                .exchange()
                .expectStatus().isCreated();

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().post()
                .uri("/api/prospects/{id}/convert", prospectId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.roles[?(@ == 'CUSTOMER')]").exists();

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().patch()
                .uri("/api/prospects/{id}/qualification", prospectId.get())
                .bodyValue(Map.of(
                        "segment", "VIP_PIPELINE",
                        "qualificationScore", 88))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.segment").isEqualTo("VIP_PIPELINE")
                .jsonPath("$.data.qualificationScore").isEqualTo(88);

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().patch()
                .uri("/api/prospects/{id}/follow-up/schedule", prospectId.get())
                .bodyValue(Map.of("nextFollowUpAt", "2031-01-15T10:15:30Z"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.followUpStatus").isEqualTo("SCHEDULED")
                .jsonPath("$.data.nextFollowUpAt").isEqualTo("2031-01-15T10:15:30Z");

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().patch()
                .uri("/api/prospects/{id}/follow-up/complete", prospectId.get())
                .bodyValue(Map.of(
                        "contactedAt", "2031-01-15T10:15:30Z",
                        "nextFollowUpAt", "2031-02-01T08:00:00Z"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.followUpStatus").isEqualTo("SCHEDULED")
                .jsonPath("$.data.lastContactedAt").isEqualTo("2031-01-15T10:15:30Z")
                .jsonPath("$.data.nextFollowUpAt").isEqualTo("2031-02-01T08:00:00Z");

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().post()
                .uri("/api/customers")
                .bodyValue(Map.of(
                        "organizationId", organizationId,
                        "partyType", "ORGANIZATION",
                        "partyId", UUID.randomUUID().toString(),
                        "referenceCode", "CUS-" + UUID.randomUUID().toString().substring(0, 8),
                        "displayName", "Customer Memory",
                        "accountingAccount", "411CUS",
                        "active", true,
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> customerId.set(value.toString()));

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().get()
                .uri(uriBuilder -> uriBuilder.path("/api/prospects/statistics/conversions")
                        .queryParam("organizationId", organizationId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isEqualTo(1);

        userClient(user).get()
                .uri("/api/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.plan").isEqualTo("FREE_TIER");

        userClient(user).put()
                .uri("/api/users/me/plan")
                .bodyValue(Map.of("plan", "FREELANCE"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.plan").isEqualTo("FREELANCE");

        userClient(user).put()
                .uri("/api/users/me/onboarding")
                .bodyValue(Map.of("step", 2, "status", "IN_PROGRESS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.onboardingStep").isEqualTo(2)
                .jsonPath("$.data.onboardingStatus").isEqualTo("IN_PROGRESS");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource("hello-memory".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "hello.txt";
            }
        }).contentType(MediaType.TEXT_PLAIN);

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().post()
                .uri("/api/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> fileId.set(value.toString()));

        userClient(user).get()
                .uri(uriBuilder -> uriBuilder.path("/api/system-audits/me").queryParam("limit", 20).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
.jsonPath("$.data.length()").value(Integer.class, length -> org.assertj.core.api.Assertions.assertThat(length).isGreaterThan(0));

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().get()
                .uri(uriBuilder -> uriBuilder.path("/api/system-audits/organization").queryParam("limit", 20).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
.jsonPath("$.data.length()").value(Integer.class, length -> org.assertj.core.api.Assertions.assertThat(length).isGreaterThan(0));

        userClient(user).get()
                .uri("/api/files/{id}", fileId.get())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN);

        MultipartBodyBuilder invalidBuilder = new MultipartBodyBuilder();
        invalidBuilder.part("file", new ByteArrayResource("{\"a\":1}".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "../bad.json";
            }
        }).contentType(MediaType.APPLICATION_JSON);

        userClient(user).mutate().defaultHeader("X-Organization-Id", organizationId).build().post()
                .uri("/api/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(invalidBuilder.build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INVALID_STORED_FILE");
    }

    private WebTestClient systemClient() {
        return webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", API_KEY)
                .defaultHeader("X-Tenant-Id", TENANT_ID)
                .build();
    }

    private TestUser bootstrapUser(String alias, Set<String> permissions) {
        String email = alias + "." + UUID.randomUUID() + "@example.com";
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
                "Password!123",
                "LOCAL")).block()).id();

        if (!permissions.isEmpty()) {
            UUID roleId = Objects.requireNonNull(createRoleUseCase.createRole(new CreateRoleCommand(
                    tenantId,
                    "ROLE-" + alias.toUpperCase(),
                    alias + " role",
                    permissions)).block()).id();
            assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
                    tenantId,
                    userId,
                    roleId,
                    "GLOBAL")).block();
        }
        return new TestUser(userId.toString(), actorId.toString());
    }

    private WebTestClient userClient(TestUser user) {
        return systemClient().mutate()
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

    private record TestUser(String userId, String actorId) {
    }
}
