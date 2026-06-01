package yowyob.comops.api.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class IntegrationAdapterArchitectureTests {

    private static final Path KERNEL_ADAPTERS_PATH = Path.of(
            "../RT-comops-kernel-core/src/main/java/yowyob/comops/api/kernel/adapter/out");
    private static final Path KERNEL_WEB_PATH = Path.of(
            "../RT-comops-kernel-core/src/main/java/yowyob/comops/api/kernel");

    @Test
    void redisPermissionCacheMustStayOnReactiveRedisTemplateAndMustNotUseHttpClients() throws IOException {
        Path redisAdapter = KERNEL_ADAPTERS_PATH.resolve("cache/RedisReactivePermissionCache.java").normalize();
        String source = Files.readString(redisAdapter);

        assertThat(source).contains("ReactiveStringRedisTemplate");
        assertThat(source).doesNotContain("WebClient");
        assertThat(source).doesNotContain("RestTemplate");
    }

    @Test
    void kafkaAdaptersMustStayOnSpringKafkaAndMustNotUseHttpClients() throws IOException {
        Path kafkaDeliveryAdapter = KERNEL_ADAPTERS_PATH.resolve("integration/KafkaBusinessEventDeliverySink.java")
                .normalize();
        Path kafkaConsumerAdapter = KERNEL_ADAPTERS_PATH.resolve("integration/KafkaBusinessEventConsumerListener.java")
                .normalize();

        String deliverySource = Files.readString(kafkaDeliveryAdapter);
        String consumerSource = Files.readString(kafkaConsumerAdapter);

        assertThat(deliverySource).contains("KafkaTemplate");
        assertThat(deliverySource).doesNotContain("WebClient");
        assertThat(deliverySource).doesNotContain("RestTemplate");

        assertThat(consumerSource).contains("@KafkaListener");
        assertThat(consumerSource).doesNotContain("WebClient");
        assertThat(consumerSource).doesNotContain("RestTemplate");
    }

    @Test
    void backendMustNotTrustIdentityHeadersFromExternalRequests() throws IOException {
        String authenticationConverter = Files.readString(
                KERNEL_WEB_PATH.resolve("config/ApiKeyServerAuthenticationConverter.java").normalize());
        String tenantFilter = Files.readString(
                KERNEL_WEB_PATH.resolve("adapter/in/web/TenantWebFilter.java").normalize());

        assertThat(authenticationConverter).doesNotContain("X-User-Id");
        assertThat(authenticationConverter).doesNotContain("X-Actor-Id");
        assertThat(authenticationConverter).doesNotContain("firstNonNull(parseUuid(exchange, USER_HEADER)");
        assertThat(authenticationConverter).doesNotContain("firstNonNull(parseUuid(exchange, ACTOR_HEADER)");

        assertThat(tenantFilter).doesNotContain("X-User-Id");
        assertThat(tenantFilter).doesNotContain("X-Actor-Id");
        assertThat(tenantFilter).doesNotContain("firstNonNull(parseUuidHeader(request, USER_HEADER)");
        assertThat(tenantFilter).doesNotContain("firstNonNull(parseUuidHeader(request, ACTOR_HEADER)");
    }

    @Test
    void kernelRuntimeMustNotUseBlockingReactiveCalls() throws IOException {
        try (Stream<Path> sourceFiles = Files.walk(KERNEL_WEB_PATH)) {
            String offendingSources = sourceFiles
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> {
                        try {
                            String source = Files.readString(path);
                            return source.contains(".block(") ? path.toString() : null;
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .sorted()
                    .reduce("", (left, right) -> left.isEmpty() ? right : left + "\n" + right);

            assertThat(offendingSources)
                    .as("Kernel runtime source files must not contain blocking reactive calls")
                    .isEmpty();
        }
    }
}
