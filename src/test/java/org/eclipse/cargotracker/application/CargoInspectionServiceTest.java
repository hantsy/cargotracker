package org.eclipse.cargotracker.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.enterprise.event.Event;

import org.eclipse.cargotracker.application.internal.DefaultCargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CargoInspectionServiceTest {

    //
    private final ApplicationEvents applicationEvents = mock(ApplicationEvents.class);
    private final CargoRepository cargoRepository = mock(CargoRepository.class);
    private final HandlingEventRepository handlingEventRepository =
            mock(HandlingEventRepository.class);

    @SuppressWarnings("unchecked")
    private final Event<Cargo> cargoEvent = (Event<Cargo>) mock(Event.class);

    //
    private CargoInspectionService service;

    @BeforeEach
    public void setUp() {
        service =
                new DefaultCargoInspectionService(
                        applicationEvents, cargoRepository, handlingEventRepository, cargoEvent);
    }

    @AfterEach
    public void tearDown() {
        reset(applicationEvents, cargoRepository, handlingEventRepository, cargoEvent);
    }

    @Test
    public void testCargoIsNull() {
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(null);

        service.inspectCargo(new TrackingId("ABC123"));

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verifyNoMoreInteractions(cargoRepository);
        verifyNoInteractions(applicationEvents, handlingEventRepository, cargoEvent);
    }

    @Test
    public void testCargoIsInspected() {
        Cargo cargo =
                new Cargo(
                        new TrackingId("ABC"),
                        new RouteSpecification(
                                SampleLocations.DALLAS, SampleLocations.HONGKONG, LocalDate.now()));
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(handlingEventRepository.lookupHandlingHistoryOfCargo(any(TrackingId.class)))
                .thenReturn(new HandlingHistory(Collections.emptyList()));
        doNothing().when(applicationEvents).cargoWasMisdirected(any(Cargo.class));
        doNothing().when(applicationEvents).cargoHasArrived(any(Cargo.class));
        doNothing().when(cargoRepository).store(any(Cargo.class));
        doNothing().when(cargoEvent).fire(any(Cargo.class));

        service.inspectCargo(new TrackingId("ABC123"));

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verify(cargoRepository, times(1)).store(any(Cargo.class));
        verify(cargoEvent, times(1)).fire(any(Cargo.class));

        verifyNoMoreInteractions(cargoRepository, cargoEvent);
        verifyNoInteractions(applicationEvents);
    }

    @Test
    public void testCargoWasArrivedAsExpected() {
        Cargo cargo =
                new Cargo(
                        new TrackingId("ABC"),
                        new RouteSpecification(
                                SampleLocations.DALLAS, SampleLocations.HONGKONG, LocalDate.now()));
        cargo.assignToRoute(
                new Itinerary(
                        Arrays.asList(
                                new Leg(
                                        SampleVoyages.DALLAS_TO_HELSINKI,
                                        SampleLocations.DALLAS,
                                        SampleLocations.HELSINKI,
                                        LocalDateTime.now().minusDays(9),
                                        LocalDateTime.now().minusDays(9)),
                                new Leg(
                                        SampleVoyages.HELSINKI_TO_HONGKONG,
                                        SampleLocations.HELSINKI,
                                        SampleLocations.HONGKONG,
                                        LocalDateTime.now().minusDays(9),
                                        LocalDateTime.now().minusDays(9)))));
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(handlingEventRepository.lookupHandlingHistoryOfCargo(any(TrackingId.class)))
                .thenReturn(
                        new HandlingHistory(
                                Arrays.asList(
                                        new HandlingEvent(
                                                cargo,
                                                LocalDateTime.now().minusDays(10),
                                                LocalDateTime.now().minusDays(9),
                                                HandlingEvent.Type.RECEIVE,
                                                SampleLocations.DALLAS),
                                        new HandlingEvent(
                                                cargo,
                                                LocalDateTime.now().minusDays(9),
                                                LocalDateTime.now().minusDays(8),
                                                HandlingEvent.Type.LOAD,
                                                SampleLocations.DALLAS,
                                                SampleVoyages.DALLAS_TO_HELSINKI),
                                        new HandlingEvent(
                                                cargo,
                                                LocalDateTime.now().minusDays(8),
                                                LocalDateTime.now().minusDays(7),
                                                HandlingEvent.Type.UNLOAD,
                                                SampleLocations.HELSINKI,
                                                SampleVoyages.DALLAS_TO_HELSINKI),
                                        new HandlingEvent(
                                                cargo,
                                                LocalDateTime.now().minusDays(7),
                                                LocalDateTime.now().minusDays(6),
                                                HandlingEvent.Type.LOAD,
                                                SampleLocations.HELSINKI,
                                                SampleVoyages.HELSINKI_TO_HONGKONG),
                                        new HandlingEvent(
                                                cargo,
                                                LocalDateTime.now().minusDays(6),
                                                LocalDateTime.now().minusDays(5),
                                                HandlingEvent.Type.UNLOAD,
                                                SampleLocations.HONGKONG,
                                                SampleVoyages.HELSINKI_TO_HONGKONG))));
        doNothing().when(applicationEvents).cargoWasMisdirected(any(Cargo.class));
        doNothing().when(applicationEvents).cargoHasArrived(any(Cargo.class));
        doNothing().when(cargoRepository).store(any(Cargo.class));
        doNothing().when(cargoEvent).fire(any(Cargo.class));

        service.inspectCargo(new TrackingId("ABC123"));

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verify(applicationEvents, times(1)).cargoHasArrived(any(Cargo.class));
        verify(applicationEvents, times(0)).cargoWasMisdirected(any(Cargo.class));
        verify(cargoRepository, times(1)).store(any(Cargo.class));
        verify(cargoEvent, times(1)).fire(any(Cargo.class));

        verifyNoMoreInteractions(cargoRepository, cargoEvent, applicationEvents);
    }

    @Test
    public void testCargoWasMisredirected() {
        Cargo cargo =
                new Cargo(
                        new TrackingId("ABC"),
                        new RouteSpecification(
                                SampleLocations.DALLAS, SampleLocations.HONGKONG, LocalDate.now()));
        cargo.assignToRoute(
                new Itinerary(
                        List.of(
                                new Leg(
                                        SampleVoyages.DALLAS_TO_HELSINKI,
                                        SampleLocations.DALLAS,
                                        SampleLocations.HELSINKI,
                                        LocalDateTime.now().minusDays(9),
                                        LocalDateTime.now().minusDays(9)),
                                new Leg(
                                        SampleVoyages.HELSINKI_TO_HONGKONG,
                                        SampleLocations.HELSINKI,
                                        SampleLocations.HONGKONG,
                                        LocalDateTime.now().minusDays(9),
                                        LocalDateTime.now().minusDays(9)))));
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(handlingEventRepository.lookupHandlingHistoryOfCargo(any(TrackingId.class)))
                .thenReturn(
                        new HandlingHistory(
                                List.of(
                                        new HandlingEvent(
                                                cargo,
                                                LocalDateTime.now().minusDays(10),
                                                LocalDateTime.now().minusDays(9),
                                                HandlingEvent.Type.RECEIVE,
                                                SampleLocations.DALLAS),
                                        // load on wrong voyage.
                                        new HandlingEvent(
                                                cargo,
                                                LocalDateTime.now().minusDays(9),
                                                LocalDateTime.now().minusDays(7),
                                                HandlingEvent.Type.LOAD,
                                                SampleLocations.DALLAS,
                                                SampleVoyages.DALLAS_TO_HELSINKI_ALT))));
        doNothing().when(applicationEvents).cargoWasMisdirected(any(Cargo.class));
        doNothing().when(applicationEvents).cargoHasArrived(any(Cargo.class));
        doNothing().when(cargoRepository).store(any(Cargo.class));
        doNothing().when(cargoEvent).fire(any(Cargo.class));

        service.inspectCargo(new TrackingId("ABC123"));

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verify(applicationEvents, times(0)).cargoHasArrived(any(Cargo.class));
        verify(applicationEvents, times(1)).cargoWasMisdirected(any(Cargo.class));
        verify(cargoRepository, times(1)).store(any(Cargo.class));
        verify(cargoEvent, times(1)).fire(any(Cargo.class));

        verifyNoMoreInteractions(cargoRepository, cargoEvent, applicationEvents);
    }
}
