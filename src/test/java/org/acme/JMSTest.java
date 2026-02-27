package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.startsWith;

@QuarkusTest
public class JMSTest {

    @Test
    void testSendReceive() {

        String s = "" + System.currentTimeMillis();

        given()
                .when().post("/jms/send/?text=" + s + "&count=1")
                .then()
                .statusCode(200)
                .body(startsWith("OK"));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when().get("/jms/last")
                    .then()
                    .statusCode(200)
                    .body(startsWith(s + "_0"));
        });
    }
}
