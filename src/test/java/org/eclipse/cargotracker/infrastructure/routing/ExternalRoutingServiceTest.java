package org.eclipse.cargotracker.infrastructure.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.infrastructure.routing.client.GraphTraversalResourceClient;
import org.eclipse.pathfinder.api.TransitEdge;
import org.eclipse.pathfinder.api.TransitPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ExternalRoutingServiceTest {

    private final VoyageRepository voyageRepository = mock(VoyageRepository.class);
    private final LocationRepository locationRepository = mock(LocationRepository.class);
    private final GraphTraversalResourceClient graphTraversalResourceClient =
            mock(GraphTraversalResourceClient.class);
    private ExternalRoutingService externalRoutingService;

    @BeforeEach
    public void setUp() {
        this.externalRoutingService =
                new ExternalRoutingService(
                        locationRepository, voyageRepository, graphTraversalResourceClient);
    }

    @Test
    public void testCalculatePossibleRoutes() {
        TrackingId trackingId = new TrackingId("ABC");
        RouteSpecification routeSpecification =
                new RouteSpecification(
                        SampleLocations.HONGKONG, SampleLocations.HELSINKI, LocalDate.now());
        Cargo cargo = new Cargo(trackingId, routeSpecification);

        when(voyageRepository.find(any(VoyageNumber.class))).thenReturn(SampleVoyages.CM002);
        when(locationRepository.find(SampleLocations.HONGKONG.getUnLocode()))
                .thenReturn(SampleLocations.HONGKONG);
        when(locationRepository.find(SampleLocations.CHICAGO.getUnLocode()))
                .thenReturn(SampleLocations.CHICAGO);
        when(locationRepository.find(SampleLocations.HELSINKI.getUnLocode()))
                .thenReturn(SampleLocations.HELSINKI);
        when(graphTraversalResourceClient.findShortestPath(anyString(), anyString()))
                .thenReturn(
                        List.of(
                                new TransitPath(
                                        List.of(
                                                new TransitEdge(
                                                        "CM002",
                                                        SampleLocations.HONGKONG
                                                                .getUnLocode()
                                                                .getIdString(),
                                                        SampleLocations.CHICAGO
                                                                .getUnLocode()
                                                                .getIdString(),
                                                        LocalDateTime.now().plusDays(1),
                                                        LocalDateTime.now().plusDays(10)),
                                                new TransitEdge(
                                                        "CM002",
                                                        SampleLocations.CHICAGO
                                                                .getUnLocode()
                                                                .getIdString(),
                                                        SampleLocations.HELSINKI
                                                                .getUnLocode()
                                                                .getIdString(),
                                                        LocalDateTime.now().plusDays(11),
                                                        LocalDateTime.now().plusDays(30))))));

        List<Itinerary> candidates =
                externalRoutingService.fetchRoutesForSpecification(routeSpecification);
        assertThat(candidates).isNotNull();

        for (Itinerary itinerary : candidates) {
            List<Leg> legs = itinerary.legs();
            assertThat(legs).isNotNull();
            assertThat(legs).isNotEmpty();

            // Cargo origin and start of first leg should match
            assertEquals(cargo.getOrigin(), legs.get(0).getLoadLocation());

            // Cargo final destination and last leg stop should match
            Location lastLegStop = legs.get(legs.size() - 1).getUnloadLocation();
            assertEquals(cargo.getRouteSpecification().destination(), lastLegStop);

            // Assert that all legs are connected
            for (int i = 0; i < legs.size() - 1; i++) {
                assertEquals(legs.get(i).getUnloadLocation(), legs.get(i + 1).getLoadLocation());
            }
        }

        verify(graphTraversalResourceClient, times(1)).findShortestPath(anyString(), anyString());
        verify(voyageRepository, atLeastOnce()).find(any(VoyageNumber.class));
        verify(locationRepository, atLeastOnce()).find(any(UnLocode.class));

        verifyNoMoreInteractions(
                voyageRepository, locationRepository, graphTraversalResourceClient);
    }
}
