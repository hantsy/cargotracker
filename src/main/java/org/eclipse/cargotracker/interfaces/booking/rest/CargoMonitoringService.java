package org.eclipse.cargotracker.interfaces.booking.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;

import java.util.List;

@Stateless
@Path("/cargo")
public class CargoMonitoringService {

    @Inject private CargoRepository cargoRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getAllCargo() {
        List<Cargo> cargos = cargoRepository.findAll();

        JsonArrayBuilder builder = Json.createArrayBuilder();

        cargos.stream().map(this::cargoToJson).forEach(builder::add);

        return builder.build();
    }

    private JsonObjectBuilder cargoToJson(Cargo cargo) {
        return Json.createObjectBuilder()
                .add("trackingId", cargo.getTrackingId().id())
                .add("routingStatus", cargo.getDelivery().routingStatus().toString())
                .add("misdirected", cargo.getDelivery().misdirected())
                .add("transportStatus", cargo.getDelivery().transportStatus().toString())
                .add("atDestination", cargo.getDelivery().isUnloadedAtDestination())
                .add("origin", cargo.getOrigin().getUnLocode().getIdString())
                .add(
                        "lastKnownLocation",
                        cargo.getDelivery()
                                        .lastKnownLocation()
                                        .getUnLocode()
                                        .getIdString()
                                        .equals("XXXXX")
                                ? "Unknown"
                                : cargo.getDelivery()
                                        .lastKnownLocation()
                                        .getUnLocode()
                                        .getIdString());
    }
}
