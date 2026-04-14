package org.acme.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hello")
public class GreetingResource {

    private static final Logger log = LoggerFactory.getLogger(GreetingResource.class);

    @GET
    @Produces("text/plain")
    public String hello() {
        return "Hello world!";
    }

}