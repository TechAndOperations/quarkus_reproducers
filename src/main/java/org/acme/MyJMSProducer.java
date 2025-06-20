package org.acme;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.transaction.Transactional;
import org.apache.activemq.artemis.jms.client.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@ApplicationScoped
public class MyJMSProducer {

    Logger log = LoggerFactory.getLogger(MyJMSProducer.class);

    private final Queue queue = ActiveMQDestination.createQueue("jms.queue.MyQueue");

    @Inject
    ConnectionFactory factory;

    private int counter = 0;

    @Scheduled(every = "5s")
    void produceOneMessage() throws JMSException {
        send();
    }

    @SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    @Transactional
    public void send() throws JMSException {
        try (JMSContext context = factory.createContext()) {
            int correlationId = counter++;
            String msg = LocalDateTime.now() + "/" + correlationId;
            log.info("sending jms message {}", msg);
            JMSProducer producer = context.createProducer();
            TextMessage message = context.createTextMessage(msg);
            message.setJMSCorrelationID("correlation-id-" + correlationId);
            producer.send(queue, message);
        }
    }
}
