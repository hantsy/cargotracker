package org.eclipse.cargotracker.domain.model.handling;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HandlingEventFactoryTest {

    // create mocks for deps
    private final CargoRepository cargoRepository = mock(CargoRepository.class);
    private final VoyageRepository voyageRepository = mock(VoyageRepository.class);
    private final LocationRepository locationRepository = mock(LocationRepository.class);

    private final Cargo cargo =
            new Cargo(
                    new TrackingId("ABC"),
                    new RouteSpecification(
                            SampleLocations.HELSINKI, SampleLocations.HONGKONG, LocalDate.now()));
    // declare HandlingEventFactory
    private HandlingEventFactory handlingEventFactory;

    @BeforeEach
    public void setUp() {
        this.handlingEventFactory =
                new HandlingEventFactory(cargoRepository, voyageRepository, locationRepository);
    }

    @AfterEach
    public void tearDown() {
        reset(cargoRepository, voyageRepository, locationRepository);
    }

    @Test
    public void testAllWorks() {
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(voyageRepository.find(any(VoyageNumber.class)))
                .thenReturn(SampleVoyages.HELSINKI_TO_HONGKONG);
        when(locationRepository.find(any(UnLocode.class))).thenReturn(SampleLocations.HELSINKI);

        try {
            this.handlingEventFactory.createHandlingEvent(
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    cargo.getTrackingId(),
                    SampleVoyages.HELSINKI_TO_HONGKONG.getVoyageNumber(),
                    SampleLocations.HELSINKI.getUnLocode(),
                    HandlingEvent.Type.LOAD);
        } catch (CannotCreateHandlingEventException e) {
            e.printStackTrace();
        }

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verify(voyageRepository, times(1)).find(any(VoyageNumber.class));
        verify(locationRepository, times(1)).find(any(UnLocode.class));

        verifyNoMoreInteractions(cargoRepository, voyageRepository, locationRepository);
    }

    @Test
    public void testAllWorks_VoyageNumberIsNullButNotRequiredWhenCreatingHandlingEvent() {
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(locationRepository.find(any(UnLocode.class))).thenReturn(SampleLocations.HONGKONG);

        try {
            this.handlingEventFactory.createHandlingEvent(
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    new TrackingId("ABC"),
                    null,
                    SampleLocations.HONGKONG.getUnLocode(),
                    HandlingEvent.Type.CLAIM);
        } catch (CannotCreateHandlingEventException e) {
            e.printStackTrace();
        }

        verify(cargoRepository, times(1)).find(any(TrackingId.class));

        verify(locationRepository, times(1)).find(any(UnLocode.class));
        verifyNoMoreInteractions(cargoRepository, voyageRepository, locationRepository);
        verifyNoInteractions(voyageRepository);
    }

    @Test
    public void testCargoIsUnknown() {
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(null);
        when(voyageRepository.find(any(VoyageNumber.class)))
                .thenReturn(SampleVoyages.HELSINKI_TO_HONGKONG);
        when(locationRepository.find(any(UnLocode.class))).thenReturn(SampleLocations.HELSINKI);

        assertThatThrownBy(
                        () -> {
                            this.handlingEventFactory.createHandlingEvent(
                                    LocalDateTime.now(),
                                    LocalDateTime.now(),
                                    new TrackingId("ABC"),
                                    SampleVoyages.HELSINKI_TO_HONGKONG.getVoyageNumber(),
                                    SampleLocations.HELSINKI.getUnLocode(),
                                    HandlingEvent.Type.LOAD);
                        })
                .isInstanceOf(UnknownCargoException.class);

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verifyNoInteractions(voyageRepository);
        verifyNoInteractions(locationRepository);
        verifyNoMoreInteractions(cargoRepository, voyageRepository, locationRepository);
    }

    @Test
    public void testVoyageIsUnknown() {
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(voyageRepository.find(any(VoyageNumber.class))).thenReturn(null);
        when(locationRepository.find(any(UnLocode.class))).thenReturn(SampleLocations.HELSINKI);

        assertThatThrownBy(
                        () -> {
                            this.handlingEventFactory.createHandlingEvent(
                                    LocalDateTime.now(),
                                    LocalDateTime.now(),
                                    new TrackingId("ABC"),
                                    SampleVoyages.HELSINKI_TO_HONGKONG.getVoyageNumber(),
                                    SampleLocations.HELSINKI.getUnLocode(),
                                    HandlingEvent.Type.LOAD);
                        })
                .isInstanceOf(UnknownVoyageException.class);

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verify(voyageRepository, times(1)).find(any(VoyageNumber.class));
        verifyNoInteractions(locationRepository);
        verifyNoMoreInteractions(cargoRepository, voyageRepository, locationRepository);
    }

    @Test
    public void testVoyageNumberIsNullButRequiredWhenCreatingHandlingEvent() {
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(locationRepository.find(any(UnLocode.class))).thenReturn(SampleLocations.HELSINKI);

        assertThatThrownBy(
                        () -> {
                            this.handlingEventFactory.createHandlingEvent(
                                    LocalDateTime.now(),
                                    LocalDateTime.now(),
                                    new TrackingId("ABC"),
                                    null,
                                    SampleLocations.HELSINKI.getUnLocode(),
                                    HandlingEvent.Type.LOAD);
                        })
                .isInstanceOf(CannotCreateHandlingEventException.class);

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verifyNoInteractions(voyageRepository);
        verify(locationRepository, times(1)).find(any(UnLocode.class));
        verifyNoMoreInteractions(cargoRepository, voyageRepository, locationRepository);
    }

    @Test
    public void testLocationIsUnknown() {
        when(cargoRepository.find(any(TrackingId.class))).thenReturn(cargo);
        when(voyageRepository.find(any(VoyageNumber.class)))
                .thenReturn(SampleVoyages.HELSINKI_TO_HONGKONG);
        when(locationRepository.find(any(UnLocode.class))).thenReturn(null);

        assertThatThrownBy(
                        () -> {
                            this.handlingEventFactory.createHandlingEvent(
                                    LocalDateTime.now(),
                                    LocalDateTime.now(),
                                    new TrackingId("ABC"),
                                    SampleVoyages.HELSINKI_TO_HONGKONG.getVoyageNumber(),
                                    SampleLocations.HELSINKI.getUnLocode(),
                                    HandlingEvent.Type.LOAD);
                        })
                .isInstanceOf(UnknownLocationException.class);

        verify(cargoRepository, times(1)).find(any(TrackingId.class));
        verify(voyageRepository, times(1)).find(any(VoyageNumber.class));
        verify(locationRepository, times(1)).find(any(UnLocode.class));
        verifyNoMoreInteractions(cargoRepository, voyageRepository, locationRepository);
    }
}
