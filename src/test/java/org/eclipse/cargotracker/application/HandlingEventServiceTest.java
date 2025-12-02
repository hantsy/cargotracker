package org.eclipse.cargotracker.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.cargotracker.application.internal.DefaultHandlingEventService;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventFactory;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HandlingEventServiceTest {

	private final ApplicationEvents applicationEvents = mock(ApplicationEvents.class);

	private final HandlingEventRepository handlingEventRepository = mock(HandlingEventRepository.class);

	private final HandlingEventFactory handlingEventFactory = mock(HandlingEventFactory.class);

	private final Cargo cargo = new Cargo(new TrackingId("ABC"),
			new RouteSpecification(SampleLocations.HAMBURG, SampleLocations.TOKYO, LocalDate.now()));

	private DefaultHandlingEventService service;

	@BeforeEach
	public void setUp() {
		service = new DefaultHandlingEventService(applicationEvents, handlingEventRepository, handlingEventFactory);
	}

	@Test
	public void testRegisterEvent() throws Exception {
		// assume
		when(handlingEventFactory.createHandlingEvent(any(LocalDateTime.class), any(LocalDateTime.class),
				any(TrackingId.class), any(VoyageNumber.class), any(UnLocode.class), any(HandlingEvent.Type.class)))
			.thenReturn(new HandlingEvent(cargo, LocalDateTime.now(), LocalDateTime.now(), HandlingEvent.Type.LOAD,
					SampleLocations.STOCKHOLM, SampleVoyages.CM001));
		doNothing().when(handlingEventRepository).store(any(HandlingEvent.class));
		doNothing().when(applicationEvents).cargoWasHandled(any(HandlingEvent.class));

		// call registerHandlingEvent
		service.registerHandlingEvent(LocalDateTime.now(), cargo.getTrackingId(), SampleVoyages.CM001.getVoyageNumber(),
				SampleLocations.STOCKHOLM.getUnLocode(), HandlingEvent.Type.LOAD);

		// verify
		verify(handlingEventFactory, times(1)).createHandlingEvent(any(LocalDateTime.class), any(LocalDateTime.class),
				any(TrackingId.class), any(VoyageNumber.class), any(UnLocode.class), any(HandlingEvent.Type.class));
		verify(handlingEventRepository, times(1)).store(any(HandlingEvent.class));
		verify(applicationEvents, times(1)).cargoWasHandled(any(HandlingEvent.class));

		verifyNoMoreInteractions(handlingEventFactory, handlingEventRepository, applicationEvents);
	}

}
