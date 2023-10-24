package org.eclipse.cargotracker.interfaces.booking.facade.dto;

/** Location DTO. */
public record LocationDto(String unLocode, String name) {

    public String nameAndUnLocode() {
        return name + " (" + unLocode + ")";
    }
}
