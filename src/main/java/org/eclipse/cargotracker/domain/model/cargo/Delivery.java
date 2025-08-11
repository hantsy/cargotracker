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
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;

import static org.eclipse.cargotracker.domain.model.cargo.RoutingStatus.MISROUTED;
import static org.eclipse.cargotracker.domain.model.cargo.RoutingStatus.NOT_ROUTED;
import static org.eclipse.cargotracker.domain.model.cargo.RoutingStatus.ROUTED;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.CLAIMED;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.IN_PORT;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.NOT_RECEIVED;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.ONBOARD_CARRIER;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.UNKNOWN;

/**
 * The actual transportation of the cargo, as opposed to the customer requirement
 * (RouteSpecification) and the plan (Itinerary).
 */
@Embeddable
//@formatter:off
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

	/**
	 * Check if cargo is misdirected.
	 *
	 * <p>
	 *
	 * <ul>
	 * <li>A cargo is misdirected if it is in a location that's not in the itinerary.
	 * <li>A cargo with no itinerary can not be misdirected.
	 * <li>A cargo that has received no handling events can not be misdirected.
	 * </ul>
	 * @return <code>true</code> if the cargo has been misdirected,
	 */
	@NotNull
	@Column(name = "misdirected")
	boolean misdirected,

	@Column(name = "eta")
	LocalDateTime eta,

	@Embedded
	HandlingActivity nextExpectedActivity,

	@Column(name = "unloaded_at_dest")
	@NotNull
	boolean isUnloadedAtDestination,

	@Enumerated(EnumType.STRING)
	@Column(name = "routing_status")
	@NotNull
	RoutingStatus routingStatus,

	@Column(name = "calculated_at")
	@NotNull
	LocalDateTime calculatedAt,

	@ManyToOne
	@JoinColumn(name = "last_event_id")
	HandlingEvent lastEvent
) {
//@formatter:on
	// Null object pattern
	public static final LocalDateTime ETA_UNKOWN = null;
    public static final HandlingActivity NO_ACTIVITY = null;

    // Remove static factory methods and private static calculation methods

    /**
     * Creates a new delivery snapshot based on the complete handling history of a cargo,
     * as well as its route specification and itinerary.
     * @param routeSpecification route specification
     * @param itinerary itinerary
     * @param handlingHistory delivery history
     * @return An up to date delivery.
     */
    static Delivery derivedFrom(RouteSpecification routeSpecification, Itinerary itinerary,
            HandlingHistory handlingHistory) {
        return DeliveryFactory.create(routeSpecification, itinerary, handlingHistory);
    }

    Delivery updateOnRouting(RouteSpecification routeSpecification, Itinerary itinerary) {
        Objects.requireNonNull(routeSpecification, "Route specification is required");
        return DeliveryFactory.create(routeSpecification, itinerary, this.lastEvent);
    }

	public Location lastKnownLocation() {
		return Objects.requireNonNullElse(lastKnownLocation, Location.UNKNOWN);
	}

	public Voyage currentVoyage() {
		return Objects.requireNonNullElse(currentVoyage, Voyage.NONE);
	}

	public LocalDateTime estimatedTimeOfArrival() {
		return eta;
	}

	// Hibernate issue:
	// After an empty HandlingActivity is persisted, when retrieving it from database it is a NULL
	public HandlingActivity nextExpectedActivity() {
		return Objects.requireNonNullElse(nextExpectedActivity, NO_ACTIVITY);
	}

	private boolean sameValueAs(Delivery other) {
		return other != null && Objects.equals(this, (Delivery) other);
	}
}
