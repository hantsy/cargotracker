package org.eclipse.cargotracker.domain.model.location;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * United nations location code.
 *
 * <p>http://www.unece.org/cefact/locode/
 * http://www.unece.org/cefact/locode/DocColumnDescription.htm#LOCODE
 */
@Embeddable
public record UnLocode(
        @NotEmpty(message = "Location code must not be empty")
        @Pattern(regexp = "[a-zA-Z]{2}[a-zA-Z2-9]{3}")
        @Column(name = "un_locode")
        String unlocode
)  {


    private static final java.util.regex.Pattern VALID_PATTERN =
            java.util.regex.Pattern.compile("[a-zA-Z]{2}[a-zA-Z2-9]{3}");

    public static UnLocode fromCountryAndLocation(String countryAndLocation) {
        if (countryAndLocation == null) {
            throw new IllegalArgumentException("Country and location may not be null");
        }
        if (!VALID_PATTERN.matcher(countryAndLocation).matches()) {
            throw new IllegalArgumentException(countryAndLocation + " is not a valid UN/LOCODE (does not match pattern)");
        }
        return new UnLocode(countryAndLocation.toUpperCase());
    }
}
