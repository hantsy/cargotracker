package org.eclipse.cargotracker.interfaces.booking.web;

import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
public class ChangeDestination implements Serializable {

    private static final long serialVersionUID = 1L;

    private String trackingId;
    private CargoRouteDto cargo;
    private List<LocationDto> locations;
    private String destinationUnlocode;

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

    public List<LocationDto> getLocations() {
        return locations;
    }

    public List<LocationDto> getPotentialDestinations() {
        // Potential destination = All Locations - Origin - Current Destination
        List<LocationDto> destinationsToRemove = new ArrayList<>();
        for (LocationDto loc : locations) {
            if (loc.code().equalsIgnoreCase(cargo.origin().code())
                    || loc.code().equalsIgnoreCase(cargo.finalDestination().code())) {
                destinationsToRemove.add(loc);
            }
        }
        locations.removeAll(destinationsToRemove);
        return locations;
    }

    public String getDestinationUnlocode() {
        return destinationUnlocode;
    }

    public void setDestinationUnlocode(String destinationUnlocode) {
        this.destinationUnlocode = destinationUnlocode;
    }

    public void load() {
        locations = bookingServiceFacade.listShippingLocations();
        cargo = bookingServiceFacade.loadCargoForRouting(trackingId);
    }

    public String changeDestination() {
        bookingServiceFacade.changeDestination(trackingId, destinationUnlocode);
        return "show.html?faces-redirect=true&trackingId=" + trackingId;
    }
}
