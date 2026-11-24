package repro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.jboss.logmanager.handlers.QueueHandler;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.quarkus.runtime.Application;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import repro.utils.OtlpReceiver;

@QuarkusTest
@WithTestResource(OtlpReceiver.class)
class ShutdownTest {
    @TestHTTPResource("/hello")
    URI hello;

    OtlpReceiver receiver;

    @Inject
    OpenTelemetry telemetry;

    @Test
    void exportsLogsWithoutThrowingOnShutdown() throws Exception {
        // GIVEN log export works while Quarkus is running.
        // Flush the /hello log explicitly so the batch timer stays out of the shutdown test.
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(hello).timeout(Duration.ofSeconds(5)).build();
            assertEquals(200, client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode());
        }
        OpenTelemetrySdk sdk = assertInstanceOf(OpenTelemetrySdk.class, telemetry);
        assertTrue(sdk.getSdkLoggerProvider().forceFlush().join(10, TimeUnit.SECONDS).isSuccess(),
                "Could not flush OTLP logs before shutdown");
        assertTrue(receiver.receivedHelloLog(), "The /hello log never reached the OTLP receiver");

        // Listen before stopping Quarkus: the OpenTelemetry worker reports errors
        // on a background thread, so stop() can return even if the worker throws.
        Logger worker = Logger.getLogger("io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor$Worker");
        QueueHandler capture = new QueueHandler();
        worker.addHandler(capture);

        // WHEN Quarkus stops, the shutdown observer writes logs that the worker must flush.
        try {
            Application.currentApplication().stop();
        } finally {
            worker.removeHandler(capture);
        }

        // THEN fail on any worker exception, keeping its stack trace in the test report.
        for (var record : capture.getQueue()) {
            if (record.getThrown() != null) {
                fail("OTLP log exporter threw during shutdown", record.getThrown());
            }
        }
        // AND the receiver saw "shutting down 19", the last log from the shutdown observer.
        // This catches silent log loss even when the worker reports no exception.
        assertTrue(receiver.receivedShutdownLog(), "Final shutdown log never reached the OTLP receiver");
    }
}
