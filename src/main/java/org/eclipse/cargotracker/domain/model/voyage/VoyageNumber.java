package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VoyageNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "voyage_number")
    @NotEmpty(message = "Voyage number cannot be empty")
    private String number;

    public VoyageNumber() {
        // Nothing to initialize.
    }

    public VoyageNumber(String number) {
        Objects.requireNonNull(number, "Voyage number is required");

        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VoyageNumber other)) {
            return false;
        }

        return Objects.equals(number, other.number);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(number);
    }

    @Override
    public String toString() {
        return number;
    }

    public String getIdString() {
        return number;
    }
}
