package org.acme;

import io.quarkiverse.ironjacamar.ResourceEndpoint;
import io.smallrye.common.vertx.ContextLocals;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ResourceEndpoint(activationSpecConfigKey = "myqueue")
public class MyJMSConsumer implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MyJMSConsumer.class);

    @Inject
    @Channel("words-out")
    MutinyEmitter<String> emitter;

    @Override
    @Transactional
    public void onMessage(Message message) {
        try {
            try {
                ContextLocals.put("correlation-id", message.getJMSCorrelationID());
            } catch (Exception ex) {
                log.error("unable to set correlation id in duplicated context: " + ex);
            }
            String msg = message.getBody(String.class);
            log.info("receiving jms message " + msg);
            emitter.sendAndAwait(msg);
        } catch (JMSException e) {
            throw new RuntimeException("unable to get body from message " + message, e);
        }
    }
}
