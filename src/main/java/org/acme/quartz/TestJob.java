package org.acme.quartz;

import io.quarkus.scheduler.ScheduledExecution;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class TestJob {

    private final AtomicInteger executions = new AtomicInteger();

    @PostConstruct
    void start() {
        executions.incrementAndGet();
    }

    public void onScheduled(ScheduledExecution ignored) {
        executions.incrementAndGet();
    }

    public int getExecutions() {
        return executions.get();
    }
}
