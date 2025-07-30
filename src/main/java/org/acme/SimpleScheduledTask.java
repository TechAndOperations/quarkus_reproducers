package org.acme;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimpleScheduledTask {

    @Scheduled(every = "1s", executeWith = Scheduled.SIMPLE)
    public void run() {
        System.out.println("Executing SimpleScheduledTask");
    }
}
