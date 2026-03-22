package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A voyage schedule.
 */
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
        Objects.requireNonNull(carrierMovements, "Carrier movements is required");
        if (carrierMovements.isEmpty()) {
            throw new IllegalArgumentException("Carrier movements must not be empty");
        }
        if (carrierMovements.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Carrier movements must not contain null elements");
        }

        this.carrierMovements = carrierMovements;
    }

    public List<CarrierMovement> getCarrierMovements() {
        return List.copyOf(carrierMovements);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Schedule that)) {
            return false;
        }

        return Objects.equals(List.copyOf(carrierMovements), List.copyOf(that.carrierMovements));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(List.copyOf(this.carrierMovements));
    }
}
