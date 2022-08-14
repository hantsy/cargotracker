package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LocationDtoAssembler {

    public LocationDto toDto(
            Location location) {
        return new LocationDto(
                location.getUnLocode().getIdString(), location.getName());
    }

    public List<LocationDto> toDtoList(
            List<Location> allLocations) {
        List<LocationDto> dtoList =
                allLocations.stream()
                        .map(this::toDto)
                        .sorted(
                                Comparator.comparing(
                                        LocationDto
                                                ::getName))
                        .collect(Collectors.toList());
        return dtoList;
    }
}
