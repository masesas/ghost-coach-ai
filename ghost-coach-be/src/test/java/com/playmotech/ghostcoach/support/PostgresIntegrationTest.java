package com.playmotech.ghostcoach.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for {@code *IT.java} integration tests that need a real PostgreSQL
 * instance (e.g. for {@code jsonb} columns that H2 cannot emulate).
 *
 * <p>The container is started once for the whole JVM and shared across every
 * subclass via a singleton pattern. JUnit's {@code @Container} annotation is
 * deliberately NOT used here because it would tie the container lifecycle to a
 * single class, causing later test classes to fail with "connection refused"
 * after the first class tears the container down.
 */
@SpringBootTest
@ActiveProfiles("integration")
public abstract class PostgresIntegrationTest {

    @SuppressWarnings("resource") // shared for JVM lifetime; closed by JVM shutdown hook
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("ghostcoach_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
