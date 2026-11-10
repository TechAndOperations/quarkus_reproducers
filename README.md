# IronJacamar IdleRemover and ConnectionValidator delay the stop of the application by 10 s

Each stop of an application that uses `quarkus-artemis-jms-ra` (and more generally `quarkus-ironjacamar`) takes
about 10 s more than needed, with the default configuration.

Versions: Quarkus `4.0.0.Beta3`, `quarkus-artemis` `4.0.0.Beta2`, `quarkus-ironjacamar` `2.0.0.Beta2`,
`ironjacamar-core-impl` `3.1.0.Final`, JDK 25.

## Reproduce

```shell
mvn verify
```

No broker is needed: the application has no code, it opens no JMS connection and activates no endpoint.
`StopDurationIT` starts `target/quarkus-app/quarkus-run.jar`, sends a SIGTERM once it is started and measures the stop:

| Test | Configuration | Stop |
|---|---|---|
| `stopIsFastWithTheDefaultConfiguration` | default (`idle-timeout-minutes=30`) | **10.2 s** (fails) |
| `stopIsFastWithTheConnectionValidator` | `idle-timeout-minutes=0`, `background-validation=true` | **10.2 s** (fails) |
| `stopIsFastWithoutTheIdleRemover` | `idle-timeout-minutes=0` | 0.15 s |

Log of the stop with the default configuration:

```
12:20:19,844 INFO  [io.qua.iro.runtime] (Shutdown thread) QIJ000016: Stopping JCA Pool Idle Remover service
12:20:24,848 INFO  [io.qua.thread-pool] (Shutdown thread) Awaiting thread pool shutdown; 1 thread(s) running
12:20:29,857 INFO  [io.qua.thread-pool] (Shutdown thread) Awaiting thread pool shutdown; 1 thread(s) running
12:20:29,863 INFO  [io.qua.iro.runtime] (jca-worker-pool-<default>-1) QIJ000002: Stopping Resource Adapter <default>
12:20:29,991 INFO  [io.quarkus] (Shutdown thread) code-with-quarkus stopped in 10.151s
```

The resource adapter is only stopped after the thread pool shutdown interrupted the remaining thread, after
`quarkus.thread-pool.shutdown-interrupt` (10 s by default, checked every `shutdown-check-interval`, 5 s).

## Cause

`IronJacamarRecorder.startIdleRemover` (`quarkus-ironjacamar`) starts the IdleRemover when a resource adapter has
`idle-timeout-minutes > 0`, which is the default (30):

```java
IdleRemover idleRemover = IdleRemover.getInstance();
idleRemover.setExecutorService(managedExecutor); // the executor of Quarkus: isExternal = true
idleRemover.start();                             // executorService.execute(new IdleRemoverRunner())
shutdownContext.addShutdownTask(idleRemover::stop);
```

`startConnectionValidator` does the same with the `ConnectionValidator` when `background-validation=true`.

In IronJacamar (`org.jboss.jca.core.connectionmanager.pool.idle.IdleRemover`, read from the bytecode of 3.1.0.Final):

- `IdleRemoverRunner.run()` loops on `condition.await(interval, MILLISECONDS)` while `shutdown` is `false`. The
  interval is `Long.MAX_VALUE` when no pool is registered, otherwise derived from the idle timeout (minutes).
- `stop()` sets `shutdown` to `true`, calls `executorService.shutdownNow()` **only if the executor is not external**,
  and clears the registered pools. It never signals `condition`.

With an external executor nothing wakes the runner: its thread (`executor-thread-N`) stays in `Condition.await` until
the shutdown of the Quarkus thread pool interrupts it. `ConnectionValidator.stop()` and its runner have the same code.

Artemis is not involved: the resource adapter stops in a few milliseconds once its turn comes.

## Possible fixes

- IronJacamar: `IdleRemover.stop()` and `ConnectionValidator.stop()` should wake the runner (`lock.lock();
  condition.signalAll(); lock.unlock()` after setting `shutdown`), whatever the executor.
- `quarkus-ironjacamar`, without waiting for IronJacamar: do not give the `ManagedExecutor` to the IdleRemover and the
  ConnectionValidator (they then create their own single thread executor, interrupted by `shutdownNow()` in `stop()`),
  or give them a dedicated executor that the shutdown task stops with `shutdownNow()` after `stop()`.

## Workarounds

- `quarkus.thread-pool.shutdown-interrupt=1s`: the stop takes about 5 s (the first check of the thread pool happens
  after `shutdown-check-interval`, 5 s); it also shortens the interruption of the other tasks of the application.
- `quarkus.ironjacamar.ra.cm.pool.config.idle-timeout-minutes=0`: no stop delay, but the idle connections of the
  outbound pool are no longer removed.
