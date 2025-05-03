package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.TrackingEventsDto;

public class TrackingEventsDtoAssembler {

	public TrackingEventsDto toDto(Cargo cargo, HandlingEvent handlingEvent) {
		String location = locationFrom(handlingEvent);
		HandlingEvent.Type type = handlingEvent.getType();
		String voyageNumber = voyageNumberFrom(handlingEvent);
		return new TrackingEventsDto(cargo.getItinerary().isExpected(handlingEvent),
				descriptionFrom(type, location, voyageNumber), timeFrom(handlingEvent));
	}

	private String timeFrom(HandlingEvent event) {
		return DateUtil.toString(event.getCompletionTime());
	}

	private String descriptionFrom(HandlingEvent.Type type, String location, String voyageNumber) {
		return switch (type) {
			case LOAD -> "Loaded onto voyage " + voyageNumber + " in " + location;
			case UNLOAD -> "Unloaded off voyage " + voyageNumber + " in " + location;
			case RECEIVE -> "Received in " + location;
			case CLAIM -> "Claimed in " + location;
			case CUSTOMS -> "Cleared customs in " + location;
			default -> "[Unknown]";
		};
	}

	private String voyageNumberFrom(HandlingEvent handlingEvent) {
		Voyage voyage = handlingEvent.getVoyage();
		return voyage.getVoyageNumber().number();
	}

	private String locationFrom(HandlingEvent handlingEvent) {
		return handlingEvent.getLocation().getName();
	}

}
