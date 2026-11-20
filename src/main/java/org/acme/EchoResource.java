package org.acme;

import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("/echo")
public class EchoResource {

    @POST
    @Path("/string")
    public String string(String body) {
        return body;
    }

    @POST
    @Path("/stream")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] stream(InputStream body) throws IOException {
        return body.readAllBytes();
    }
}
