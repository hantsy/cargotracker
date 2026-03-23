package org.eclipse.cargotracker.domain.model.cargo;

import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static org.eclipse.cargotracker.domain.model.cargo.RoutingStatus.MISROUTED;
import static org.eclipse.cargotracker.domain.model.cargo.RoutingStatus.NOT_ROUTED;
import static org.eclipse.cargotracker.domain.model.cargo.RoutingStatus.ROUTED;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.CLAIMED;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.IN_PORT;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.NOT_RECEIVED;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.ONBOARD_CARRIER;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.UNKNOWN;

/**
 * Factory class for creating {@link Delivery} instances.
 * Contains all the complex calculation logic for determining cargo delivery status.
 */
public class DeliveryFactory {

    private static final Logger LOGGER = Logger.getLogger(DeliveryFactory.class.getName());

    private DeliveryFactory() {
        // Utility class, prevent instantiation
    }

    /**
     * Creates a new delivery snapshot based on the complete handling history of a cargo, as well as
     * its route specification and itinerary.
     *
     * @param routeSpecification route specification
     * @param itinerary          itinerary
     * @param handlingHistory    delivery history
     * @return An up to date delivery.
     */
    public static Delivery create(
            RouteSpecification routeSpecification,
            Itinerary itinerary,
            HandlingHistory handlingHistory) {
        Objects.requireNonNull(routeSpecification, "Route specification is required");
        Objects.requireNonNull(handlingHistory, "Delivery history is required");

        HandlingEvent lastEvent = handlingHistory.getMostRecentlyCompletedEvent();
        LocalDateTime calculatedAt = LocalDateTime.now();

        return new Delivery(
                calculateTransportStatus(lastEvent),
                calculateLastKnownLocation(lastEvent),
                calculateCurrentVoyage(lastEvent),
                calculateMisdirectionStatus(lastEvent, itinerary),
                calculateEta(lastEvent, itinerary),
                calculateNextExpectedActivity(lastEvent, routeSpecification, itinerary),
                calculateUnloadedAtDestination(lastEvent, routeSpecification),
                calculateRoutingStatus(itinerary, routeSpecification),
                calculatedAt,
                lastEvent
        );
    }

    /**
     * Creates a new delivery snapshot to reflect changes in routing, i.e. when the route
     * specification or the itinerary has changed but no additional handling of the cargo has been
     * performed.
     *
     * @param currentDelivery    the current delivery
     * @param routeSpecification route specification
     * @param itinerary          itinerary
     * @return An updated delivery based on routing changes.
     */
    public static Delivery updateOnRouting(
            Delivery currentDelivery,
            RouteSpecification routeSpecification,
            Itinerary itinerary) {
        Objects.requireNonNull(routeSpecification, "Route specification is required");
        Objects.requireNonNull(currentDelivery, "Current delivery is required");

        HandlingHistory handlingHistory = currentDelivery.lastEvent() == null
                ? HandlingHistory.EMPTY
                : new HandlingHistory(List.of(currentDelivery.lastEvent()));
        return create(routeSpecification, itinerary, handlingHistory);
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
        } else {
            return null;
        }
    }

    private static Voyage calculateCurrentVoyage(HandlingEvent lastEvent) {
        if (lastEvent != null && calculateTransportStatus(lastEvent).equals(ONBOARD_CARRIER)) {
            return lastEvent.getVoyage();
        } else {
            return null;
        }
    }

    /**
     * Check if cargo is misdirected.
     *
     * <p>
     *
     * <ul>
     *   <li>A cargo is misdirected if it is in a location that's not in the itinerary.
     *   <li>A cargo with no itinerary can not be misdirected.
     *   <li>A cargo that has received no handling events can not be misdirected.
     * </ul>
     *
     * @param lastEvent the last handling event
     * @param itinerary the itinerary
     * @return <code>true</code> if the cargo has been misdirected
     */
    private static boolean calculateMisdirectionStatus(HandlingEvent lastEvent, Itinerary itinerary) {
        if (lastEvent == null) {
            return false;
        } else {
            return !itinerary.isExpected(lastEvent);
        }
    }

    private static LocalDateTime calculateEta(HandlingEvent lastEvent, Itinerary itinerary) {
        if (onTrack(lastEvent, itinerary)) {
            return itinerary.getFinalArrivalDate();
        } else {
            return Delivery.ETA_UNKOWN;
        }
    }

    /**
     * @param lastEvent          the last handling event
     * @param routeSpecification route specification
     * @param itinerary          itinerary
     * @return the next expected handling activity
     */
    private static HandlingActivity calculateNextExpectedActivity(
            HandlingEvent lastEvent,
            RouteSpecification routeSpecification,
            Itinerary itinerary) {
        if (!onTrack(lastEvent, itinerary)) {
            return Delivery.NO_ACTIVITY;
        }

        if (lastEvent == null) {
            return new HandlingActivity(HandlingEvent.Type.RECEIVE, routeSpecification.origin());
        }

        return switch (lastEvent.getType()) {
            case LOAD -> {
                for (Leg leg : itinerary.getLegs()) {
                    if (leg.getLoadLocation().equals(lastEvent.getLocation())) {
                        yield new HandlingActivity(
                                HandlingEvent.Type.UNLOAD,
                                leg.getUnloadLocation(),
                                leg.getVoyage());
                    }
                }
                yield Delivery.NO_ACTIVITY;
            }
            case UNLOAD -> {
                for (Iterator<Leg> iterator = itinerary.getLegs().iterator();
                     iterator.hasNext(); ) {
                    Leg leg = iterator.next();

                    if (leg.getUnloadLocation().equals(lastEvent.getLocation())) {
                        if (iterator.hasNext()) {
                            Leg nextLeg = iterator.next();
                            yield new HandlingActivity(
                                    HandlingEvent.Type.LOAD,
                                    nextLeg.getLoadLocation(),
                                    nextLeg.getVoyage());
                        } else {
                            yield new HandlingActivity(
                                    HandlingEvent.Type.CLAIM, leg.getUnloadLocation());
                        }
                    }
                }
                yield Delivery.NO_ACTIVITY;
            }
            case RECEIVE -> {
                Leg firstLeg = itinerary.getLegs().iterator().next();
                yield new HandlingActivity(
                        HandlingEvent.Type.LOAD, firstLeg.getLoadLocation(), firstLeg.getVoyage());
            }
            default -> Delivery.NO_ACTIVITY;
        };
    }

    private static RoutingStatus calculateRoutingStatus(
            Itinerary itinerary, RouteSpecification routeSpecification) {
        if (itinerary == null || itinerary == Itinerary.EMPTY) {
            return NOT_ROUTED;
        } else {
            if (routeSpecification.isSatisfiedBy(itinerary)) {
                return ROUTED;
            } else {
                return MISROUTED;
            }
        }
    }

    private static boolean calculateUnloadedAtDestination(
            HandlingEvent lastEvent, RouteSpecification routeSpecification) {
        return lastEvent != null
                && HandlingEvent.Type.UNLOAD.equals(lastEvent.getType())
                && routeSpecification.destination().equals(lastEvent.getLocation());
    }

    private static boolean onTrack(HandlingEvent lastEvent, Itinerary itinerary) {
        RoutingStatus routingStatus = calculateRoutingStatus(itinerary, null);
        boolean misdirected = calculateMisdirectionStatus(lastEvent, itinerary);
        return routingStatus.equals(ROUTED) && !misdirected;
    }
}
