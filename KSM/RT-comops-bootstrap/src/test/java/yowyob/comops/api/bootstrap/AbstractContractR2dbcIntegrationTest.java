package yowyob.comops.api.bootstrap;

import yowyob.comops.api.kernel.config.UserSessionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
abstract class AbstractContractR2dbcIntegrationTest {

    protected static final String CLIENT_ID = "test-client";
    protected static final String API_KEY = "test-key";

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected UserSessionTokenService userSessionTokenService;

    protected WebTestClient userClient(String tenantId, String userId) {
        return organizationUserClient(tenantId, userId, null);
    }

    protected WebTestClient organizationUserClient(String tenantId, String userId, String organizationId) {
        WebTestClient.Builder builder = webTestClient.mutate()
                .defaultHeader("X-Client-Id", CLIENT_ID)
                .defaultHeader("X-Api-Key", API_KEY)
                .defaultHeader("X-Tenant-Id", tenantId)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userSessionTokenService.issue(
                        java.util.UUID.fromString(tenantId),
                        organizationId == null ? null : java.util.UUID.fromString(organizationId),
                        null,
                        java.util.UUID.fromString(userId),
                        null,
                        java.util.Set.of()));
        if (organizationId != null) {
            builder.defaultHeader("X-Organization-Id", organizationId);
        }
        return builder.build();
    }
}
