package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Delivery;
import org.eclipse.cargotracker.domain.model.cargo.HandlingActivity;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoStatusDto;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.TrackingEventsDto;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

public class CargoStatusDtoAssembler {

	public CargoStatusDto toDto(Cargo cargo, List<HandlingEvent> handlingEvents) {
		List<TrackingEventsDto> trackingEvents;

		TrackingEventsDtoAssembler assembler = new TrackingEventsDtoAssembler();

		trackingEvents = handlingEvents.stream().map(handlingEvent -> assembler.toDto(cargo, handlingEvent)).toList();
		return new CargoStatusDto(cargo.getRouteSpecification().destination().getName(), getCargoStatusText(cargo),
				cargo.getDelivery().misdirected(), getEta(cargo), getNextExpectedActivity(cargo), trackingEvents);
	}

	private String getCargoStatusText(Cargo cargo) {
		Delivery delivery = cargo.getDelivery();

		return switch (delivery.transportStatus()) {
			case IN_PORT -> "In port " + delivery.lastKnownLocation().getName();
			case ONBOARD_CARRIER -> "Onboard voyage " + delivery.currentVoyage().getVoyageNumber().number();
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
		}
		else {
			return DateUtil.toString(eta);
		}
	}

	private String getNextExpectedActivity(Cargo cargo) {
		HandlingActivity activity = cargo.getDelivery().nextExpectedActivity();

		if (activity == null) {
			return "";
		}

		String textLoad = "Next expected activity is to {0} cargo onto voyage {1} in {2}";
		String textUnload = "Next expected activity is to {0} cargo off of {1} in {2}";
		String textInPort = "Next expected activity is to {0} cargo in {1}";

		HandlingEvent.Type type = activity.type();
		return switch (type) {
			case HandlingEvent.Type.LOAD -> MessageFormat.format(textLoad, type.name().toLowerCase(),
					activity.voyage().getVoyageNumber(), activity.location().getName());
			case HandlingEvent.Type.UNLOAD -> MessageFormat.format(textUnload, type.name().toLowerCase(),
					activity.voyage().getVoyageNumber(), activity.location().getName());
			default -> MessageFormat.format(textInPort, type.name().toLowerCase(), activity.location().getName());
		};
	}

}
