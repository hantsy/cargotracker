package org.eclipse.cargotracker.domain.model.handling;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HandlingEventTest {

    private final TrackingId trackingId = new TrackingId("XYZ");
    private final RouteSpecification routeSpecification =
            new RouteSpecification(
                    SampleLocations.HONGKONG, SampleLocations.NEWYORK, LocalDate.now());
    private final Cargo cargo = new Cargo(trackingId, routeSpecification);

    @Test
    public void testNewWithCarrierMovement() {
        HandlingEvent event1 =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.LOAD,
                        SampleLocations.HONGKONG,
                        SampleVoyages.CM003);
        assertEquals(SampleLocations.HONGKONG, event1.getLocation());

        HandlingEvent event2 =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.UNLOAD,
                        SampleLocations.NEWYORK,
                        SampleVoyages.CM003);
        assertThat( event2.getLocation()).isEqualTo(SampleLocations.NEWYORK);

        // These event types prohibit a carrier movement association
        for (HandlingEvent.Type type :
                Arrays.asList(
                        HandlingEvent.Type.CLAIM,
                        HandlingEvent.Type.RECEIVE,
                        HandlingEvent.Type.CUSTOMS)) {
            try {
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        type,
                        SampleLocations.HONGKONG,
                        SampleVoyages.CM003);
                fail("Handling event type " + type + " prohibits carrier movement");
            } catch (IllegalArgumentException expected) {
            }
        }

        // These event types requires a carrier movement association
        for (HandlingEvent.Type type :
                Arrays.asList(HandlingEvent.Type.LOAD, HandlingEvent.Type.UNLOAD)) {
            try {
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        type,
                        SampleLocations.HONGKONG,
                        null);
                fail("Handling event type " + type + " requires carrier movement");
            } catch (NullPointerException expected) {
            }
        }
    }

    @Test
    public void testNewWithLocation() {
        HandlingEvent event1 =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.CLAIM,
                        SampleLocations.HELSINKI);
        assertThat( event1.getLocation()).isEqualTo(SampleLocations.HELSINKI);
    }

    @Test
    public void testCurrentLocationLoadEvent() throws Exception {
        HandlingEvent event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.LOAD,
                        SampleLocations.CHICAGO,
                        SampleVoyages.CM004);

        assertThat( event.getLocation()).isEqualTo(SampleLocations.CHICAGO);
    }

    @Test
    public void testCurrentLocationUnloadEvent() throws Exception {
        HandlingEvent ev =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.UNLOAD,
                        SampleLocations.HAMBURG,
                        SampleVoyages.CM004);

        assertThat( ev.getLocation()).isEqualTo(SampleLocations.HAMBURG);
    }

    @Test
    public void testCurrentLocationReceivedEvent() throws Exception {
        HandlingEvent event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.RECEIVE,
                        SampleLocations.CHICAGO);

        assertThat( event.getLocation()).isEqualTo(SampleLocations.CHICAGO);
    }

    @Test
    public void testCurrentLocationClaimedEvent() throws Exception {
        HandlingEvent event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.CLAIM,
                        SampleLocations.CHICAGO);

        assertThat( event.getLocation()).isEqualTo(SampleLocations.CHICAGO);
    }
}
