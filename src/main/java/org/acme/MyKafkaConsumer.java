package org.acme;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.ce.IncomingCloudEventMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.acme.CloudEventOutgoingInterceptor.APPLICATION;
import static org.acme.CloudEventOutgoingInterceptor.CORRELATIONID;

@ApplicationScoped
public class MyKafkaConsumer {

    Logger logger = LoggerFactory.getLogger(MyKafkaConsumer.class);

    @Incoming("words-in")
    public Uni<Void> consume(Message<String> message) {
        logger.info("Received kafka message: {}", message.getPayload());

        message.getMetadata(IncomingCloudEventMetadata.class).ifPresent(metadata -> {
            logger.info("metadata application: {}", metadata.getExtension(APPLICATION).orElse("======= MISSING!! ======="));
            logger.info("metadata correlationid: {}", metadata.getExtension(CORRELATIONID).orElse("======= MISSING!! ======="));
        });

        message.ack();
        return Uni.createFrom().voidItem();
    }
}
