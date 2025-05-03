package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class LocationDtoAssemblerTest {

	@Test
	public void toDto() {
		final LocationDtoAssembler assembler = new LocationDtoAssembler();
		var dto = assembler.toDto(SampleLocations.STOCKHOLM);
		assertThat(dto.code()).isEqualTo("SESTO");
		assertThat(dto.nameAndCode()).contains("Stockholm");
	}

	@Test
	public void toDtoList() {

		final LocationDtoAssembler assembler = new LocationDtoAssembler();
		final List<Location> locationList = Arrays.asList(SampleLocations.STOCKHOLM, SampleLocations.HAMBURG);

		final List<LocationDto> dtos = assembler.toDtoList(locationList);

		assertThat(dtos).hasSize(2);

		var dto = dtos.get(0);
		assertThat(dto.code()).isEqualTo("DEHAM");
		assertThat(dto.nameAndCode()).contains("Hamburg");

		// There is different from original version.
		// It is ordered by name.
		dto = dtos.get(1);
		assertThat(dto.code()).isEqualTo("SESTO");
		assertThat(dto.nameAndCode()).contains("Stockholm");
	}

}
