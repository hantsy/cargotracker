package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Objects;

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
        Objects.requireNonNull(voyage, "voyage must not be null");
        Objects.requireNonNull(loadLocation, "loadLocation must not be null");
        Objects.requireNonNull(unloadLocation, "unloadLocation must not be null");
        Objects.requireNonNull(loadTime, "loadTime must not be null");
        Objects.requireNonNull(unloadTime, "unloadTime must not be null");

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

    private boolean sameValueAs(Leg other) {
        LOGGER.log(
                Level.INFO,
                "this.loadTime == other.loadTime:{0}",
                this.loadTime.equals(other.loadTime));
        LOGGER.log(
                Level.INFO,
                "this.unloadTime == other.unloadTime:{0}",
                this.unloadTime.equals(other.unloadTime));
        return other != null
                && new EqualsBuilder()
                        .append(this.voyage, other.voyage)
                        .append(this.loadLocation, other.loadLocation)
                        .append(this.unloadLocation, other.unloadLocation)
                        // use truncatedTo to remove nanoseconds fields in timestamp.
                        .append(this.loadTime, other.loadTime)
                        .append(this.unloadTime, other.unloadTime)
                        .isEquals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Leg)) {
            return false;
        }

        Leg leg = (Leg) o;

        return sameValueAs(leg);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.voyage)
                .append(this.loadLocation)
                .append(this.unloadLocation)
                .append(this.loadTime)
                .append(this.unloadTime)
                .toHashCode();
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
