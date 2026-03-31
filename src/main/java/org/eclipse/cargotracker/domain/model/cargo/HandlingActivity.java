package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.io.Serializable;
import java.util.Objects;

/**
 * A handling activity represents how and where a cargo can be handled, and can be used to express
 * predictions about what is expected to happen to a cargo in the future.
 */
@Embeddable
public record HandlingActivity(
        @Enumerated(EnumType.STRING)
        @Column(name = "next_expected_handling_event_type")
        //@NotNull(message = "Handling event type is required.")
        HandlingEvent.Type type,

        @ManyToOne
        @JoinColumn(name = "next_expected_location_id")
        //@NotNull(message = "Location is required.")
        Location location,

        @ManyToOne
        @JoinColumn(name = "next_expected_voyage_id")
        Voyage voyage
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final HandlingActivity EMPTY = new HandlingActivity(null, null, null);

    /**
     * Static factory method to create a HandlingActivity without voyage.
     *
     * @param type     the handling event type
     * @param location the location
     * @return a new HandlingActivity instance
     */
    public static HandlingActivity of(HandlingEvent.Type type, Location location) {
        Objects.requireNonNull(type, "Handling event type is required");
        Objects.requireNonNull(location, "Location is required");

        return new HandlingActivity(type, location, null);
    }

    /**
     * Static factory method to create a HandlingActivity with voyage.
     *
     * @param type     the handling event type
     * @param location the location
     * @param voyage   the voyage
     * @return a new HandlingActivity instance
     */
    public static HandlingActivity of(HandlingEvent.Type type, Location location, Voyage voyage) {
        Objects.requireNonNull(type, "Handling event type is required");
        Objects.requireNonNull(location, "Location is required");
        Objects.requireNonNull(voyage, "Voyage is required");

        return new HandlingActivity(type, location, voyage);
    }

    public boolean isEmpty() {
        if (type != null) {
            return false;
        }

        if (location != null) {
            return false;
        }

        return voyage == null;
    }

}
