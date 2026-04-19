package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.location.Location;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A carrier movement is a vessel voyage from one location to another.
 */
@Entity
@Table(name = "carrier_movements")
public class CarrierMovement implements Serializable {

    // Null object pattern
    public static final CarrierMovement NONE = new CarrierMovement(Location.UNKNOWN, Location.UNKNOWN, LocalDateTime.MIN, LocalDateTime.MIN);
    private static final long serialVersionUID = 1L;

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

    @Column(name = "departure_time", secondPrecision = 0)
    @NotNull
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", secondPrecision = 0)
    @NotNull
    private LocalDateTime arrivalTime;

    public CarrierMovement() {
        // Nothing to initialize.
    }

    public CarrierMovement(
            Location departureLocation,
            Location arrivalLocation,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime) {
        Objects.requireNonNull(departureLocation, "Departure location is required");
        Objects.requireNonNull(arrivalLocation, "Arrival location is required");
        Objects.requireNonNull(departureTime, "Departure time is required");
        Objects.requireNonNull(arrivalTime, "Arrival time is required");

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
}
