package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.application.util.DateUtil;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO [Clean Code] Could this be a CDI singleton?
public class ItineraryCandidateDtoAssembler {

    public RouteCandidateDto toDto(Itinerary itinerary) {
        List<LegDto> legDTOs =
                itinerary.getLegs().stream().map(this::toLegDTO).collect(Collectors.toList());
        return new RouteCandidateDto(legDTOs);
    }

    protected LegDto toLegDTO(Leg leg) {
        VoyageNumber voyageNumber = leg.getVoyage().getVoyageNumber();
        return new LegDto(
                voyageNumber.getIdString(),
                leg.getLoadLocation().getUnLocode().getIdString(),
                leg.getLoadLocation().getName(),
                leg.getUnloadLocation().getUnLocode().getIdString(),
                leg.getUnloadLocation().getName(),
                leg.getLoadTime(),
                leg.getUnloadTime());
    }

    public Itinerary fromDTO(
            RouteCandidateDto routeCandidateDTO,
            VoyageRepository voyageRepository,
            LocationRepository locationRepository) {
        List<Leg> legs = new ArrayList<>(routeCandidateDTO.getLegs().size());

        for (LegDto legDTO : routeCandidateDTO.getLegs()) {
            VoyageNumber voyageNumber = new VoyageNumber(legDTO.getVoyageNumber());
            Voyage voyage = voyageRepository.find(voyageNumber);
            Location from = locationRepository.find(new UnLocode(legDTO.getFromUnLocode()));
            Location to = locationRepository.find(new UnLocode(legDTO.getToUnLocode()));

            legs.add(
                    new Leg(
                            voyage,
                            from,
                            to,
                            DateUtil.toDateTime(legDTO.getLoadTime()),
                            DateUtil.toDateTime(legDTO.getUnloadTime())));
        }

        return new Itinerary(legs);
    }
}
