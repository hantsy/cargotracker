package org.eclipse.cargotracker.interfaces.booking.sse;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
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
public class CargoInspectedSseEventStub {
    private static final Logger LOGGER = Logger.getLogger(CargoInspectedSseEventStub.class.getName());

    @Inject @CargoInspected Event<Cargo> cargoEvent;

    @Inject ManagedScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void initialize() {
        LOGGER.log(Level.INFO, "raise event after 5 seconds...");
        scheduledExecutorService.schedule(
                () ->
                        cargoEvent.fire(
                                new Cargo(
                                        new TrackingId("AAA"),
                                        new RouteSpecification(
                                                SampleLocations.HONGKONG,
                                                SampleLocations.NEWYORK,
                                                LocalDate.now().plusMonths(6)))),
                5000,
                TimeUnit.MILLISECONDS);
    }
}
