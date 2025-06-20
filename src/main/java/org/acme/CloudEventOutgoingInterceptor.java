package org.acme;

import io.smallrye.common.vertx.ContextLocals;
import io.smallrye.reactive.messaging.OutgoingInterceptor;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadataBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class CloudEventOutgoingInterceptor implements OutgoingInterceptor {

    private final Logger logger = LoggerFactory.getLogger(CloudEventOutgoingInterceptor.class);

    public static final String CORRELATIONID = "correlationid";
    public static final String APPLICATION = "application";

    @Override
    public Message<?> onMessage(Message<?> message) {

        OutgoingCloudEventMetadataBuilder<Object> builder = OutgoingCloudEventMetadata.builder();
        builder
                .withExtension(APPLICATION, "my-application")
                .withSource(URI.create("https://example.com/source"))
                .withType("my-type");
        try {
            Optional<Object> correlationId = ContextLocals.get("correlation-id");
            if (correlationId.isPresent()) {
                logger.info("found correlation ID in context locals: {}", correlationId.get());
                builder.withExtension(CORRELATIONID, correlationId.get()).build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving correlation ID from context locals: " + e);
        }

        return message.addMetadata(builder.build());
    }

    @Override
    public void onMessageAck(Message<?> message) {
    }

    @Override
    public void onMessageNack(Message<?> message, Throwable failure) {
    }
}
