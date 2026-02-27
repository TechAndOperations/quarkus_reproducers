package org.acme;

import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.apache.activemq.artemis.jms.client.ActiveMQDestination;

@Path("/jms")
public class JMSResource {

    public static final String QUEUE = "jms.queue.my-queue";

    private final Queue queue = ActiveMQDestination.createQueue(QUEUE);

    @Inject
    ConnectionFactory factory;

    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String send(@QueryParam("text") @DefaultValue("hello") String text,
            @QueryParam("count") @DefaultValue("1") int count) {
        try (JMSContext context = factory.createContext()) {
            JMSProducer producer = context.createProducer();
            for (int i = 0; i < count; i++) {
                TextMessage message = context.createTextMessage(text + "_" + i);
                producer.send(queue, message);
            }
        }
        return "OK: sent " + count + " message(s) with text " + text;
    }

    @GET
    @Path("/last")
    @Produces(MediaType.TEXT_PLAIN)
    public String last() {
        return "" + JMSConsumer.last;
    }
}
