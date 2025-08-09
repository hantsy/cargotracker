package org.eclipse.cargotracker.domain.model.handling;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A HandlingEvent is used to register the event when, for instance, a cargo is unloaded
 * from a carrier at a some location at a given time.
 *
 * <p>
 * The HandlingEvent's are sent from different Incident Logging Applications some time
 * after the event occurred and contain information about the null {@link TrackingId},
 * {@link Location}, time stamp of the completion of the event, and possibly, if
 * applicable a {@link Voyage}.
 *
 * <p>
 * This class is the only member, and consequently the root, of the HandlingEvent
 * aggregate.
 *
 * <p>
 * HandlingEvent's could contain information about a {@link Voyage} and if so, the event
 * type must be either {@link Type#LOAD} or {@link Type#UNLOAD}.
 *
 * <p>
 * All other events must be of {@link Type#RECEIVE}, {@link Type#CLAIM} or
 * {@link Type#CUSTOMS}.
 */
@Entity
@Table(name = "handling_events")
@NamedQuery(name = "HandlingEvent.findByTrackingId",
        query = "Select e from HandlingEvent e where e.cargo.trackingId = :trackingId")
public class HandlingEvent {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;

    @ManyToOne
    @JoinColumn(name = "voyage_id")
    private Voyage voyage;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @NotNull
    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    @NotNull
    @Column(name = "registration_time")
    private LocalDateTime registrationTime;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @Transient
    private String summary;

    public HandlingEvent() {
        // Nothing to initialize.
    }

    /**
     * @param cargo            The cargo
     * @param completionTime   completion time, the reported time that the event actually
     *                         happened (e.g. the receive took place).
     * @param registrationTime registration time, the time the message is received
     * @param type             type of event
     * @param location         where the event took place
     * @param voyage           the voyage
     */
    public HandlingEvent(Cargo cargo, LocalDateTime completionTime, LocalDateTime registrationTime, Type type,
                         Location location, Voyage voyage) {
        Objects.requireNonNull(cargo, "Cargo is required");
        Objects.requireNonNull(completionTime, "Completion time is required");
        Objects.requireNonNull(registrationTime, "Registration time is required");
        Objects.requireNonNull(type, "Handling event type is required");
        Objects.requireNonNull(location, "Location is required");
        Objects.requireNonNull(voyage, "Voyage is required");

        if (type.prohibitsVoyage()) {
            throw new IllegalArgumentException("Voyage is not allowed with event type " + type);
        }

        this.voyage = voyage;
        this.completionTime = completionTime;
        this.registrationTime = registrationTime;
        this.type = type;
        this.location = location;
        this.cargo = cargo;
    }

    /**
     * @param cargo            cargo
     * @param completionTime   completion time, the reported time that the event actually
     *                         happened (e.g. the receive took place).
     * @param registrationTime registration time, the time the message is received
     * @param type             type of event
     * @param location         where the event took place
     */
    public HandlingEvent(Cargo cargo, LocalDateTime completionTime, LocalDateTime registrationTime, Type type,
                         Location location) {
        Objects.requireNonNull(cargo, "Cargo is required");
        Objects.requireNonNull(completionTime, "Completion time is required");
        Objects.requireNonNull(registrationTime, "Registration time is required");
        Objects.requireNonNull(type, "Handling event type is required");
        Objects.requireNonNull(location, "Location is required");

        if (type.requiresVoyage()) {
            throw new IllegalArgumentException("Voyage is required for event type " + type);
        }

        this.completionTime = completionTime;
        this.registrationTime = registrationTime;
        this.type = type;
        this.location = location;
        this.cargo = cargo;
        this.voyage = null;
    }

    public Type getType() {
        return this.type;
    }

    public Voyage getVoyage() {
        return Objects.requireNonNullElse(this.voyage, Voyage.NONE);
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public Location getLocation() {
        return this.location;
    }

    public Cargo getCargo() {
        return this.cargo;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HandlingEvent that)) return false;
        return type == that.type
                && Objects.equals(voyage, that.voyage)
                && Objects.equals(location, that.location)
                && Objects.equals(completionTime, that.completionTime)
                && Objects.equals(cargo, that.cargo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, voyage, location, completionTime, cargo);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("\n--- Handling event ---\n")
                .append("Cargo: ")
                .append(cargo.getTrackingId())
                .append("\n")
                .append("Type: ")
                .append(type)
                .append("\n")
                .append("Location: ")
                .append(location.getName())
                .append("\n")
                .append("Completed on: ")
                .append(completionTime)
                .append("\n")
                .append("Registered on: ")
                .append(registrationTime)
                .append("\n");

        if (voyage != null) {
            builder.append("Voyage: ").append(voyage.getVoyageNumber()).append("\n");
        }

        return builder.toString();
    }

    /**
     * Handling event type. Either requires or prohibits a carrier movement association,
     * it's never optional.
     */
    public enum Type {

        // Loaded onto voyage from port location.
        LOAD(true),
        // Unloaded from voyage to port location
        UNLOAD(true),
        // Received by carrier
        RECEIVE(false),
        // Cargo claimed by recepient
        CLAIM(false),
        // Cargo went through customs
        CUSTOMS(false);

        private final boolean voyageRequired;

        /**
         * Private enum constructor.
         *
         * @param voyageRequired whether or not a voyage is associated with this event
         *                       type
         */
        Type(boolean voyageRequired) {
            this.voyageRequired = voyageRequired;
        }

        /**
         * @return True if a voyage association is required for this event type.
         */
        public boolean requiresVoyage() {
            return voyageRequired;
        }

        /**
         * @return True if a voyage association is prohibited for this event type.
         */
        public boolean prohibitsVoyage() {
            return !requiresVoyage();
        }

        public boolean sameValueAs(Type other) {
            return other != null && this.equals(other);
        }

    }

}
