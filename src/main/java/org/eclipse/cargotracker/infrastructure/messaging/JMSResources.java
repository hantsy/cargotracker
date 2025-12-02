package org.eclipse.cargotracker.infrastructure.messaging;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSDestinationDefinition;

import java.util.logging.Level;
import java.util.logging.Logger;

// A custom connection factory can connect to an external message broker.
// @JMSConnectionFactoryDefinition(name = "java:comp/env/CargoTrackerCF")
@JMSDestinationDefinition(name = "java:app/jms/CargoHandledQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "CargoHandledQueue"
)
@JMSDestinationDefinition(name = "java:app/jms/MisdirectedCargoQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "MisdirectedCargoQueue"
)
@JMSDestinationDefinition(name = "java:app/jms/DeliveredCargoQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "DeliveredCargoQueue"
)
@JMSDestinationDefinition(name = "java:app/jms/RejectedRegistrationAttemptsQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "RejectedRegistrationAttemptsQueue"
)
@JMSDestinationDefinition(name = "java:app/jms/HandlingEventRegistrationAttemptQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "HandlingEventRegistrationAttemptQueue"
)
@ApplicationScoped
public class JMSResources {

    private static final Logger LOGGER = Logger.getLogger(JMSResources.class.getName());

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "JMS connectionFactory is available : {0} ", connectionFactory != null);
    }

}
