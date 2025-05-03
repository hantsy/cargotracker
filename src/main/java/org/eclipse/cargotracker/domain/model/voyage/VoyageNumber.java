package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

@Embeddable
//@formatter:off
public record VoyageNumber(

	@Column(name = "voyage_number")
	@NotBlank(message = "Voyage number cannot be blank")
	String number
) {
//@formatter:on

	public VoyageNumber {
		Objects.requireNonNull(number, "Voyage number is required");
	}

	boolean sameValueAs(VoyageNumber other) {
		return other != null && this.number.equals(other.number);
	}
}
