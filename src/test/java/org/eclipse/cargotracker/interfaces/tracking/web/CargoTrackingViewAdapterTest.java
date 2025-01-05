package org.eclipse.cargotracker.interfaces.tracking.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CargoTrackingViewAdapterTest {
    @Test
    public void testCreate() {
        Cargo cargo =
                new Cargo(
                        new TrackingId("XYZ"),
                        new RouteSpecification(
                                SampleLocations.HANGZOU,
                                SampleLocations.HELSINKI,
                                LocalDate.now()));

        List<HandlingEvent> events = new ArrayList<>();
        events.add(
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now().minusDays(9),
                        LocalDateTime.now().minusDays(10),
                        HandlingEvent.Type.RECEIVE,
                        SampleLocations.HANGZOU));

        events.add(
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now().minusDays(6),
                        LocalDateTime.now().minusDays(7),
                        HandlingEvent.Type.LOAD,
                        SampleLocations.HANGZOU,
                        SampleVoyages.CM001));
        events.add(
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now().minusDays(3),
                        LocalDateTime.now().minusDays(5),
                        HandlingEvent.Type.UNLOAD,
                        SampleLocations.HELSINKI,
                        SampleVoyages.CM001));

        cargo.deriveDeliveryProgress(new HandlingHistory(events));

        CargoTrackingViewAdapter adapter = new CargoTrackingViewAdapter(cargo, events);

        assertThat(adapter.getTrackingId()).isEqualTo("XYZ");
        assertThat(adapter.getOriginName()).isEqualTo("Hangzhou");
        assertThat(adapter.getDestinationName()).isEqualTo("Helsinki");
        assertThat(adapter.getStatusText()).isEqualTo("In port Helsinki");

        Iterator<CargoTrackingViewAdapter.HandlingEventViewAdapter> it =
                adapter.getEvents().iterator();

        CargoTrackingViewAdapter.HandlingEventViewAdapter event = it.next();
        // assertThat(event.getType()).isEqualTo("RECEIVE");
        // assertThat(event.getLocation()).isEqualTo("Hangzhou");
        // assertThat(event.getTime()).isEqualTo("1970-01-01 01:00");
        // assertThat(event.getVoyageNumber()).isEqualTo("");
        assertThat(event.isExpected()).isTrue();

        event = it.next();
        // assertThat(event.getType()).isEqualTo("LOAD");
        // assertThat(event.getLocation()).isEqualTo("Hangzhou");
        // assertThat(event.getTime()).isEqualTo("1970-01-01 01:00");
        // assertThat(event.getVoyageNumber()).isEqualTo("CM001");
        assertThat(event.isExpected()).isTrue();

        event = it.next();
        // assertThat(event.getType()).isEqualTo("UNLOAD");
        // assertThat(event.getLocation()).isEqualTo("Helsinki");
        // assertThat(event.getTime()).isEqualTo("1970-01-01 01:00");
        // assertThat(event.getVoyageNumber()).isEqualTo("CM001");
        assertThat(event.isExpected()).isTrue();
    }
}
