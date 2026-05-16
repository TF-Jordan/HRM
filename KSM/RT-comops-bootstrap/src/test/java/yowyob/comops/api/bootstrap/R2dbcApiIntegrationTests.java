package yowyob.comops.api.bootstrap;

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
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
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
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline"
        })
@AutoConfigureWebTestClient
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.r2dbc.enabled", matches = "true")
class R2dbcApiIntegrationTests {

    private static final String CLIENT_ID = "test-client";
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private RelayOutboxEventsUseCase relayOutboxEventsUseCase;

    @Autowired
    private DomainEventProjectionRepository domainEventProjectionRepository;

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
    void actorCanBeCreatedAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        String email = "actor-" + UUID.randomUUID() + "@example.com";
        systemClient(tenantId).post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Grace",
                        "lastName", "Hopper",
                        "email", email))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.email").isEqualTo(email);
    }

    @Test
    void legacyParityIdentityOrganizationFlowWorksAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        String loginPassword = "Password!123";
        String loginEmail = "parity.pg." + UUID.randomUUID() + "@example.com";
        AtomicReference<String> actorId = new AtomicReference<>();
        AtomicReference<String> userId = new AtomicReference<>();
        AtomicReference<String> roleId = new AtomicReference<>();
        AtomicReference<String> businessActorId = new AtomicReference<>();
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> invitedEmail = new AtomicReference<>();
        AtomicReference<String> invitedActorId = new AtomicReference<>();

        systemClient(tenantId).post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Parity",
                        "lastName", "Pg",
                        "email", loginEmail))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> actorId.set(value.toString()));

        UUID tenant = UUID.fromString(tenantId);
        UUID registeredUserId = Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                tenant,
                UUID.fromString(actorId.get()),
                "parity-pg",
                loginEmail,
                loginPassword,
                "LOCAL")).block()).id();
        userId.set(registeredUserId.toString());

        UUID createdRoleId = Objects.requireNonNull(createRoleUseCase.createRole(new CreateRoleCommand(
                tenant,
                "PARITY-PG-ORG",
                "Parity Pg Org",
                Set.of("organizations:write"))).block()).id();
        roleId.set(createdRoleId.toString());
        assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
                tenant,
                registeredUserId,
                createdRoleId,
                "GLOBAL")).block();

        webTestClient.post()
                .uri("/api/auth/login")
                .header("X-Client-Id", CLIENT_ID)
                .header("X-Api-Key", "test-key")
                .header("X-Tenant-Id", tenantId)
                .bodyValue(Map.of(
                        "principal", "parity-pg",
                        "password", loginPassword))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(userId.get())
                .jsonPath("$.data.tokenType").isEqualTo("Bearer")
                .jsonPath("$.data.sessionToken").value(value -> {
                    String token = value.toString();
                    org.assertj.core.api.Assertions.assertThat(token.split("\\.")).hasSize(3);
                    yowyob.comops.api.kernel.config.UserSessionTokenClaims claims =
                            userSessionTokenService.verify(token).orElseThrow();
                    org.assertj.core.api.Assertions.assertThat(claims.tenantId())
                            .isEqualTo(UUID.fromString(tenantId));
                    org.assertj.core.api.Assertions.assertThat(claims.userId())
                            .isEqualTo(UUID.fromString(userId.get()));
                    org.assertj.core.api.Assertions.assertThat(claims.actorId())
                            .isEqualTo(UUID.fromString(actorId.get()));
                });

        TestUser manager = new TestUser(userId.get(), actorId.get());

        userClient(tenantId, manager).post()
                .uri("/api/actors/onboarding")
                .bodyValue(Map.of(
                        "name", "Parity PG Actor",
                        "businessId", "BA-PG-01"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> businessActorId.set(value.toString()));

        userClient(tenantId, manager).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", businessActorId.get(),
                        "code", "ORG-PG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "legalName", "Parity PG Legal",
                        "displayName", "Parity PG Display",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));

        userClient(tenantId, manager).mutate()
                .defaultHeader("X-Organization-Id", organizationId.get())
                .build()
                .post()
                .uri("/api/warehouses")
                .bodyValue(Map.of(
                        "code", "WH-PG-01",
                        "name", "Warehouse PG",
                        "agencyType", "WAREHOUSE"))
                .exchange()
                .expectStatus().isCreated();

        systemClient(tenantId).post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Employee",
                        "lastName", "Pg",
                        "email", "employee.pg." + UUID.randomUUID() + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.email").value(value -> invitedEmail.set(value.toString()))
                .jsonPath("$.data.id").value(value -> invitedActorId.set(value.toString()));

        registerUserUseCase.register(new RegisterUserCommand(
                UUID.fromString(tenantId),
                UUID.fromString(invitedActorId.get()),
                "employee-pg-" + UUID.randomUUID().toString().substring(0, 8),
                invitedEmail.get(),
                "Password!456",
                "LOCAL")).block();

        userClient(tenantId, manager).post()
                .uri(uriBuilder -> uriBuilder.path("/api/employees/invite")
                        .queryParam("organizationId", organizationId.get())
                        .build())
                .bodyValue(Map.of("email", invitedEmail.get(), "roleId", roleId.get()))
                .exchange()
                .expectStatus().isCreated();

        userClient(tenantId, manager).get()
                .uri(uriBuilder -> uriBuilder.path("/api/employees")
                        .queryParam("organizationId", organizationId.get())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1);
    }

    @Test
    void organizationAndProductCanBeCreatedAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "orgprod", Set.of("organizations:write", "products:write"));
        String organizationCode = "ORG-" + UUID.randomUUID().toString().substring(0, 8);
        String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 8);
        AtomicReference<String> organizationId = new AtomicReference<>();

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
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", sku,
                        "name", "Product " + sku,
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 19.99,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.sku").isEqualTo(sku.toUpperCase())
                .jsonPath("$.data.organizationId").isEqualTo(organizationId.get());
    }

    @Test
    void salesOrderNumberCanBeGeneratedAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "salespg", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "settings:write",
                "sales:write"));
        String organizationCode = "ORG-" + UUID.randomUUID().toString().substring(0, 8);
        String agencyId = UUID.randomUUID().toString();
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> thirdPartyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();

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
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "partyType", "ACTOR",
                        "partyId", UUID.randomUUID().toString(),
                        "referenceCode", "TP-" + UUID.randomUUID().toString().substring(0, 8),
                        "displayName", "R2DBC Customer",
                        "roles", new String[]{"CLIENT"},
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> thirdPartyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Generated Product",
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/settings/document-sequences")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId,
                        "documentType", "SALES_ORDER",
                        "prefix", "SO-",
                        "paddingWidth", 4,
                        "nextNumber", 1))
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/sales/orders")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId,
                        "customerThirdPartyId", thirdPartyId.get(),
                        "productId", productId.get(),
                        "quantity", 2.0,
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.orderNumber").isEqualTo("SO-0001");
    }

    @Test
    void invoiceLinesArePersistedAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "accpg", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "accounting:write"));
        String organizationCode = "ORG-" + UUID.randomUUID().toString().substring(0, 8);
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> thirdPartyId = new AtomicReference<>();
        AtomicReference<String> firstProductId = new AtomicReference<>();
        AtomicReference<String> secondProductId = new AtomicReference<>();
        AtomicReference<String> invoiceId = new AtomicReference<>();

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
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "partyType", "ACTOR",
                        "partyId", UUID.randomUUID().toString(),
                        "referenceCode", "TP-" + UUID.randomUUID().toString().substring(0, 8),
                        "displayName", "R2DBC Customer",
                        "roles", new String[]{"CLIENT"},
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> thirdPartyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Invoice Product 1",
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> firstProductId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Invoice Product 2",
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 30.00,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> secondProductId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/accounting/invoices")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "customerThirdPartyId", thirdPartyId.get(),
                        "invoiceNumber", "INV-" + UUID.randomUUID().toString().substring(0, 8),
                        "currency", "EUR",
                        "lines", List.of(
                                Map.of("productId", firstProductId.get(), "quantity", 2.0, "unitPrice", 10.0),
                                Map.of("productId", secondProductId.get(), "quantity", 1.0, "unitPrice", 25.0))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> invoiceId.set(value.toString()))
                .jsonPath("$.data.lines.length()").isEqualTo(2)
                .jsonPath("$.data.totalAmount").isEqualTo(45);

        userClient(tenantId, user).get()
                .uri("/api/accounting/invoices/{invoiceId}", invoiceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.lines.length()").isEqualTo(2)
                .jsonPath("$.data.totalQuantity").isEqualTo(3);
    }

    @Test
    void resourceCanBeRegisteredAndReadAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "respg", Set.of("organizations:write", "resources:write"));
        String organizationCode = "ORG-" + UUID.randomUUID().toString().substring(0, 8);
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> agencyId = new AtomicReference<>();
        AtomicReference<String> resourceId = new AtomicReference<>();
        AtomicReference<String> reservationId = new AtomicReference<>();

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
                        "code", "AGY-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Resource Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> agencyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/resources")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "resourceCode", "res-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Real DB Router",
                        "category", "network",
                        "serialNumber", "sn-" + UUID.randomUUID().toString().substring(0, 8),
                        "latitude", 4.05,
                        "longitude", 9.70,
                        "ipAddress", "10.10.0.20",
                        "macAddress", "aa:bb:cc:dd:ee:11"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> resourceId.set(value.toString()))
                .jsonPath("$.data.category").isEqualTo("NETWORK")
                .jsonPath("$.data.macAddress").isEqualTo("AA:BB:CC:DD:EE:11");

        userClient(tenantId, user).get()
                .uri("/api/resources/{resourceId}", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("AVAILABLE");

        userClient(tenantId, user).post()
                .uri("/api/resources/{resourceId}/reservations", resourceId.get())
                .bodyValue(Map.of(
                        "reserveeType", "AGENCY",
                        "reserveeId", agencyId.get(),
                        "reason", "Initial deployment"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("RESERVED");

        userClient(tenantId, user).get()
                .uri("/api/resources/{resourceId}/reservations", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].id").value(value -> reservationId.set(value.toString()))
                .jsonPath("$.data[0].status").isEqualTo("ACTIVE");

        userClient(tenantId, user).post()
                .uri("/api/resources/{resourceId}/assignments", resourceId.get())
                .bodyValue(Map.of(
                        "assigneeType", "AGENCY",
                        "assigneeId", agencyId.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("ASSIGNED");

        userClient(tenantId, user).post()
                .uri("/api/resources/{resourceId}/network-observations", resourceId.get())
                .bodyValue(Map.of(
                        "ipAddress", "10.10.0.30",
                        "macAddress", "aa:bb:cc:dd:ee:22"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.macAddress").isEqualTo("AA:BB:CC:DD:EE:22");

        userClient(tenantId, user).post()
                .uri("/api/resources/{resourceId}/unassign", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("AVAILABLE");

        userClient(tenantId, user).get()
                .uri("/api/resources/{resourceId}/assignments", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("CLOSED");

        userClient(tenantId, user).get()
                .uri("/api/resources/{resourceId}/reservations", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].status").isEqualTo("FULFILLED");

        userClient(tenantId, user).post()
                .uri("/api/resources/{resourceId}/dispose", resourceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("DISPOSED");
    }

    @Test
    void confirmedSalesOrdersCanGenerateInvoicesAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "sapg", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "inventory:write",
                "sales:write",
                "accounting:write",
                "settings:write"));
        String organizationCode = "ORG-" + UUID.randomUUID().toString().substring(0, 8);
        String agencyCode = "AGY-" + UUID.randomUUID().toString().substring(0, 6);
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> agencyId = new AtomicReference<>();
        AtomicReference<String> thirdPartyId = new AtomicReference<>();
        AtomicReference<String> firstProductId = new AtomicReference<>();
        AtomicReference<String> secondProductId = new AtomicReference<>();
        AtomicReference<String> orderId = new AtomicReference<>();

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
                        "code", agencyCode,
                        "name", "Sales Agency",
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
                        "referenceCode", "TP-" + UUID.randomUUID().toString().substring(0, 8),
                        "displayName", "R2DBC Customer",
                        "roles", new String[]{"CLIENT"},
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> thirdPartyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Sales Product 1",
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 12.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> firstProductId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Sales Product 2",
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 30.00,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> secondProductId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/settings/document-sequences")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "documentType", "SALES_INVOICE",
                        "prefix", "INV-R2-",
                        "paddingWidth", 4,
                        "nextNumber", 1))
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/inventory/movements")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "productId", firstProductId.get(),
                        "referenceNumber", "MOV-IN-" + UUID.randomUUID().toString().substring(0, 8),
                        "movementType", "INBOUND",
                        "quantity", 5.0))
                .exchange()
                .expectStatus().isCreated();

        userClient(tenantId, user).post()
                .uri("/api/inventory/movements")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "productId", secondProductId.get(),
                        "referenceNumber", "MOV-IN-" + UUID.randomUUID().toString().substring(0, 8),
                        "movementType", "INBOUND",
                        "quantity", 5.0))
                .exchange()
                .expectStatus().isCreated();

        userClient(tenantId, user).post()
                .uri("/api/sales/orders")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "customerThirdPartyId", thirdPartyId.get(),
                        "orderNumber", "SO-" + UUID.randomUUID().toString().substring(0, 8),
                        "currency", "EUR",
                        "lines", List.of(
                                Map.of("productId", firstProductId.get(), "quantity", 2.0, "unitPrice", 10.0),
                                Map.of("productId", secondProductId.get(), "quantity", 1.0, "unitPrice", 25.0))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> orderId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/sales/orders/{orderId}/confirm", orderId.get())
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).get()
                .uri(uriBuilder -> uriBuilder.path("/api/inventory/movements/balance")
                        .queryParam("organizationId", organizationId.get())
                        .queryParam("agencyId", agencyId.get())
                        .queryParam("productId", firstProductId.get())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.onHandQuantity").isEqualTo(3);

        userClient(tenantId, user).post()
                .uri("/api/accounting/invoices/from-orders/{orderId}", orderId.get())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.orderId").isEqualTo(orderId.get())
                .jsonPath("$.data.lines.length()").isEqualTo(2)
                .jsonPath("$.data.invoiceNumber").isEqualTo("INV-R2-0001");

        List<String> eventTypes = outboxEvents(tenantId).stream()
                .map(OutboxEvent::eventType)
                .toList();

        org.assertj.core.api.Assertions.assertThat(eventTypes).contains(
                "STOCK_MOVEMENT_RECORDED",
                "SALES_ORDER_STOCK_DISPATCHED",
                "SALES_ORDER_CREATED",
                "SALES_ORDER_CONFIRMED",
                "INVOICE_CREATED");

        Integer relayedCount = relayOutboxEventsUseCase.relayBatch(100)
                .blockOptional()
                .orElse(0);

        org.assertj.core.api.Assertions.assertThat(relayedCount).isGreaterThanOrEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(outboxEvents(tenantId))
                .filteredOn(event -> event.publishedAt() != null)
                .extracting(OutboxEvent::eventType)
                .contains(
                        "STOCK_MOVEMENT_RECORDED",
                        "SALES_ORDER_STOCK_DISPATCHED",
                        "SALES_ORDER_CREATED",
                        "SALES_ORDER_CONFIRMED",
                        "INVOICE_CREATED");
        org.assertj.core.api.Assertions.assertThat(domainEventProjections(tenantId))
                .extracting(DomainEventProjection::domainType)
                .contains("SALES", "ACCOUNTING");
    }

    @Test
    void postedInvoicesCanBeSettledAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "settlepg", Set.of(
                "organizations:write",
                "third-parties:write",
                "products:write",
                "inventory:write",
                "sales:write",
                "accounting:write",
                "settings:write",
                "treasury:manage"));
        String organizationCode = "ORG-" + UUID.randomUUID().toString().substring(0, 8);
        String agencyCode = "AGY-" + UUID.randomUUID().toString().substring(0, 6);
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> agencyId = new AtomicReference<>();
        AtomicReference<String> thirdPartyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> orderId = new AtomicReference<>();
        AtomicReference<String> invoiceId = new AtomicReference<>();
        AtomicReference<String> bankAccountId = new AtomicReference<>();

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
                        "code", agencyCode,
                        "name", "Settlement Agency",
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
                        "referenceCode", "TP-" + UUID.randomUUID().toString().substring(0, 8),
                        "displayName", "Settlement Customer",
                        "roles", new String[]{"CLIENT"},
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> thirdPartyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Settlement Product",
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 15.00,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/settings/document-sequences")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "documentType", "SALES_INVOICE",
                        "prefix", "INV-STL-",
                        "paddingWidth", 4,
                        "nextNumber", 1))
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/settings/document-sequences")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "documentType", "STOCK_MOVEMENT",
                        "prefix", "MOV-STL-",
                        "paddingWidth", 4,
                        "nextNumber", 1))
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/settings/document-sequences")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "documentType", "SALES_ORDER",
                        "prefix", "ORD-STL-",
                        "paddingWidth", 4,
                        "nextNumber", 1))
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/settings/document-sequences")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "documentType", "INVOICE_SETTLEMENT",
                        "prefix", "SET-STL-",
                        "paddingWidth", 4,
                        "nextNumber", 1))
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/inventory/movements")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "productId", productId.get(),
                        "movementType", "INBOUND",
                        "quantity", 5.0))
                .exchange()
                .expectStatus().isCreated();

        userClient(tenantId, user).post()
                .uri("/api/sales/orders")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "customerThirdPartyId", thirdPartyId.get(),
                        "productId", productId.get(),
                        "quantity", 2.0,
                        "unitPrice", 15.0,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> orderId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/sales/orders/{orderId}/confirm", orderId.get())
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/accounting/invoices/from-orders/{orderId}", orderId.get())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> invoiceId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/accounting/invoices/{invoiceId}/post", invoiceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("POSTED");

        userClient(tenantId, user).post()
                .uri("/api/treasury/bank-accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankName", "Settlement Postgres Bank",
                        "accountNumber", "ACC-" + UUID.randomUUID().toString().substring(0, 8),
                        "iban", "FR7630006000011234567890192",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/treasury/bank-accounts/invoice-settlements")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankAccountId", bankAccountId.get(),
                        "invoiceId", invoiceId.get(),
                        "paymentMethod", "BANK_TRANSFER",
                        "amount", 30.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.settlementNumber").isEqualTo("SET-STL-0001");

        userClient(tenantId, user).get()
                .uri("/api/accounting/invoices/{invoiceId}", invoiceId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.paymentStatus").isEqualTo("PAID")
                .jsonPath("$.data.settledAmount").isEqualTo(30)
                .jsonPath("$.data.outstandingAmount").isEqualTo(0);

        org.assertj.core.api.Assertions.assertThat(outboxEvents(tenantId).stream()
                        .map(OutboxEvent::eventType)
                        .toList())
                .contains("INVOICE_POSTED", "INVOICE_SETTLEMENT_REGISTERED", "INVOICE_SETTLEMENT_APPLIED");
    }

    @Test
    void inventoryAndTreasuryEventsArePersistedToOutboxAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "evtpg", Set.of(
                "organizations:write",
                "products:write",
                "inventory:write",
                "treasury:manage"));
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> sourceAgencyId = new AtomicReference<>();
        AtomicReference<String> targetAgencyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> transferId = new AtomicReference<>();
        AtomicReference<String> bankAccountId = new AtomicReference<>();
        AtomicReference<String> statementId = new AtomicReference<>();
        AtomicReference<String> reconciliationId = new AtomicReference<>();
        String organizationCode = "ORG-" + UUID.randomUUID().toString().substring(0, 8);

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
                        "code", "AGY-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Inventory Source Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> sourceAgencyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .bodyValue(Map.of(
                        "code", "AGY-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Inventory Target Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> targetAgencyId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8),
                        "name", "Inventory Event Product",
                        "familyCode", "FAMILY-R2DBC",
                        "variantLabel", "STANDARD",
                        "unitPrice", 9.50,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/inventory/movements")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", sourceAgencyId.get(),
                        "productId", productId.get(),
                        "referenceNumber", "MOV-" + UUID.randomUUID().toString().substring(0, 8),
                        "movementType", "INBOUND",
                        "quantity", 6.0))
                .exchange()
                .expectStatus().isCreated();

        userClient(tenantId, user).post()
                .uri("/api/inventory/transfers")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sourceAgencyId", sourceAgencyId.get(),
                        "targetAgencyId", targetAgencyId.get(),
                        "productId", productId.get(),
                        "referenceNumber", "WHT-" + UUID.randomUUID().toString().substring(0, 8),
                        "quantity", 2.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> transferId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/inventory/transfers/{transferId}/complete", transferId.get())
                .exchange()
                .expectStatus().isOk();

        userClient(tenantId, user).post()
                .uri("/api/treasury/bank-accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankName", "Postgres Event Bank",
                        "accountNumber", "ACC-" + UUID.randomUUID().toString().substring(0, 8),
                        "iban", "FR7630006000011234567890187",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/treasury/statements")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankAccountId", bankAccountId.get(),
                        "statementNumber", "STM-" + UUID.randomUUID().toString().substring(0, 8),
                        "statementDate", "2026-03-08",
                        "openingBalance", 200.0,
                        "closingBalance", 260.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> statementId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/treasury/bank-accounts/reconciliations")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankAccountId", bankAccountId.get(),
                        "statementId", statementId.get(),
                        "referenceNumber", "REC-" + UUID.randomUUID().toString().substring(0, 8)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> reconciliationId.set(value.toString()));

        userClient(tenantId, user).post()
                .uri("/api/treasury/bank-accounts/reconciliations/{reconciliationId}/close", reconciliationId.get())
                .exchange()
                .expectStatus().isOk();

        List<String> eventTypes = outboxEvents(tenantId).stream()
                .map(OutboxEvent::eventType)
                .toList();

        org.assertj.core.api.Assertions.assertThat(eventTypes).contains(
                "STOCK_MOVEMENT_RECORDED",
                "WAREHOUSE_TRANSFER_CREATED",
                "WAREHOUSE_TRANSFER_COMPLETED",
                "BANK_STATEMENT_REGISTERED",
                "RECONCILIATION_OPENED",
                "RECONCILIATION_CLOSED");

        Integer relayedCount = relayOutboxEventsUseCase.relayBatch(100)
                .blockOptional()
                .orElse(0);

        org.assertj.core.api.Assertions.assertThat(relayedCount).isGreaterThanOrEqualTo(6);
        org.assertj.core.api.Assertions.assertThat(outboxEvents(tenantId))
                .filteredOn(event -> event.publishedAt() != null)
                .extracting(OutboxEvent::eventType)
                .contains(
                        "STOCK_MOVEMENT_RECORDED",
                        "WAREHOUSE_TRANSFER_CREATED",
                        "WAREHOUSE_TRANSFER_COMPLETED",
                        "BANK_STATEMENT_REGISTERED",
                        "RECONCILIATION_OPENED",
                        "RECONCILIATION_CLOSED");
        org.assertj.core.api.Assertions.assertThat(domainEventProjections(tenantId))
                .extracting(DomainEventProjection::domainType)
                .contains("INVENTORY", "TREASURY");
    }

    @Test
    void administrationCoreWorksAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser adminUser = bootstrapUser(tenantId, "adminpg", Set.of(
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
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> agencyId = new AtomicReference<>();
        AtomicReference<String> roleId = new AtomicReference<>();
        AtomicReference<String> clonedRoleId = new AtomicReference<>();
        AtomicReference<String> assignmentId = new AtomicReference<>();
        AtomicReference<String> targetActorId = new AtomicReference<>();
        AtomicReference<String> targetUserId = new AtomicReference<>();
        AtomicReference<String> targetBusinessActorId = new AtomicReference<>();
        String targetEmail = "admin.target." + UUID.randomUUID() + "@example.com";

        userClient(tenantId, adminUser).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", UUID.randomUUID().toString(),
                        "code", "ORG-ADM-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "legalName", "Admin PG Legal",
                        "displayName", "Admin PG Display",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));

        WebTestClient scopedClient = userClient(tenantId, adminUser).mutate()
                .defaultHeader("X-Organization-Id", organizationId.get())
                .build();

        systemClient(tenantId).post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Admin",
                        "lastName", "Target",
                        "email", targetEmail))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> targetActorId.set(value.toString()));

        targetUserId.set(Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                UUID.fromString(tenantId),
                UUID.fromString(targetActorId.get()),
                "admin-target-" + UUID.randomUUID().toString().substring(0, 8),
                targetEmail,
                "Password!123",
                "LOCAL")).block()).id().toString());

        userClient(tenantId, new TestUser(targetUserId.get(), targetActorId.get())).post()
                .uri("/api/actors/onboarding")
                .bodyValue(Map.of(
                        "name", "Target Business Actor PG",
                        "businessId", "BAPG-" + UUID.randomUUID().toString().substring(0, 8)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> targetBusinessActorId.set(value.toString()));

        userClient(tenantId, adminUser).post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .bodyValue(Map.of(
                        "code", "AGY-ADM-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Admin PG Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> agencyId.set(value.toString()));

        scopedClient.get()
                .uri("/api/administration/permissions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[?(@.code == 'administration:permissions:read')]").exists()
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
                        "code", "OPS-PG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "name", "Operations PG",
                        "scopeType", "ORGANIZATION",
                        "permissions", List.of("products:write", "inventory:write", "hrm:employee:read",
                                "blockchain:block:read")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> roleId.set(value.toString()));

        scopedClient.post()
                .uri("/api/administration/roles/{roleId}/clone", roleId.get())
                .bodyValue(Map.of(
                        "code", "OPS-CPG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "name", "Operations PG Clone"))
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
                .uri("/api/administration/users/{userId}/roles", targetUserId.get())
                .bodyValue(Map.of(
                        "roleId", roleId.get(),
                        "scope", "ORGANIZATION"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> assignmentId.set(value.toString()));

        scopedClient.get()
                .uri("/api/administration/users/{userId}/roles", targetUserId.get())
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
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/administration/governance/business-actors/{businessActorId}", targetBusinessActorId.get())
                .bodyValue(Map.of(
                        "action", "approve",
                        "reason", "postgres approved"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED");

        scopedClient.post()
                .uri("/api/administration/governance/organizations/{organizationId}", organizationId.get())
                .bodyValue(Map.of(
                        "action", "approve",
                        "reason", "postgres governance approved"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED");

        scopedClient.post()
                .uri("/api/administration/governance/agencies/{agencyId}", agencyId.get())
                .bodyValue(Map.of(
                        "action", "suspend",
                        "reason", "postgres maintenance"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("SUSPENDED");

        scopedClient.get()
                .uri("/api/administration/audit")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").value(value -> org.assertj.core.api.Assertions.assertThat((Integer) value).isGreaterThanOrEqualTo(6));

        scopedClient.delete()
                .uri("/api/administration/users/{userId}/roles/{assignmentId}", targetUserId.get(), assignmentId.get())
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
    void legacyParitySettingsInventoryAndBankingRoutesWorkAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser user = bootstrapUser(tenantId, "legacypg", Set.of(
                "organizations:write",
                "products:write",
                "settings:write",
                "inventory:write",
                "treasury:manage"));
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> agencyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();
        AtomicReference<String> inventorySessionId = new AtomicReference<>();
        AtomicReference<String> bankAccountId = new AtomicReference<>();
        AtomicReference<String> statementId = new AtomicReference<>();
        AtomicReference<String> transactionId = new AtomicReference<>();

        userClient(tenantId, user).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", UUID.randomUUID().toString(),
                        "code", "ORG-LPG-" + UUID.randomUUID().toString().substring(0, 6),
                        "legalName", "Legacy PG Legal",
                        "displayName", "Legacy PG Display",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));

        WebTestClient scopedClient = userClient(tenantId, user).mutate()
                .defaultHeader("X-Organization-Id", organizationId.get())
                .build();

        scopedClient.post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .bodyValue(Map.of(
                        "code", "AGY-LPG-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Legacy PG Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> agencyId.set(value.toString()));

        scopedClient.get()
                .uri("/api/generalOptions")
                .exchange()
                .expectStatus().isOk();

        scopedClient.put()
                .uri("/api/generalOptions")
                .bodyValue(Map.ofEntries(
                        Map.entry("agencyId", agencyId.get()),
                        Map.entry("negotiateSellingPrice", true),
                        Map.entry("sellingPriceIncludeVat", false),
                        Map.entry("authorizeExceptionalDiscount", true),
                        Map.entry("grantableDiscountRate", 5.0),
                        Map.entry("printLogo", true),
                        Map.entry("paperFormat", "A4"),
                        Map.entry("lengthOfVatInvoiceNumber", 9),
                        Map.entry("prefixOfVatInvoiceNumber", "VATPG"),
                        Map.entry("lowStockAlert", true),
                        Map.entry("preventiveMaintenanceAlert", false)))
                .exchange()
                .expectStatus().isOk();

        scopedClient.post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-LPG-" + UUID.randomUUID().toString().substring(0, 6),
                        "name", "Legacy PG Product",
                        "familyCode", "LEGACY",
                        "variantLabel", "STD",
                        "unitPrice", 15.0,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()));

        scopedClient.post()
                .uri("/api/inventories")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "productId", productId.get(),
                        "referenceNumber", "INV-LPG-" + UUID.randomUUID().toString().substring(0, 6),
                        "countedQuantity", 8.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> inventorySessionId.set(value.toString()));

        scopedClient.post()
                .uri("/api/inventories/{inventoryId}/validate", inventorySessionId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("VALIDATED");

        scopedClient.post()
                .uri("/api/banking/accounts")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankName", "Legacy PG Bank",
                        "accountNumber", "ACC-LPG-" + UUID.randomUUID().toString().substring(0, 6),
                        "iban", "FR7630006000011234567890190",
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> bankAccountId.set(value.toString()));

        scopedClient.post()
                .uri("/api/banking/statements/import")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankAccountId", bankAccountId.get(),
                        "statementNumber", "STM-LPG-" + UUID.randomUUID().toString().substring(0, 6),
                        "statementDate", "2026-03-08",
                        "openingBalance", 200.0,
                        "closingBalance", 170.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> statementId.set(value.toString()));

        scopedClient.post()
                .uri("/api/banking/transactions")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "bankAccountId", bankAccountId.get(),
                        "referenceNumber", "TRX-LPG-" + UUID.randomUUID().toString().substring(0, 6),
                        "transactionType", "FEE",
                        "transactionDate", "2026-03-08",
                        "amount", -30.0,
                        "description", "Legacy PG fee"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> transactionId.set(value.toString()));

        scopedClient.post()
                .uri("/api/banking/reconciliation/manual")
                .bodyValue(Map.of(
                        "transactionId", transactionId.get(),
                        "statementLineId", statementId.get()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("RECONCILED");
    }

    @Test
    void platformOptionsDriveRemainingSelfServiceWorkflowsAgainstRealPostgresql() {
        String tenantId = UUID.randomUUID().toString();
        TestUser adminUser = bootstrapUser(tenantId, "adminsspg", Set.of(
                "administration:read",
                "administration:write",
                "administration:settings:write",
                "administration:govern:business-actors",
                "organizations:write"));
        TestUser businessUser = bootstrapUser(tenantId, "businesssspg", Set.of("organizations:write"));
        AtomicReference<String> businessActorId = new AtomicReference<>();
        AtomicReference<String> organizationId = new AtomicReference<>();

        userClient(tenantId, businessUser).post()
                .uri("/api/actors/onboarding")
                .bodyValue(Map.of(
                        "name", "Self Service PG Business Actor",
                        "businessId", "SSPG-" + UUID.randomUUID().toString().substring(0, 8)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> businessActorId.set(value.toString()));

        userClient(tenantId, businessUser).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", businessActorId.get(),
                        "code", "ORG-SSPG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "legalName", "Self Service PG Org",
                        "displayName", "Self Service PG Org",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()));

        WebTestClient adminScopedClient = userClient(tenantId, adminUser).mutate()
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

        userClient(tenantId, businessUser).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", businessActorId.get(),
                        "code", "ORG-NOPG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "legalName", "Disabled PG Org",
                        "displayName", "Disabled PG Org",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("ORGANIZATION_SELF_SERVICE_DISABLED");

        userClient(tenantId, businessUser).post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .bodyValue(Map.of(
                        "code", "AGY-NOPG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                        "name", "Disabled PG Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("AGENCY_SELF_SERVICE_DISABLED");

        adminScopedClient.post()
                .uri("/api/administration/governance/business-actors/{businessActorId}", businessActorId.get())
                .bodyValue(Map.of(
                        "action", "suspend",
                        "reason", "postgres suspension"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("SUSPENDED");

        userClient(tenantId, businessUser).post()
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

        userClient(tenantId, businessUser).post()
                .uri("/api/actors/me/reactivate")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.governanceStatus").isEqualTo("APPROVED");
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
                        UUID.fromString(user.actorId())))
                .build();
    }

    private TestUser bootstrapUser(String tenantId, String usernamePrefix, Set<String> permissions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = usernamePrefix + "." + suffix + "@example.com";
        String username = usernamePrefix + "-" + suffix;
        UUID tenant = UUID.fromString(tenantId);
        UUID actorId = Objects.requireNonNull(createActorUseCase.createActor(new CreateActorCommand(
                tenant,
                "User",
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

        return new TestUser(userId.toString(), actorId.toString());
    }

    private List<OutboxEvent> outboxEvents(String tenantId) {
        return outboxEventRepository.findByTenantId(UUID.fromString(tenantId))
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private List<DomainEventProjection> domainEventProjections(String tenantId) {
        return domainEventProjectionRepository.findByTenantId(UUID.fromString(tenantId))
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    private record TestUser(String userId, String actorId) {
    }
}
