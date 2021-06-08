package org.eclipse.cargotracker.infrastructure.messaging;

public class JmsQueueNames {
    public static final String CARGO_HANDLED_QUEUE = "java:app/jms/CargoHandledQueue";
    public static final String MISDIRECTED_CARGO_QUEUE = "java:app/jms/MisdirectedCargoQueue";
    public static final String DELIVERED_CARGO_QUEUE = "java:app/jms/DeliveredCargoQueue";
    public static final String REJECTED_REGISTRATION_ATTEMPTS_QUEUE =
            "java:app/jms/RejectedRegistrationAttemptsQueue";
    public static final String HANDLING_EVENT_REGISTRATION_ATTEMPT_QUEUE =
            "java:app/jms/HandlingEventRegistrationAttemptQueue";

    private JmsQueueNames() {}
}
