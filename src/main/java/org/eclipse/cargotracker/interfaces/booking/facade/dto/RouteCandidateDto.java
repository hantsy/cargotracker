package org.eclipse.cargotracker.interfaces.booking.facade.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** DTO for presenting and selecting an itinerary from a collection of candidates. */
public class RouteCandidateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<LegDto> legs;

    public RouteCandidateDto(List<LegDto> legs) {
        this.legs = legs;
    }

    public List<LegDto> getLegs() {
        return Collections.unmodifiableList(legs);
    }

    @Override
    public String toString() {
        return "RouteCandidate{" + "legs=" + legs + '}';
    }
}
