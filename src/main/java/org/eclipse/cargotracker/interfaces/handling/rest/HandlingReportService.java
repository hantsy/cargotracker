package org.eclipse.cargotracker.interfaces.handling.rest;

import static jakarta.ws.rs.core.Response.accepted;
import static jakarta.ws.rs.core.Response.status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

/**
 * This REST end-point implementation performs basic validation and parsing of incoming data, and in
 * case of a valid registration attempt, sends an asynchronous message with the information to the
 * handling event registration system for proper registration.
 */
@Stateless
@Path("handling")
public class HandlingReportService {
    public static final Logger LOGGER = Logger.getLogger(HandlingReportService.class.getName());
    // public static final String ISO_8601_FORMAT = "yyyy-MM-dd HH:mm";

    @Inject private ApplicationEvents applicationEvents;

    public HandlingReportService() {}

    @POST
    @Path("reports")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submitReport(@NotNull @Valid HandlingReport handlingReport) {
        LocalDateTime completionTime = DateUtil.toDateTime(handlingReport.getCompletionTime());
        VoyageNumber voyageNumber = null;

        if (handlingReport.getVoyageNumber() != null) {
            voyageNumber = new VoyageNumber(handlingReport.getVoyageNumber());
        }

        HandlingEvent.Type type = HandlingEvent.Type.valueOf(handlingReport.getEventType());
        UnLocode unLocode = new UnLocode(handlingReport.getUnLocode());

        TrackingId trackingId = new TrackingId(handlingReport.getTrackingId());

        HandlingEventRegistrationAttempt attempt =
                new HandlingEventRegistrationAttempt(
                        LocalDateTime.now(),
                        completionTime,
                        trackingId,
                        voyageNumber,
                        type,
                        unLocode);

        try {
            applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
            return accepted().build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Handling event registration failed: {0}", e.getMessage());
            return status(Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", e.getMessage()))
                    .build();
        }
    }
}
