package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JpaVoyageRepository implements VoyageRepository, Serializable {

    private static final long serialVersionUID = 1L;

    @Inject Logger logger;

    @PersistenceContext private EntityManager entityManager;

    @Override
    public Voyage find(VoyageNumber voyageNumber) {
        Voyage voyage = null;
        try {
            voyage =
                    entityManager
                            .createNamedQuery("Voyage.findByVoyageNumber", Voyage.class)
                            .setParameter("voyageNumber", voyageNumber)
                            .getSingleResult();
        } catch (NoResultException e) {
            logger.log(
                    Level.WARNING,
                    "Find called on non-existing voyageNumber: {0}.",
                    e.getMessage());
        }
        return voyage;
    }

    @Override
    public List<Voyage> findAll() {
        return entityManager.createNamedQuery("Voyage.findAll", Voyage.class).getResultList();
    }
}
