package org.eclipse.cargotracker.domain.model.handling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class HandlingHistory {
    // private static final Logger LOGGER = Logger.getLogger(HandlingHistory.class.getName());

    // Null object pattern.
    public static final HandlingHistory EMPTY = new HandlingHistory(Collections.emptyList());
    private static final Comparator<HandlingEvent> BY_COMPLETION_TIME_COMPARATOR =
            Comparator.comparing(HandlingEvent::getCompletionTime);
    private final List<HandlingEvent> handlingEvents;

    public HandlingHistory(Collection<HandlingEvent> handlingEvents) {
        Objects.requireNonNull(handlingEvents, "Handling events are required");

        this.handlingEvents = new ArrayList<>(handlingEvents);
    }

    public List<HandlingEvent> getAllHandlingEvents() {
        return handlingEvents;
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
        // LOGGER.log(Level.INFO, "distinct events: {0}", distinctEvents);
        return distinctEvents.isEmpty() ? null : distinctEvents.getLast();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HandlingHistory that)) return false;
        return Objects.equals(handlingEvents, that.handlingEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(handlingEvents);
    }
}
