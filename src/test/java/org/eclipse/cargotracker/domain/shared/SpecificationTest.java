package org.eclipse.cargotracker.domain.shared;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class SpecificationTest {

    @Test
    public void testAndSpecification() {
        Specification<Object> alwaysTrue = t -> true;
        Specification<Object> alwaysFalse = t -> false;

        Specification<Object> andSpec1 = alwaysTrue.and(alwaysTrue);
        assertThat(andSpec1.isSatisfiedBy(new Object())).isTrue();

        Specification<Object> andSpec2 = alwaysTrue.and(alwaysFalse);
        assertThat(andSpec2.isSatisfiedBy(new Object())).isFalse();

        Specification<Object> andSpec3 = alwaysFalse.and(alwaysTrue);
        assertThat(andSpec3.isSatisfiedBy(new Object())).isFalse();

        Specification<Object> andSpec4 = alwaysFalse.and(alwaysFalse);
        assertThat(andSpec4.isSatisfiedBy(new Object())).isFalse();
    }

    @Test
    public void testOrSpecification() {
        Specification<Object> alwaysTrue = t -> true;
        Specification<Object> alwaysFalse = t -> false;

        Specification<Object> orSpec1 = alwaysTrue.or(alwaysTrue);
        assertThat(orSpec1.isSatisfiedBy(new Object())).isTrue();

        Specification<Object> orSpec2 = alwaysTrue.or(alwaysFalse);
        assertThat(orSpec2.isSatisfiedBy(new Object())).isTrue();

        Specification<Object> orSpec3 = alwaysFalse.or(alwaysTrue);
        assertThat(orSpec3.isSatisfiedBy(new Object())).isTrue();

        Specification<Object> orSpec4 = alwaysFalse.or(alwaysFalse);
        assertThat(orSpec4.isSatisfiedBy(new Object())).isFalse();
    }

    @Test
    public void testNotSpecification() {
        Specification<Object> alwaysTrue = t -> true;
        Specification<Object> alwaysFalse = t -> false;

        Specification<Object> notSpec1 = alwaysTrue.not();
        assertThat(notSpec1.isSatisfiedBy(new Object())).isFalse();

        Specification<Object> notSpec2 = alwaysFalse.not();
        assertThat(notSpec2.isSatisfiedBy(new Object())).isTrue();
    }
    
    @Test
    public void testExplicitInstantiation() {
        Specification<Object> alwaysTrue = t -> true;
        Specification<Object> alwaysFalse = t -> false;

        Specification<Object> andSpec = new AndSpecification<>(alwaysTrue, alwaysTrue);
        assertThat(andSpec.isSatisfiedBy(new Object())).isTrue();

        Specification<Object> orSpec = new OrSpecification<>(alwaysFalse, alwaysTrue);
        assertThat(orSpec.isSatisfiedBy(new Object())).isTrue();

        Specification<Object> notSpec = new NotSpecification<>(alwaysTrue);
        assertThat(notSpec.isSatisfiedBy(new Object())).isFalse();
    }
}
