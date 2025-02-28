package org.eclipse.cargotracker.interfaces.booking.socket;

import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** WebSocket service for tracking all cargoes in real time. */
// work with EJB Singleton, but failed with CDI Singleton/ApplicationScoped
@Singleton
@ServerEndpoint("/tracking")
public class RealtimeCargoTrackingWebSocketService {
    private static final Logger LOGGER =
            Logger.getLogger(RealtimeCargoTrackingWebSocketService.class.getName());

    private final Set<Session> sessions = new HashSet<>();

    @OnOpen
    public void onOpen(final Session session) {
        // Infinite by default on GlassFish. We need this principally for WebLogic.
        LOGGER.log(Level.INFO, "open session: {0}", session.getId());
        session.setMaxIdleTimeout(5L * 60 * 1000);
        sessions.add(session);
    }

    @OnClose
    public void onClose(final Session session) {
        LOGGER.log(Level.INFO, "close session: {0}", session.getId());
        sessions.remove(session);
    }

    public void onCargoInspected(@Observes @CargoInspected Cargo cargo) {
        LOGGER.log(
                Level.INFO, "observers cargo inspected event of cargo: {0}", cargo.getTrackingId());
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
        LOGGER.log(Level.INFO, "sending message to client: {0}", jsonValue);
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(jsonValue);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Unable to publish WebSocket message", ex);
            }
        }
    }
}
