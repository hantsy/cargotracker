package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import org.eclipse.cargotracker.domain.model.location.Location;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "voyages")
@NamedQuery(name = "Voyage.findByVoyageNumber", query = "Select v from Voyage v where v.voyageNumber = :voyageNumber")
@NamedQuery(name = "Voyage.findAll", query = "Select v from Voyage v order by v.voyageNumber")
public class Voyage {

	// Null object pattern
	public static final Voyage NONE = new Voyage(new VoyageNumber(""), Schedule.EMPTY);

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Embedded
	@NotNull(message = "Voyage number is required")
	private VoyageNumber voyageNumber;

	@Embedded
	@NotNull(message = "Schedule is required")
	private Schedule schedule;

	public Voyage() {
		// Nothing to initialize
	}

	public Voyage(VoyageNumber voyageNumber, Schedule schedule) {
		Objects.requireNonNull(voyageNumber, "Voyage number is required");
		Objects.requireNonNull(schedule, "Schedule is required");

		this.voyageNumber = voyageNumber;
		this.schedule = schedule;
	}

	public VoyageNumber getVoyageNumber() {
		return voyageNumber;
	}

	public Schedule getSchedule() {
		return schedule;
	}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Voyage voyage)) return false;
        return Objects.equals(voyageNumber, voyage.voyageNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(voyageNumber);
    }

    @Override
	public String toString() {
		return "Voyage " + voyageNumber;
	}

	/**
	 * Builder pattern is used for incremental construction of a Voyage aggregate. This
	 * serves as an aggregate factory.
	 */
	public static class Builder {

		private final List<CarrierMovement> carrierMovements = new ArrayList<>();

		private final VoyageNumber voyageNumber;

		private Location departureLocation;

		public Builder(VoyageNumber voyageNumber, Location departureLocation) {
			Objects.requireNonNull(voyageNumber, "Voyage number is required");
			Objects.requireNonNull(departureLocation, "Departure location is required");

			this.voyageNumber = voyageNumber;
			this.departureLocation = departureLocation;
		}

		public Builder addMovement(Location arrivalLocation, LocalDateTime departureTime, LocalDateTime arrivalTime) {
			carrierMovements.add(new CarrierMovement(departureLocation, arrivalLocation, departureTime, arrivalTime));

			// Next departure location is the same as this arrival location
			this.departureLocation = arrivalLocation;

			return this;
		}

		public Voyage build() {
			return new Voyage(voyageNumber, new Schedule(carrierMovements));
		}

	}

}
