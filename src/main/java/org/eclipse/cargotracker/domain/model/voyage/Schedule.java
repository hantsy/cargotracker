package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A voyage schedule. */
@Embeddable
public record Schedule(/** List of carrier movements. */
@NotNull @Size(min = 1) @OneToMany(cascade = CascadeType.ALL) @JoinColumn(name = "voyage_id") @OrderColumn(
		name = "cm_index") List<CarrierMovement> carrierMovements) {

	// Null object pattern.
	public static final Schedule EMPTY = new Schedule(Collections.emptyList());

	public Schedule {
		Objects.requireNonNull(carrierMovements, "Carrier movements is required");

		if (carrierMovements.isEmpty()) {
			throw new IllegalArgumentException("Carrier movements must not be empty");
		}
		carrierMovements.forEach(Objects::requireNonNull);
	}

	public List<CarrierMovement> getCarrierMovements() {
		return Collections.unmodifiableList(carrierMovements);
	}

	private boolean sameValueAs(Schedule other) {
		return other != null && Objects.equals(List.copyOf(carrierMovements), List.copyOf(other.carrierMovements));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Schedule that = (Schedule) o;

		return sameValueAs(that);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(List.copyOf(this.carrierMovements));
	}
}
