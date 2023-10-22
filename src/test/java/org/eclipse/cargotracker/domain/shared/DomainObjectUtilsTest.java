package org.eclipse.cargotracker.domain.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainObjectUtilsTest {

    @Test
    public void testNullSafe() {
        String nullObject = null;
        String noneNullObject = new String("noneNull");

        var safeObject = DomainObjectUtils.nullSafe(nullObject, "safe");
        assertThat(safeObject).isEqualTo("safe");

        var safeObject2 = DomainObjectUtils.nullSafe(noneNullObject, "safe");
        assertThat(safeObject2).isEqualTo(noneNullObject);
    }
}
