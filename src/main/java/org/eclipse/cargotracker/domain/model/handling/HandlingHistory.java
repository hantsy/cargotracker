package org.eclipse.cargotracker.domain.model.handling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * The handling history of a cargo.
 */
public record HandlingHistory(List<HandlingEvent> handlingEvents) {

    // Null object pattern.
    public static final HandlingHistory EMPTY = new HandlingHistory(Collections.emptyList());

    private static final Comparator<HandlingEvent> BY_COMPLETION_TIME_COMPARATOR =
            Comparator.comparing(HandlingEvent::getCompletionTime);

    public HandlingHistory {
        Objects.requireNonNull(handlingEvents, "Handling events are required");
    }

    /**
     * @return A distinct list (no duplicate registrations) of handling events, ordered by
     * completion time.
     */
    public List<HandlingEvent> getDistinctEventsByCompletionTime() {
        List<HandlingEvent> ordered = new ArrayList<>(new HashSet<>(handlingEvents));
        ordered.sort(BY_COMPLETION_TIME_COMPARATOR);

        return Collections.unmodifiableList(ordered);
    }

    /**
     * @return Most recently completed event, or null if the delivery history is empty.
     */
    public HandlingEvent getMostRecentlyCompletedEvent() {
        List<HandlingEvent> distinctEvents = getDistinctEventsByCompletionTime();
        return distinctEvents.isEmpty() ? null : distinctEvents.getLast();
    }
}
