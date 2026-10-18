package org.eclipse.pathfinder.api;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents an edge in a path through a graph, describing the route of a cargo.
 */
public record TransitEdge(
        String voyageNumber,
        String fromUnLocode,
        String toUnLocode,
        LocalDateTime fromDate,
        LocalDateTime toDate
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public TransitEdge {
        // Validation can be added here if needed
    }

    @Override
    public String toString() {
        return "TransitEdge{"
                + "voyageNumber=" + voyageNumber
                + ", fromUnLocode=" + fromUnLocode
                + ", toUnLocode=" + toUnLocode
                + ", fromDate=" + fromDate
                + ", toDate=" + toDate
                + '}';
    }
}
