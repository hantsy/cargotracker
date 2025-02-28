package org.eclipse.cargotracker.interfaces.booking.socket;

import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CargoInspectedWebSocketEventStub {
    private static final Logger LOGGER = Logger.getLogger(CargoInspectedWebSocketEventStub.class.getName());

    @Inject @CargoInspected Event<Cargo> cargoEvent;
    @Inject    ManagedScheduledExecutorService scheduledExecutorService;


    public void initialize(@Observes Startup event) {
        LOGGER.log(Level.INFO, "raise CDI event after 5 seconds...");
        scheduledExecutorService.schedule(this::raiseEvent, 5000, TimeUnit.MILLISECONDS);
    }

    private void raiseEvent() {
        Cargo cargo =
                new Cargo(
                        new TrackingId("AAA"),
                        new RouteSpecification(
                                SampleLocations.HONGKONG,
                                SampleLocations.NEWYORK,
                                LocalDate.now().plusMonths(6)));
        LOGGER.log(Level.INFO, "raise event: {0}", cargo);
        this.cargoEvent.fire(cargo);
    }
}
