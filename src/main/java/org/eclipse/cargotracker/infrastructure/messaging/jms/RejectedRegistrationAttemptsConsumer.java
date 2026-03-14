package org.eclipse.cargotracker.infrastructure.messaging.jms;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(
        activationConfig = {
            @ActivationConfigProperty(
                    propertyName = "destinationType",
                    propertyValue = "jakarta.jms.Queue"),
            @ActivationConfigProperty(
                    propertyName = "destinationLookup",
                    propertyValue = "java:app/jms/RejectedRegistrationAttemptsQueue")
        })
public class RejectedRegistrationAttemptsConsumer implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(RejectedRegistrationAttemptsConsumer.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            LOGGER.log(
                    Level.INFO,
                    "Rejected registration attempt of cargo with tracking ID {0}.",
                    message.getBody(String.class));
        } catch (JMSException ex) {
            LOGGER.log(Level.WARNING, "Error processing message.", ex);
        }
    }
}