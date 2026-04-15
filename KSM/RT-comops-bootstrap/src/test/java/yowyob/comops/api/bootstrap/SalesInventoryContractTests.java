package yowyob.comops.api.bootstrap;

import java.util.Map;
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
                "spring.liquibase.change-log=classpath:db/changelog/contracts/sales-inventory-contract.yaml",
                "spring.liquibase.drop-first=false",
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline"
        })
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.r2dbc.enabled", matches = "true")
class SalesInventoryContractTests extends AbstractContractR2dbcIntegrationTest {

    private static final String TENANT_ID = "21000000-0000-0000-0000-000000000001";
    private static final String USER_ID = "21000000-0000-0000-0000-000000000011";
    private static final String ORGANIZATION_ID = "21000000-0000-0000-0000-000000000110";
    private static final String AGENCY_ID = "21000000-0000-0000-0000-000000000111";
    private static final String CUSTOMER_ID = "21000000-0000-0000-0000-000000000112";
    private static final String PRODUCT_ID = "21000000-0000-0000-0000-000000000113";

    @Test
    void salesAndInventoryContractIsStableOnSeededDataset() {
        AtomicReference<String> orderId = new AtomicReference<>();

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri(uriBuilder -> uriBuilder.path("/api/inventory/movements/balance")
                        .queryParam("organizationId", ORGANIZATION_ID)
                        .queryParam("agencyId", AGENCY_ID)
                        .queryParam("productId", PRODUCT_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.onHandQuantity").isEqualTo(8);

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/sales/orders")
                .bodyValue(Map.of(
                        "organizationId", ORGANIZATION_ID,
                        "agencyId", AGENCY_ID,
                        "customerThirdPartyId", CUSTOMER_ID,
                        "productId", PRODUCT_ID,
                        "quantity", 3.0,
                        "unitPrice", 15.0,
                        "currency", "EUR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> orderId.set(value.toString()))
                .jsonPath("$.data.orderNumber").isEqualTo("SO-C-0001")
                .jsonPath("$.data.status").isEqualTo("DRAFT")
                .jsonPath("$.data.totalAmount").isEqualTo(45);

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/sales/orders/{orderId}/confirm", orderId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("CONFIRMED");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri("/api/sales/orders/{orderId}", orderId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.orderNumber").isEqualTo("SO-C-0001")
                .jsonPath("$.data.status").isEqualTo("CONFIRMED");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri(uriBuilder -> uriBuilder.path("/api/inventory/movements/balance")
                        .queryParam("organizationId", ORGANIZATION_ID)
                        .queryParam("agencyId", AGENCY_ID)
                        .queryParam("productId", PRODUCT_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.onHandQuantity").isEqualTo(5);
    }
}
