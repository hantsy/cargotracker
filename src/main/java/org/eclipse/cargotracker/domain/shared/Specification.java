package org.eclipse.cargotracker.domain.shared;

/**
 * Abstract base implementation of composite {@link Specification} with default implementations for
 * {@code and}, {@code or} and {@code not}.
 */
public interface Specification<T> {

    /**
     * Check if {@code t} is satisfied by the specification.
     *
     * @param t Object to test.
     * @return {@code true} if {@code t} satisfies the specification.
     */
    boolean isSatisfiedBy(T t);

    /**
     * Create a new specification that is the AND operation of {@code this} specification and
     * another specification.
     *
     * @param specification Specification to AND.
     * @return A new specification.
     */
    default Specification<T> and(Specification<T> specification) {
        return new AndSpecification<>(this, specification);
    }

    /**
     * Create a new specification that is the OR operation of {@code this} specification and another
     * specification.
     *
     * @param specification Specification to OR.
     * @return A new specification.
     */
    default Specification<T> or(Specification<T> specification) {
        return new OrSpecification<>(this, specification);
    }

    /**
     * Create a new specification that is the NOT operation of {@code this} specification.
     *
     * @param specification Specification to NOT.
     * @return A new specification.
     */
    default Specification<T> not(Specification<T> specification) {
        return new NotSpecification<>(specification);
    }
}
