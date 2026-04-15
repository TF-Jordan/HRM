package yowyob.comops.api.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "yowyob.comops.api")
@EnableScheduling
public class IwmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IwmBackendApplication.class, args);
    }
}
