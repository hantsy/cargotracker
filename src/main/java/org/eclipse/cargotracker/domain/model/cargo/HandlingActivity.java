package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.util.Objects;

/**
 * A handling activity represents how and where a cargo can be handled, and can be used to express
 * predictions about what is expected to happen to a cargo in the future.
 */
@Embeddable
public record HandlingActivity(
        /** The type of the next expected handling event. */
        @Enumerated(EnumType.STRING)
                @Column(name = "next_expected_handling_event_type")
                @NotNull(message = "Handling event type is required.")
                HandlingEvent.Type type,

        /** The location of the next expected handling event. */
        @ManyToOne
                @JoinColumn(name = "next_expected_location_id")
                @NotNull(message = "Location is required.")
                Location location,

        /** The voyage of the next expected handling event, if applicable. */
        @ManyToOne @JoinColumn(name = "next_expected_voyage_id") Voyage voyage) {

    public static final HandlingActivity EMPTY = new HandlingActivity(null, null, null);

    public HandlingActivity {
        Objects.requireNonNull(type, "Handling event type is required");
        Objects.requireNonNull(location, "Location is required");
    }

    public HandlingActivity(HandlingEvent.Type type, Location location) {
        this(type, Objects.requireNonNull(location, "Location is required"), null);
    }

    public boolean isEmpty() {
        return type == null && location == null && voyage == null;
    }
}
