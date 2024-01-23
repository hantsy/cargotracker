package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.Serializable;
import java.time.LocalDate;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;

/**
 * Handles changing the cargo destination. Operates against a dedicated service facade, and could
 * easily be rewritten as a thick Swing client. Completely separated from the domain layer, unlike
 * the tracking user interface.
 *
 * <p>In order to successfully keep the domain model shielded from user interface considerations,
 * this approach is generally preferred to the one taken in the tracking controller. However, there
 * is never any one perfect solution for all situations, so we've chosen to demonstrate two
 * polarized ways to build user interfaces.
 */
@Named
@ViewScoped
public class ChangeArrivalDeadline implements Serializable {

    private static final long serialVersionUID = 1L;
    private String trackingId;
    private CargoRouteDto cargo;
    private LocalDate arrivalDeadline;

    @Inject private BookingServiceFacade bookingServiceFacade;

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public CargoRouteDto getCargo() {
        return cargo;
    }

    public LocalDate getArrivalDeadline() {
        return arrivalDeadline;
    }

    public void setArrivalDeadline(LocalDate arrivalDeadline) {
        this.arrivalDeadline = arrivalDeadline;
    }

    public void load() {
        cargo = bookingServiceFacade.loadCargoForRouting(trackingId);
        arrivalDeadline = cargo.arrivalDeadline();
    }

    public String changeArrivalDeadline() {
        bookingServiceFacade.changeDeadline(trackingId, arrivalDeadline);
        return "/admin/show.xhtml?faces-redirect=true&trackingId=" + trackingId;
    }
}
