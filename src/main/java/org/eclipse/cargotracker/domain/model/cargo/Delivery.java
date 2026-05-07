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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The actual transportation of the cargo, as opposed to the customer requirement
 * (RouteSpecification) and the plan (Itinerary).
 */
@Embeddable
public class Delivery implements Serializable {
    // Null object pattern.
    public static final LocalDateTime ETA_UNKOWN = null;
    // Null object pattern
    public static final HandlingActivity NO_ACTIVITY = HandlingActivity.EMPTY;
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_status")
    @NotNull
    private TransportStatus transportStatus;

    @ManyToOne
    @JoinColumn(name = "last_known_location_id")
    private Location lastKnownLocation;

    @ManyToOne
    @JoinColumn(name = "current_voyage_id")
    private Voyage currentVoyage;

    @NotNull
    @Column(name = "misdirected")
    private boolean misdirected;

    @Column(name = "eta", secondPrecision = 0)
    private LocalDateTime eta;

    @Embedded
    private HandlingActivity nextExpectedActivity = null;

    @Column(name = "unloaded_at_dest")
    @NotNull
    private boolean isUnloadedAtDestination;

    @Enumerated(EnumType.STRING)
    @Column(name = "routing_status")
    @NotNull
    private RoutingStatus routingStatus;

    @Column(name = "calculated_at")
    @NotNull
    private LocalDateTime calculatedAt;

    @ManyToOne
    @JoinColumn(name = "last_event_id")
    private HandlingEvent lastEvent;

    public Delivery() {
        // Nothing to initialize
    }

    // New constructor for DeliveryFactory
    public Delivery(TransportStatus transportStatus, Location lastKnownLocation, Voyage currentVoyage,
                    boolean misdirected, LocalDateTime eta, HandlingActivity nextExpectedActivity,
                    boolean isUnloadedAtDestination, RoutingStatus routingStatus,
                    LocalDateTime calculatedAt, HandlingEvent lastEvent) {
        this.transportStatus = transportStatus;
        this.lastKnownLocation = lastKnownLocation;
        this.currentVoyage = currentVoyage;
        this.misdirected = misdirected;
        this.eta = eta;
        this.nextExpectedActivity = nextExpectedActivity;
        this.isUnloadedAtDestination = isUnloadedAtDestination;
        this.routingStatus = routingStatus;
        this.calculatedAt = calculatedAt;
        this.lastEvent = lastEvent;
    }

    public TransportStatus getTransportStatus() {
        return transportStatus;
    }

    public void setTransportStatus(TransportStatus transportStatus) {
        this.transportStatus = transportStatus;
    }

    public Location getLastKnownLocation() {
        return Objects.requireNonNullElse(lastKnownLocation, Location.UNKNOWN);
    }

    public void setLastKnownLocation(Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public Voyage getCurrentVoyage() {
        return Objects.requireNonNullElse(currentVoyage, Voyage.NONE);
    }

    public void setCurrentVoyage(Voyage currentVoyage) {
        this.currentVoyage = currentVoyage;
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
     * @return <code>true</code> if the cargo has been misdirected,
     */
    public boolean isMisdirected() {
        return misdirected;
    }

    public void setMisdirected(boolean misdirected) {
        this.misdirected = misdirected;
    }

    public LocalDateTime getEstimatedTimeOfArrival() {
        return eta;
    }

    public void setEta(LocalDateTime eta) {
        this.eta = eta;
    }

    // Hibernate issue:
    // After an empty HandlingActivity is persisted, when retrieving it from database it is a
    // *NULL*.
    public HandlingActivity getNextExpectedActivity() {
        // return nextExpectedActivity;
        return Objects.requireNonNullElse(nextExpectedActivity, NO_ACTIVITY);
    }

    public void setNextExpectedActivity(HandlingActivity nextExpectedActivity) {
        this.nextExpectedActivity = nextExpectedActivity;
    }

    /**
     * @return True if the cargo has been unloaded at the final destination.
     */
    public boolean isUnloadedAtDestination() {
        return isUnloadedAtDestination;
    }

    public void setUnloadedAtDestination(boolean isUnloadedAtDestination) {
        this.isUnloadedAtDestination = isUnloadedAtDestination;
    }

    public RoutingStatus getRoutingStatus() {
        return routingStatus;
    }

    public void setRoutingStatus(RoutingStatus routingStatus) {
        this.routingStatus = routingStatus;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public HandlingEvent getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(HandlingEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Delivery delivery)) return false;
        return misdirected == delivery.misdirected
                && isUnloadedAtDestination == delivery.isUnloadedAtDestination
                && transportStatus == delivery.transportStatus
                && Objects.equals(lastKnownLocation, delivery.lastKnownLocation)
                && Objects.equals(currentVoyage, delivery.currentVoyage)
                && Objects.equals(eta, delivery.eta)
                && Objects.equals(nextExpectedActivity, delivery.nextExpectedActivity)
                && routingStatus == delivery.routingStatus
                && Objects.equals(calculatedAt, delivery.calculatedAt)
                && Objects.equals(lastEvent, delivery.lastEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                transportStatus,
                lastKnownLocation,
                currentVoyage,
                misdirected,
                eta,
                nextExpectedActivity,
                isUnloadedAtDestination,
                routingStatus,
                calculatedAt,
                lastEvent
        );
    }
}
