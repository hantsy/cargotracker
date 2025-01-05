package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.shared.Specification;

import java.time.LocalDate;

/**
 * Route specification. Describes where a cargo origin and destination is, and the arrival deadline.
 *
 * @param origin origin location - can't be the same as the destination
 * @param destination destination location - can't be the same as the origin
 * @param arrivalDeadline arrival deadline
 */
@Embeddable
public record RouteSpecification(
        @ManyToOne @JoinColumn(name = "spec_origin_id") Location origin,
        @ManyToOne @JoinColumn(name = "spec_destination_id") Location destination,
        @Column(name = "spec_arrival_deadline") @NotNull LocalDate arrivalDeadline)
        implements Specification<Itinerary> {

    public RouteSpecification {
        Validate.notNull(origin, "Origin is required");
        Validate.notNull(destination, "Destination is required");
        Validate.notNull(arrivalDeadline, "Arrival deadline is required");
        Validate.isTrue(
                !origin.sameIdentityAs(destination),
                "Origin and destination can't be the same: " + origin);
    }

    @Override
    public boolean isSatisfiedBy(Itinerary itinerary) {
        return itinerary != null
                && origin().sameIdentityAs(itinerary.getInitialDepartureLocation())
                && destination().sameIdentityAs(itinerary.getFinalArrivalLocation())
                && arrivalDeadline().isAfter(itinerary.getFinalArrivalDate().toLocalDate());
    }

    private boolean sameValueAs(RouteSpecification other) {
        return other != null
                && new EqualsBuilder()
                        .append(this.origin, other.origin)
                        .append(this.destination, other.destination)
                        .append(this.arrivalDeadline, other.arrivalDeadline)
                        .isEquals();
    }

    //    @Override
    //    public boolean equals(Object o) {
    //        if (this == o) {
    //            return true;
    //        }
    //        if (o == null || getClass() != o.getClass()) {
    //            return false;
    //        }
    //
    //        RouteSpecification that = (RouteSpecification) o;
    //
    //        return sameValueAs(that);
    //    }
    //
    //    @Override
    //    public int hashCode() {
    //        return new HashCodeBuilder()
    //                .append(this.origin)
    //                .append(this.destination)
    //                .append(this.arrivalDeadline)
    //                .toHashCode();
    //    }
    //
    //    @Override
    //    public String toString() {
    //        return "RouteSpecification{"
    //                + "origin="
    //                + origin
    //                + ", destination="
    //                + destination
    //                + ", arrivalDeadline="
    //                + arrivalDeadline
    //                + '}';
    //    }
}
