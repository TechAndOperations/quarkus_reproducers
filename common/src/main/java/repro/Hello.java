package repro;

import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/hello")
public class Hello {
    private static final Logger LOG = Logger.getLogger(Hello.class);

    @GET
    public String hello() {
        LOG.info("hello called");
        return "hello";
    }

    void onStart(@Observes StartupEvent e) {
        LOG.info("started");
    }

    // Queue logs during shutdown so the batch worker still has records to export during teardown.
    void onStop(@Observes ShutdownEvent e) {
        for (int i = 0; i < 20; i++) {
            LOG.info("shutting down " + i);
        }
    }
}
