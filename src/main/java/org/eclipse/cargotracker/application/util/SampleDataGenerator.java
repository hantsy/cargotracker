package org.eclipse.cargotracker.application.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;

import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.handling.*;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/** Loads sample data for demo. */
@ApplicationScoped
public class SampleDataGenerator {

	private static final Logger LOGGER = Logger.getLogger(SampleDataGenerator.class.getName());

	// private @PersistenceContext EntityManager entityManager;
	// private @Inject HandlingEventFactory handlingEventFactory;
	// private @Inject HandlingEventRepository handlingEventRepository;
    
    private @Inject EntityManagerFactory entityManagerFactory;

	private @Inject HandlingEventFactory handlingEventFactory;

	private @Inject HandlingEventRepository handlingEventRepository;

	// required by CDI
	// public SampleDataGenerator() {}
	//
	// @Inject
	// public SampleDataGenerator(
	// EntityManager entityManager,
	// HandlingEventFactory handlingEventFactory,
	// HandlingEventRepository handlingEventRepository) {
	// this.entityManager = entityManager;
	// this.handlingEventFactory = handlingEventFactory;
	// this.handlingEventRepository = handlingEventRepository;
	// }

	@Transactional
	public void loadSampleData(@Observes Startup startup) {
		LOGGER.info("Loading sample data.");
		unLoadAll(); // Fail-safe in case of application restart that does not trigger a
						// JPA schema
		// drop.
        entityManagerFactory.runInTransaction(entityManager -> {
            loadSampleLocations(entityManager);
            loadSampleVoyages(entityManager);
            loadSampleCargos(entityManager);
        });
	}

	private void unLoadAll() {
		LOGGER.info("Unloading all existing data.");
		// In order to remove handling events, must remove references in cargo.
		// Dropping cargo first won't work since handling events have references
		// to it.
		// TODO [Clean Code] See if there is a better way to do this.
		List<Cargo> cargos = entityManager.createQuery("Select c from Cargo c", Cargo.class).getResultList();
		for (Cargo cargo : cargos) {
			Delivery delivery = cargo.getDelivery();
            delivery.lastEvent()
			entityManager.merge(cargo);
		}
		entityManager.flush();
//
//		// Delete all entities
//		// TODO [Clean Code] See why cascade delete is not working.
//		entityManager.createQuery("Delete from HandlingEvent").executeUpdate();
//		entityManager.createQuery("Delete from Leg").executeUpdate();
//		entityManager.createQuery("Delete from Cargo").executeUpdate();
//		entityManager.createQuery("Delete from CarrierMovement").executeUpdate();
//		entityManager.createQuery("Delete from Voyage").executeUpdate();
//		entityManager.createQuery("Delete from Location").executeUpdate();
        entityManagerFactory.getSchemaManager().truncate();
	}

	private void loadSampleLocations(EntityManager entityManager) {
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
		entityManager.persist(SampleLocations.HANGZHOU);
		entityManager.persist(SampleLocations.NEWYORK);
		entityManager.persist(SampleLocations.DALLAS);
	}

	private void loadSampleVoyages(EntityManager entityManager) {
		LOGGER.info("Loading sample voyages.");

		entityManager.persist(SampleVoyages.HONGKONG_TO_NEW_YORK);
		entityManager.persist(SampleVoyages.NEW_YORK_TO_DALLAS);
		entityManager.persist(SampleVoyages.DALLAS_TO_HELSINKI);
		entityManager.persist(SampleVoyages.HELSINKI_TO_HONGKONG);
		entityManager.persist(SampleVoyages.DALLAS_TO_HELSINKI_ALT);
	}

	private void loadSampleCargos(EntityManager entityManager) {
		LOGGER.info("Loading sample cargo data.");

		// Cargo ABC123. This one is en-route.
		TrackingId trackingId1 = new TrackingId("ABC123");

		RouteSpecification routeSpecification1 = new RouteSpecification(SampleLocations.HONGKONG,
				SampleLocations.HELSINKI, LocalDate.now().plusDays(15));
		Cargo abc123 = new Cargo(trackingId1, routeSpecification1);

		Itinerary itinerary1 = new Itinerary(Arrays.asList(
				new Leg(SampleVoyages.HONGKONG_TO_NEW_YORK, SampleLocations.HONGKONG, SampleLocations.NEWYORK,
						LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1)),
				new Leg(SampleVoyages.NEW_YORK_TO_DALLAS, SampleLocations.NEWYORK, SampleLocations.DALLAS,
						LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(6)),
				new Leg(SampleVoyages.DALLAS_TO_HELSINKI, SampleLocations.DALLAS, SampleLocations.HELSINKI,
						LocalDateTime.now().plusDays(8), LocalDateTime.now().plusDays(14))));
		abc123.assignToRoute(itinerary1);

		entityManager.persist(abc123);

		try {
			HandlingEvent event1 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(10), trackingId1, null, SampleLocations.HONGKONG.getUnLocode(),
					HandlingEvent.Type.RECEIVE);
			entityManager.persist(event1);

			HandlingEvent event2 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(7), trackingId1, SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber(),
					SampleLocations.HONGKONG.getUnLocode(), HandlingEvent.Type.LOAD);
			entityManager.persist(event2);

			HandlingEvent event3 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(1), trackingId1, SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber(),
					SampleLocations.NEWYORK.getUnLocode(), HandlingEvent.Type.UNLOAD);
			entityManager.persist(event3);
		}
		catch (CannotCreateHandlingEventException e) {
			throw new RuntimeException(e);
		}

		HandlingHistory handlingHistory1 = handlingEventRepository.lookupHandlingHistoryOfCargo(trackingId1);
		abc123.deriveDeliveryProgress(handlingHistory1);

		entityManager.persist(abc123);

		// Cargo JKL567. This one was loaded on the wrong voyage.
		TrackingId trackingId2 = new TrackingId("JKL567");

		RouteSpecification routeSpecification2 = new RouteSpecification(SampleLocations.HANGZHOU,
				SampleLocations.STOCKHOLM, LocalDate.now().plusDays(18));
		Cargo jkl567 = new Cargo(trackingId2, routeSpecification2);

		Itinerary itinerary2 = new Itinerary(Arrays.asList(
				new Leg(SampleVoyages.HONGKONG_TO_NEW_YORK, SampleLocations.HANGZHOU, SampleLocations.NEWYORK,
						LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(3)),
				new Leg(SampleVoyages.NEW_YORK_TO_DALLAS, SampleLocations.NEWYORK, SampleLocations.DALLAS,
						LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2)),
				new Leg(SampleVoyages.DALLAS_TO_HELSINKI, SampleLocations.DALLAS, SampleLocations.STOCKHOLM,
						LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(15))));
		jkl567.assignToRoute(itinerary2);

		entityManager.persist(jkl567);

		try {
			HandlingEvent event1 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(15), trackingId2, null, SampleLocations.HANGZHOU.getUnLocode(),
					HandlingEvent.Type.RECEIVE);
			entityManager.persist(event1);

			HandlingEvent event2 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(10), trackingId2,
					SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber(), SampleLocations.HANGZHOU.getUnLocode(),
					HandlingEvent.Type.LOAD);
			entityManager.persist(event2);

			HandlingEvent event3 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(3), trackingId2, SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber(),
					SampleLocations.NEWYORK.getUnLocode(), HandlingEvent.Type.UNLOAD);
			entityManager.persist(event3);

			// The wrong voyage!
			HandlingEvent event4 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(2), trackingId2, SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber(),
					SampleLocations.NEWYORK.getUnLocode(), HandlingEvent.Type.LOAD);
			entityManager.persist(event4);
		}
		catch (CannotCreateHandlingEventException e) {
			throw new RuntimeException(e);
		}

		HandlingHistory handlingHistory2 = handlingEventRepository.lookupHandlingHistoryOfCargo(trackingId2);
		jkl567.deriveDeliveryProgress(handlingHistory2);

		entityManager.persist(jkl567);

		// Cargo definition DEF789. This one will remain un-routed.
		TrackingId trackingId3 = new TrackingId("DEF789");

		RouteSpecification routeSpecification3 = new RouteSpecification(SampleLocations.HONGKONG,
				SampleLocations.MELBOURNE, LocalDate.now().plusMonths(2));

		Cargo def789 = new Cargo(trackingId3, routeSpecification3);
		entityManager.persist(def789);

		// Cargo definition MNO456. This one will be claimed properly.
		TrackingId trackingId4 = new TrackingId("MNO456");
		RouteSpecification routeSpecification4 = new RouteSpecification(SampleLocations.NEWYORK, SampleLocations.DALLAS,
				LocalDate.now().minusDays(24));

		Cargo mno456 = new Cargo(trackingId4, routeSpecification4);

		Itinerary itinerary4 = new Itinerary(
                List.of(new Leg(SampleVoyages.NEW_YORK_TO_DALLAS, SampleLocations.NEWYORK, SampleLocations.DALLAS,
                        LocalDateTime.now().minusDays(34), LocalDateTime.now().minusDays(28))));

		mno456.assignToRoute(itinerary4);
		entityManager.persist(mno456);

		try {
			HandlingEvent event1 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(37), trackingId4, null, SampleLocations.NEWYORK.getUnLocode(),
					HandlingEvent.Type.RECEIVE);

			entityManager.persist(event1);

			HandlingEvent event2 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(34), trackingId4, SampleVoyages.NEW_YORK_TO_DALLAS.getVoyageNumber(),
					SampleLocations.NEWYORK.getUnLocode(), HandlingEvent.Type.LOAD);

			entityManager.persist(event2);

			HandlingEvent event3 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(28), trackingId4, SampleVoyages.NEW_YORK_TO_DALLAS.getVoyageNumber(),
					SampleLocations.DALLAS.getUnLocode(), HandlingEvent.Type.UNLOAD);

			entityManager.persist(event3);

			HandlingEvent event4 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(27), trackingId4, null, SampleLocations.DALLAS.getUnLocode(),
					HandlingEvent.Type.CUSTOMS);

			entityManager.persist(event4);

			HandlingEvent event5 = handlingEventFactory.createHandlingEvent(LocalDateTime.now(),
					LocalDateTime.now().minusDays(26), trackingId4, null, SampleLocations.DALLAS.getUnLocode(),
					HandlingEvent.Type.CLAIM);

			entityManager.persist(event5);

			HandlingHistory handlingHistory3 = handlingEventRepository.lookupHandlingHistoryOfCargo(trackingId4);

			mno456.deriveDeliveryProgress(handlingHistory3);

			entityManager.persist(mno456);
		}
		catch (CannotCreateHandlingEventException e) {
			throw new RuntimeException(e);
		}
	}

}
