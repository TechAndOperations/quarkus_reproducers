package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@Path("/hello")
public class GreetingResource {

    @ConfigProperty(name = "foo_bar.foo")
    Optional<String> foobar;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/foobar")
    public String foobar() {
        return "foo_bar.foo=" + foobar;
    }
}
