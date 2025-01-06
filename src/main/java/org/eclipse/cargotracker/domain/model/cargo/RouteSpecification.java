package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.shared.Specification;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Route specification. Describes where a cargo origin and destination is, and the arrival deadline.
 */
@Embeddable
public class RouteSpecification implements Specification<Itinerary>, Serializable {

    private static final long serialVersionUID = 1L;

    // private static final Logger LOGGER = Logger.getLogger(RouteSpecification.class.getName());

    @ManyToOne
    @JoinColumn(name = "spec_origin_id")
    private Location origin;

    @ManyToOne
    @JoinColumn(name = "spec_destination_id")
    private Location destination;

    // @Temporal(TemporalType.DATE)
    @Column(name = "spec_arrival_deadline")
    @NotNull
    private LocalDate arrivalDeadline;

    public RouteSpecification() {}

    /**
     * @param origin origin location - can't be the same as the destination
     * @param destination destination location - can't be the same as the origin
     * @param arrivalDeadline arrival deadline
     */
    public RouteSpecification(Location origin, Location destination, LocalDate arrivalDeadline) {
        Validate.notNull(origin, "Origin is required");
        Validate.notNull(destination, "Destination is required");
        Validate.notNull(arrivalDeadline, "Arrival deadline is required");
        Validate.isTrue(
                !origin.sameIdentityAs(destination),
                "Origin and destination can't be the same: " + origin);

        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = arrivalDeadline;
    }

    public Location origin() {
        return origin;
    }

    public Location destination() {
        return destination;
    }

    public LocalDate arrivalDeadline() {
        return arrivalDeadline;
    }

    @Override
    public boolean isSatisfiedBy(Itinerary itinerary) {
        return itinerary != null
                && origin().sameIdentityAs(itinerary.initialDepartureLocation())
                && destination().sameIdentityAs(itinerary.finalArrivalLocation())
                && arrivalDeadline().isAfter(itinerary.finalArrivalDate().toLocalDate());
    }

    private boolean sameValueAs(RouteSpecification other) {
        return other != null
                && new EqualsBuilder()
                        .append(this.origin, other.origin)
                        .append(this.destination, other.destination)
                        .append(this.arrivalDeadline, other.arrivalDeadline)
                        .isEquals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RouteSpecification that = (RouteSpecification) o;

        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.origin)
                .append(this.destination)
                .append(this.arrivalDeadline)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "RouteSpecification{"
                + "origin="
                + origin
                + ", destination="
                + destination
                + ", arrivalDeadline="
                + arrivalDeadline
                + '}';
    }
}
