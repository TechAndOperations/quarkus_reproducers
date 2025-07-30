package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SimpleScheduledTaskTest {

    @Inject
    SimpleScheduledTask simpleScheduledTask;

    @Test
    void should() {
        simpleScheduledTask.run();
    }
}