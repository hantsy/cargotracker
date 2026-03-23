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
public record RouteSpecification(

        @ManyToOne
        @JoinColumn(name = "spec_origin_id")
        Location origin,

        @ManyToOne
        @JoinColumn(name = "spec_destination_id")
        Location destination,

        @Column(name = "spec_arrival_deadline")
        @NotNull
        LocalDate arrivalDeadline
) implements Specification<Itinerary>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @param origin          origin location - can't be the same as the destination
     * @param destination     destination location - can't be the same as the origin
     * @param arrivalDeadline arrival deadline
     */
    public RouteSpecification {
        Objects.requireNonNull(origin, "Origin is required");
        Objects.requireNonNull(destination, "Destination is required");
        Objects.requireNonNull(arrivalDeadline, "Arrival deadline is required");
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Origin and destination can't be the same: " + origin);
        }
    }

    @Override
    public boolean isSatisfiedBy(Itinerary itinerary) {
        return itinerary != null
                && origin().equals(itinerary.getInitialDepartureLocation())
                && destination().equals(itinerary.getFinalArrivalLocation())
                && arrivalDeadline().isAfter(itinerary.getFinalArrivalDate().toLocalDate());
    }

}
