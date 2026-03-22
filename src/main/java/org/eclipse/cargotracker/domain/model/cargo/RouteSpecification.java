package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.shared.Specification;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

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

    
    @Column(name = "spec_arrival_deadline")
    @NotNull
    private LocalDate arrivalDeadline;

    public RouteSpecification() {
    }

    /**
     * @param origin          origin location - can't be the same as the destination
     * @param destination     destination location - can't be the same as the origin
     * @param arrivalDeadline arrival deadline
     */
    public RouteSpecification(Location origin, Location destination, LocalDate arrivalDeadline) {
        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(destination, "Destination is required");
        Objects.requireNonNull(arrivalDeadline, "Arrival deadline is required");
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Origin and destination can't be the same: " + origin);
        }

        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = arrivalDeadline;
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public LocalDate getArrivalDeadline() {
        return arrivalDeadline;
    }

    @Override
    public boolean isSatisfiedBy(Itinerary itinerary) {
        return itinerary != null
                && getOrigin().equals(itinerary.getInitialDepartureLocation())
                && getDestination().equals(itinerary.getFinalArrivalLocation())
                && getArrivalDeadline().isAfter(itinerary.getFinalArrivalDate().toLocalDate());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RouteSpecification that)) return false;
        return Objects.equals(origin, that.origin)
                && Objects.equals(destination, that.destination)
                && Objects.equals(arrivalDeadline, that.arrivalDeadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, arrivalDeadline);
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
