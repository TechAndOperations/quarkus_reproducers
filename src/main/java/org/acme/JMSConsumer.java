package org.acme;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkiverse.ironjacamar.ResourceEndpoint;

@ApplicationScoped
@ResourceEndpoint(activationSpecConfigKey = "myqueue")
public class JMSConsumer implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(JMSConsumer.class);

    static AtomicReference<String> last = new AtomicReference();

    @Override
    @Transactional
    public void onMessage(Message message) {
        try {
            String text = message.getBody(String.class);
            log.info("Received message with text {}", text);
            last.set(text);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
