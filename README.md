# Quarkus 4.0.0.CR1: NPE in BatchLogRecordProcessor on shutdown

With `quarkus-opentelemetry` and OTLP log export, log records still queued when the application stops make
`BatchLogRecordProcessor$Worker` call `VertxHttpSender.initiateSend`, which fails with a
`NullPointerException` because `SmallRyeContextManagerProvider.getManager()` is already `null`:

```
WARNING [io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor$Worker] Exporter threw an Exception:
java.lang.NullPointerException: Cannot invoke "io.smallrye.context.SmallRyeContextManager.defaultThreadContext()"
  because the return value of "io.smallrye.context.SmallRyeContextManagerProvider.getManager()" is null
	at io.smallrye.context.SmallRyeThreadContext.getCurrentThreadContextOrDefaultContexts(SmallRyeThreadContext.java:160)
	at io.smallrye.mutiny.context.DefaultContextPropagationInterceptor.getThreadContext(...)
	at io.smallrye.mutiny.groups.UniCreate.completionStage(UniCreate.java:140)
	at io.quarkus.opentelemetry.runtime.exporter.otlp.sender.VertxHttpSender.initiateSend(VertxHttpSender.java:158)
	at io.quarkus.opentelemetry.runtime.exporter.otlp.logs.VertxHttpLogRecordExporter.export(...)
	at io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor$Worker.flush(BatchLogRecordProcessor.java:242)
```

Seen first in a Temporal worker (JVM and native), where shutdown takes longer and the exporter fails repeatedly.

## Run

Both Maven modules compile the same application and GIVEN / WHEN / THEN `ShutdownTest` from `common/src`.
With Java 21+ and Maven, run both with the workaround off:

```sh
mvn -fae clean test
```

| Module | Quarkus version | Expected result |
| --- | --- | --- |
| `quarkus-3.40` | 3.40.1.2 | Passes: the receiver gets the final shutdown log. |
| `quarkus-4.0` | 4.0.0.CR1 | Fails: the exporter hits the SmallRye shutdown NPE above. |

The combined command intentionally exits nonzero. Run just the failing module with
`mvn -pl quarkus-4.0 test`; its stack trace is in `quarkus-4.0/target/surefire-reports/repro.ShutdownTest.txt`.