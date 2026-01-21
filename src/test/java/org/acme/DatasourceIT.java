package org.acme;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class DatasourceIT {

    @Inject
    DataSource dataSource;

    @Test
    void should_start_testcontainers_datasource() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            Log.info("Successfully obtained a connection from Testcontainers-backed datasource");
        }
    }
}