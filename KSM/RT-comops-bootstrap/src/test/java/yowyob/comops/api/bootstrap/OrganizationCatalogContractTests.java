package yowyob.comops.api.bootstrap;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {                "spring.r2dbc.url=r2dbc:postgresql://localhost:54329/iwm",
                "spring.r2dbc.username=iwm",
                "spring.r2dbc.password=iwm",
                "spring.liquibase.url=jdbc:postgresql://localhost:54329/iwm",
                "spring.liquibase.user=iwm",
                "spring.liquibase.password=iwm",
                "spring.liquibase.change-log=classpath:db/changelog/contracts/organization-catalog-contract.yaml",
                "spring.liquibase.drop-first=false",
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline"
        })
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.r2dbc.enabled", matches = "true")
class OrganizationCatalogContractTests extends AbstractContractR2dbcIntegrationTest {

    private static final String TENANT_ID = "24020000-0000-0000-0000-000000000001";
    private static final String USER_ID = "24020000-0000-0000-0000-000000000011";
    private static final String BUSINESS_ACTOR_ID = "24020000-0000-0000-0000-000000000010";
    private static final String EXTERNAL_ACTOR_ID = "24020000-0000-0000-0000-000000000012";

    @Test
    void organizationThirdPartyAndCatalogContractIsStableOnDedicatedDataset() {
        AtomicReference<String> organizationId = new AtomicReference<>();
        AtomicReference<String> agencyId = new AtomicReference<>();
        AtomicReference<String> thirdPartyId = new AtomicReference<>();
        AtomicReference<String> productId = new AtomicReference<>();

        userClient(TENANT_ID, USER_ID).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", BUSINESS_ACTOR_ID,
                        "code", "ORG-CATALOG-CONTRACT",
                        "legalName", "Catalog Contract Org",
                        "displayName", "Catalog Contract Org",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> organizationId.set(value.toString()))
                .jsonPath("$.data.code").isEqualTo("ORG-CATALOG-CONTRACT");

        userClient(TENANT_ID, USER_ID).post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .bodyValue(Map.of(
                        "code", "AGY-CATALOG-CONTRACT",
                        "name", "Catalog Contract Agency",
                        "agencyType", "BRANCH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> agencyId.set(value.toString()))
                .jsonPath("$.data.code").isEqualTo("AGY-CATALOG-CONTRACT");

        userClient(TENANT_ID, USER_ID).post()
                .uri("/api/organizations/opening-hours")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "dayOfWeek", "MONDAY",
                        "opensAt", "08:00:00",
                        "closesAt", "18:00:00",
                        "closed", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dayOfWeek").isEqualTo("MONDAY")
                .jsonPath("$.data.closed").isEqualTo(false);

        userClient(TENANT_ID, USER_ID).post()
                .uri("/api/organizations/points-of-interest")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "agencyId", agencyId.get(),
                        "name", "Main Warehouse",
                        "poiType", "WAREHOUSE",
                        "latitude", 4.052,
                        "longitude", 9.704))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("Main Warehouse");

        organizationUserClient(TENANT_ID, USER_ID, organizationId.get()).post()
                .uri("/api/third-parties")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "partyType", "ACTOR",
                        "partyId", EXTERNAL_ACTOR_ID,
                        "referenceCode", "TP-CATALOG-CONTRACT",
                        "displayName", "Catalog Contract Customer",
                        "roles", Set.of("CLIENT"),
                        "prospect", false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> thirdPartyId.set(value.toString()))
                .jsonPath("$.data.referenceCode").isEqualTo("TP-CATALOG-CONTRACT");

        organizationUserClient(TENANT_ID, USER_ID, organizationId.get()).post()
                .uri("/api/products")
                .bodyValue(Map.of(
                        "organizationId", organizationId.get(),
                        "sku", "SKU-CATALOG-CONTRACT",
                        "name", "Catalog Contract Product",
                        "familyCode", "CATALOG-CONTRACT",
                        "variantLabel", "STANDARD",
                        "barcode", "1234567890123",
                        "description", "Contract product",
                        "unitPrice", 19.99,
                        "currency", "EUR",
                        "status", "ACTIVE"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> productId.set(value.toString()))
                .jsonPath("$.data.sku").isEqualTo("SKU-CATALOG-CONTRACT");

        userClient(TENANT_ID, USER_ID).get()
                .uri("/api/organizations/{organizationId}", organizationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.displayName").isEqualTo("Catalog Contract Org");

        userClient(TENANT_ID, USER_ID).get()
                .uri("/api/organizations/{organizationId}/agencies", organizationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].id").isEqualTo(agencyId.get());

        userClient(TENANT_ID, USER_ID).get()
                .uri("/api/organizations/opening-hours/{organizationId}/agencies/{agencyId}",
                        organizationId.get(), agencyId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].opensAt").isEqualTo("08:00:00");

        userClient(TENANT_ID, USER_ID).get()
                .uri("/api/organizations/points-of-interest/{organizationId}/agencies/{agencyId}",
                        organizationId.get(), agencyId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].poiType").isEqualTo("WAREHOUSE");

        organizationUserClient(TENANT_ID, USER_ID, organizationId.get()).get()
                .uri(uriBuilder -> uriBuilder.path("/api/third-parties")
                        .queryParam("organizationId", organizationId.get())
                        .queryParam("role", "CLIENT")
                        .queryParam("prospect", false)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].id").isEqualTo(thirdPartyId.get());

        organizationUserClient(TENANT_ID, USER_ID, organizationId.get()).get()
                .uri(uriBuilder -> uriBuilder.path("/api/products")
                        .queryParam("organizationId", organizationId.get())
                        .queryParam("familyCode", "CATALOG-CONTRACT")
                        .queryParam("status", "ACTIVE")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].id").isEqualTo(productId.get());

        organizationUserClient(TENANT_ID, USER_ID, organizationId.get()).get()
                .uri("/api/products/{productId}", productId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("Catalog Contract Product");
    }
}
