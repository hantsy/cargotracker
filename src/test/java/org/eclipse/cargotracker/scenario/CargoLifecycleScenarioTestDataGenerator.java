package org.eclipse.cargotracker.scenario;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// WildFly issue:
// EJB can not be an inner class.
@Singleton
@Startup
public class CargoLifecycleScenarioTestDataGenerator {

    @Inject Logger logger;

    @PersistenceContext EntityManager entityManager;

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void loadSampleData() {
        logger.info("Loading sample data.");
        unLoadAll(); // Fail-safe in case of application restart that does not
        // trigger a JPA schema drop.
        loadSampleLocations();
        loadSampleVoyages();
        // loadSampleCargos();
        entityManager
                .createQuery("select v from Voyage v ", Voyage.class)
                .getResultList()
                .forEach(voyage -> logger.log(Level.INFO, "saved voyage: {0}", voyage));
    }

    private void unLoadAll() {
        logger.info("Unloading all existing data.");
        // In order to remove handling events, must remove references in cargo.
        // Dropping cargo first won't work since handling events have references
        // to it.
        // TODO [Clean Code] See if there is a better way to do this.
        List<Cargo> cargos =
                entityManager.createQuery("Select c from Cargo c", Cargo.class).getResultList();
        cargos.forEach(
                cargo -> {
                    cargo.getDelivery().setLastEvent(null);
                    entityManager.merge(cargo);
                });
        this.entityManager.flush();

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
        logger.info("Loading sample locations.");

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
        logger.info("Loading sample voyages.");

        // voyages
        entityManager.persist(SampleVoyages.v100);
        entityManager.persist(SampleVoyages.v200);
        entityManager.persist(SampleVoyages.v300);
        entityManager.persist(SampleVoyages.v400);
    }
}
