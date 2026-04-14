package org.acme.quartz;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuartzTestScheduler {

    @Inject
    TestJob job;

    @Scheduled(every = "2s", delayed = "1s")
    void run() {
        job.onScheduled(null);
    }
}
