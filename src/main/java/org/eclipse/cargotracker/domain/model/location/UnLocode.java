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
public record UnLocode(
        @NotEmpty(message = "Location code must not be empty")
        // Country code is exactly two letters.
        // Location code is usually three letters, but may contain the numbers 2-9
        // as well.
        @Pattern(regexp = "[a-zA-Z]{2}[a-zA-Z2-9]{3}")
        @Column(name = "un_locode")
        String unlocode
) implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final java.util.regex.Pattern VALID_PATTERN =
            java.util.regex.Pattern.compile("[a-zA-Z]{2}[a-zA-Z2-9]{3}");

    /**
     * @param unlocode Location string.
     */
    public UnLocode {
        Objects.requireNonNull(unlocode, "Country and location may not be null");
        if (!VALID_PATTERN.matcher(unlocode).matches()) {
            throw new IllegalArgumentException(unlocode + " is not a valid UN/LOCODE (does not match pattern)");
        }
    }
}
