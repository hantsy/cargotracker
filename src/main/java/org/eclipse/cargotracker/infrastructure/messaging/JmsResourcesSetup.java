package org.eclipse.cargotracker.infrastructure.messaging;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.ConnectionFactory;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import java.util.logging.Level;
import java.util.logging.Logger;

// A custom connection factory can connect to an external message broker.
// @JMSConnectionFactoryDefinition(name = "java:comp/env/CargoTrackerCF")
//
// @JMSDestinationDefinition is not repeatable.
@JMSDestinationDefinitions({
    @JMSDestinationDefinition(
            name = JmsQueueNames.CARGO_HANDLED_QUEUE,
            // resourceAdapter = "jmsra",
            interfaceName = "javax.jms.Queue",
            destinationName = "CargoHandledQueue"),
    @JMSDestinationDefinition(
            name = JmsQueueNames.MISDIRECTED_CARGO_QUEUE,
            // resourceAdapter = "jmsra",
            interfaceName = "javax.jms.Queue",
            destinationName = "MisdirectedCargoQueue"),
    @JMSDestinationDefinition(
            name = JmsQueueNames.DELIVERED_CARGO_QUEUE,
            // resourceAdapter = "jmsra",
            interfaceName = "javax.jms.Queue",
            destinationName = "DeliveredCargoQueue"),
    @JMSDestinationDefinition(
            name = JmsQueueNames.REJECTED_REGISTRATION_ATTEMPTS_QUEUE,
            // resourceAdapter = "jmsra",
            interfaceName = "javax.jms.Queue",
            destinationName = "RejectedRegistrationAttemptsQueue"),
    @JMSDestinationDefinition(
            name = JmsQueueNames.HANDLING_EVENT_REGISTRATION_ATTEMPT_QUEUE,
            // resourceAdapter = "jmsra",
            interfaceName = "javax.jms.Queue",
            destinationName = "HandlingEventRegistrationAttemptQueue")
})
@Startup
@Singleton
public class JmsResourcesSetup {
    private static final Logger LOGGER = Logger.getLogger(JmsResourcesSetup.class.getName());

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "JMS connectionFactory : {0} ", connectionFactory != null);
    }
}
