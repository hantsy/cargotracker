package org.eclipse.cargotracker.interfaces.booking.sse;

import static jakarta.ws.rs.core.MediaType.*;

import static java.util.UUID.*;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.sse.Sse;

import java.util.UUID;
import java.util.logging.Logger;

/** SSE service for tracking all cargoes in real time. */
@RequestScoped
@Path("tracking")
public class RealtimeCargoTrackingSseService {
    private static final Logger LOGGER =
            Logger.getLogger(RealtimeCargoTrackingSseService.class.getName());

    @Inject SseHandler sseHandler;

    @GET
    @Produces(SERVER_SENT_EVENTS)
    public void onOpen(@Context Sse sse, @Context jakarta.ws.rs.sse.SseEventSink sink) {
        // it could be the user identity in the real world application.
        String userId = UUID.randomUUID().toString();
        sseHandler.register(userId, new SseRequest(sse, sink));
    }

    @DELETE
    @Path("{id}")
    public void onClose(@PathParam("id") String id) {
        sseHandler.deregister(id);
    }
}
