package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import org.eclipse.cargotracker.TxUtil;
import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.addApplicationBase;
import static org.eclipse.cargotracker.Deployments.addDomainModels;
import static org.eclipse.cargotracker.Deployments.addDomainRepositories;
import static org.eclipse.cargotracker.Deployments.addExtraJars;
import static org.eclipse.cargotracker.Deployments.addInfraBase;
import static org.eclipse.cargotracker.Deployments.addInfraPersistence;
import static org.eclipse.cargotracker.domain.model.handling.HandlingEvent.Type.LOAD;

@ArquillianTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CargoRepositoryIT {
    private static final Logger LOGGER = Logger.getLogger(CargoRepositoryIT.class.getName());
    @Inject
    UserTransaction utx;

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private HandlingEventRepository handlingEventRepository;

    @Inject
    private CargoRepository cargoRepository;

    @Inject
    private VoyageRepository voyageRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-CargoRepositoryIT.war");

        addExtraJars(war);
        addDomainModels(war);
        addDomainRepositories(war);
        addInfraBase(war);
        addInfraPersistence(war);
        addApplicationBase(war);

//        war.addClass(RestActivator.class);
        war.addClass(SampleDataGenerator.class)
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class)
                // add TxUtil
                .addClass(TxUtil.class)
                // add persistence unit descriptor
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")

                // add web xml
                .addAsWebInfResource("test-web.xml", "web.xml")

                // add Wildfly specific deployment descriptor
                .addAsWebInfResource("test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    TxUtil tx = null;

    @BeforeEach
    public void setup() {
        tx = new TxUtil(utx, entityManager);
    }

    @AfterEach
    public void teardown() {
        tx = null;
    }


    @Test
    @Order(1)
    public void testFindAll() {
        List<Cargo> all = cargoRepository.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all).hasSize(4);
    }

    @Test
    @Order(2)
    public void testNextTrackingId() {
        TrackingId trackingId = cargoRepository.nextTrackingId();
        assertThat(trackingId).isNotNull();

        TrackingId trackingId2 = cargoRepository.nextTrackingId();
        assertThat(trackingId2).isNotNull();
        assertThat(trackingId).isNotEqualTo(trackingId2);
    }

    @Test
    @Order(3)
    public void testFindByCargoIdUnknownId() {
        assertThat(cargoRepository.find(new TrackingId("UNKNOWN"))).isNull();
    }

    @Test
    @Order(4)
    public void testFindByCargoId() {
        final TrackingId trackingId = new TrackingId("ABC123");

        tx.runInTx(() -> {
            final Cargo cargo = cargoRepository.find(trackingId);
            assertThat(cargo.getOrigin()).isEqualTo(SampleLocations.HONGKONG);
            assertThat(cargo.getRouteSpecification().origin()).isEqualTo(SampleLocations.HONGKONG);
            assertThat(cargo.getRouteSpecification().destination()).isEqualTo(SampleLocations.HELSINKI);

            assertThat(cargo.getDelivery()).isNotNull();

            final List<HandlingEvent> events = handlingEventRepository
                    .lookupHandlingHistoryOfCargo(trackingId)
                    .getDistinctEventsByCompletionTime();
            assertThat(events).hasSize(3);

            HandlingEvent firstEvent = events.get(0);
            assertHandlingEvent(
                    cargo,
                    firstEvent,
                    HandlingEvent.Type.RECEIVE,
                    SampleLocations.HONGKONG,
                    100,
                    160,
                    Voyage.NONE);

            HandlingEvent secondEvent = events.get(1);

            Voyage hongkongTonNewYork = new Voyage.Builder(new VoyageNumber("0100S"), SampleLocations.HONGKONG)
                    .addMovement(SampleLocations.HANGZOU, LocalDateTime.now(), LocalDateTime.now())
                    .addMovement(SampleLocations.TOKYO, LocalDateTime.now(), LocalDateTime.now())
                    .addMovement(SampleLocations.MELBOURNE, LocalDateTime.now(), LocalDateTime.now())
                    .addMovement(SampleLocations.NEWYORK, LocalDateTime.now(), LocalDateTime.now())
                    .build();

            assertHandlingEvent(cargo, secondEvent, LOAD, SampleLocations.HONGKONG, 150, 110, hongkongTonNewYork);

            List<Leg> legs = cargo.getItinerary().legs();
            assertThat(legs).hasSize(3);

            Leg firstLeg = legs.get(0);
            assertLeg(firstLeg, "0100S", SampleLocations.HONGKONG, SampleLocations.NEWYORK);

            Leg secondLeg = legs.get(1);
            assertLeg(secondLeg, "0200T", SampleLocations.NEWYORK, SampleLocations.DALLAS);

            Leg thirdLeg = legs.get(2);
            assertLeg(thirdLeg, "0300A", SampleLocations.DALLAS, SampleLocations.HELSINKI);
        });

    }

    private void assertHandlingEvent(
            Cargo cargo,
            HandlingEvent event,
            HandlingEvent.Type expectedEventType,
            Location expectedLocation,
            int completionTimeMs,
            int registrationTimeMs,
            Voyage voyage) {
        assertThat(event.getType()).isEqualTo(expectedEventType);
        assertThat(event.getLocation()).isEqualTo(expectedLocation);
        // TODO ignore the registration and completion time assertions.
        //        LocalDateTime expectedCompletionTime =
        // SampleDataGenerator.offset(completionTimeMs);
        //        assertThat(event.getCompletionTime()).isEqualTo(expectedCompletionTime);
        //
        //        LocalDateTime expectedRegistrationTime =
        // SampleDataGenerator.offset(registrationTimeMs);
        //        assertThat(event.getRegistrationTime()).isEqualTo(expectedRegistrationTime);

        assertThat(event.getVoyage()).isEqualTo(voyage);
        assertThat(event.getCargo()).isEqualTo(cargo);
    }

    private void assertLeg(Leg firstLeg, String vn, Location expectedFrom, Location expectedTo) {
        assertThat(firstLeg.getVoyage().getVoyageNumber()).isEqualTo(new VoyageNumber(vn));
        assertThat(firstLeg.getLoadLocation()).isEqualTo(expectedFrom);
        assertThat(firstLeg.getUnloadLocation()).isEqualTo(expectedTo);
    }

    @Test
    @Order(5)
    public void testSave() throws Exception {
        TrackingId trackingId = new TrackingId("AAA");
        VoyageNumber voyageNumber = new VoyageNumber("0300A");
        Location dallas = SampleLocations.DALLAS;
        Location helsinki = SampleLocations.HELSINKI;

        tx.runInTx(() -> {
            Location origin = locationRepository.find(dallas.getUnLocode());
            Location destination = locationRepository.find(helsinki.getUnLocode());

            Cargo cargo = new Cargo(trackingId, new RouteSpecification(origin, destination, LocalDate.now()));
            cargoRepository.store(cargo);
            cargo.assignToRoute(
                    new Itinerary(
                            List.of(
                                    new Leg(
                                            voyageRepository.find(voyageNumber),
                                            locationRepository.find(dallas.getUnLocode()),
                                            locationRepository.find(helsinki.getUnLocode()),
                                            LocalDateTime.now(),
                                            LocalDateTime.now()
                                    )
                            )
                    )
            );
        });

        // verify in new tx
        tx.runInTx(() -> {
            var result = this.entityManager
                    .createQuery("select c from Cargo c where c.trackingId=:trackingId", Cargo.class)
                    .setParameter("trackingId", trackingId)
                    .getSingleResult();

            assertThat(result.getTrackingId()).isEqualTo(trackingId);
            assertThat(result.getRouteSpecification().origin()).isEqualTo(dallas);
            assertThat(result.getRouteSpecification().destination()).isEqualTo(helsinki);
            assertThat(result.getItinerary().legs()).hasSize(1);
        });
    }

    @Test
    @Order(6)
    public void testSpecifyNewRoute() throws Exception {
        LOGGER.log(Level.INFO, "run test :: testSpecifyNewRoute");
        var trackingId = new TrackingId("AAA");

        tx.runInTx(() -> {
            Cargo cargo = cargoRepository.find(trackingId);
            LOGGER.log(Level.INFO, "retrieved cargo: {0}", cargo.toString(true));
            assertThat(cargo).isNotNull();

            Location origin = locationRepository.find(SampleLocations.NEWYORK.getUnLocode());
            Location destination = locationRepository.find(SampleLocations.HELSINKI.getUnLocode());

            cargo.specifyNewRoute(new RouteSpecification(origin, destination, LocalDate.now()));

            cargoRepository.store(cargo);
            LOGGER.log(Level.INFO, "saved cargo: {0}", cargo.toString(true));
        });

        // verify in the new tx
        LOGGER.log(Level.INFO, "run test :: verify cargo state");
        tx.runInTx(() -> {
            var result =
                    this.entityManager
                            .createQuery(
                                    "select c from Cargo c where c.trackingId=:trackingId", Cargo.class)
                            .setParameter("trackingId", trackingId)
                            .getSingleResult();
            LOGGER.log(
                    Level.INFO,
                    "query cargo by tracking id: {0}, \n result: {1}",
                    new Object[]{trackingId, result.toString(true)});
            assertThat(result.getTrackingId()).isEqualTo(trackingId);
            assertThat(result.getRouteSpecification().origin()).isEqualTo(SampleLocations.NEWYORK);
            assertThat(result.getRouteSpecification().destination()).isEqualTo(SampleLocations.HELSINKI);
            assertThat(result.getItinerary().legs()).hasSize(1);
        });
    }

    @Test
    @Order(7)
    public void testReplaceItinerary() throws Exception {
        var trackingId = new TrackingId("AAA");

        tx.runInTx(() -> {
            Cargo cargo = cargoRepository.find(trackingId);
            assertLegCount(trackingId, 1);

            Itinerary newItinerary = new Itinerary(
                    Arrays.asList(
                            new Leg(
                                    SampleVoyages.NEW_YORK_TO_DALLAS,
                                    locationRepository.find(SampleLocations.NEWYORK.getUnLocode()),
                                    locationRepository.find(SampleLocations.DALLAS.getUnLocode()),
                                    LocalDateTime.now(),
                                    LocalDateTime.now()
                            ),
                            new Leg(
                                    SampleVoyages.DALLAS_TO_HELSINKI,
                                    locationRepository.find(SampleLocations.DALLAS.getUnLocode()),
                                    locationRepository.find(SampleLocations.HELSINKI.getUnLocode()),
                                    LocalDateTime.now(),
                                    LocalDateTime.now()
                            )
                    )
            );

            cargo.assignToRoute(newItinerary);

            cargoRepository.store(cargo);
        });


        // verify in the new tx
        tx.runInTx(() -> {
            assertLegCount(trackingId, 2);
        });

    }

    private void assertLegCount(TrackingId trackingId, int expected) {
        LOGGER.log(Level.INFO, "count legs by cargo tracking id: {0}", trackingId);

        //  Payara/EclipseLinks issue:
        //   the following JPQL returns 0 instead of the real count.
        // var count = this.entityManager.createQuery("select count(e) from Cargo c join
        // c.itinerary.legs e where c.trackingId = :id")
        //        .setParameter("id", trackingId).getSingleResult();
        // LOGGER.log(Level.INFO, "leg count: {0}", (Long) count);
        // assertThat(((Long) count).intValue()).isEqualTo(expected);

        var count = cargoRepository.find(trackingId).getItinerary().legs().size();
        LOGGER.log(Level.INFO, "leg count: {0}", count);
        assertThat(count).isEqualTo(expected);
    }
}
