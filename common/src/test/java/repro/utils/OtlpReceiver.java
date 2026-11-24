package repro.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Test sink that proves shutdown logs arrive, not just that no NPE was logged.
 */
public class OtlpReceiver implements QuarkusTestResourceLifecycleManager {
    private volatile boolean helloLogReceived;
    private volatile boolean shutdownLogReceived;
    private HttpServer server;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0, "/", this::receive);
        } catch (IOException e) {
            throw new IllegalStateException("Could not start OTLP receiver", e);
        }
        server.start();
        // Keep shutdown logs queued until the batch processor is stopped.
        return Map.of("quarkus.otel.exporter.otlp.endpoint", "http://127.0.0.1:" + server.getAddress().getPort(),
                "quarkus.otel.blrp.schedule.delay", "1h",
                "quarkus.http.test-port", "0");
    }

    private void receive(HttpExchange exchange) throws IOException {
        try (exchange) {
            byte[] body = exchange.getRequestBody().readAllBytes();
            if (body.length > 0 && "POST".equals(exchange.getRequestMethod())
                    && "/v1/logs".equals(exchange.getRequestURI().getPath())) {
                // The uncompressed protobuf payload contains the log message as UTF-8.
                String logs = new String(body, StandardCharsets.UTF_8);
                helloLogReceived |= logs.contains("hello called");
                shutdownLogReceived |= logs.contains("shutting down 19");
            }
            exchange.getResponseHeaders().set("Content-Type", "application/x-protobuf");
            exchange.sendResponseHeaders(200, -1);
        }
    }

    public boolean receivedHelloLog() {
        return helloLogReceived;
    }

    public boolean receivedShutdownLog() {
        return shutdownLogReceived;
    }

    @Override
    public void inject(TestInjector injector) {
        injector.injectIntoFields(this, new TestInjector.MatchesType(OtlpReceiver.class));
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
