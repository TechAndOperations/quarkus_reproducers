package org.acme;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.quartz.*;

@ApplicationScoped
public class QuartzScheduledTask {

    @Inject
    Scheduler scheduler;

    public void run(@Observes final StartupEvent event) throws SchedulerException {
        final JobDetail job = JobBuilder.newJob(QuartzScheduledTaskJob.class)
                .build();
        final Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("* * */1 * * ?")
                        .withMisfireHandlingInstructionDoNothing())
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static final class QuartzScheduledTaskJob implements Job {

        @Override
        public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Executing QuartzScheduledTaskJob");
        }
    }
}
