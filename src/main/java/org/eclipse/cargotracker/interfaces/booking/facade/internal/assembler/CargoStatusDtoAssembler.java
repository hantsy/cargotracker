package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Delivery;
import org.eclipse.cargotracker.domain.model.cargo.HandlingActivity;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatusDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.TrackingEventsDto;

import java.time.LocalDateTime;
import java.util.List;

public class CargoStatusDtoAssembler {

    public CargoStatusDto toDto(Cargo cargo, List<HandlingEvent> handlingEvents) {
        List<TrackingEventsDto> trackingEvents;

        TrackingEventsDtoAssembler assembler = new TrackingEventsDtoAssembler();

        trackingEvents =
                handlingEvents.stream()
                        .map(handlingEvent -> assembler.toDto(cargo, handlingEvent))
                        .toList();
        return new CargoStatusDto(
                cargo.getRouteSpecification().destination().getName(),
                getCargoStatusText(cargo),
                cargo.getDelivery().misdirected(),
                getEta(cargo),
                getNextExpectedActivity(cargo),
                trackingEvents);
    }

    private String getCargoStatusText(Cargo cargo) {
        Delivery delivery = cargo.getDelivery();

        return switch (delivery.transportStatus()) {
            case IN_PORT -> "In port " + delivery.lastKnownLocation().getName();
            case ONBOARD_CARRIER ->
                    "Onboard voyage " + delivery.currentVoyage().getVoyageNumber().getIdString();
            case CLAIMED -> "Claimed";
            case NOT_RECEIVED -> "Not received";
            case UNKNOWN -> "Unknown";
            default -> "[Unknown status]"; // Should never happen.
        };
    }

    private String getEta(Cargo cargo) {
        LocalDateTime eta = cargo.getDelivery().estimatedTimeOfArrival();

        if (eta == null) {
            return "?";
        } else {
            return DateUtil.toString(eta);
        }
    }

    private String getNextExpectedActivity(Cargo cargo) {
        HandlingActivity activity = cargo.getDelivery().nextExpectedActivity();

        if ((activity == null) || (activity.isEmpty())) {
            return "";
        }

        String text = "Next expected activity is to ";
        HandlingEvent.Type type = activity.getType();

        return switch (type) {
            case HandlingEvent.Type.LOAD ->
                    text
                            + type.name().toLowerCase()
                            + " cargo onto voyage "
                            + activity.getVoyage().getVoyageNumber()
                            + " in "
                            + activity.getLocation().getName();
            case HandlingEvent.Type.UNLOAD ->
                    text
                            + type.name().toLowerCase()
                            + " cargo off of "
                            + activity.getVoyage().getVoyageNumber()
                            + " in "
                            + activity.getLocation().getName();
            default ->
                    text
                            + type.name().toLowerCase()
                            + " cargo in "
                            + activity.getLocation().getName();
        };
    }
}
