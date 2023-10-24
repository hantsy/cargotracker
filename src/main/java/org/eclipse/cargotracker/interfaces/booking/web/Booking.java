package org.eclipse.cargotracker.interfaces.booking.web;

import jakarta.annotation.PostConstruct;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;
import org.omnifaces.util.Messages;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Named
@ConversationScoped
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
    private long duration = -1;

    @Inject private BookingServiceFacade bookingServiceFacade;

    @Inject private Conversation conversation;

    @Inject private FacesContext facesContext;

    @PostConstruct
    public void init() {
        today = LocalDate.now();
        locations = bookingServiceFacade.listShippingLocations();
    }

    public void startConversation() {
        if (!facesContext.isPostback() && conversation.isTransient()) {
            conversation.begin();
        }
    }

    public List<LocationDto> getLocations() {
        return locations;
    }

    public String getOriginUnlocode() {
        return originUnlocode;
    }

    public void setOriginUnlocode(String originUnlocode) {
        this.originUnlocode = originUnlocode;
        for (LocationDto location : locations) {
            if (location.unLocode().equalsIgnoreCase(originUnlocode)) {
                this.originName = location.name();
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
            if (location.unLocode().equalsIgnoreCase(destinationUnlocode)) {
                destinationName = location.name();
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
        this.duration = ChronoUnit.DAYS.between(today, arrivalDeadline);
    }

    public long getDuration() {
        return duration;
    }

    public String submit() {
        if (originUnlocode.equals(destinationUnlocode)) {
            Messages.addGlobalError("Origin and destination cannot be the same.");
            return null;
        }
        if (duration < MIN_JOURNEY_DURATION) {
            Messages.addGlobalError("Journey duration must be at least 1 day.");
            return null;
        }
        return "/admin/booking/confirm.xhtml";
    }

    public String back() {
        return "/admin/booking/booking.xhtml";
    }

    public String register() {
        bookingServiceFacade.bookNewCargo(originUnlocode, destinationUnlocode, arrivalDeadline);

        // end the conversation
        if (!conversation.isTransient()) {
            conversation.end();
        }
        return "/admin/dashboard.xhtml?faces-redirect=true";
    }
}
