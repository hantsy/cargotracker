package org.eclipse.cargotracker.infrastructure.messaging.jms;

import jakarta.annotation.Resource;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JmsApplicationEvents implements ApplicationEvents, Serializable {

    private static final long serialVersionUID = 1L;
    private static final int LOW_PRIORITY = 0;
    @Inject JMSContext jmsContext;

    @Resource(lookup = "java:app/jms/CargoHandledQueue")
    private Destination cargoHandledQueue;

    @jakarta.annotation.Resource(lookup = "java:app/jms/MisdirectedCargoQueue")
    private Destination misdirectedCargoQueue;

    @jakarta.annotation.Resource(lookup = "java:app/jms/DeliveredCargoQueue")
    private Destination deliveredCargoQueue;

    @jakarta.annotation.Resource(lookup = "java:app/jms/HandlingEventRegistrationAttemptQueue")
    private Destination handlingEventQueue;

    @Inject private Logger logger;

    @Override
    public void cargoWasHandled(HandlingEvent event) {
        Cargo cargo = event.getCargo();
        logger.log(Level.INFO, "Cargo was handled {0}", cargo);
        jmsContext
                .createProducer()
                .setPriority(LOW_PRIORITY)
                .setDisableMessageID(true)
                .setDisableMessageTimestamp(true)
                .send(cargoHandledQueue, cargo.getTrackingId().getIdString());
    }

    @Override
    public void cargoWasMisdirected(Cargo cargo) {
        logger.log(Level.INFO, "Cargo was misdirected {0}", cargo);
        jmsContext
                .createProducer()
                .setPriority(LOW_PRIORITY)
                .setDisableMessageID(true)
                .setDisableMessageTimestamp(true)
                .send(misdirectedCargoQueue, cargo.getTrackingId().getIdString());
    }

    @Override
    public void cargoHasArrived(Cargo cargo) {
        logger.log(Level.INFO, "Cargo has arrived {0}", cargo);
        jmsContext
                .createProducer()
                .setPriority(LOW_PRIORITY)
                .setDisableMessageID(true)
                .setDisableMessageTimestamp(true)
                .send(deliveredCargoQueue, cargo.getTrackingId().getIdString());
    }

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        logger.log(Level.INFO, "Received handling event registration attempt {0}", attempt);
        jmsContext
                .createProducer()
                .setPriority(LOW_PRIORITY)
                .setDisableMessageID(true)
                .setDisableMessageTimestamp(true)
                .setTimeToLive(1000)
                .send(handlingEventQueue, attempt);
    }
}
