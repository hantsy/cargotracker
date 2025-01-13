package org.eclipse.cargotracker.interfaces.booking.sse;

import static jakarta.ws.rs.core.MediaType.*;

import static java.util.UUID.*;
import static java.util.logging.Level.INFO;

import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** SSE service for tracking all cargoes in real time. */
@Singleton
// @ApplicationScoped
@Path("tracking")
public class RealtimeCargoTrackingSseService {
    private static final Logger LOGGER =
            Logger.getLogger(RealtimeCargoTrackingSseService.class.getName());
    private Sse sse;
    private SseBroadcaster sseBroadcaster;

    public RealtimeCargoTrackingSseService() {}

    public RealtimeCargoTrackingSseService(@Context Sse sse) {
        this.sse = sse;
        this.sseBroadcaster = sse.newBroadcaster();
        this.sseBroadcaster.onClose(
                sseEventSink -> {
                    try {
                        sseEventSink.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        this.sseBroadcaster.onError(
                (sseEventSink, throwable) -> {
                    sseEventSink.send(sse.newEvent("error", throwable.getMessage()));
                    try {
                        sseEventSink.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @GET
    @Produces(SERVER_SENT_EVENTS)
    public void register(@Context SseEventSink sink) {
        LOGGER.log(Level.INFO, "register sink: {0}", sink);
        this.sseBroadcaster.register(sink);
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

        OutboundSseEvent event =
                sse.newEventBuilder()
                        .name("event")
                        .mediaType(APPLICATION_JSON_TYPE)
                        .data(jsonValue)
                        .build();
        LOGGER.log(INFO, "broadcast event: {0}", event);
        this.sseBroadcaster.broadcast(event);
    }
}
