package org.eclipse.cargotracker.scenario;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

@ApplicationScoped
public class SynchronousApplicationEventsStub implements ApplicationEvents {

    @Inject Instance<CargoInspectionService> cargoInspectionServiceInstance;

    // no-args constructor required by CDI
    public SynchronousApplicationEventsStub() {}

    @Override
    public void cargoWasHandled(HandlingEvent event) {
        System.out.println("EVENT: cargo was handled: " + event);
        cargoInspectionServiceInstance.get().inspectCargo(event.getCargo().getTrackingId());
    }

    @Override
    public void cargoWasMisdirected(Cargo cargo) {
        System.out.println("EVENT: cargo was misdirected");
    }

    @Override
    public void cargoHasArrived(Cargo cargo) {
        System.out.println("EVENT: cargo has arrived: " + cargo.getTrackingId().getIdString());
    }

    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        System.out.println("EVENT: received handling event registration attempt");
    }
}
