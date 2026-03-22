package org.eclipse.cargotracker.domain.model.location;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;

/**
 * United nations location code.
 *
 * <p>http://www.unece.org/cefact/locode/
 * http://www.unece.org/cefact/locode/DocColumnDescription.htm#LOCODE
 */
@Embeddable
public class UnLocode implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final java.util.regex.Pattern VALID_PATTERN =
            java.util.regex.Pattern.compile("[a-zA-Z]{2}[a-zA-Z2-9]{3}");

    @NotEmpty(message = "Location code must not be empty")
    // Country code is exactly two letters.
    // Location code is usually three letters, but may contain the numbers 2-9
    // as well.
    @Pattern(regexp = "[a-zA-Z]{2}[a-zA-Z2-9]{3}")
    @Column(name = "un_locode")
    private String unlocode;

    public UnLocode() {
        // Nothing to initialize.
    }

    /**
     * @param countryAndLocation Location string.
     */
    public UnLocode(String countryAndLocation) {
        Objects.requireNonNull(countryAndLocation, "Country and location may not be null");
        if (!VALID_PATTERN.matcher(countryAndLocation).matches()) {
            throw new IllegalArgumentException(countryAndLocation + " is not a valid UN/LOCODE (does not match pattern)");
        }

        this.unlocode = countryAndLocation.toUpperCase();
    }

    /**
     * @return country code and location code concatenated, always upper case.
     */
    public String getIdString() {
        return unlocode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnLocode other)) {
            return false;
        }

        return Objects.equals(unlocode, other.unlocode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unlocode);
    }

    @Override
    public String toString() {
        return getIdString();
    }
}
