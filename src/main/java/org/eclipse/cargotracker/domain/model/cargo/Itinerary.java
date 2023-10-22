package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.lang3.Validate;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Embeddable
public class Itinerary implements Serializable {

    // Null object pattern.
    public static final Itinerary EMPTY_ITINERARY = new Itinerary();
    private static final long serialVersionUID = 1L;

    // TODO [Clean Code] Look into why cascade delete doesn't work.
    // Hibernate issue:
    // Changes applied according to WildFly/Hibernate requirements.
    // The `orphanRemoval = true` option will causes a `all-delete-orphan` exception under
    // WildFly/Hibernate.
    // (There is a famous lazy initialization exception you could encounter WildFly/Hibernate.
    // The `fetch = FetchType.EAGER` fixes the Hibernate lazy initialization exception
    // but maybe cause bad performance. A good practice is accessing the one-to-many relations
    // in a session/tx boundary)
    //
    // @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "cargo_id")
    // TODO [Clean Code] Index this is in leg_index
    // Hibernate issue:
    // Hibernate does not persist the order of the list element when saving into db.
    // The `OrderColumn` will persist the position of list elements in db.
    @OrderColumn(name = "leg_index")
    // The `OrderBy` only ensures the order of list elements in memory. Only `@OrderBy("loadTime")`
    // is added some tests are still failed under WildFly/Hibernate.
    // @OrderBy("loadTime")
    @Size(min = 1)
    @NotEmpty(message = "Legs must not be empty")
    private List<Leg> legs = Collections.emptyList();

    public Itinerary() {
        // Nothing to initialize.
    }

    public Itinerary(List<Leg> legs) {
        Validate.notEmpty(legs);
        Validate.noNullElements(legs);

        this.legs = legs;
    }

    public List<Leg> getLegs() {
        // this.legs.sort(Comparator.comparing(Leg::getLoadTime));
        return Collections.unmodifiableList(this.legs);
    }

    /** Test if the given handling event is expected when executing this itinerary. */
    public boolean isExpected(HandlingEvent event) {
        if (legs.isEmpty()) {
            return true;
        }

        // Check that the first leg's origin is the event's location
        // Check that the last leg's destination is from the event's
        // location
        return switch (event.getType()) {
            case RECEIVE -> {
                Leg leg = legs.get(0);
                yield leg.getLoadLocation().equals(event.getLocation());
            }
            case LOAD -> legs.stream()
                    .anyMatch(
                            leg ->
                                    leg.getLoadLocation().equals(event.getLocation())
                                            && leg.getVoyage().equals(event.getVoyage()));
            case UNLOAD ->
            // Check that the there is one leg with same unload location and
            // voyage
            legs.stream()
                    .anyMatch(
                            leg ->
                                    leg.getUnloadLocation().equals(event.getLocation())
                                            && leg.getVoyage().equals(event.getVoyage()));
            case CLAIM -> {
                Leg leg = getLastLeg();
                yield leg.getUnloadLocation().equals(event.getLocation());
            }
            case CUSTOMS -> true;
            default -> throw new RuntimeException("Event case is not handled");
        };
    }

    Location getInitialDepartureLocation() {
        if (legs.isEmpty()) {
            return Location.UNKNOWN;
        } else {
            return legs.get(0).getLoadLocation();
        }
    }

    Location getFinalArrivalLocation() {
        if (legs.isEmpty()) {
            return Location.UNKNOWN;
        } else {
            return getLastLeg().getUnloadLocation();
        }
    }

    /**
     * @return Date when cargo arrives at final destination.
     */
    LocalDateTime getFinalArrivalDate() {
        Leg lastLeg = getLastLeg();

        if (lastLeg == null) {
            return LocalDateTime.MAX;
        } else {
            return lastLeg.getUnloadTime();
        }
    }

    /**
     * @return The last leg on the itinerary.
     */
    Leg getLastLeg() {
        if (legs.isEmpty()) {
            return null;
        } else {
            return legs.get(legs.size() - 1);
        }
    }

    private boolean sameValueAs(Itinerary other) {
        // return other != null && legs.equals(other.legs);
        //
        // Hibernate issue:
        // When comparing a `List` type property of an entity, it is also a proxy class in runtime.
        // Use a `copyOf` to compare using the contained items temporally.
        return other != null && Objects.equals(List.copyOf(this.legs), List.copyOf(other.legs));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        //        if (o == null || getClass() != o.getClass()) {
        //            return false;
        //        }
        //
        // https://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java
        // Hibernate issue:
        // `getClass() != o.getClass()` will fail if comparing the objects in different
        // transactions/sessions.
        // The generated dynamic proxies are always different classes.
        if (o == null || !(o instanceof Itinerary)) {
            return false;
        }

        Itinerary itinerary = (Itinerary) o;

        return sameValueAs(itinerary);
    }

    @Override
    public int hashCode() {
        // return legs.hashCode();
        return Objects.hashCode(List.copyOf(legs));
    }

    @Override
    public String toString() {
        return "Itinerary{" + "legs=" + legs + '}';
    }
}
