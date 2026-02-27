package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

    @Test
    void testWords() {
        await().atMost(10, SECONDS).pollInterval(1, SECONDS).untilAsserted(() ->
            given()
                .when().get("/hello/words")
                .then()
                .statusCode(200)
                .body(is("words=[HELLO, WITH, QUARKUS, MESSAGING, MESSAGE]"))
        );
    }

}