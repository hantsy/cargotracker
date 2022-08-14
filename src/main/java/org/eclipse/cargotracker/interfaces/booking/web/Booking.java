package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;
import org.omnifaces.util.Messages;

@Named
@FlowScoped("booking")
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final long MIN_JOURNEY_DURATION = 1; // Journey should be 1 day minimum.

    private LocalDate today = null;
    private List<LocationDto> locations;

    private String originUnlocode;
    private String originName;
    private String destinationName;
    private String destinationUnlocode;
    private LocalDate arrivalDeadline;

    private boolean bookable = false;
    private long duration = -1;

    @Inject private BookingServiceFacade bookingServiceFacade;

    @PostConstruct
    public void init() {
        today = LocalDate.now();
        locations = bookingServiceFacade.listShippingLocations();
    }

    public List<LocationDto> getLocations() {
        List<LocationDto> filteredLocations = new ArrayList<>();
        String locationToRemove = null;

        // TODO [Jakarta EE 8] Use injection instead?
        if (FacesContext.getCurrentInstance()
                .getViewRoot()
                .getViewId()
                .endsWith("destination.xhtml")) {
            // In the destination menu, origin can't be selected.
            locationToRemove = originUnlocode;
        } else { // Vice-versa.
            if (destinationUnlocode != null) {
                locationToRemove = destinationUnlocode;
            }
        }

        for (LocationDto location : locations) {
            if (!location.getUnLocode().equalsIgnoreCase(locationToRemove)) {
                filteredLocations.add(location);
            }
        }

        return filteredLocations;
    }

    public String getOriginUnlocode() {
        return originUnlocode;
    }

    public void setOriginUnlocode(String originUnlocode) {
        this.originUnlocode = originUnlocode;
        for (LocationDto location : locations) {
            if (location.getUnLocode().equalsIgnoreCase(originUnlocode)) {
                this.originName = location.getNameOnly();
            }
        }
    }

    public String getOriginName() {
        return originName;
    }

    public String getDestinationUnlocode() {
        return destinationUnlocode;
    }

    public void setDestinationUnlocode(String destinationUnlocode) {
        this.destinationUnlocode = destinationUnlocode;
        for (LocationDto location : locations) {
            if (location.getUnLocode().equalsIgnoreCase(destinationUnlocode)) {
                destinationName = location.getNameOnly();
            }
        }
    }

    public String getDestinationName() {
        return destinationName;
    }

    public LocalDate getToday() {
        return today;
    }

    public LocalDate getArrivalDeadline() {
        return arrivalDeadline;
    }

    public void setArrivalDeadline(LocalDate arrivalDeadline) {
        this.arrivalDeadline = arrivalDeadline;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isBookable() {
        return bookable;
    }

    public void deadlineUpdated() {
        duration = ChronoUnit.DAYS.between(today, arrivalDeadline);
        bookable = duration >= MIN_JOURNEY_DURATION;
    }

    public String register() {
        if (!originUnlocode.equals(destinationUnlocode)) {
            bookingServiceFacade.bookNewCargo(originUnlocode, destinationUnlocode, arrivalDeadline);
        } else {
            Messages.addGlobalError("Origin and destination cannot be the same.");
            return null;
        }

        return "/admin/dashboard.xhtml";
    }

    public String getReturnValue() {
        return "/admin/dashboard.xhtml";
    }
}
