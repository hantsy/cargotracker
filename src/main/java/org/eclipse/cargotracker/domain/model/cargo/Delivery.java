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

	public static final HandlingActivity NO_ACTIVITY = HandlingActivity.EMPTY;

	public static Delivery of(RouteSpecification routeSpecification, Itinerary itinerary, HandlingEvent lastEvent) {
		var calculatedAt = LocalDateTime.now();
		var misdirected = calculateMisdirectionStatus(itinerary, lastEvent);
		var routingStatus = calculateRoutingStatus(itinerary, routeSpecification);
		var transportStatus = calculateTransportStatus(lastEvent);
		var lastKnownLocation = calculateLastKnownLocation(lastEvent);
		var currentVoyage = calculateCurrentVoyage(transportStatus, lastEvent);
		var eta = calculateEta(itinerary, routingStatus, misdirected);
		var nextExpectedActivity = calculateNextExpectedActivity(routeSpecification, itinerary, lastEvent,
				routingStatus, misdirected);
		var isUnloadedAtDestination = calculateUnloadedAtDestination(routeSpecification, lastEvent);

		return new Delivery(transportStatus, lastKnownLocation, currentVoyage, misdirected, eta, nextExpectedActivity,
				isUnloadedAtDestination, routingStatus, calculatedAt, lastEvent);
	}

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
		Objects.requireNonNull(routeSpecification, "Route specification is required");
		Objects.requireNonNull(handlingHistory, "Delivery history is required");

		HandlingEvent lastEvent = handlingHistory.getMostRecentlyCompletedEvent();

		return of(routeSpecification, itinerary, lastEvent);
	}

	/**
	 * Creates a new delivery snapshot to reflect changes in routing, i.e. when the route
	 * specification or the itinerary has changed but no additional handling of the cargo
	 * has been performed.
	 */
	Delivery updateOnRouting(RouteSpecification routeSpecification, Itinerary itinerary) {
		Objects.requireNonNull(routeSpecification, "Route specification is required");
		return Delivery.of(routeSpecification, itinerary, this.lastEvent);
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
	// After an empty HandlingActivity is persisted, when retrieving it from database it
	// is a
	// *NULL*.
	public HandlingActivity nextExpectedActivity() {
		return Objects.requireNonNullElse(nextExpectedActivity, NO_ACTIVITY);
	}

	private static TransportStatus calculateTransportStatus(HandlingEvent lastEvent) {
		if (lastEvent == null) {
			return NOT_RECEIVED;
		}

		return switch (lastEvent.getType()) {
			case LOAD -> ONBOARD_CARRIER;
			case UNLOAD, RECEIVE, CUSTOMS -> IN_PORT;
			case CLAIM -> CLAIMED;
			default -> UNKNOWN;
		};
	}

	private static Location calculateLastKnownLocation(HandlingEvent lastEvent) {
		if (lastEvent != null) {
			return lastEvent.getLocation();
		}
		else {
			return null;
		}
	}

	private static Voyage calculateCurrentVoyage(TransportStatus transportStatus, HandlingEvent lastEvent) {
		if (transportStatus.equals(ONBOARD_CARRIER) && lastEvent != null) {
			return lastEvent.getVoyage();
		}
		else {
			return null;
		}
	}

	private static boolean calculateMisdirectionStatus(Itinerary itinerary, HandlingEvent lastEvent) {
		if (lastEvent == null) {
			return false;
		}
		else {
			return !itinerary.isExpected(lastEvent);
		}
	}

	private static LocalDateTime calculateEta(Itinerary itinerary, RoutingStatus routingStatus, boolean misdirected) {
		if (onTrack(routingStatus, misdirected)) {
			return itinerary.finalArrivalDate();
		}
		else {
			return ETA_UNKOWN;
		}
	}

	private static HandlingActivity calculateNextExpectedActivity(RouteSpecification routeSpecification,
			Itinerary itinerary, HandlingEvent lastEvent, RoutingStatus routingStatus, boolean misdirected) {
		if (!onTrack(routingStatus, misdirected)) {
			return NO_ACTIVITY;
		}

		if (lastEvent == null) {
			return new HandlingActivity(HandlingEvent.Type.RECEIVE, routeSpecification.origin());
		}

		return switch (lastEvent.getType()) {
			case LOAD -> {
				for (Leg leg : itinerary.legs()) {
					if (leg.getLoadLocation().sameIdentityAs(lastEvent.getLocation())) {
						yield new HandlingActivity(HandlingEvent.Type.UNLOAD, leg.getUnloadLocation(), leg.getVoyage());
					}
				}
				yield NO_ACTIVITY;
			}
			case UNLOAD -> {
				for (Iterator<Leg> iterator = itinerary.legs().iterator(); iterator.hasNext();) {
					Leg leg = iterator.next();

					if (leg.getUnloadLocation().sameIdentityAs(lastEvent.getLocation())) {
						if (iterator.hasNext()) {
							Leg nextLeg = iterator.next();
							yield new HandlingActivity(HandlingEvent.Type.LOAD, nextLeg.getLoadLocation(),
									nextLeg.getVoyage());
						}
						else {
							yield new HandlingActivity(HandlingEvent.Type.CLAIM, leg.getUnloadLocation());
						}
					}
				}
				yield NO_ACTIVITY;
			}
			case RECEIVE -> {
				Leg firstLeg = itinerary.legs().iterator().next();
				yield new HandlingActivity(HandlingEvent.Type.LOAD, firstLeg.getLoadLocation(), firstLeg.getVoyage());
			}
			default -> NO_ACTIVITY;
		};
	}

	private static RoutingStatus calculateRoutingStatus(Itinerary itinerary, RouteSpecification routeSpecification) {
		if (itinerary == null || itinerary == Itinerary.EMPTY_ITINERARY) {
			return NOT_ROUTED;
		}
		else {
			if (routeSpecification.isSatisfiedBy(itinerary)) {
				return ROUTED;
			}
			else {
				return MISROUTED;
			}
		}
	}

	private static boolean calculateUnloadedAtDestination(RouteSpecification routeSpecification,
			HandlingEvent lastEvent) {
		return lastEvent != null && HandlingEvent.Type.UNLOAD.sameValueAs(lastEvent.getType())
				&& routeSpecification.destination().sameIdentityAs(lastEvent.getLocation());
	}

	private static boolean onTrack(RoutingStatus routingStatus, boolean misdirected) {
		return routingStatus.equals(ROUTED) && !misdirected;
	}

	private boolean sameValueAs(Delivery other) {
		return other != null && Objects.equals(this, (Delivery) other);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof Delivery)) {
			return false;
		}

		Delivery other = (Delivery) o;

		return sameValueAs(other);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this);
	}

	@Override
	public String toString() {
		return Objects.toString(this);
	}
}
