package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The actual transportation of the cargo, as opposed to the customer requirement
 * (RouteSpecification) and the plan (Itinerary).
 */
@Embeddable
public record Delivery(
        @Enumerated(EnumType.STRING)
        @Column(name = "transport_status")
        @NotNull
        TransportStatus transportStatus,

        @ManyToOne
        @JoinColumn(name = "last_known_location_id")
        Location lastKnownLocation,

        @ManyToOne
        @JoinColumn(name = "current_voyage_id")
        Voyage currentVoyage,

        /*
          Check if cargo is misdirected.
          <p>

          <ul>
            <li>A cargo is misdirected if it is in a location that's not in the itinerary.
            <li>A cargo with no itinerary can not be misdirected.
            <li>A cargo that has received no handling events can not be misdirected.
          </ul>

          True if the cargo has been misdirected.
         */
        @NotNull
        @Column(name = "misdirected")
        boolean misdirected,

        @Column(name = "eta", secondPrecision = 0)
        LocalDateTime eta,

        @Embedded
        HandlingActivity nextExpectedActivity,


        /*
          True if the cargo has been unloaded at the final destination.
         */
        @NotNull
        @Column(name = "unloaded_at_dest")
        boolean unloadedAtDestination,

        @Enumerated(EnumType.STRING)
        @Column(name = "routing_status")
        @NotNull
        RoutingStatus routingStatus,

        @NotNull
        @Column(name = "calculated_at", secondPrecision = 0)
        LocalDateTime calculatedAt,

        @ManyToOne
        @JoinColumn(name = "last_event_id")
        HandlingEvent lastEvent
) implements Serializable {

    private static final long serialVersionUID = 1L;

    // Null object pattern.
    public static final LocalDateTime ETA_UNKOWN = null;
    // Null object pattern
    public static final HandlingActivity NO_ACTIVITY = HandlingActivity.EMPTY;

    /**
     * Compact constructor for validation.
     */
    public Delivery {
        Objects.requireNonNull(transportStatus, "Transport status is required");
        Objects.requireNonNull(routingStatus, "Routing status is required");
        Objects.requireNonNull(calculatedAt, "Calculated at is required");
    }

    @Override
    public Location lastKnownLocation() {
        return Objects.requireNonNullElse(lastKnownLocation, Location.UNKNOWN);
    }

    @Override
    public Voyage currentVoyage() {
        return Objects.requireNonNullElse(currentVoyage, Voyage.NONE);
    }

    public LocalDateTime estimatedTimeOfArrival() {
        return eta;
    }

    // Hibernate issue:
    // After an empty HandlingActivity is persisted, when retrieving it from database it is a
    // *NULL*.
    @Override
    public HandlingActivity nextExpectedActivity() {
        return Objects.requireNonNullElse(nextExpectedActivity, NO_ACTIVITY);
    }


}
