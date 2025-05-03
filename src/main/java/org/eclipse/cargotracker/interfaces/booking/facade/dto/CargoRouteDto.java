package org.eclipse.cargotracker.interfaces.booking.facade.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DTO for registering and routing a cargo. */
public record CargoRouteDto(String trackingId, LocationDto origin, LocationDto finalDestination,
		LocalDate arrivalDeadline, boolean misrouted, boolean claimed, LocationDto lastKnownLocation,
		String transportStatus, List<LegDto> legs) {

	public CargoRouteDto(String trackingId, LocationDto origin, LocationDto finalDestination, LocalDate arrivalDeadline,
			boolean misrouted, boolean claimed, LocationDto lastKnownLocation, String transportStatus) {
		this(trackingId, origin, finalDestination, arrivalDeadline, misrouted, claimed, lastKnownLocation,
				transportStatus, new ArrayList<>());
	}

	public void addLeg(String voyageNumber, LocationDto from, LocationDto to, LocalDateTime loadTime,
			LocalDateTime unloadTime) {
		legs.add(new LegDto(voyageNumber, from, to, loadTime, unloadTime));
	}

	public boolean routed() {
		return !legs.isEmpty();
	}
}
