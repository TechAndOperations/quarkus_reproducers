package org.acme.quartz;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class QuartzDevServicesTest {

    @Test
    void scheduledJobRunsWithDevServices() {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> RestAssured.given()
                        .accept("application/json")
                        .when().get("/rest/admin/quartz/status")
                        .then()
                        .statusCode(200)
                        .contentType("application/json")
                        .body(equalTo("JobStatus[executions=2, ran=true]")));
    }
}



