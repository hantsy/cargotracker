package org.eclipse.cargotracker.interfaces.booking.sse;

import static java.util.logging.Level.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;

import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ApplicationScoped
public class SseHandler {
    private static final Logger LOGGER = Logger.getLogger(SseHandler.class.getName());
    private final ConcurrentHashMap<String, SseRequest> sessions = new ConcurrentHashMap<>();

    public void register(String id, SseRequest request) {
        this.sessions.putIfAbsent(id, request);
    }

    public void deregister(String id) {
        try {
            sessions.get(id).sink().close();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        this.sessions.remove(id);
    }

    public void onCargoInspected(@Observes @CargoInspected Cargo cargo) {
        LOGGER.log(INFO, "observers cargo inspected event of cargo: {0}", cargo.getTrackingId());
        Writer writer = new StringWriter();

        try (JsonGenerator generator = Json.createGenerator(writer)) {
            generator
                    .writeStartObject()
                    .write("trackingId", cargo.getTrackingId().id())
                    .write("origin", cargo.getOrigin().getName())
                    .write("destination", cargo.getRouteSpecification().destination().getName())
                    .write("lastKnownLocation", cargo.getDelivery().lastKnownLocation().getName())
                    .write("transportStatus", cargo.getDelivery().transportStatus().name())
                    .writeEnd();
        }

        String jsonValue = writer.toString();
        LOGGER.log(INFO, "sending message to client: {0}", jsonValue);
        sessions.forEach(
                (key, value) -> {
                    LOGGER.log(INFO, "sending to user id:{0}", key);
                    try {
                        value.sink().send(value.sse().newEvent(jsonValue));
                    } catch (Exception ex) {
                        LOGGER.log(WARNING, "sending message failed: {0}", ex.getMessage());
                    }
                });
    }
}
