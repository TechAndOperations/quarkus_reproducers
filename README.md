# Quarkus 3.34 / Prometheus startup hang

## Problem

After upgrading to Quarkus 3.34, our tests hang on startup when `quarkus-micrometer-registry-prometheus` is enabled and the management port is set to `0` (`quarkus.management.port=0` or `quarkus.management.test-port=0`).

This appears after the change:

> Prometheus: throw an exception if meter registration fails

During startup, Quarkus logs:

> `Prometheus requires that all meters with the same name have the same set of tag keys. There is already an existing meter named 'http_server_bytes_read' containing tag keys [server_port].`

## Reproducer

`src/main/resources/application.properties`:

```properties
quarkus.quartz.store-type=ram
quarkus.http.root-path=/rest
quarkus.quartz.clustered=false
quarkus.micrometer.enabled=true
quarkus.management.enabled=true
# quarkus.management.port=0
quarkus.management.test-port=0
```

## Expected

Quarkus starts normally and the test passes.

## Actual

Startup blocks/hangs when Prometheus is enabled.