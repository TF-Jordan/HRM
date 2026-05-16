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
                "spring.liquibase.change-log=classpath:db/changelog/contracts/resource-contract.yaml",
                "spring.liquibase.drop-first=false",
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline"
        })
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.r2dbc.enabled", matches = "true")
class ResourceContractTests extends AbstractContractR2dbcIntegrationTest {

    private static final String TENANT_ID = "23000000-0000-0000-0000-000000000001";
    private static final String USER_ID = "23000000-0000-0000-0000-000000000011";
    private static final String ORGANIZATION_ID = "23000000-0000-0000-0000-000000000110";
    private static final String AGENCY_ID = "23000000-0000-0000-0000-000000000111";
    private static final String RESOURCE_ID = "23000000-0000-0000-0000-000000000112";

    @Test
    void resourceContractIsStableOnSeededDataset() {
        AtomicReference<String> reservationId = new AtomicReference<>();

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri(uriBuilder -> uriBuilder.path("/api/resources")
                        .queryParam("organizationId", ORGANIZATION_ID)
                        .queryParam("agencyId", AGENCY_ID)
                        .queryParam("category", "NETWORK")
                        .queryParam("status", "AVAILABLE")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].resourceCode").isEqualTo("RES-CONTRACT-0001");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/resources/{resourceId}/reservations", RESOURCE_ID)
                .bodyValue(Map.of(
                        "reserveeType", "AGENCY",
                        "reserveeId", AGENCY_ID,
                        "reason", "Contract reservation"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("RESERVED");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).get()
                .uri("/api/resources/{resourceId}/reservations", RESOURCE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].status").isEqualTo("ACTIVE")
                .jsonPath("$.data[0].id").value(value -> reservationId.set(value.toString()));

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/resources/{resourceId}/reservations/{reservationId}/release", RESOURCE_ID, reservationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("AVAILABLE");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/resources/{resourceId}/assignments", RESOURCE_ID)
                .bodyValue(Map.of(
                        "assigneeType", "AGENCY",
                        "assigneeId", AGENCY_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("ASSIGNED");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/resources/{resourceId}/unassign", RESOURCE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("AVAILABLE");

        organizationUserClient(TENANT_ID, USER_ID, ORGANIZATION_ID).post()
                .uri("/api/resources/{resourceId}/dispose", RESOURCE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.status").isEqualTo("DISPOSED");
    }
}
