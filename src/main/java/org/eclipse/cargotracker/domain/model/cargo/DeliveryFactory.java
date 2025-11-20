package org.eclipse.cargotracker.domain.model.cargo;

import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;

import static org.eclipse.cargotracker.domain.model.cargo.RoutingStatus.*;
import static org.eclipse.cargotracker.domain.model.cargo.TransportStatus.*;

public final class DeliveryFactory {

    private DeliveryFactory() {
        // prevent it to instantiate
    }

    public static Delivery create(RouteSpecification routeSpecification, Itinerary itinerary, HandlingEvent lastEvent) {
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

    public static Delivery create(RouteSpecification routeSpecification, Itinerary itinerary, HandlingHistory handlingHistory) {
        Objects.requireNonNull(routeSpecification, "Route specification is required");
        Objects.requireNonNull(handlingHistory, "Delivery history is required");

        HandlingEvent lastEvent = handlingHistory.getMostRecentlyCompletedEvent();

        return create(routeSpecification, itinerary, lastEvent);
    }

    static TransportStatus calculateTransportStatus(HandlingEvent lastEvent) {
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

    static Location calculateLastKnownLocation(HandlingEvent lastEvent) {
        return lastEvent != null ? lastEvent.getLocation() : null;
    }

    static Voyage calculateCurrentVoyage(TransportStatus transportStatus, HandlingEvent lastEvent) {
        return transportStatus.equals(ONBOARD_CARRIER) && lastEvent != null ? lastEvent.getVoyage() : null;
    }

    static boolean calculateMisdirectionStatus(Itinerary itinerary, HandlingEvent lastEvent) {
        return lastEvent != null && !itinerary.isExpected(lastEvent);
    }

    static LocalDateTime calculateEta(Itinerary itinerary, RoutingStatus routingStatus, boolean misdirected) {
        if (onTrack(routingStatus, misdirected)) {
            return itinerary.finalArrivalDate();
        }
        return Delivery.ETA_UNKNOWN;
    }

    static HandlingActivity calculateNextExpectedActivity(RouteSpecification routeSpecification,
                                                          Itinerary itinerary, HandlingEvent lastEvent, RoutingStatus routingStatus, boolean misdirected) {
        if (!onTrack(routingStatus, misdirected)) {
            return Delivery.NO_ACTIVITY;
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
                yield Delivery.NO_ACTIVITY;
            }
            case UNLOAD -> {
                for (Iterator<Leg> iterator = itinerary.legs().iterator(); iterator.hasNext(); ) {
                    Leg leg = iterator.next();

                    if (leg.getUnloadLocation().sameIdentityAs(lastEvent.getLocation())) {
                        if (iterator.hasNext()) {
                            Leg nextLeg = iterator.next();
                            yield new HandlingActivity(HandlingEvent.Type.LOAD, nextLeg.getLoadLocation(),
                                    nextLeg.getVoyage());
                        } else {
                            yield new HandlingActivity(HandlingEvent.Type.CLAIM, leg.getUnloadLocation());
                        }
                    }
                }
                yield Delivery.NO_ACTIVITY;
            }
            case RECEIVE -> {
                Leg firstLeg = itinerary.legs().getFirst();
                yield new HandlingActivity(HandlingEvent.Type.LOAD, firstLeg.getLoadLocation(), firstLeg.getVoyage());
            }
            default -> Delivery.NO_ACTIVITY;
        };
    }

    static RoutingStatus calculateRoutingStatus(Itinerary itinerary, RouteSpecification routeSpecification) {
        if (itinerary == null || itinerary == Itinerary.EMPTY_ITINERARY) {
            return NOT_ROUTED;
        }

        return routeSpecification.isSatisfiedBy(itinerary) ? ROUTED : MISROUTED;
    }

    static boolean calculateUnloadedAtDestination(RouteSpecification routeSpecification, HandlingEvent lastEvent) {
        return lastEvent != null
                && HandlingEvent.Type.UNLOAD.sameValueAs(lastEvent.getType())
                && routeSpecification.destination().sameIdentityAs(lastEvent.getLocation());
    }

    static boolean onTrack(RoutingStatus routingStatus, boolean misdirected) {
        return routingStatus.equals(ROUTED) && !misdirected;
    }
}