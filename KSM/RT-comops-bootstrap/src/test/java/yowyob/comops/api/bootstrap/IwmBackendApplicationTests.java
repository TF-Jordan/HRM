package yowyob.comops.api.bootstrap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class IwmBackendApplicationTests {

    @Test
    void applicationClassCarriesSpringBootAnnotation() {
        SpringBootApplication annotation = IwmBackendApplication.class.getAnnotation(SpringBootApplication.class);

        assertNotNull(annotation);
        assertTrue(annotation.scanBasePackages().length > 0);
    }
}
