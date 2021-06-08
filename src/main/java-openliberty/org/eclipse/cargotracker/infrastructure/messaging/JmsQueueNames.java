package org.eclipse.cargotracker.infrastructure.messaging;

// for Open liberty.
// removed the global profix.
public class JmsQueueNames {
    public static final String CARGO_HANDLED_QUEUE = "jms/CargoHandledQueue";
    public static final String MISDIRECTED_CARGO_QUEUE = "jms/MisdirectedCargoQueue";
    public static final String DELIVERED_CARGO_QUEUE = "jms/DeliveredCargoQueue";
    public static final String REJECTED_REGISTRATION_ATTEMPTS_QUEUE =
            "jms/RejectedRegistrationAttemptsQueue";
    public static final String HANDLING_EVENT_REGISTRATION_ATTEMPT_QUEUE =
            "jms/HandlingEventRegistrationAttemptQueue";

    private JmsQueueNames() {}
}
