package org.eclipse.cargotracker.domain.model.handling;

/**
 * If a {@link HandlingEvent} can't be created from a given set of parameters.
 *
 * <p>It is an unchecked exception (runtime exception) because it's not a programming error, but
 * rather a special case that the application is built to handle. It can occur during normal program
 * execution. Since it extends RuntimeException, callers are not required to catch or declare it.
 */
public class CannotCreateHandlingEventException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CannotCreateHandlingEventException(Exception e) {
        super(e);
    }

    public CannotCreateHandlingEventException() {
        super();
    }
}