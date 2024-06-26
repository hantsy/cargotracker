package org.eclipse.cargotracker.domain.model.handling;

import org.apache.commons.lang3.Validate;

import java.util.*;

public class HandlingHistory {
    // private static final Logger LOGGER = Logger.getLogger(HandlingHistory.class.getName());

    // Null object pattern.
    public static final HandlingHistory EMPTY = new HandlingHistory(Collections.emptyList());
    private static final Comparator<HandlingEvent> BY_COMPLETION_TIME_COMPARATOR =
            Comparator.comparing(HandlingEvent::getCompletionTime);
    private final List<HandlingEvent> handlingEvents;

    public HandlingHistory(Collection<HandlingEvent> handlingEvents) {
        Validate.notNull(handlingEvents, "Handling events are required");

        this.handlingEvents = new ArrayList<>(handlingEvents);
    }

    public List<HandlingEvent> getAllHandlingEvents() {
        return handlingEvents;
    }

    /**
     * @return A distinct list (no duplicate registrations) of handling events, ordered by
     *     completion time.
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
        if (distinctEvents.isEmpty()) {
            return null;
        } else {
            return distinctEvents.get(distinctEvents.size() - 1);
        }
    }

    private boolean sameValueAs(HandlingHistory other) {
        return other != null && this.handlingEvents.equals(other.handlingEvents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HandlingHistory other = (HandlingHistory) o;
        return sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return handlingEvents.hashCode();
    }
}
