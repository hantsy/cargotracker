package org.eclipse.cargotracker.infrastructure.messaging.jms;

import org.eclipse.cargotracker.infrastructure.messaging.JmsQueueNames;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(
        activationConfig = {
            @ActivationConfigProperty(
                    propertyName = "destinationType",
                    propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(
                    propertyName = "destinationLookup",
                    propertyValue = JmsQueueNames.MISDIRECTED_CARGO_QUEUE)
        })
public class MisdirectedCargoConsumer implements MessageListener {

    @Inject private Logger logger;

    @Override
    public void onMessage(Message message) {
        try {
            logger.log(
                    Level.INFO,
                    "Cargo with tracking ID {0} misdirected.",
                    message.getBody(String.class));
        } catch (JMSException ex) {
            logger.log(Level.WARNING, "Error processing message.", ex);
        }
    }
}
