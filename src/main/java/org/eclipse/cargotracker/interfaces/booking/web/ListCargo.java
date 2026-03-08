package org.eclipse.cargotracker.interfaces.booking.web;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRouteDto;

import java.util.ArrayList;
import java.util.List;

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

    private List<CargoRouteDto> cargos;
    private List<CargoRouteDto> routedCargos;
    private List<CargoRouteDto> notRoutedCargos;
    private List<CargoRouteDto> claimedCargos;
    private List<CargoRouteDto> routedUnclaimedCargos;

    @Inject private BookingServiceFacade bookingServiceFacade;

    public List<CargoRouteDto> getCargos() {
        return cargos;
    }

    @PostConstruct
    public void init() {
        cargos = bookingServiceFacade.listAllCargos();
    }

    public List<CargoRouteDto> getRoutedCargos() {
        routedCargos = new ArrayList<>();

        cargos.stream().filter(CargoRouteDto::routed).forEach(route -> routedCargos.add(route));

        return routedCargos;
    }

    public List<CargoRouteDto> getRoutedUnclaimedCargos() {
        routedUnclaimedCargos = new ArrayList<>();
        cargos.stream()
                .filter(route -> route.routed() && !route.claimed())
                .forEach(route -> routedUnclaimedCargos.add(route));

        return routedUnclaimedCargos;
    }

    public List<CargoRouteDto> getClaimedCargos() {
        claimedCargos = new ArrayList<>();

        cargos.stream().filter(CargoRouteDto::claimed).forEach(route -> claimedCargos.add(route));

        return claimedCargos;
    }

    public List<CargoRouteDto> getNotRoutedCargos() {
        notRoutedCargos = new ArrayList<>();
        cargos.stream()
                .filter(route -> !route.routed())
                .forEach(route -> notRoutedCargos.add(route));

        return notRoutedCargos;
    }
}
