package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

@Embeddable
public record VoyageNumber(
        @Column(name = "voyage_number") @NotBlank(message = "Voyage number cannot be blank")
                String number)
        implements Serializable {
    private static final long serialVersionUID = 1L;

    //    public VoyageNumber {
    //        // Nothing to initialize.
    //        Validate.notNull(number, "Voyage number is required");
    //    }

    //
    //    @Override
    //    public boolean equals(Object o) {
    //        if (this == o) {
    //            return true;
    //        }
    //        if (o == null) {
    //            return false;
    //        }
    //        if (!(o instanceof VoyageNumber)) {
    //            return false;
    //        }
    //
    //        VoyageNumber other = (VoyageNumber) o;
    //
    //        return sameValueAs(other);
    //    }
    //
    //    @Override
    //    public int hashCode() {
    //        return number.hashCode();
    //    }

    boolean sameValueAs(VoyageNumber other) {
        return other != null && this.number.equals(other.number);
    }

    //    @Override
    //    public String toString() {
    //        return number;
    //    }
}
