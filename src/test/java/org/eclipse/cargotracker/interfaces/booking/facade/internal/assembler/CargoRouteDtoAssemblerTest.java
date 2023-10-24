package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LegDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CargoRouteDtoAssemblerTest {

    @Test
    public void testToDTO() {
        final CargoRouteDtoAssembler assembler = new CargoRouteDtoAssembler();

        final Location origin = SampleLocations.STOCKHOLM;
        final Location destination = SampleLocations.MELBOURNE;
        final Cargo cargo =
                new Cargo(
                        new TrackingId("XYZ"),
                        new RouteSpecification(origin, destination, LocalDate.now()));

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

        cargo.assignToRoute(itinerary);

        final CargoRouteDto dto = assembler.toDto(cargo);

        assertThat(dto.legs()).hasSize(2);

        LegDto legDTO = dto.legs().get(0);
        assertThat(legDTO.voyageNumber()).isEqualTo("CM001");
        assertThat(legDTO.fromNameAndUnLcode())
                .contains("SESTO"); // this is a little different from original codes.
        assertThat(legDTO.toNameAndUnLocode()).contains("CNSHA");

        legDTO = dto.legs().get(1);
        assertThat(legDTO.voyageNumber()).isEqualTo("CM001");
        assertThat(legDTO.from().unLocode()).contains("NLRTM");
        assertThat(legDTO.to().unLocode()).contains("AUMEL");
    }

    @Test
    public void testToDTO_NoItinerary() {
        final CargoRouteDtoAssembler assembler = new CargoRouteDtoAssembler();

        final Cargo cargo =
                new Cargo(
                        new TrackingId("XYZ"),
                        new RouteSpecification(
                                SampleLocations.STOCKHOLM,
                                SampleLocations.MELBOURNE,
                                LocalDate.now()));
        final CargoRouteDto dto = assembler.toDto(cargo);

        assertThat(dto.trackingId()).isEqualTo("XYZ");
        assertThat(dto.origin().unLocode()).contains("SESTO");
        assertThat(dto.finalDestination().unLocode()).contains("AUMEL");
        assertThat(dto.legs().isEmpty()).isTrue();
    }
}
