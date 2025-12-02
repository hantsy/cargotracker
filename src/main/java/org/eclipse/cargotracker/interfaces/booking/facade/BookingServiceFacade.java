package org.eclipse.cargotracker.interfaces.booking.facade;

import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatusDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidateDto;

import java.time.LocalDate;
import java.util.List;

/**
 * This facade shields the domain layer - model, services, repositories - from concerns
 * about such things as the user interface and remote communication.
 */
public interface BookingServiceFacade {

	String bookNewCargo(String origin, String destination, LocalDate arrivalDeadline);

	CargoRouteDto loadCargoForRouting(String trackingId);

	CargoStatusDto loadCargoForTracking(String trackingId);

	void assignCargoToRoute(String trackingId, RouteCandidateDto route);

	void changeDestination(String trackingId, String destinationUnLocode);

	void changeDeadline(String trackingId, LocalDate arrivalDeadline);

	List<RouteCandidateDto> requestPossibleRoutesForCargo(String trackingId);

	List<LocationDto> listShippingLocations();

	List<CargoRouteDto> listAllCargos();

	List<String> listAllTrackingIds();

}
