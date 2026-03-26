package org.eclipse.cargotracker.interfaces.handling;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;

@ApplicationScoped
public class ApplicationEventsStub implements ApplicationEvents {

    private final List<HandlingEventRegistrationAttempt> attempts = new ArrayList<>();

    @Override
    public void cargoWasHandled(HandlingEvent event) {}

    @Override
    public void cargoWasMisdirected(Cargo cargo) {}

    @Override
    public void cargoHasArrived(Cargo cargo) {}

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        this.attempts.add(attempt);
    }

    public List<HandlingEventRegistrationAttempt> getAttempts() {
        return attempts;
    }

    public void clear() {
        this.attempts.clear();
    }
}
