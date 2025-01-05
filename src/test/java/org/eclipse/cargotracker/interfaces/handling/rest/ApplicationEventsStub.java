package org.eclipse.cargotracker.interfaces.handling.rest;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@ApplicationScoped
public class ApplicationEventsStub implements ApplicationEvents {

    HandlingEventRegistrationAttempt attempt;

    @Override
    public void cargoWasHandled(HandlingEvent event) {}

    @Override
    public void cargoWasMisdirected(Cargo cargo) {}

    @Override
    public void cargoHasArrived(Cargo cargo) {}

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        this.attempt = attempt;
    }

    public HandlingEventRegistrationAttempt getAttempt() {
        return attempt;
    }
}
