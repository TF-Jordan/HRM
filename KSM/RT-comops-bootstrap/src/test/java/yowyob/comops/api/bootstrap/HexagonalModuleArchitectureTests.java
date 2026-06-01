package yowyob.comops.api.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class HexagonalModuleArchitectureTests {

    private static final List<ModuleDescriptor> MODULES = List.of(
            new ModuleDescriptor("../RT-comops-kernel-core", "yowyob/comops/api/kernel"),
            new ModuleDescriptor("../RT-comops-common-core", "yowyob/comops/api/common"),
            new ModuleDescriptor("../RT-comops-actor-core", "yowyob/comops/api/actor"),
            new ModuleDescriptor("../RT-comops-organization-core", "yowyob/comops/api/organization"),
            new ModuleDescriptor("../RT-comops-tp-core", "yowyob/comops/api/tp"),
            new ModuleDescriptor("../RT-comops-auth-core", "yowyob/comops/api/auth"),
            new ModuleDescriptor("../RT-comops-roles-core", "yowyob/comops/api/roles"),
            new ModuleDescriptor("../RT-comops-administration-core", "yowyob/comops/api/administration"),
            new ModuleDescriptor("../RT-comops-product-core", "yowyob/comops/api/product"),
            new ModuleDescriptor("../RT-comops-inventory-core", "yowyob/comops/api/inventory"),
            new ModuleDescriptor("../RT-comops-resource-core", "yowyob/comops/api/resource"),
            new ModuleDescriptor("../RT-comops-settings-core", "yowyob/comops/api/settings"),
            new ModuleDescriptor("../RT-comops-sales-core", "yowyob/comops/api/sales"),
            new ModuleDescriptor("../RT-comops-accounting-core", "yowyob/comops/api/accounting"),
            new ModuleDescriptor("../RT-comops-treasury-core", "yowyob/comops/api/treasury"),
            new ModuleDescriptor("../RT-comops-file-core", "yowyob/comops/api/file"));

    @Test
    void allCoreModulesMustExposeExpectedHexagonalDirectories() {
        for (ModuleDescriptor module : MODULES) {
            Path base = module.javaRoot();

            assertThat(base.resolve("domain"))
                    .as("domain directory must exist for %s", module.modulePath())
                    .exists();
            assertThat(base.resolve("application"))
                    .as("application directory must exist for %s", module.modulePath())
                    .exists();
            assertThat(base.resolve("adapter"))
                    .as("adapter directory must exist for %s", module.modulePath())
                    .exists();
            assertThat(base.resolve("application/port/in"))
                    .as("application.port.in directory must exist for %s", module.modulePath())
                    .exists();
            assertThat(base.resolve("application/port/out"))
                    .as("application.port.out directory must exist for %s", module.modulePath())
                    .exists();
            assertThat(base.resolve("adapter/in"))
                    .as("adapter.in directory must exist for %s", module.modulePath())
                    .exists();
            assertThat(base.resolve("adapter/out"))
                    .as("adapter.out directory must exist for %s", module.modulePath())
                    .exists();
        }
    }

    @Test
    void domainCodeMustNotDependOnSpringOrAdapters() throws IOException {
        for (ModuleDescriptor module : MODULES) {
            try (Stream<Path> domainFiles = Files.walk(module.javaRoot().resolve("domain"))) {
                for (Path javaFile : domainFiles.filter(path -> path.toString().endsWith(".java")).toList()) {
                    String source = Files.readString(javaFile);
                    assertThat(source)
                            .as("Domain source must not depend on Spring: %s", javaFile)
                            .doesNotContain("org.springframework");
                    assertThat(source)
                            .as("Domain source must not depend on adapters: %s", javaFile)
                            .doesNotContain(".adapter.");
                    assertThat(source)
                            .as("Domain source must not depend on web annotations: %s", javaFile)
                            .doesNotContain("@RestController")
                            .doesNotContain("@Controller")
                            .doesNotContain("@Component")
                            .doesNotContain("@Service")
                            .doesNotContain("@Repository");
                }
            }
        }
    }

    @Test
    void applicationPortsAndServicesMustNotDependOnAdapters() throws IOException {
        for (ModuleDescriptor module : MODULES) {
            Path applicationRoot = module.javaRoot().resolve("application");
            try (Stream<Path> applicationFiles = Files.walk(applicationRoot)) {
                for (Path javaFile : applicationFiles.filter(path -> path.toString().endsWith(".java")).toList()) {
                    String source = Files.readString(javaFile);
                    assertThat(source)
                            .as("Application source must not depend on adapter packages: %s", javaFile)
                            .doesNotContain(".adapter.");
                }
            }
        }
    }

    private record ModuleDescriptor(String modulePath, String packagePath) {
        Path javaRoot() {
            return Path.of(modulePath, "src/main/java").resolve(packagePath).normalize();
        }
    }
}
