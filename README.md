# Reproducer: dev mode fails with `grafana.endpoint` / `otel-collector.url` when the OTLP endpoint is explicitly configured

Quarkus 4.0.0.Beta1, application with only `quarkus-rest` and `quarkus-opentelemetry`.

## Steps

`application.properties` sets an explicit OTLP endpoint, as an application exporting to an external APM server would:

```properties
quarkus.otel.exporter.otlp.endpoint=http://localhost:4318
```

Then:

```
mvn quarkus:dev
```

## Actual

The LGTM dev service is skipped because of the explicit endpoint, then the application fails to start:

```
INFO  [io.quarkus.observability.devresource.lgtm.LgtmResource] (build-6) Not starting LGTM Dev Services: 'quarkus.otel.exporter.otlp.endpoint' is explicitly configured.
Failed to load config value of type class java.lang.String for: grafana.endpoint
Failed to load config value of type class java.lang.String for: otel-collector.url
...
INFO  [io.quarkus.deployment.dev.IsolatedDevModeMain] (main) Attempting to start live reload endpoint to recover from previous Quarkus startup failure
```

## Expected

The application starts and exports to the explicitly configured OTLP endpoint, without the LGTM dev service
(and without the Grafana/LGTM Dev UI cards).

## Analysis

- Since Quarkus 4, `quarkus-opentelemetry` declares `quarkus-observability-devservices-lgtm` as a
  `conditional-dev-dependency`, so the LGTM dev service is added to every OpenTelemetry application in dev mode.
- `LgtmResource.enable()` returns false when `quarkus.otel.exporter.otlp.endpoint` is explicitly configured,
  so the LGTM container is not started.
- `ObservabilityDevUIProcessor` still registers `ObservabilityJsonRPCService`: its conditions
  (`IsDevServicesSupportedByLaunchMode`, `DevServicesConfig.Enabled`, `IsLgtmDevUiEnabled`) do not check
  whether the LGTM container is actually started.
- `ObservabilityJsonRPCService` injects `@ConfigProperty(name = "grafana.endpoint") String` and
  `@ConfigProperty(name = "otel-collector.url") String`. These properties are only provided by the running
  container, so config validation fails at startup.

## Workaround

```
mvn quarkus:dev -Dquarkus.observability.enabled=false
```

(or `quarkus.observability.enabled=false` in `application.properties`)

This disables both the LGTM dev service and its Dev UI page (`IsLgtmDevUiEnabled`); the application then starts
and keeps exporting to the configured OTLP endpoint.
