package org.eclipse.cargotracker.infrastructure.messaging.jms;

import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.infrastructure.messaging.JmsQueueNames;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Consumes JMS messages and delegates notification of misdirected cargo to the tracking service.
 *
 * <p>This is a programmatic hook into the JMS infrastructure to make cargo inspection
 * message-driven.
 */
@MessageDriven(
        activationConfig = {
            @ActivationConfigProperty(
                    propertyName = "destinationType",
                    propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(
                    propertyName = "destinationLookup",
                    propertyValue = JmsQueueNames.CARGO_HANDLED_QUEUE)
        })
public class CargoHandledConsumer implements MessageListener {

    @Inject private Logger logger;

    @Inject private CargoInspectionService cargoInspectionService;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String trackingIdString = textMessage.getText();

            cargoInspectionService.inspectCargo(new TrackingId(trackingIdString));
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error procesing JMS message", e);
        }
    }
}
