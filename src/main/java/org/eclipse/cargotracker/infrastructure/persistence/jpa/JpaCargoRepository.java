package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JpaCargoRepository implements CargoRepository, Serializable {

    private static final Logger logger = Logger.getLogger(JpaCargoRepository.class.getName());

    private EntityManager entityManager;

    // no-args constructor required by CDI
    public JpaCargoRepository() {}

    @Inject
    public JpaCargoRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Cargo find(TrackingId trackingId) {
        Cargo cargo;

        try {
            cargo =
                    entityManager
                            .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                            .setParameter("trackingId", trackingId)
                            .getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Find called on non-existant tracking ID.", e);
            cargo = null;
        }

        return cargo;
    }

    @Override
    public void store(Cargo cargo) {
        // TODO [Clean Code] See why cascade is not working correctly for legs.
        cargo.getItinerary().legs().forEach(leg -> entityManager.persist(leg));

        entityManager.persist(cargo);

        // Hibernate issue:
        // Delete-orphan does not seem to work correctly when the parent is a component
        this.entityManager
                .createNativeQuery("DELETE FROM legs WHERE cargo_id IS NULL")
                .executeUpdate();
    }

    @Override
    public TrackingId nextTrackingId() {
        String random = UUID.randomUUID().toString().toUpperCase();

        return new TrackingId(random.substring(0, random.indexOf("-")));
    }

    @Override
    public List<Cargo> findAll() {
        return entityManager.createNamedQuery("Cargo.findAll", Cargo.class).getResultList();
    }
}
