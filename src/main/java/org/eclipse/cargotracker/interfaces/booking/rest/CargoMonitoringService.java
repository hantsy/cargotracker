package org.eclipse.cargotracker.interfaces.booking.rest;

import jakarta.enterprise.context.RequestScoped;
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
import org.eclipse.cargotracker.domain.model.location.Location;

import java.util.List;

@RequestScoped
@Path("/cargo")
public class CargoMonitoringService {

	private CargoRepository cargoRepository;

	public CargoMonitoringService() {
	}

	@Inject
	public CargoMonitoringService(CargoRepository cargoRepository) {
		this.cargoRepository = cargoRepository;
	}

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
			.add("routingStatus", cargo.getDelivery().routingStatus().name())
			.add("misdirected", cargo.getDelivery().misdirected())
			.add("transportStatus", cargo.getDelivery().transportStatus().name())
			.add("atDestination", cargo.getDelivery().isUnloadedAtDestination())
			.add("origin", cargo.getOrigin().getUnLocode().value())
			.add("lastKnownLocation", cargo.getDelivery().lastKnownLocation().equals(Location.UNKNOWN) ? "Unknown"
					: cargo.getDelivery().lastKnownLocation().getUnLocode().value());
	}

}
