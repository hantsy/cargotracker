package org.eclipse.cargotracker.interfaces.booking.facade.dto;

import java.time.LocalDateTime;

/** DTO for a leg in an itinerary. */
public record LegDto(
        String voyageNumber,
        LocationDto from,
        LocationDto to,
        LocalDateTime loadTime,
        LocalDateTime unloadTime) {

    public String fromNameAndUnLcode() {
        return from.nameAndCode();
    }

    public String toNameAndUnLocode() {
        return to.nameAndCode();
    }
}
