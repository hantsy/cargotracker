package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LegDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidateDto;

import java.util.List;

public class ItineraryCandidateDtoAssembler {
    LocationDtoAssembler locationDtoAssembler = new LocationDtoAssembler();

    public RouteCandidateDto toDto(Itinerary itinerary) {
        List<LegDto> legDTOs = itinerary.getLegs().stream().map(this::toLegDto).toList();
        return new RouteCandidateDto(legDTOs);
    }

    protected LegDto toLegDto(Leg leg) {
        VoyageNumber voyageNumber = leg.getVoyage().getVoyageNumber();
        return new LegDto(
                voyageNumber.getIdString(),
                locationDtoAssembler.toDto(leg.getLoadLocation()),
                locationDtoAssembler.toDto(leg.getUnloadLocation()),
                leg.getLoadTime(),
                leg.getUnloadTime());
    }

    public Itinerary fromDto(
            RouteCandidateDto routeCandidate,
            VoyageRepository voyageRepository,
            LocationRepository locationRepository) {
        List<Leg> legs =
                routeCandidate.legs().stream()
                        .map(
                                leg -> {
                                    VoyageNumber voyageNumber =
                                            new VoyageNumber(leg.voyageNumber());
                                    Voyage voyage = voyageRepository.find(voyageNumber);
                                    Location from =
                                            locationRepository.find(
                                                    new UnLocode(leg.from().unLocode()));
                                    Location to =
                                            locationRepository.find(
                                                    new UnLocode(leg.to().unLocode()));
                                    return new Leg(
                                            voyage, from, to, leg.loadTime(), leg.unloadTime());
                                })
                        .toList();

        return new Itinerary(legs);
    }
}
