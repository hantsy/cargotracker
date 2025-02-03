package org.eclipse.cargotracker.interfaces.booking.sse;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.SERVER_SENT_EVENTS;
import static java.util.logging.Level.INFO;

/**
 * SSE service for tracking all cargoes in real time.
 */
@ApplicationScoped
@Path("tracking")
public class RealtimeCargoTrackingSseService {
    private static final Logger LOGGER =
            Logger.getLogger(RealtimeCargoTrackingSseService.class.getName());
    private @Context Sse sse;
    private SseBroadcaster sseBroadcaster;

    // constructor injection does not work withe EJB Singleton/Stateful and
    // CDI Singleton/ApplicationScoped
    // public RealtimeCargoTrackingSseService(@Context Sse sse) {}

    @PostConstruct
    public void init() {
        this.sseBroadcaster = sse.newBroadcaster();
        this.sseBroadcaster.onClose(
                sseEventSink -> {
                    try {
                        sseEventSink.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, e.getMessage(), e);
                    }
                });
        this.sseBroadcaster.onError(
                (sseEventSink, throwable) -> {
                    sseEventSink.send(sse.newEvent("error", throwable.getMessage()));
                    try {
                        sseEventSink.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, e.getMessage(), e);
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
