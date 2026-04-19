package org.eclipse.cargotracker.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;

import java.util.List;
import java.util.logging.Logger;

// WildFly issue:
// EJB can not be a inner class.

/**
 * Loads sample data for demo.
 */
@ApplicationScoped
@Transactional
public class BookingServiceTestDataGenerator {

    private final Logger LOGGER = Logger.getLogger(BookingServiceTestDataGenerator.class.getName());

    @PersistenceContext
    private EntityManager entityManager;

    public void loadSampleData(@Observes Startup startup) {
        LOGGER.info("Loading sample data.");
        unLoadAll(); // Fail-safe in case of application restart that does not
        // trigger a JPA schema drop.
        loadSampleLocations();
        loadSampleVoyages();
        // loadSampleCargos();
    }

    private void unLoadAll() {
        LOGGER.info("Unloading all existing data.");
        // In order to remove handling events, must remove references in cargo.
        // Dropping cargo first won't work since handling events have references
        // to it.
        // TODO [Clean Code] See if there is a better way to do this.
        // Note: Delivery is now a record (immutable), so we use native SQL to clear the last_event_id
        // on cargos because Delivery is immutable
        entityManager.createNativeQuery("UPDATE cargos SET last_event_id = null").executeUpdate();
        entityManager.flush();

        // Delete all entities
        // TODO [Clean Code] See why cascade delete is not working.
        entityManager.createQuery("Delete from HandlingEvent").executeUpdate();
        entityManager.createQuery("Delete from Leg").executeUpdate();
        entityManager.createQuery("Delete from Cargo").executeUpdate();
        entityManager.createQuery("Delete from CarrierMovement").executeUpdate();
        entityManager.createQuery("Delete from Voyage").executeUpdate();
        entityManager.createQuery("Delete from Location").executeUpdate();
    }

    private void loadSampleLocations() {
        LOGGER.info("Loading sample locations.");

        entityManager.persist(SampleLocations.HONGKONG);
        entityManager.persist(SampleLocations.MELBOURNE);
        entityManager.persist(SampleLocations.STOCKHOLM);
        entityManager.persist(SampleLocations.HELSINKI);
        entityManager.persist(SampleLocations.CHICAGO);
        entityManager.persist(SampleLocations.TOKYO);
        entityManager.persist(SampleLocations.HAMBURG);
        entityManager.persist(SampleLocations.SHANGHAI);
        entityManager.persist(SampleLocations.ROTTERDAM);
        entityManager.persist(SampleLocations.GOTHENBURG);
        entityManager.persist(SampleLocations.HANGZOU);
        entityManager.persist(SampleLocations.NEWYORK);
        entityManager.persist(SampleLocations.DALLAS);
    }

    private void loadSampleVoyages() {
        LOGGER.info("Loading sample voyages.");

        entityManager.persist(SampleVoyages.HONGKONG_TO_NEW_YORK);
        entityManager.persist(SampleVoyages.NEW_YORK_TO_DALLAS);
        entityManager.persist(SampleVoyages.DALLAS_TO_HELSINKI);
        entityManager.persist(SampleVoyages.HELSINKI_TO_HONGKONG);
        entityManager.persist(SampleVoyages.DALLAS_TO_HELSINKI_ALT);
    }
}