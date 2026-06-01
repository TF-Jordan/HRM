package yowyob.comops.api.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchAdapterArchitectureTests {

    private static final Path SEARCH_ADAPTERS_PATH = Path.of(
            "src/main/java/yowyob/comops/api/bootstrap/integration/search");

    @Test
    void elasticsearchSearchGatewaysMustStayReactiveAndMustNotUseWebClientDirectly() throws IOException {
        List<Path> gatewayFiles = Files.list(SEARCH_ADAPTERS_PATH)
                .filter(path -> path.getFileName().toString().startsWith("Elasticsearch"))
                .filter(path -> path.getFileName().toString().endsWith("SearchGateway.java"))
                .sorted()
                .toList();

        assertThat(gatewayFiles).isNotEmpty();

        for (Path gatewayFile : gatewayFiles) {
            String source = Files.readString(gatewayFile);
            assertThat(source)
                    .as("Reactive search gateway must use ReactiveElasticsearchOperations: %s", gatewayFile)
                    .contains("ReactiveElasticsearchOperations");
            assertThat(source)
                    .as("Search gateway must not use WebClient directly: %s", gatewayFile)
                    .doesNotContain("WebClient");
            assertThat(source)
                    .as("Search gateway must not depend on the removed HTTP workaround: %s", gatewayFile)
                    .doesNotContain("ElasticsearchHttpSearchSupport");
        }
    }

    @Test
    void removedElasticHttpWorkaroundMustStayAbsent() {
        assertThat(SEARCH_ADAPTERS_PATH.resolve("ElasticsearchHttpSearchSupport.java"))
                .doesNotExist();
    }
}
