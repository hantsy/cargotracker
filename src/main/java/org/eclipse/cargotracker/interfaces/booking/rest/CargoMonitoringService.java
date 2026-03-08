package org.eclipse.cargotracker.interfaces.booking.rest;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;

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
                .add("trackingId", cargo.getTrackingId().getIdString())
                .add("routingStatus", cargo.getDelivery().getRoutingStatus().toString())
                .add("misdirected", cargo.getDelivery().isMisdirected())
                .add("transportStatus", cargo.getDelivery().getTransportStatus().toString())
                .add("atDestination", cargo.getDelivery().isUnloadedAtDestination())
                .add("origin", cargo.getOrigin().getUnLocode().getIdString())
                .add(
                        "lastKnownLocation",
                        cargo.getDelivery()
                                        .getLastKnownLocation()
                                        .getUnLocode()
                                        .getIdString()
                                        .equals("XXXXX")
                                ? "Unknown"
                                : cargo.getDelivery()
                                        .getLastKnownLocation()
                                        .getUnLocode()
                                        .getIdString());
    }
}
