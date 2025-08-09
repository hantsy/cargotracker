package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "legs")
public class Leg {

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

    @Column(name = "load_time", secondPrecision = 3)
    @NotNull
    private LocalDateTime loadTime;

    @Column(name = "unload_time", secondPrecision = 3)
    @NotNull
    private LocalDateTime unloadTime;

    public Leg() {
        // Nothing to initialize.
    }

    public Leg(Voyage voyage, Location loadLocation, Location unloadLocation, LocalDateTime loadTime,
               LocalDateTime unloadTime) {
        Objects.requireNonNull(voyage, "voyage must not be null");
        Objects.requireNonNull(loadLocation, "loadLocation must not be null");
        Objects.requireNonNull(unloadLocation, "unloadLocation must not be null");
        Objects.requireNonNull(loadTime, "loadTime must not be null");
        Objects.requireNonNull(unloadTime, "unloadTime must not be null");

        this.voyage = voyage;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;

        // Hibernate issue:
        // when the `LocalDateTime` field is persisted into db, and retrieved from db, the
        // values
        // are
        // different in nanoseconds.
        // any good idea to overcome this?
        // https://github.com/jakartaee/persistence/issues/563
        this.loadTime = loadTime;
        this.unloadTime = unloadTime;
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
        return "Leg{" + "id=" + id + ", voyage=" + voyage + ", loadLocation=" + loadLocation + ", unloadLocation="
                + unloadLocation + ", loadTime=" + loadTime + ", unloadTime=" + unloadTime + '}';
    }

    public boolean isNew() {
        return this.id == null;
    }

}
