package org.acme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.quarkus.test.junit.QuarkusTest;

/**
 * The REST client is called from the test thread (not a Vert.x thread) while a span is current: the HTTP client span
 * must be a child of that span.
 */
@QuarkusTest
class RestClientParentSpanTest {

    private static final int CALLS = 10;

    @Inject
    Tracer tracer;

    @Inject
    InMemorySpanExporter exporter;

    @Inject
    @RestClient
    EchoClient client;

    @BeforeEach
    void reset() {
        exporter.reset();
    }

    /** Passes on 3.40.1 and 4.0.0.CR1. */
    @Test
    void stringBody() throws Exception {
        assertEquals(CALLS, childrenOfCurrentSpan(c -> c.string("hello")), "client spans with the expected parent");
    }

    /** Passes on 3.40.1, fails on 4.0.0.CR1: most client spans start a new trace. */
    @Test
    void inputStreamBody() throws Exception {
        assertEquals(CALLS, childrenOfCurrentSpan(c -> c.stream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }))),
                "client spans with the expected parent");
    }

    private int childrenOfCurrentSpan(Consumer<EchoClient> call) throws InterruptedException {
        int children = 0;
        for (int i = 0; i < CALLS; i++) {
            exporter.reset();
            Span parent = tracer.spanBuilder("parent").startSpan();
            try (Scope ignored = parent.makeCurrent()) {
                call.accept(client);
            } finally {
                parent.end();
            }
            SpanData clientSpan = awaitClientSpan();
            if (clientSpan.getParentSpanId().equals(parent.getSpanContext().getSpanId())) {
                children++;
            }
        }
        return children;
    }

    private SpanData awaitClientSpan() throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline) {
            List<SpanData> spans = exporter.getFinishedSpanItems().stream()
                    .filter(span -> span.getKind() == SpanKind.CLIENT)
                    .toList();
            if (!spans.isEmpty()) {
                return spans.get(0);
            }
            Thread.sleep(10);
        }
        throw new AssertionError("no client span exported");
    }

    @Singleton
    static class ExporterProducer {

        @Produces
        @Singleton
        InMemorySpanExporter exporter() {
            return InMemorySpanExporter.create();
        }
    }
}
