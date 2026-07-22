package org.eclipse.cargotracker.interfaces.booking.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles listing cargo. Operates against a dedicated service facade, and could easily be rewritten
 * as a thick Swing client. Completely separated from the domain layer, unlike the tracking user
 * interface.
 *
 * <p>In order to successfully keep the domain model shielded from user interface considerations,
 * this approach is generally preferred to the one taken in the tracking controller. However, there
 * is never any one perfect solution for all situations, so we've chosen to demonstrate two
 * polarized ways to build user interfaces.
 */
@Named
@RequestScoped
public class ListCargo {

    private List<CargoRouteDto> routedUnclaimedCargos;
    private List<CargoRouteDto> notRoutedCargos;
    private List<CargoRouteDto> claimedCargos;

    @Inject
    private BookingServiceFacade bookingServiceFacade;

    public List<CargoRouteDto> getRoutedUnclaimedCargos() {
        return routedUnclaimedCargos;
    }

    public List<CargoRouteDto> getClaimedCargos() {
        return claimedCargos;
    }

    public List<CargoRouteDto> getNotRoutedCargos() {
        return notRoutedCargos;
    }

    public void init() {
        List<CargoRouteDto> cargos = bookingServiceFacade.listAllCargos();
        routedUnclaimedCargos = cargos.stream()
                .filter(route -> route.routed() && !route.claimed())
                .collect(Collectors.toCollection(ArrayList::new));
        notRoutedCargos = cargos.stream()
                .filter(route -> !route.routed())
                .collect(Collectors.toCollection(ArrayList::new));
        claimedCargos = cargos.stream()
                .filter(CargoRouteDto::claimed)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
