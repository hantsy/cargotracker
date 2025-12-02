package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.RoutingStatus;
import org.eclipse.cargotracker.domain.model.cargo.TransportStatus;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;

public class CargoRouteDtoAssembler {

	LocationDtoAssembler locationDtoAssembler = new LocationDtoAssembler();

	public CargoRouteDto toDto(Cargo cargo) {
		CargoRouteDto dto = new CargoRouteDto(cargo.getTrackingId().id(), locationDtoAssembler.toDto(cargo.getOrigin()),
				locationDtoAssembler.toDto(cargo.getRouteSpecification().destination()),
				cargo.getRouteSpecification().arrivalDeadline(),
				cargo.getDelivery().routingStatus().sameValueAs(RoutingStatus.MISROUTED),
				cargo.getDelivery().transportStatus().sameValueAs(TransportStatus.CLAIMED),
				locationDtoAssembler.toDto(cargo.getDelivery().lastKnownLocation()),
				cargo.getDelivery().transportStatus().name());

		cargo.getItinerary()
			.legs()
			.forEach(leg -> dto.addLeg(leg.getVoyage().getVoyageNumber().number(),
					locationDtoAssembler.toDto(leg.getLoadLocation()),
					locationDtoAssembler.toDto(leg.getUnloadLocation()), leg.getLoadTime(), leg.getUnloadTime()));

		return dto;
	}

}
