package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.location.Location;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A carrier movement is a vessel voyage from one location to another.
 */
@Entity
@Table(name = "carrier_movements")
public class CarrierMovement {

    // Null object pattern
    public static final CarrierMovement NONE = new CarrierMovement(Location.UNKNOWN, Location.UNKNOWN,
            LocalDateTime.MIN, LocalDateTime.MIN);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "departure_location_id")
    @NotNull
    private Location departureLocation;

    @ManyToOne
    @JoinColumn(name = "arrival_location_id")
    @NotNull
    private Location arrivalLocation;

    @Column(name = "departure_time")
    @NotNull
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    @NotNull
    private LocalDateTime arrivalTime;

    public CarrierMovement() {
        // Nothing to initialize.
    }

    public CarrierMovement(Location departureLocation, Location arrivalLocation, LocalDateTime departureTime,
                           LocalDateTime arrivalTime) {
        Objects.requireNonNull(departureLocation, "Departure location must not be null");
        Objects.requireNonNull(arrivalLocation, "Arrival location must not be null");
        Objects.requireNonNull(departureTime, "Departure time must not be null");
        Objects.requireNonNull(arrivalTime, "Arrival time must not be null");
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.departureLocation = departureLocation;
        this.arrivalLocation = arrivalLocation;
    }

    public Location getDepartureLocation() {
        return departureLocation;
    }

    public Location getArrivalLocation() {
        return arrivalLocation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CarrierMovement that)) return false;
        return Objects.equals(departureLocation, that.departureLocation)
                && Objects.equals(arrivalLocation, that.arrivalLocation)
                && Objects.equals(departureTime, that.departureTime)
                && Objects.equals(arrivalTime, that.arrivalTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departureLocation, arrivalLocation, departureTime, arrivalTime);
    }

    @Override
    public String toString() {
        return "CarrierMovement{" +
                "id=" + id +
                ", departureLocation=" + departureLocation +
                ", arrivalLocation=" + arrivalLocation +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                '}';
    }
}
