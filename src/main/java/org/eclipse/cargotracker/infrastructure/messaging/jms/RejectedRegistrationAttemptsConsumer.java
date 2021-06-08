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
                    propertyValue = JmsQueueNames.REJECTED_REGISTRATION_ATTEMPTS_QUEUE)
        })
public class RejectedRegistrationAttemptsConsumer implements MessageListener {

    @Inject private Logger logger;

    @Override
    public void onMessage(Message message) {
        try {
            logger.log(
                    Level.INFO,
                    "Rejected registration attempt of cargo with tracking ID {0}.",
                    message.getBody(String.class));
        } catch (JMSException ex) {
            logger.log(Level.WARNING, "Error processing message.", ex);
        }
    }
}
