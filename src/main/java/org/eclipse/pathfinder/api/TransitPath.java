package org.eclipse.pathfinder.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record TransitPath(List<TransitEdge> transitEdges) implements Serializable {

    private static final long serialVersionUID = 1L;

    public TransitPath {
        if (transitEdges == null) {
            transitEdges = new ArrayList<>();
        }
    }

    /**
     * Static factory method to create an empty TransitPath.
     *
     * @return an empty TransitPath
     */
    public static TransitPath empty() {
        return new TransitPath(new ArrayList<>());
    }

    /**
     * Returns an unmodifiable view of the transit edges.
     *
     * @return unmodifiable list of transit edges
     */
    @Override
    public List<TransitEdge> transitEdges() {
        return List.copyOf(transitEdges);
    }

    /**
     * Adds a transit edge to this path.
     *
     * @param edge the edge to add
     * @return this TransitPath for method chaining
     */
    public TransitPath addEdge(TransitEdge edge) {
        Objects.requireNonNull(edge, "Transit edge cannot be null");
        transitEdges.add(edge);
        return this;
    }

    @Override
    public String toString() {
        return "TransitPath{" + "transitEdges=" + transitEdges + '}';
    }
}
