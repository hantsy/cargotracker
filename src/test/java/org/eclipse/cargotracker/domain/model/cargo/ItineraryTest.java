package org.eclipse.cargotracker.domain.model.cargo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItineraryTest {

    private Voyage voyage =
            new Voyage.Builder(new VoyageNumber("0123"), SampleLocations.SHANGHAI)
                    .addMovement(
                            SampleLocations.ROTTERDAM, LocalDateTime.now(), LocalDateTime.now())
                    .addMovement(
                            SampleLocations.GOTHENBURG, LocalDateTime.now(), LocalDateTime.now())
                    .build();
    private Voyage wrongVoyage =
            new Voyage.Builder(new VoyageNumber("666"), SampleLocations.NEWYORK)
                    .addMovement(
                            SampleLocations.STOCKHOLM, LocalDateTime.now(), LocalDateTime.now())
                    .addMovement(SampleLocations.HELSINKI, LocalDateTime.now(), LocalDateTime.now())
                    .build();

    @Test
    public void testCargoOnTrack() {
        TrackingId trackingId = new TrackingId("CARGO1");
        RouteSpecification routeSpecification =
                new RouteSpecification(
                        SampleLocations.SHANGHAI, SampleLocations.GOTHENBURG, LocalDate.now());
        Cargo cargo = new Cargo(trackingId, routeSpecification);

        Itinerary itinerary =
                new Itinerary(
                        Arrays.asList(
                                new Leg(
                                        voyage,
                                        SampleLocations.SHANGHAI,
                                        SampleLocations.ROTTERDAM,
                                        LocalDateTime.now(),
                                        LocalDateTime.now()),
                                new Leg(
                                        voyage,
                                        SampleLocations.ROTTERDAM,
                                        SampleLocations.GOTHENBURG,
                                        LocalDateTime.now(),
                                        LocalDateTime.now())));

        // Happy path
        HandlingEvent event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.RECEIVE,
                        SampleLocations.SHANGHAI);
        assertThat(itinerary.isExpected(event)).isTrue();

        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.LOAD,
                        SampleLocations.SHANGHAI,
                        voyage);
        assertThat(itinerary.isExpected(event)).isTrue();

        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.UNLOAD,
                        SampleLocations.ROTTERDAM,
                        voyage);
        assertThat(itinerary.isExpected(event)).isTrue();

        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.LOAD,
                        SampleLocations.ROTTERDAM,
                        voyage);
        assertThat(itinerary.isExpected(event)).isTrue();

        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.UNLOAD,
                        SampleLocations.GOTHENBURG,
                        voyage);
        assertThat(itinerary.isExpected(event)).isTrue();

        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.CLAIM,
                        SampleLocations.GOTHENBURG);
        assertThat(itinerary.isExpected(event)).isTrue();

        // Customs event changes nothing
        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.CUSTOMS,
                        SampleLocations.GOTHENBURG);
        assertThat(itinerary.isExpected(event)).isTrue();

        // Received at the wrong location
        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.RECEIVE,
                        SampleLocations.HANGZOU);
        assertThat(itinerary.isExpected(event)).isFalse();

        // Loaded to onto the wrong ship, correct location
        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.LOAD,
                        SampleLocations.ROTTERDAM,
                        wrongVoyage);
        assertThat(itinerary.isExpected(event)).isFalse();

        // Unloaded from the wrong ship in the wrong location
        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.UNLOAD,
                        SampleLocations.HELSINKI,
                        wrongVoyage);
        assertThat(itinerary.isExpected(event)).isFalse();

        event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.CLAIM,
                        SampleLocations.ROTTERDAM);
        assertThat(itinerary.isExpected(event)).isFalse();
    }

    @Test
    public void testNextExpectedEvent() {
        // TODO [TDD] Complete this test.
    }

    @Test
    public void testCreateItinerary() {
        try {
            @SuppressWarnings("unused")
            Itinerary itinerary = new Itinerary(new ArrayList<>());
            fail("An empty itinerary is not OK");
        } catch (IllegalArgumentException iae) {
            // Expected
        }

        try {
            List<Leg> legs = null;
            @SuppressWarnings("unused")
            Itinerary itinerary = new Itinerary(legs);
            fail("Null itinerary is not OK");
        } catch (NullPointerException npe) {
            // Expected
        }
    }
}
