package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.util.Objects;

/**
 * A handling activity represents how and where a cargo can be handled, and can be used to
 * express predictions about what is expected to happen to a cargo in the future.
 */
@Embeddable
//@formatter:off
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
	@ManyToOne
	@JoinColumn(name = "next_expected_voyage_id")
	Voyage voyage
) {
//@formatter:on

	public static final HandlingActivity EMPTY = null; //new HandlingActivity(null, null, null);

	public HandlingActivity {
	}

	public static HandlingActivity of(HandlingEvent.Type type, Location location) {
		Objects.requireNonNull(type, "Handling event type is required");
		Objects.requireNonNull(location, "Location is required");
		return new HandlingActivity(type, location, null);
	}

	public static HandlingActivity of(HandlingEvent.Type type, Location location, Voyage voyage) {
		Objects.requireNonNull(type, "Handling event type is required");
		Objects.requireNonNull(location, "Location is required");
		Objects.requireNonNull(voyage, "Voyage is required");
		return new HandlingActivity(type, location, voyage);
	}

	public boolean isEmpty() {
		return type == null && location == null && voyage == null;
	}
}
