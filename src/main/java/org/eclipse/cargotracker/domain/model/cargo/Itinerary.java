package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Embeddable
//@formatter:off
public record Itinerary(

	/**
	 * The legs of the itinerary.
	 *
	 * <p>
	 * Hibernate issue: - Cascade delete doesn't work with `orphanRemoval = true`
	 * under WildFly/Hibernate. - `OrderColumn` persists the position of list elements
	 * in the database. - `@OrderBy` ensures the order of list elements in memory but
	 * may not work in all cases.
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "cargo_id")
	@OrderColumn(name = "leg_index")
	@Size(min = 1)
	@NotEmpty(message = "Legs must not be empty")
	List<Leg> legs
) {
//@formatter:on

	public static final Itinerary EMPTY_ITINERARY = new Itinerary(Collections.emptyList());

	public Itinerary {
		Objects.requireNonNull(legs, "Legs must not be null");
		if (legs.isEmpty()) {
			throw new IllegalArgumentException("Legs must not be empty");
		}
		if (legs.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("Legs must not contain null elements");
		}
		legs = List.copyOf(legs);
	}

	/** Test if the given handling event is expected when executing this itinerary. */
	public boolean isExpected(HandlingEvent event) {
		if (legs.isEmpty()) {
			return true;
		}

		// Check that the first leg's origin is the event's location
		// Check that the last leg's destination is from the event's
		// location
		return switch (event.getType()) {
			case RECEIVE -> {
				Leg leg = legs.getFirst();
				yield leg.getLoadLocation().equals(event.getLocation());
			}
			case LOAD -> legs.stream()
				.anyMatch(leg -> leg.getLoadLocation().equals(event.getLocation())
						&& leg.getVoyage().equals(event.getVoyage()));
			case UNLOAD ->
				// Check that the there is one leg with same unload location and
				// voyage
				legs.stream()
					.anyMatch(leg -> leg.getUnloadLocation().equals(event.getLocation())
							&& leg.getVoyage().equals(event.getVoyage()));
			case CLAIM -> {
				Leg leg = lastLeg();
				yield leg != null && leg.getUnloadLocation().equals(event.getLocation());
			}
			case CUSTOMS -> true;
			default -> throw new RuntimeException("Event case is not handled");
		};
	}

	Location initialDepartureLocation() {
		if (legs.isEmpty()) {
			return Location.UNKNOWN;
		}
		else {
			return legs.get(0).getLoadLocation();
		}
	}

	Location finalArrivalLocation() {
		if (legs.isEmpty()) {
			return Location.UNKNOWN;
		}
		else {
			return lastLeg().getUnloadLocation();
		}
	}

	/**
	 * @return Date when cargo arrives at final destination.
	 */
	LocalDateTime finalArrivalDate() {
		Leg lastLeg = lastLeg();

		if (lastLeg == null) {
			return LocalDateTime.MAX;
		}
		else {
			return lastLeg.getUnloadTime();
		}
	}

	/**
	 * @return The last leg on the itinerary.
	 */
	Leg lastLeg() {
		if (legs.isEmpty()) {
			return null;
		}
		else {
			return legs.getLast();
		}
	}

	private boolean sameValueAs(Itinerary other) {
		// return other != null && legs.equals(other.legs);
		//
		// Hibernate issue:
		// When comparing a `List` type property of an entity, it is also a proxy class in
		// runtime.
		// Use a `copyOf` to compare using the contained items temporally.
		return other != null && Objects.equals(List.copyOf(this.legs), List.copyOf(other.legs));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		// if (o == null || getClass() != o.getClass()) {
		// return false;
		// }
		//
		// https://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java
		// Hibernate issue:
		// `getClass() != o.getClass()` will fail if comparing the objects in different
		// transactions/sessions.
		// The generated dynamic proxies are always different classes.
		if (o == null || !(o instanceof Itinerary)) {
			return false;
		}

		Itinerary itinerary = (Itinerary) o;

		return sameValueAs(itinerary);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(List.copyOf(legs));
	}

	@Override
	public String toString() {
		return "Itinerary{" + "legs=" + legs + '}';
	}
}
