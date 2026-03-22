package org.eclipse.cargotracker.interfaces.handling;

import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This is a simple transfer object for passing incoming handling event registration attempts to the
 * proper registration procedure.
 *
 * <p>It is used as a message queue element.
 */
public record HandlingEventRegistrationAttempt(LocalDateTime registrationTime,
                                               LocalDateTime completionTime,
                                               TrackingId trackingId,
                                               VoyageNumber voyageNumber,
                                               HandlingEvent.Type type,
                                               UnLocode unLocode) implements Serializable {
}
