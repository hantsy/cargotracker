package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A voyage schedule. */
@Embeddable
public class Schedule implements Serializable {

    // Null object pattern.
    public static final Schedule EMPTY = new Schedule();
    private static final long serialVersionUID = 1L;

    // TODO [Clean Code] Look into why cascade delete doesn't work.
    // Hibernate issue:
    // orphanRemoval = true will cause exception under WildFly/Hibernate
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "voyage_id")
    // TODO [Clean Code] Index as cm_index
    @OrderColumn(name = "cm_index")
    @NotNull
    @Size(min = 1)
    private List<CarrierMovement> carrierMovements = Collections.emptyList();

    public Schedule() {
        // Nothing to initialize.
    }

    public Schedule(@Nonnull List<CarrierMovement> carrierMovements) {
        Validate.notNull(carrierMovements, "Carrier movements is required");
        Validate.noNullElements(carrierMovements);
        Validate.notEmpty(carrierMovements);

        this.carrierMovements = carrierMovements;
    }

    public List<CarrierMovement> getCarrierMovements() {
        return Collections.unmodifiableList(carrierMovements);
    }

    private boolean sameValueAs(Schedule other) {
        return other != null
                && Objects.equals(
                        List.copyOf(carrierMovements), List.copyOf(other.carrierMovements));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Schedule that = (Schedule) o;

        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(List.copyOf(this.carrierMovements));
    }
}
