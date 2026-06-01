package yowyob.comops.api.bootstrap;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
@ActiveProfiles("test-memory")
class BootstrapSecurityIntegrationTests {

    private static final String CLIENT_ID = "test-client";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void roleAndAuthBootstrapEndpointsAreForbiddenWithoutAdminUserContext() {
        String tenantId = UUID.randomUUID().toString();

        systemClient(tenantId).post()
                .uri("/api/roles")
                .bodyValue(Map.of(
                        "code", "ROLE-BLOCKED",
                        "name", "Blocked Role",
                        "permissions", new String[]{"products:write"}))
                .exchange()
                .expectStatus().isForbidden();

        AtomicReference<String> actorId = new AtomicReference<>();
        systemClient(tenantId).post()
                .uri("/api/actors")
                .bodyValue(Map.of(
                        "firstName", "Blocked",
                        "lastName", "Bootstrap",
                        "email", "blocked.bootstrap." + UUID.randomUUID() + "@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.id").value(value -> actorId.set(value.toString()));

        systemClient(tenantId).post()
                .uri("/api/auth/register")
                .bodyValue(Map.of(
                        "actorId", actorId.get(),
                        "username", "blocked-" + UUID.randomUUID().toString().substring(0, 8),
                        "email", "blocked.user." + UUID.randomUUID() + "@example.com",
                        "authProvider", "LOCAL"))
                .exchange()
                .expectStatus().isForbidden();
    }

    private WebTestClient systemClient(String tenantId) {
        return webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", "test-key")
                .defaultHeader("X-Tenant-Id", tenantId)
                .build();
    }
}
