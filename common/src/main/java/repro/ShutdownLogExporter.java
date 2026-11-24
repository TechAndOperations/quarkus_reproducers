package repro;

import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.interceptor.Interceptor;

@ApplicationScoped
class ShutdownLogExporter {
    private static final Logger LOG = Logger.getLogger(ShutdownLogExporter.class);

    // Run after application shutdown observers but before Quarkus tears down the context manager.
    void stop(@Observes @Priority(Interceptor.Priority.PLATFORM_AFTER) ShutdownEvent event, OpenTelemetry telemetry,
            @ConfigProperty(name = "repro.early-otel-shutdown", defaultValue = "false") boolean enabled) {
        if (!enabled) {
            return;
        }
        if (!(telemetry instanceof OpenTelemetrySdk sdk)) {
            LOG.warn("Cannot drain OpenTelemetry logs early: no OpenTelemetry SDK");
            return;
        }
        // Close only the log pipeline. It cannot export any records logged after this point.
        if (!sdk.getSdkLoggerProvider().shutdown().join(10, TimeUnit.SECONDS).isSuccess()) {
            LOG.warn("Could not drain OpenTelemetry logs before context shutdown");
        }
    }
}
