package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.shared.Specification;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Route specification. Describes where a cargo origin and destination is, and the arrival
 * deadline.
 *
 * @param origin          origin location - can't be the same as the destination
 * @param destination     destination location - can't be the same as the origin
 * @param arrivalDeadline arrival deadline
 */
@Embeddable
//@formatter:off
public record RouteSpecification(

	/** Origin location - can't be the same as the destination. */
	@ManyToOne
	@JoinColumn(name = "spec_origin_id")
	Location origin,

	/** Destination location - can't be the same as the origin. */
	@ManyToOne
	@JoinColumn(name = "spec_destination_id")
	Location destination,

	/** Arrival deadline. */
	@Column(name = "spec_arrival_deadline")
	@NotNull
	LocalDate arrivalDeadline
) implements Specification<Itinerary> {
//@formatter:on

    public RouteSpecification {
        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(destination, "Destination is required");
        Objects.requireNonNull(arrivalDeadline, "Arrival deadline is required");
        if (origin.sameIdentityAs(destination)) {
            throw new IllegalArgumentException("Origin and destination can't be the same: " + origin);
        }
    }

    @Override
    public boolean isSatisfiedBy(Itinerary itinerary) {
        return itinerary != null && origin.sameIdentityAs(itinerary.initialDepartureLocation())
                && destination.sameIdentityAs(itinerary.finalArrivalLocation())
                && arrivalDeadline.isAfter(itinerary.finalArrivalDate().toLocalDate());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RouteSpecification that = (RouteSpecification) o;
        return Objects.equals(origin, that.origin)
                && Objects.equals(destination, that.destination)
                && Objects.equals(arrivalDeadline, that.arrivalDeadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, arrivalDeadline);
    }
}
