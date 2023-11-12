package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JpaLocationRepository implements LocationRepository, Serializable {

    private static final long serialVersionUID = 1L;

    @Inject Logger logger;

    @PersistenceContext private EntityManager entityManager;

    @Override
    public Location find(UnLocode unLocode) {
        Location location;
        try {
            location =
                    entityManager
                            .createNamedQuery("Location.findByUnLocode", Location.class)
                            .setParameter("unLocode", unLocode)
                            .getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.WARNING, "Can not find Location by code: {0}", e.getMessage());
            location = null;
        }
        return location;
    }

    @Override
    public List<Location> findAll() {
        return entityManager.createNamedQuery("Location.findAll", Location.class).getResultList();
    }
}
