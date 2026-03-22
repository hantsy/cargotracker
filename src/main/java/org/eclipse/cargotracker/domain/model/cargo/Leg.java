package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.logging.Logger;

@Entity
@Table(name = "legs")
public class Leg implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(Leg.class.getName());
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voyage_id")
    @NotNull
    private Voyage voyage;

    @ManyToOne
    @JoinColumn(name = "load_location_id")
    @NotNull
    private Location loadLocation;

    @ManyToOne
    @JoinColumn(name = "unload_location_id")
    @NotNull
    private Location unloadLocation;

    // @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "load_time")
    @NotNull
    private LocalDateTime loadTime;

    // @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "unload_time")
    @NotNull
    private LocalDateTime unloadTime;

    public Leg() {
        // Nothing to initialize.
    }

    public Leg(
            Voyage voyage,
            Location loadLocation,
            Location unloadLocation,
            LocalDateTime loadTime,
            LocalDateTime unloadTime) {
        Objects.requireNonNull(voyage, "Voyage is required");
        Objects.requireNonNull(loadLocation, "Load location is required");
        Objects.requireNonNull(unloadLocation, "Unload location is required");
        Objects.requireNonNull(loadTime, "Load time is required");
        Objects.requireNonNull(unloadTime, "Unload time is required");

        this.voyage = voyage;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;

        // Hibernate issue:
        // when the `LocalDateTime` field is persisted into db, and retrieved from db, the values
        // are
        // different in nanoseconds.
        // any good idea to overcome this?
        this.loadTime = loadTime.truncatedTo(ChronoUnit.SECONDS);
        this.unloadTime = unloadTime.truncatedTo(ChronoUnit.SECONDS);
    }

    public Voyage getVoyage() {
        return voyage;
    }

    public Location getLoadLocation() {
        return loadLocation;
    }

    public Location getUnloadLocation() {
        return unloadLocation;
    }

    public LocalDateTime getLoadTime() {
        return this.loadTime;
    }

    public LocalDateTime getUnloadTime() {
        return this.unloadTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Leg leg)) return false;
        return Objects.equals(voyage, leg.voyage)
                && Objects.equals(loadLocation, leg.loadLocation)
                && Objects.equals(unloadLocation, leg.unloadLocation)
                && Objects.equals(loadTime, leg.loadTime)
                && Objects.equals(unloadTime, leg.unloadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voyage, loadLocation, unloadLocation, loadTime, unloadTime);
    }

    @Override
    public String toString() {
        return "Leg{"
                + "id="
                + id
                + ", voyage="
                + voyage
                + ", loadLocation="
                + loadLocation
                + ", unloadLocation="
                + unloadLocation
                + ", loadTime="
                + loadTime
                + ", unloadTime="
                + unloadTime
                + '}';
    }

    public boolean isNew() {
        return this.id == null;
    }
}
