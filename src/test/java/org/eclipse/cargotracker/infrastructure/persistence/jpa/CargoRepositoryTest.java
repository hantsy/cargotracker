package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import org.eclipse.cargotracker.IntegrationTests;
import org.eclipse.cargotracker.application.util.RestConfiguration;
import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.cargo.*;
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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;
import static org.eclipse.cargotracker.domain.model.handling.HandlingEvent.Type.LOAD;

@RunWith(Arquillian.class)
@Category(IntegrationTests.class)
public class CargoRepositoryTest {
    private static final Logger LOGGER = Logger.getLogger(CargoRepositoryTest.class.getName());
    @Inject UserTransaction utx;
    @Inject private LocationRepository locationRepository;

    @Inject private HandlingEventRepository handlingEventRepository;

    @Inject private CargoRepository cargoRepository;

    @Inject private VoyageRepository voyageRepository;

    @PersistenceContext private EntityManager entityManager;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-CargoRepositoryTest.war");

        addExtraJars(war);
        addDomainModels(war);
        addDomainRepositories(war);
        addInfraBase(war);
        addInfraPersistence(war);
        addApplicationBase(war);

        war.addClass(RestConfiguration.class);
        war.addClass(SampleDataGenerator.class)
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class)
                // add persistence unit descriptor
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")

                // add web xml
                .addAsWebInfResource("test-web.xml", "web.xml")

                // add Wildfly specific deployment descriptor
                .addAsWebInfResource(
                        "test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    @Before
    public void setup() {}

    public void startTransaction() throws Exception {
        utx.begin();
        entityManager.joinTransaction();
    }

    public void commitTransaction() throws Exception {
        LOGGER.log(Level.INFO, "UserTransaction status is: {0}", utx.getStatus());
        if (utx.getStatus() == Status.STATUS_ACTIVE) {
            utx.commit();
        }
    }

    @Test
    @InSequence(1)
    public void testFindAll() {
        List<Cargo> all = cargoRepository.findAll();
        assertThat(all).isNotNull();
        assertThat(all).hasSize(4);
    }

    @Test
    @InSequence(2)
    public void testNextTrackingId() {
        TrackingId trackingId = cargoRepository.nextTrackingId();
        assertThat(trackingId).isNotNull();

        TrackingId trackingId2 = cargoRepository.nextTrackingId();
        assertThat(trackingId2).isNotNull();
        assertThat(trackingId.equals(trackingId2)).isFalse();
    }

    @Test
    @InSequence(4)
    public void testFindByCargoIdUnknownId() {
        assertThat(cargoRepository.find(new TrackingId("UNKNOWN"))).isNull();
    }

    @Test
    @InSequence(5)
    public void testFindByCargoId() throws Exception {
        startTransaction();
        final TrackingId trackingId = new TrackingId("ABC123");
        final Cargo cargo = cargoRepository.find(trackingId);
        assertThat(cargo.getOrigin()).isEqualTo(SampleLocations.HONGKONG);
        assertThat(cargo.getRouteSpecification().getOrigin()).isEqualTo(SampleLocations.HONGKONG);
        assertThat(cargo.getRouteSpecification().getDestination())
                .isEqualTo(SampleLocations.HELSINKI);

        assertThat(cargo.getDelivery()).isNotNull();

        final List<HandlingEvent> events =
                handlingEventRepository
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

        Voyage hongkongTonNewYork =
                new Voyage.Builder(new VoyageNumber("0100S"), SampleLocations.HONGKONG)
                        .addMovement(
                                SampleLocations.HANGZOU, LocalDateTime.now(), LocalDateTime.now())
                        .addMovement(
                                SampleLocations.TOKYO, LocalDateTime.now(), LocalDateTime.now())
                        .addMovement(
                                SampleLocations.MELBOURNE, LocalDateTime.now(), LocalDateTime.now())
                        .addMovement(
                                SampleLocations.NEWYORK, LocalDateTime.now(), LocalDateTime.now())
                        .build();

        assertHandlingEvent(
                cargo, secondEvent, LOAD, SampleLocations.HONGKONG, 150, 110, hongkongTonNewYork);

        List<Leg> legs = cargo.getItinerary().getLegs();
        assertThat(legs).hasSize(3);

        Leg firstLeg = legs.get(0);
        assertLeg(firstLeg, "0100S", SampleLocations.HONGKONG, SampleLocations.NEWYORK);

        Leg secondLeg = legs.get(1);
        assertLeg(secondLeg, "0200T", SampleLocations.NEWYORK, SampleLocations.DALLAS);

        Leg thirdLeg = legs.get(2);
        assertLeg(thirdLeg, "0300A", SampleLocations.DALLAS, SampleLocations.HELSINKI);
        commitTransaction();
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
    @InSequence(6)
    public void testSave() throws Exception {
        startTransaction();
        TrackingId trackingId = new TrackingId("AAA");
        Location origin = locationRepository.find(SampleLocations.DALLAS.getUnLocode());
        Location destination = locationRepository.find(SampleLocations.HELSINKI.getUnLocode());

        Cargo cargo =
                new Cargo(trackingId, new RouteSpecification(origin, destination, LocalDate.now()));
        cargoRepository.store(cargo);

        cargo.assignToRoute(
                new Itinerary(
                        Arrays.asList(
                                new Leg(
                                        voyageRepository.find(new VoyageNumber("0300A")),
                                        locationRepository.find(
                                                SampleLocations.DALLAS.getUnLocode()),
                                        locationRepository.find(
                                                SampleLocations.HELSINKI.getUnLocode()),
                                        LocalDateTime.now(),
                                        LocalDateTime.now()))));

        this.entityManager.flush();
        commitTransaction();

        // verify in new tx
        startTransaction();

        var result =
                this.entityManager
                        .createQuery(
                                "select c from Cargo c where c.trackingId=:trackingId", Cargo.class)
                        .setParameter("trackingId", trackingId)
                        .getSingleResult();

        assertThat(result.getTrackingId()).isEqualTo(trackingId);
        assertThat(result.getRouteSpecification().getOrigin()).isEqualTo(origin);
        assertThat(result.getRouteSpecification().getDestination()).isEqualTo(destination);
        assertThat(result.getItinerary().getLegs()).hasSize(1);
        commitTransaction();
    }

    @Test
    @InSequence(7)
    public void testSpecifyNewRoute() throws Exception {
        LOGGER.log(Level.INFO, "run test :: testSpecifyNewRoute");
        startTransaction();
        var trackingId = new TrackingId("AAA");
        Cargo cargo = cargoRepository.find(trackingId);
        LOGGER.log(Level.INFO, "retrieved cargo: {0}", cargo.toString(true));
        assertThat(cargo).isNotNull();

        Location origin = locationRepository.find(SampleLocations.NEWYORK.getUnLocode());
        Location destination = locationRepository.find(SampleLocations.HELSINKI.getUnLocode());

        cargo.specifyNewRoute(new RouteSpecification(origin, destination, LocalDate.now()));

        cargoRepository.store(cargo);
        LOGGER.log(Level.INFO, "saved cargo: {0}", cargo.toString(true));
        commitTransaction();

        // verify in the new tx
        LOGGER.log(Level.INFO, "run test :: verify cargo state");
        startTransaction();
        var result =
                this.entityManager
                        .createQuery(
                                "select c from Cargo c where c.trackingId=:trackingId", Cargo.class)
                        .setParameter("trackingId", trackingId)
                        .getSingleResult();
        LOGGER.log(
                Level.INFO,
                "query cargo by tracking id: {0}, \n result: {1}",
                new Object[] {trackingId, result.toString(true)});
        assertThat(result.getTrackingId()).isEqualTo(trackingId);
        assertThat(result.getRouteSpecification().getOrigin()).isEqualTo(origin);
        assertThat(result.getRouteSpecification().getDestination()).isEqualTo(destination);
        assertThat(result.getItinerary().getLegs()).hasSize(1);

        commitTransaction();
    }

    @Test
    @InSequence(8)
    public void testReplaceItinerary() throws Exception {
        startTransaction();
        var trackingId = new TrackingId("AAA");
        Cargo cargo = cargoRepository.find(trackingId);
        Long cargoId = getLongId(cargo);
        assertLegCount(trackingId, 1);
        // assertThat(jdbcTemplate.queryForObject("select count(*) from Leg where cargo_id = ?", new
        // Object[]{cargoId}, Integer.class).intValue()).isEqualTo(3);

        Itinerary newItinerary =
                new Itinerary(
                        Arrays.asList(
                                new Leg(
                                        SampleVoyages.NEW_YORK_TO_DALLAS,
                                        locationRepository.find(
                                                SampleLocations.NEWYORK.getUnLocode()),
                                        locationRepository.find(
                                                SampleLocations.DALLAS.getUnLocode()),
                                        LocalDateTime.now(),
                                        LocalDateTime.now()),
                                new Leg(
                                        SampleVoyages.DALLAS_TO_HELSINKI,
                                        locationRepository.find(
                                                SampleLocations.DALLAS.getUnLocode()),
                                        locationRepository.find(
                                                SampleLocations.HELSINKI.getUnLocode()),
                                        LocalDateTime.now(),
                                        LocalDateTime.now())));

        cargo.assignToRoute(newItinerary);

        cargoRepository.store(cargo);
        commitTransaction();

        // verify in the new tx
        startTransaction();

        assertLegCount(trackingId, 2);
        // assertThat(jdbcTemplate.queryForObject("select count(*) from Leg where cargo_id = ?", new
        // Object[]{cargoId}, Integer.class).intValue()).isEqualTo(1);
        commitTransaction();
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

        var count = cargoRepository.find(trackingId).getItinerary().getLegs().size();
        LOGGER.log(Level.INFO, "leg count: {0}", count);
        assertThat(count).isEqualTo(expected);
    }

    // Payara/EclipseLinks issue:
    // it returns 0 , incorrect count
    //    private void assertLegCount(Long cargo_id, int expected) {
    //        LOGGER.log(Level.INFO, "count legs by cargo id: {0}", cargo_id);
    //        var count = this.entityManager.createNativeQuery("select count(*) from Leg where
    // cargo_id = ?1")
    //                .setParameter(1, cargo_id)
    //                .getSingleResult();
    //        LOGGER.log(Level.INFO, "leg count: {0}", count);
    //        assertThat((count.toString())).isEqualTo(String.valueOf(expected));
    //    }

    private Long getLongId(Object o) {
        try {
            Field id = o.getClass().getDeclaredField("id");
            id.setAccessible(true);
            return (Long) id.get(o);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
