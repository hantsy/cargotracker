package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.cargotracker.domain.model.location.Location;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A carrier movement is a vessel voyage from one location to another.
 */
@Entity
@Table(name = "carrier_movements")
public class CarrierMovement implements Serializable {

    // Null object pattern
    public static final CarrierMovement NONE =
            new CarrierMovement(
                    Location.UNKNOWN, Location.UNKNOWN, LocalDateTime.MIN, LocalDateTime.MIN);
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

    // @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "departure_time")
    @NotNull
    private LocalDateTime departureTime;

    // @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "arrival_time")
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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || !(o instanceof CarrierMovement)) {
//            return false;
//        }
//
//        CarrierMovement that = (CarrierMovement) o;
//
//        return sameValueAs(that);
//    }
//
//    @Override
//    public int hashCode() {
//        return new HashCodeBuilder()
//                .append(this.departureLocation)
//                .append(this.departureTime)
//                .append(this.arrivalLocation)
//                .append(this.arrivalTime)
//                .toHashCode();
//    }


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

    private boolean sameValueAs(CarrierMovement other) {
        return other != null
                && new EqualsBuilder()
                .append(this.departureLocation, other.departureLocation)
                .append(this.departureTime, other.departureTime)
                .append(this.arrivalLocation, other.arrivalLocation)
                .append(this.arrivalTime, other.arrivalTime)
                .isEquals();
    }
}
