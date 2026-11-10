package org.acme;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Starts the packaged application, stops it with a SIGTERM and measures how long the stop takes.
 */
class StopDurationIT {

    private static final Duration MAX_STOP_DURATION = Duration.ofSeconds(3);
    private static final Duration START_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration STOP_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    /**
     * Fails: the IdleRemover is started with the default configuration (idle timeout of 30 minutes) and its thread is
     * only interrupted by the shutdown of the Quarkus thread pool, after quarkus.thread-pool.shutdown-interrupt (10 s).
     */
    @Test
    void stopIsFastWithTheDefaultConfiguration() throws Exception {
        Duration stop = startAndStop();
        assertTrue(stop.compareTo(MAX_STOP_DURATION) < 0, "the stop took " + stop.toMillis() + " ms");
    }

    /**
     * Passes: without idle timeout, quarkus-ironjacamar does not start the IdleRemover.
     */
    @Test
    void stopIsFastWithoutTheIdleRemover() throws Exception {
        Duration stop = startAndStop("-Dquarkus.ironjacamar.ra.cm.pool.config.idle-timeout-minutes=0");
        assertTrue(stop.compareTo(MAX_STOP_DURATION) < 0, "the stop took " + stop.toMillis() + " ms");
    }

    /**
     * The ConnectionValidator is started on the same executor as the IdleRemover when the background validation is
     * enabled (the IdleRemover is disabled here to isolate it).
     */
    @Test
    void stopIsFastWithTheConnectionValidator() throws Exception {
        Duration stop = startAndStop("-Dquarkus.ironjacamar.ra.cm.pool.config.idle-timeout-minutes=0",
                "-Dquarkus.ironjacamar.ra.cm.pool.config.background-validation=true",
                "-Dquarkus.ironjacamar.ra.cm.pool.config.background-validation-millis=60000");
        assertTrue(stop.compareTo(MAX_STOP_DURATION) < 0, "the stop took " + stop.toMillis() + " ms");
    }

    private static Duration startAndStop(String... systemProperties) throws Exception {
        Path jar = Path.of(System.getProperty("quarkus.app.jar", "target/quarkus-app/quarkus-run.jar"));
        assertTrue(Files.exists(jar), jar + " not found, run mvn verify");
        Path log = Files.createTempFile(jar.getParent().getParent(), "app-", ".log");

        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.addAll(List.of(systemProperties));
        command.add("-jar");
        command.add(jar.toString());

        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            awaitStarted(process, log);

            long begin = System.nanoTime();
            process.destroy(); // SIGTERM: runs the shutdown hook of Quarkus
            assertTrue(process.waitFor(STOP_TIMEOUT.toSeconds(), TimeUnit.SECONDS), "the application did not stop");
            Duration stop = Duration.ofNanos(System.nanoTime() - begin);

            Files.readAllLines(log).forEach(line -> System.out.println("[app] " + line));
            System.out.println(">>> stop took " + stop.toMillis() + " ms");
            return stop;
        } finally {
            process.destroyForcibly();
        }
    }

    private static void awaitStarted(Process process, Path log) throws IOException, InterruptedException {
        long deadline = System.nanoTime() + START_TIMEOUT.toNanos();
        while (!Files.readString(log).contains("started in")) {
            assertTrue(process.isAlive(), "the application exited: " + Files.readString(log));
            assertTrue(System.nanoTime() < deadline, "the application did not start: " + Files.readString(log));
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
    }
}
