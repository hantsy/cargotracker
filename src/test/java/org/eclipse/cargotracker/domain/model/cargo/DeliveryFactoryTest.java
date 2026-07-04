package org.eclipse.cargotracker.domain.model.cargo;

import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryFactoryTest {

    private final Voyage voyage = new Voyage.Builder(new VoyageNumber("0123"), SampleLocations.SHANGHAI)
            .addMovement(SampleLocations.ROTTERDAM, LocalDateTime.now(), LocalDateTime.now())
            .addMovement(SampleLocations.GOTHENBURG, LocalDateTime.now(), LocalDateTime.now())
            .build();

    @Test
    public void testNextExpectedEvent() {
        TrackingId trackingId = new TrackingId("CARGO1");
        RouteSpecification routeSpecification = new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.GOTHENBURG, LocalDate.now());
        Cargo cargo = new Cargo(trackingId, routeSpecification);
        Itinerary itinerary = new Itinerary(
                Arrays.asList(
                        new Leg(voyage, SampleLocations.SHANGHAI, SampleLocations.ROTTERDAM, LocalDateTime.now(), LocalDateTime.now()),
                        new Leg(voyage, SampleLocations.ROTTERDAM, SampleLocations.GOTHENBURG, LocalDateTime.now(), LocalDateTime.now())
                )
        );

        // No events
        assertThat(DeliveryFactory.calculateNextExpectedActivity(routeSpecification, itinerary, null, RoutingStatus.ROUTED, false))
                .isEqualTo(HandlingActivity.of(HandlingEvent.Type.RECEIVE, SampleLocations.SHANGHAI));

        // Received
        HandlingEvent event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.RECEIVE, SampleLocations.SHANGHAI);
        assertThat(DeliveryFactory.calculateNextExpectedActivity(routeSpecification, itinerary, event, RoutingStatus.ROUTED, false))
                .isEqualTo(HandlingActivity.of(HandlingEvent.Type.LOAD, SampleLocations.SHANGHAI, voyage));

        // Loaded
        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.LOAD, SampleLocations.SHANGHAI, voyage);
        assertThat(DeliveryFactory.calculateNextExpectedActivity(routeSpecification, itinerary, event, RoutingStatus.ROUTED, false))
                .isEqualTo(HandlingActivity.of(HandlingEvent.Type.UNLOAD, SampleLocations.ROTTERDAM, voyage));

        // Unloaded
        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.UNLOAD, SampleLocations.ROTTERDAM, voyage);
        assertThat(DeliveryFactory.calculateNextExpectedActivity(routeSpecification, itinerary, event, RoutingStatus.ROUTED, false))
                .isEqualTo(HandlingActivity.of(HandlingEvent.Type.LOAD, SampleLocations.ROTTERDAM, voyage));

        // Loaded again
        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.LOAD, SampleLocations.ROTTERDAM, voyage);
        assertThat(DeliveryFactory.calculateNextExpectedActivity(routeSpecification, itinerary, event, RoutingStatus.ROUTED, false))
                .isEqualTo(HandlingActivity.of(HandlingEvent.Type.UNLOAD, SampleLocations.GOTHENBURG, voyage));

        // Unloaded at destination
        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.UNLOAD, SampleLocations.GOTHENBURG, voyage);
        assertThat(DeliveryFactory.calculateNextExpectedActivity(routeSpecification, itinerary, event, RoutingStatus.ROUTED, false))
                .isEqualTo(HandlingActivity.of(HandlingEvent.Type.CLAIM, SampleLocations.GOTHENBURG));

        // Claimed
        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.CLAIM, SampleLocations.GOTHENBURG);
        assertThat(DeliveryFactory.calculateNextExpectedActivity(routeSpecification, itinerary, event, RoutingStatus.ROUTED, false))
                .isEqualTo(HandlingActivity.EMPTY);
    }

    @Test
    public void testCalculateTransportStatus() {
        assertThat(DeliveryFactory.calculateTransportStatus(null)).isEqualTo(TransportStatus.NOT_RECEIVED);

        Cargo cargo = new Cargo(new TrackingId("ABC"), new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.ROTTERDAM, LocalDate.now()));

        HandlingEvent event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.RECEIVE, SampleLocations.SHANGHAI);
        assertThat(DeliveryFactory.calculateTransportStatus(event)).isEqualTo(TransportStatus.IN_PORT);

        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.LOAD, SampleLocations.SHANGHAI, voyage);
        assertThat(DeliveryFactory.calculateTransportStatus(event)).isEqualTo(TransportStatus.ONBOARD_CARRIER);

        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.UNLOAD, SampleLocations.ROTTERDAM, voyage);
        assertThat(DeliveryFactory.calculateTransportStatus(event)).isEqualTo(TransportStatus.IN_PORT);

        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.CLAIM, SampleLocations.ROTTERDAM);
        assertThat(DeliveryFactory.calculateTransportStatus(event)).isEqualTo(TransportStatus.CLAIMED);
    }

    @Test
    public void testCalculateRoutingStatus() {
        RouteSpecification routeSpecification = new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.GOTHENBURG, LocalDate.now());

        assertThat(DeliveryFactory.calculateRoutingStatus(null, routeSpecification)).isEqualTo(RoutingStatus.NOT_ROUTED);
        assertThat(DeliveryFactory.calculateRoutingStatus(Itinerary.EMPTY, routeSpecification)).isEqualTo(RoutingStatus.NOT_ROUTED);

        Itinerary itinerary = new Itinerary(
                Arrays.asList(
                        new Leg(voyage, SampleLocations.SHANGHAI, SampleLocations.ROTTERDAM, LocalDateTime.now().minusDays(3L), LocalDateTime.now().minusDays(2L)),
                        new Leg(voyage, SampleLocations.ROTTERDAM, SampleLocations.GOTHENBURG, LocalDateTime.now().minusDays(2L), LocalDateTime.now().minusDays(1L))
                )
        );
        assertThat(DeliveryFactory.calculateRoutingStatus(itinerary, routeSpecification)).isEqualTo(RoutingStatus.ROUTED);

        RouteSpecification wrongRouteSpec = new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.NEWYORK, LocalDate.now());
        assertThat(DeliveryFactory.calculateRoutingStatus(itinerary, wrongRouteSpec)).isEqualTo(RoutingStatus.MISROUTED);
    }

    @Test
    public void testCalculateMisdirectionStatus() {
        TrackingId trackingId = new TrackingId("CARGO1");
        RouteSpecification routeSpecification = new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.GOTHENBURG, LocalDate.now());
        Cargo cargo = new Cargo(trackingId, routeSpecification);
        Itinerary itinerary = new Itinerary(
                Arrays.asList(
                        new Leg(voyage, SampleLocations.SHANGHAI, SampleLocations.ROTTERDAM, LocalDateTime.now(), LocalDateTime.now()),
                        new Leg(voyage, SampleLocations.ROTTERDAM, SampleLocations.GOTHENBURG, LocalDateTime.now(), LocalDateTime.now())
                )
        );

        assertThat(DeliveryFactory.calculateMisdirectionStatus(itinerary, null)).isFalse();

        HandlingEvent event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.RECEIVE, SampleLocations.SHANGHAI);
        assertThat(DeliveryFactory.calculateMisdirectionStatus(itinerary, event)).isFalse();

        HandlingEvent misdirectedEvent = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.RECEIVE, SampleLocations.NEWYORK);
        assertThat(DeliveryFactory.calculateMisdirectionStatus(itinerary, misdirectedEvent)).isTrue();
    }

    @Test
    public void testCalculateUnloadedAtDestination() {
        RouteSpecification routeSpecification = new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.GOTHENBURG, LocalDate.now());
        TrackingId trackingId = new TrackingId("CARGO1");
        Cargo cargo = new Cargo(trackingId, routeSpecification);

        assertThat(DeliveryFactory.calculateUnloadedAtDestination(routeSpecification, null)).isFalse();

        HandlingEvent event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.RECEIVE, SampleLocations.SHANGHAI);
        assertThat(DeliveryFactory.calculateUnloadedAtDestination(routeSpecification, event)).isFalse();

        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.UNLOAD, SampleLocations.ROTTERDAM, voyage);
        assertThat(DeliveryFactory.calculateUnloadedAtDestination(routeSpecification, event)).isFalse();

        event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.UNLOAD, SampleLocations.GOTHENBURG, voyage);
        assertThat(DeliveryFactory.calculateUnloadedAtDestination(routeSpecification, event)).isTrue();
    }

    @Test
    public void testCalculateLastKnownLocation() {
        assertThat(DeliveryFactory.calculateLastKnownLocation(null)).isNull();

        Cargo cargo = new Cargo(new TrackingId("ABC"), new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.ROTTERDAM, LocalDate.now()));
        HandlingEvent event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.RECEIVE, SampleLocations.SHANGHAI);

        assertThat(DeliveryFactory.calculateLastKnownLocation(event)).isEqualTo(SampleLocations.SHANGHAI);
    }

    @Test
    public void testCalculateCurrentVoyage() {
        assertThat(DeliveryFactory.calculateCurrentVoyage(TransportStatus.NOT_RECEIVED, null)).isNull();

        Cargo cargo = new Cargo(new TrackingId("ABC"), new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.ROTTERDAM, LocalDate.now()));
        HandlingEvent event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.LOAD, SampleLocations.SHANGHAI, voyage);

        assertThat(DeliveryFactory.calculateCurrentVoyage(TransportStatus.ONBOARD_CARRIER, event)).isEqualTo(voyage);
        assertThat(DeliveryFactory.calculateCurrentVoyage(TransportStatus.IN_PORT, event)).isNull();
    }

    @Test
    public void testCalculateEta() {
        LocalDateTime arrivalDate = LocalDateTime.now().plusDays(2);
        Itinerary itinerary = new Itinerary(
                Arrays.asList(
                        new Leg(voyage, SampleLocations.SHANGHAI, SampleLocations.GOTHENBURG, LocalDateTime.now(), arrivalDate)
                )
        );

        assertThat(DeliveryFactory.calculateEta(itinerary, RoutingStatus.ROUTED, false)).isEqualTo(arrivalDate);
        assertThat(DeliveryFactory.calculateEta(itinerary, RoutingStatus.MISROUTED, false)).isEqualTo(Delivery.ETA_UNKOWN);
        assertThat(DeliveryFactory.calculateEta(itinerary, RoutingStatus.ROUTED, true)).isEqualTo(Delivery.ETA_UNKOWN);
    }

    @Test
    public void testOnTrack() {
        assertThat(DeliveryFactory.onTrack(RoutingStatus.ROUTED, false)).isTrue();
        assertThat(DeliveryFactory.onTrack(RoutingStatus.MISROUTED, false)).isFalse();
        assertThat(DeliveryFactory.onTrack(RoutingStatus.ROUTED, true)).isFalse();
        assertThat(DeliveryFactory.onTrack(RoutingStatus.NOT_ROUTED, false)).isFalse();
    }

    @Test
    public void testCreateDelivery() {
        RouteSpecification routeSpecification = new RouteSpecification(SampleLocations.SHANGHAI, SampleLocations.GOTHENBURG, LocalDate.now());
        Itinerary itinerary = new Itinerary(
                Arrays.asList(
                        new Leg(voyage, SampleLocations.SHANGHAI, SampleLocations.ROTTERDAM, LocalDateTime.now().minusDays(3L), LocalDateTime.now().minusDays(2L)),
                        new Leg(voyage, SampleLocations.ROTTERDAM, SampleLocations.GOTHENBURG, LocalDateTime.now().minusDays(2L), LocalDateTime.now().minusDays(1L))
                )
        );
        TrackingId trackingId = new TrackingId("CARGO1");
        Cargo cargo = new Cargo(trackingId, routeSpecification);
        HandlingEvent event = new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.RECEIVE, SampleLocations.SHANGHAI);

        Delivery delivery = DeliveryFactory.create(routeSpecification, itinerary, event);

        assertThat(delivery.transportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(delivery.lastKnownLocation()).isEqualTo(SampleLocations.SHANGHAI);
        assertThat(delivery.routingStatus()).isEqualTo(RoutingStatus.ROUTED);
        assertThat(delivery.misdirected()).isFalse();
        assertThat(delivery.lastEvent()).isEqualTo(event);
    }
}
