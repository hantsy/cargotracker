package org.eclipse.cargotracker.interfaces.booking.facade.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.cargotracker.application.BookingService;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatusDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidateDto;
import org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler.CargoRouteDtoAssembler;
import org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler.CargoStatusDtoAssembler;
import org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler.ItineraryCandidateDtoAssembler;
import org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler.LocationDtoAssembler;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Transactional
public class DefaultBookingServiceFacade implements BookingServiceFacade, Serializable {

	private BookingService bookingService;

	private LocationRepository locationRepository;

	private CargoRepository cargoRepository;

	private VoyageRepository voyageRepository;

	private HandlingEventRepository handlingEventRepository;

	public DefaultBookingServiceFacade() {
	}

	@Inject
	public DefaultBookingServiceFacade(BookingService bookingService, LocationRepository locationRepository,
			CargoRepository cargoRepository, VoyageRepository voyageRepository,
			HandlingEventRepository handlingEventRepository) {
		this.bookingService = bookingService;
		this.locationRepository = locationRepository;
		this.cargoRepository = cargoRepository;
		this.voyageRepository = voyageRepository;
		this.handlingEventRepository = handlingEventRepository;
	}

	@Override
	public List<LocationDto> listShippingLocations() {
		List<Location> allLocations = locationRepository.findAll();
		LocationDtoAssembler assembler = new LocationDtoAssembler();
		return assembler.toDtoList(allLocations);
	}

	@Override
	public String bookNewCargo(String origin, String destination, LocalDate arrivalDeadline) {
		TrackingId trackingId = bookingService.bookNewCargo(new UnLocode(origin), new UnLocode(destination),
				arrivalDeadline);
		return trackingId.id();
	}

	@Override
	public CargoRouteDto loadCargoForRouting(String trackingId) {
		Cargo cargo = cargoRepository.find(new TrackingId(trackingId));
		CargoRouteDtoAssembler assembler = new CargoRouteDtoAssembler();
		return assembler.toDto(cargo);
	}

	@Override
	public void assignCargoToRoute(String trackingIdStr, RouteCandidateDto routeCandidateDTO) {
		Itinerary itinerary = new ItineraryCandidateDtoAssembler().fromDto(routeCandidateDTO, voyageRepository,
				locationRepository);
		TrackingId trackingId = new TrackingId(trackingIdStr);

		bookingService.assignCargoToRoute(itinerary, trackingId);
	}

	@Override
	public void changeDestination(String trackingId, String destinationUnLocode) {
		bookingService.changeDestination(new TrackingId(trackingId), new UnLocode(destinationUnLocode));
	}

	@Override
	public void changeDeadline(String trackingId, LocalDate arrivalDeadline) {
		bookingService.changeDeadline(new TrackingId(trackingId), arrivalDeadline);
	}

	@Override
	public List<CargoRouteDto> listAllCargos() {
		List<Cargo> cargos = cargoRepository.findAll();
		List<CargoRouteDto> routes;

		CargoRouteDtoAssembler assembler = new CargoRouteDtoAssembler();

		routes = cargos.stream().map(assembler::toDto).toList();

		return routes;
	}

	@Override
	public List<String> listAllTrackingIds() {
		List<String> trackingIds = new ArrayList<>();
		cargoRepository.findAll().forEach(cargo -> trackingIds.add(cargo.getTrackingId().id()));

		return trackingIds;
	}

	@Override
	public CargoStatusDto loadCargoForTracking(String trackingIdValue) {
		TrackingId trackingId = new TrackingId(trackingIdValue);
		Cargo cargo = cargoRepository.find(trackingId);

		if (cargo == null) {
			return null;
		}

		CargoStatusDtoAssembler assembler = new CargoStatusDtoAssembler();

		List<HandlingEvent> handlingEvents = handlingEventRepository.lookupHandlingHistoryOfCargo(trackingId)
			.getDistinctEventsByCompletionTime();

		return assembler.toDto(cargo, handlingEvents);
	}

	@Override
	public List<RouteCandidateDto> requestPossibleRoutesForCargo(String trackingId) {
		List<Itinerary> itineraries = bookingService.requestPossibleRoutesForCargo(new TrackingId(trackingId));

		ItineraryCandidateDtoAssembler dtoAssembler = new ItineraryCandidateDtoAssembler();

		return itineraries.stream().map(dtoAssembler::toDto).toList();
	}

}
