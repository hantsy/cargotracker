package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LegDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidateDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItineraryCandidateDtoAssemblerTest {
    @Test
    public void testToDto() {
        final ItineraryCandidateDtoAssembler assembler = new ItineraryCandidateDtoAssembler();

        final Location origin = SampleLocations.STOCKHOLM;
        final Location destination = SampleLocations.MELBOURNE;

        final Itinerary itinerary =
                new Itinerary(
                        Arrays.asList(
                                new Leg(
                                        SampleVoyages.CM001,
                                        origin,
                                        SampleLocations.SHANGHAI,
                                        LocalDateTime.now(),
                                        LocalDateTime.now()),
                                new Leg(
                                        SampleVoyages.CM001,
                                        SampleLocations.ROTTERDAM,
                                        destination,
                                        LocalDateTime.now(),
                                        LocalDateTime.now())));

        final RouteCandidateDto dto = assembler.toDto(itinerary);

        assertThat(dto.getLegs()).hasSize(2);
        var legDTO = dto.getLegs().get(0);
        assertThat(legDTO.getVoyageNumber()).isEqualTo("CM001");
        assertThat(legDTO.getFrom()).contains("SESTO");
        assertThat(legDTO.getTo()).contains("CNSHA");

        legDTO = dto.getLegs().get(1);
        assertThat(legDTO.getVoyageNumber()).isEqualTo("CM001");
        assertThat(legDTO.getFrom()).contains("NLRTM");
        assertThat(legDTO.getTo()).contains("AUMEL");
    }

    @Test
    public void testFromDto() {
        final ItineraryCandidateDtoAssembler assembler = new ItineraryCandidateDtoAssembler();

        var legs = new ArrayList<LegDto>();
        legs.add(
                new LegDto(
                        "CM001",
                        "AAAAA",
                        "A",
                        "BBBBB",
                        "B",
                        LocalDateTime.now(),
                        LocalDateTime.now()));
        legs.add(
                new LegDto(
                        "CM001",
                        "BBBBB",
                        "B",
                        "CCCCC",
                        "C",
                        LocalDateTime.now(),
                        LocalDateTime.now()));

        final LocationRepository locationRepository = mock(LocationRepository.class);
        when(locationRepository.find(new UnLocode("AAAAA"))).thenReturn(SampleLocations.HONGKONG);
        when(locationRepository.find(new UnLocode("BBBBB"))).thenReturn(SampleLocations.TOKYO);
        when(locationRepository.find(new UnLocode("CCCCC"))).thenReturn(SampleLocations.CHICAGO);

        final VoyageRepository voyageRepository = mock(VoyageRepository.class);
        when(voyageRepository.find(new VoyageNumber("CM001"))).thenReturn(SampleVoyages.CM001);

        // Tested call
        final Itinerary itinerary =
                assembler.fromDTO(
                        new RouteCandidateDto(legs), voyageRepository, locationRepository);

        assertThat(itinerary).isNotNull();
        assertThat(itinerary.getLegs()).isNotNull();
        assertThat(itinerary.getLegs()).hasSize(2);

        final Leg leg1 = itinerary.getLegs().get(0);
        assertThat(leg1).isNotNull();
        assertThat(leg1.getLoadLocation()).isEqualTo(SampleLocations.HONGKONG);
        assertThat(leg1.getUnloadLocation()).isEqualTo(SampleLocations.TOKYO);

        final Leg leg2 = itinerary.getLegs().get(1);
        assertThat(leg2).isNotNull();
        assertThat(leg2.getLoadLocation()).isEqualTo(SampleLocations.TOKYO);
        assertThat(leg2.getUnloadLocation()).isEqualTo(SampleLocations.CHICAGO);
    }
}
