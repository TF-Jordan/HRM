package yowyob.comops.api.bootstrap.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class LiquibaseMigrationConfiguration {

    @Bean
    public ApplicationRunner liquibaseMigrationRunner(
            @Value("${spring.liquibase.enabled:true}") boolean enabled,
            @Value("${spring.liquibase.change-log:classpath:db/changelog/db.changelog-master.yaml}") String changeLog,
            @Value("${spring.liquibase.url}") String url,
            @Value("${spring.liquibase.user}") String user,
            @Value("${spring.liquibase.password}") String password) {
        return args -> {
            if (!enabled) {
                return;
            }
            String normalizedChangeLog = changeLog.replaceFirst("^classpath:", "");
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
                try (Liquibase liquibase = new Liquibase(
                        normalizedChangeLog,
                        new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader()),
                        database)) {
                    liquibase.update();
                }
            }
        };
    }
}
