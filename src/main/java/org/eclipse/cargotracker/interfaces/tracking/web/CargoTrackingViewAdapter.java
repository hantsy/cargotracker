package org.eclipse.cargotracker.interfaces.tracking.web;

import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Delivery;
import org.eclipse.cargotracker.domain.model.cargo.HandlingActivity;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** View adapter for displaying a cargo in a tracking context. */
public class CargoTrackingViewAdapter {

	private final Cargo cargo;

	private final List<HandlingEventViewAdapter> events;

	public CargoTrackingViewAdapter(Cargo cargo, List<HandlingEvent> handlingEvents) {
		this.cargo = cargo;
		this.events = new ArrayList<>(handlingEvents.size());

		handlingEvents.stream().map(HandlingEventViewAdapter::new).forEach(events::add);
	}

	public String getTrackingId() {
		return cargo.getTrackingId().id();
	}

	public String getOriginName() {
		return cargo.getRouteSpecification().origin().getName();
	}

	public String getOriginCode() {
		return cargo.getRouteSpecification().origin().getUnLocode().value();
	}

	public String getDestinationName() {
		return cargo.getRouteSpecification().destination().getName();
	}

	public String getDestinationCode() {
		return cargo.getRouteSpecification().destination().getUnLocode().value();
	}

	public String getLastKnownLocationName() {
		return cargo.getDelivery().lastKnownLocation().equals(Location.UNKNOWN) ? "Unknown"
				: cargo.getDelivery().lastKnownLocation().getName();
	}

	public String getLastKnownLocationCode() {
		return cargo.getDelivery().lastKnownLocation().getUnLocode().value();
	}

	public String getStatusCode() {
		if (cargo.getItinerary().legs().isEmpty()) {
			return "NOT_ROUTED";
		}

		if (cargo.getDelivery().isUnloadedAtDestination()) {
			return "AT_DESTINATION";
		}

		if (cargo.getDelivery().misdirected()) {
			return "MISDIRECTED";
		}

		return cargo.getDelivery().transportStatus().name();
	}

	/**
	 * @return A readable string describing the cargo status.
	 */
	public String getStatusText() {
		Delivery delivery = cargo.getDelivery();

		return switch (delivery.transportStatus()) {
			case IN_PORT -> "In port " + cargo.getRouteSpecification().destination().getName();
			case ONBOARD_CARRIER -> "Onboard voyage " + delivery.currentVoyage().getVoyageNumber().number();
			case CLAIMED -> "Claimed";
			case NOT_RECEIVED -> "Not received";
			case UNKNOWN -> "Unknown";
			default -> "[Unknown status]"; // Should never happen.
		};
	}

	public boolean isMisdirected() {
		return cargo.getDelivery().misdirected();
	}

	public String getEta() {
		LocalDateTime eta = cargo.getDelivery().estimatedTimeOfArrival();

		if (eta == null) {
			return "?";
		}
		else {
			return DateUtil.toString(eta);
		}
	}

	public String getNextExpectedActivity() {
		HandlingActivity activity = cargo.getDelivery().nextExpectedActivity();

		if ((activity == null) || (activity.isEmpty())) {
			return "";
		}

		String text = "Next expected activity is to ";
		HandlingEvent.Type type = activity.type();

		if (type.sameValueAs(HandlingEvent.Type.LOAD)) {
			return text + type.name().toLowerCase() + " cargo onto voyage " + activity.voyage().getVoyageNumber()
					+ " in " + activity.location().getName();
		}
		else if (type.sameValueAs(HandlingEvent.Type.UNLOAD)) {
			return text + type.name().toLowerCase() + " cargo off of " + activity.voyage().getVoyageNumber() + " in "
					+ activity.location().getName();
		}
		else {
			return text + type.name().toLowerCase() + " cargo in " + activity.location().getName();
		}
	}

	/**
	 * @return An unmodifiable list of handling event view adapters.
	 */
	public List<HandlingEventViewAdapter> getEvents() {
		return Collections.unmodifiableList(events);
	}

	/** Handling event view adapter component. */
	public class HandlingEventViewAdapter {

		private final HandlingEvent handlingEvent;

		private final boolean expected;

		public HandlingEventViewAdapter(HandlingEvent handlingEvent) {
			this.handlingEvent = handlingEvent;
			// move this executed before rendering the view.
			this.expected = cargo.getItinerary().isExpected(handlingEvent);
		}

		/**
		 * @return the date in the format MM/dd/yyyy hh:mm a z
		 */
		public String getTime() {
			// return
			// handlingEvent.getCompletionTime().format(DateTimeFormatter.ofPattern(DT_PATTERN));
			return DateUtil.toString(handlingEvent.getCompletionTime());
		}

		public boolean isExpected() {
			// This will cause Hibernate lazy initialization exception thrown in WildFly.
			// return cargo.getItinerary().isExpected(handlingEvent);
			return this.expected;
		}

		public String getDescription() {
			switch (handlingEvent.getType()) {
				case LOAD:
					return "Loaded onto voyage " + handlingEvent.getVoyage().getVoyageNumber().number() + " in "
							+ handlingEvent.getLocation().getName();
				case UNLOAD:
					return "Unloaded off voyage " + handlingEvent.getVoyage().getVoyageNumber().number() + " in "
							+ handlingEvent.getLocation().getName();
				case RECEIVE:
					return "Received in " + handlingEvent.getLocation().getName();
				case CLAIM:
					return "Claimed in " + handlingEvent.getLocation().getName();
				case CUSTOMS:
					return "Cleared customs in " + handlingEvent.getLocation().getName();
				default:
					return "[Unknown]";
			}
		}

	}

}
