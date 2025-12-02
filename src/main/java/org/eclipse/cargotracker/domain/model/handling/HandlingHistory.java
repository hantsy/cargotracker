package org.eclipse.cargotracker.domain.model.handling;

import java.util.*;

public class HandlingHistory {
    // Null object pattern.
    public static final HandlingHistory EMPTY = new HandlingHistory(Collections.emptyList());

    private static final Comparator<HandlingEvent> BY_COMPLETION_TIME_COMPARATOR = Comparator
            .comparing(HandlingEvent::getCompletionTime);

    private final List<HandlingEvent> handlingEvents;

    public HandlingHistory(Collection<HandlingEvent> handlingEvents) {
        Objects.requireNonNull(handlingEvents, "Handling events are required");
        this.handlingEvents = List.copyOf(handlingEvents);
    }

    public List<HandlingEvent> getAllHandlingEvents() {
        return handlingEvents;
    }

    /**
     * @return A distinct list (no duplicate registrations) of handling events, ordered by
     * completion time.
     */
    public List<HandlingEvent> getDistinctEventsByCompletionTime() {
        return handlingEvents.stream().distinct().sorted(BY_COMPLETION_TIME_COMPARATOR).toList();
    }

    /**
     * @return Most recently completed event, or null if the delivery history is empty.
     */
    public HandlingEvent getMostRecentlyCompletedEvent() {
        List<HandlingEvent> distinctEvents = getDistinctEventsByCompletionTime();
        if (distinctEvents.isEmpty()) return null;
        return distinctEvents.getLast();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HandlingHistory that = (HandlingHistory) o;
        return Objects.equals(handlingEvents, that.handlingEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(handlingEvents);
    }
}
