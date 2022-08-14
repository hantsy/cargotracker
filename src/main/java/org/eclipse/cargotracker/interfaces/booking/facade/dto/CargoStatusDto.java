package org.eclipse.cargotracker.interfaces.booking.facade.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CargoStatusDto {

    private final String destination;
    private final String statusText;
    private final boolean misdirected;
    private final String eta;
    private final String nextExpectedActivity;
    private final List<TrackingEventsDto> events;

    public CargoStatusDto(
            String destination,
            String statusText,
            boolean misdirected,
            String eta,
            String nextExpectedActivity,
            List<TrackingEventsDto> handlingEvents) {
        this.destination = destination;
        this.statusText = statusText;
        this.misdirected = misdirected;
        this.eta = eta;
        this.nextExpectedActivity = nextExpectedActivity;
        this.events = new ArrayList<>(handlingEvents.size());

        events.addAll(handlingEvents);
    }

    public String getDestination() {
        return destination;
    }

    /**
     * @return A readable string describing the cargo status.
     */
    public String getStatusText() {
        return statusText;
    }

    public boolean isMisdirected() {
        return misdirected;
    }

    public String getEta() {
        return eta;
    }

    public String getNextExpectedActivity() {
        return nextExpectedActivity;
    }

    public List<TrackingEventsDto> getEvents() {
        return Collections.unmodifiableList(events);
    }
}
