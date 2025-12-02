package org.eclipse.cargotracker.domain.model.location;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * A location in our model is stops on a journey, such as cargo origin or destination, or
 * carrier movement end points.
 *
 * <p>
 * It is uniquely identified by a UN location code.
 */
@Entity
@Table(name = "locations")
@NamedQuery(name = "Location.findAll", query = "Select l from Location l")
@NamedQuery(name = "Location.findByUnLocode", query = "Select l from Location l where l.unLocode = :unLocode")
public class Location {

	public static final Location UNKNOWN = new Location(new UnLocode("XXXXX"), "Unknown location");

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@NotNull
	@Embedded
	private UnLocode unLocode;

	@NotEmpty
	@Column(name = "name")
	private String name;

	public Location() {
		// Nothing to do.
	}

	/**
	 * @param unLocode UN Locode
	 * @param name Location name
	 * @throws IllegalArgumentException if the UN Locode or name is null
	 */
	public Location(UnLocode unLocode, String name) {
		Objects.requireNonNull(unLocode, "Location unlocode is required");
		Objects.requireNonNull(name, "Location name is required");

		this.unLocode = unLocode;
		this.name = name;
	}

	/**
	 * @return UN location code for this location.
	 */
	public UnLocode getUnLocode() {
		return unLocode;
	}

	/**
	 * @return Actual name of this location, e.g. "Stockholm".
	 */
	public String getName() {
		return name;
	}

	public boolean sameIdentityAs(Location other) {
		return this.unLocode.equals(other.unLocode);
	}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Location location)) return false;
        return Objects.equals(unLocode, location.unLocode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unLocode);
    }

	@Override
	public String toString() {
		return this.getName() + " (" + this.getUnLocode() + ")";
	}

}
