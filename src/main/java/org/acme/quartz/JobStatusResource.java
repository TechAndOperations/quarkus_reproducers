package org.acme.quartz;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/rest/admin/quartz")
public class JobStatusResource {

    @Inject
    TestJob job;

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public JobStatus status() {
        return new JobStatus(job.getExecutions(), job.getExecutions() > 0);
    }

    public record JobStatus(int executions, boolean ran) {
    }
}
