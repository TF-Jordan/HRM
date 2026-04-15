package yowyob.comops.api.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.actor.application.port.in.CreateActorCommand;
import yowyob.comops.api.actor.application.port.in.CreateActorUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.kernel.application.port.in.RelayOutboxEventsUseCase;
import yowyob.comops.api.kernel.config.UserSessionTokenService;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
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
                "spring.elasticsearch.uris=http://localhost:9200",
                "iwm.search.elasticsearch.enabled=true",
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline",
                "iwm.outbox.relay.enabled=false"
        })
@AutoConfigureWebTestClient
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.search.enabled", matches = "true")
class SearchSmokeIntegrationTests {

    private static final String CLIENT_ID = "test-client";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RelayOutboxEventsUseCase relayOutboxEventsUseCase;

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
    void elasticsearchSearchEndpointsReturnIndexedOrganizationsProductsThirdPartiesAndResources() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "search", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "resources:write"));

        String orgSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String organizationCode = "ORG-SEARCH-" + orgSuffix;
        String sku = "SKU-SEARCH-" + orgSuffix;
        String thirdPartyRef = "TP-SEARCH-" + orgSuffix;
        String resourceCode = "RES-SEARCH-" + orgSuffix;

        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> agencyId = new AtomicReference<>();

        userClient(tenantId, user).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", UUID.randomUUID().toString(),
                        "code", organizationCode,
                        "legalName", "Legal " + organizationCode,
                        "displayName", "Display " + organizationCode,
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .bodyValue(Map.of(
                        "code", "AGY-" + orgSuffix,
                        "name", "Search Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> agencyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "partyType", "ACTOR",
                        "partyId", UUID.randomUUID().toString(),
                        "referenceCode", thirdPartyRef,
                        "displayName", "Search Customer " + orgSuffix,
                        "roles", new String[]{"CLIENT"},
                        "prospect", false,
                        "segment", "VIP_PIPELINE",
                        "qualificationScore", 92))
                .exchange()
                .expectStatus().isCreated();

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", sku,
                        "name", "Search Product " + orgSuffix,
                        "familyCode", "FAMILY-SEARCH",
                        "variantLabel", "STANDARD",
                        "unitPrice", 25.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated();

        userClient(tenantId, user).post()
                .uri("/api/resources")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "resourceCode", resourceCode,
                        "name", "Search Router " + orgSuffix,
                        "category", "network",
                        "serialNumber", "SN-" + orgSuffix,
                        "ipAddress", "10.20.30.40",
                        "macAddress", "aa:bb:cc:dd:ee:ff"))
                .exchange()
                .expectStatus().isCreated();

        Integer relayedCount = relayOutboxEventsUseCase.relayBatch(50).blockOptional().orElse(0);
        assertThat(relayedCount).isGreaterThanOrEqualTo(4);

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> userClient(tenantId, user).get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/organizations/search")
                                .queryParam("q", organizationCode)
                                .build())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.data.length()").isEqualTo(1)
                        .jsonPath("$.data[0].id").isEqualTo(organizationId.get())
                        .jsonPath("$.data[0].code").isEqualTo(organizationCode));

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> userClient(tenantId, user).get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/products/search")
                                .queryParam("organizationId", organizationId.get())
                                .queryParam("q", sku)
                                .build())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.data.length()").isEqualTo(1)
                        .jsonPath("$.data[0].sku").isEqualTo(sku));

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> userClient(tenantId, user).get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/third-parties/search")
                                .queryParam("organizationId", organizationId.get())
                                .queryParam("q", thirdPartyRef)
                                .queryParam("segment", "VIP_PIPELINE")
                                .build())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.data.length()").isEqualTo(1)
                        .jsonPath("$.data[0].referenceCode").isEqualTo(thirdPartyRef)
                        .jsonPath("$.data[0].segment").isEqualTo("VIP_PIPELINE")
                        .jsonPath("$.data[0].qualificationScore").isEqualTo(92));

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> userClient(tenantId, user).get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/resources/search")
                                .queryParam("organizationId", organizationId.get())
                                .queryParam("q", resourceCode)
                                .build())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.data.length()").isEqualTo(1)
                        .jsonPath("$.data[0].resourceCode").isEqualTo(resourceCode));
    }

    private WebTestClient systemClient(String tenantId) {
        return webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", "test-key")
                .defaultHeader("X-Tenant-Id", tenantId)
                .build();
    }

    private WebTestClient userClient(String tenantId, TestUser user) {
        return systemClient(tenantId).mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userSessionTokenService.issue(
                        UUID.fromString(tenantId),
                        UUID.fromString(user.userId()),
                        null))
                .build();
    }

    private TestUser bootstrapUser(String tenantId, String usernamePrefix, Set<String> permissions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = usernamePrefix + "." + suffix + "@example.com";
        String username = usernamePrefix + "-" + suffix;
        UUID tenant = UUID.fromString(tenantId);
        UUID actorId = Objects.requireNonNull(createActorUseCase.createActor(new CreateActorCommand(
                tenant,
                "Search",
                suffix,
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
                username,
                email,
                "Password!123",
                "LOCAL")).block()).id();
        UUID roleId = Objects.requireNonNull(createRoleUseCase.createRole(new CreateRoleCommand(
                tenant,
                "ROLE-" + suffix.toUpperCase(),
                "Role " + suffix,
                permissions)).block()).id();
        assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
                tenant,
                userId,
                roleId,
                "TENANT")).block();

        return new TestUser(userId.toString());
    }

    private record TestUser(String userId) {
    }
}
