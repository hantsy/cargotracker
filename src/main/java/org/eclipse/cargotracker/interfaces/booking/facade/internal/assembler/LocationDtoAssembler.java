package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;

import java.util.Comparator;
import java.util.List;

public class LocationDtoAssembler {

    public LocationDto toDto(Location location) {
        return new LocationDto(location.getUnLocode().value(), location.getName());
    }

    public List<LocationDto> toDtoList(List<Location> allLocations) {
        return allLocations.stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(LocationDto::nameAndCode))
                .toList();
    }
}
