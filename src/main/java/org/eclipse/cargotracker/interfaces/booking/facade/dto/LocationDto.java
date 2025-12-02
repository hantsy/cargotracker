package org.eclipse.cargotracker.interfaces.booking.facade.dto;

/** Location DTO. */
public record LocationDto(String code, String name) {

	public String nameAndCode() {
		return name + " (" + code + ")";
	}
}
