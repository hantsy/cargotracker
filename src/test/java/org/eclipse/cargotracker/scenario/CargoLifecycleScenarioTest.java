package org.eclipse.cargotracker.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.cargotracker.Deployments.*;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.BookingService;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.application.internal.DefaultBookingService;
import org.eclipse.cargotracker.application.internal.DefaultCargoInspectionService;
import org.eclipse.cargotracker.application.internal.DefaultHandlingEventService;
import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventFactory;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.domain.service.RoutingService;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("arqtest")
public class CargoLifecycleScenarioTest {

    private static final Logger LOGGER =
            Logger.getLogger(CargoLifecycleScenarioTest.class.getName());
    /*
     * Test setup: A cargo should be shipped from Hongkong to
     * SampleLocations.STOCKHOLM, and it should arrive in no more than two weeks.
     */
    private static Location origin = SampleLocations.HONGKONG;
    private static Location destination = SampleLocations.STOCKHOLM;
    private static LocalDate arrivalDeadline =
            LocalDate.now().minusYears(1).plusMonths(3).plusDays(18);
    private static TrackingId trackingId;
    @Inject UserTransaction utx;

    /**
     * Repository implementations are part of the infrastructure layer, which in this test is
     * stubbed out by in-memory replacements.
     */
    HandlingEventRepository handlingEventRepository;

    @Inject CargoRepository cargoRepository;
    @Inject LocationRepository locationRepository;
    VoyageRepository voyageRepository;

    /**
     * This interface is part of the application layer, and defines a number of events that occur
     * during aplication execution. It is used for message-driving and is implemented using JMS.
     *
     * <p>In this test it is stubbed with synchronous calls.
     */
    ApplicationEvents applicationEvents;

    /**
     * These three components all belong to the application layer, and map against use cases of the
     * application. The "real" implementations are used in this lifecycle test, but wired with
     * stubbed infrastructure.
     */
    @Inject BookingService bookingService;

    @Inject HandlingEventService handlingEventService;
    CargoInspectionService cargoInspectionService;

    /**
     * This factory is part of the handling aggregate and belongs to the domain layer. Similar to
     * the application layer components, the "real" implementation is used here too, wired with
     * stubbed infrastructure.
     */
    HandlingEventFactory handlingEventFactory;

    /**
     * This is a domain service interface, whose implementation is part of the infrastructure layer
     * (remote call to external system).
     *
     * <p>It is stubbed in this test.
     */
    RoutingService routingService;

    @PersistenceContext private EntityManager entityManager;

    @Deployment
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class);

        addExtraJars(war);
        addDomainModels(war);
        addDomainRepositories(war);
        addInfraBase(war);
        addInfraPersistence(war);
        addApplicationBase(war);

        addDomainService(war);
        // add fake routing service to isolate the external APIs.
        war.addClass(RoutingServiceStub.class);

        // add JMS package
        // addInfraMessaging(war);

        // Now pickup application service to setup in test.
        war.addClass(ApplicationEvents.class)
                // use a ApplicationEvents stub bean instead to isolate the jms facilities
                .addClass(SynchronousApplicationEventsStub.class)
                .addClass(HandlingEventRegistrationAttempt.class)

                // add other application service
                .addClass(BookingService.class)
                .addClass(HandlingEventService.class)
                .addClass(CargoInspectionService.class)
                // Application layer components
                .addClass(DefaultBookingService.class)
                .addClass(DefaultHandlingEventService.class)
                .addClass(DefaultCargoInspectionService.class);

        // Sample data.
        war.addClass(CargoLifecycleScenarioTestDataGenerator.class)
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class);

        // add persistence unit descriptor
        war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
        war.addAsResource("test-jboss-logging.properties", "jboss-logging.properties");
        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        // add web xml
        war.addAsWebInfResource("test-web.xml", "web.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }


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

    public void doInTx(Runnable runnable) throws Exception {
        startTransaction();
        try {
            runnable.run();
            commitTransaction();
        } catch (Exception e) {
            utx.rollback();
            throw new RuntimeException(e);
        }
    }

    // Split the original testCargoFromHongkongToStockholm into steps.

    /*
     * Use case 1: booking
     *
     * A new cargo is booked, and the unique tracking id is assigned to the cargo.
     */
    @Test
    @Order(1)
    public void testBookNewCargo() throws Exception {
        doInTx(
                () -> trackingId =
                        bookingService.bookNewCargo(
                                origin.getUnLocode(),
                                destination.getUnLocode(),
                                arrivalDeadline));

        LOGGER.log(Level.INFO, "book a new cargo::tracking id: {0}", trackingId);
        /*
         * The tracking id can be used to lookup the cargo in the repository.
         *
         * Important: The cargo, and thus the domain model, is responsible for
         * determining the status of the cargo, whether it is on the right track or not
         * and so on. This is core domain logic.
         *
         * Tracking the cargo basically amounts to presenting information extracted from
         * the cargo aggregate in a suitable way.
         */

        Cargo cargo = findCargo();
        assertThat(cargo).isNotNull();
        assertThat(cargo.getDelivery().transportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
        assertThat(cargo.getDelivery().routingStatus()).isEqualTo(RoutingStatus.NOT_ROUTED);
        assertThat(cargo.getDelivery().misdirected()).isFalse();
        assertThat(cargo.getDelivery().estimatedTimeOfArrival()).isNull();
        assertThat(cargo.getDelivery().nextExpectedActivity()).isEqualTo(HandlingActivity.EMPTY);
    }

    /*
     * Use case 2: routing
     *
     * A number of possible routes for this cargo is requested and may be presented
     * to the customer in some way for him/her to choose from. Selection could be
     * affected by things like price and time of delivery, but this test simply uses
     * an arbitrary selection to mimic that process.
     *
     * The cargo is then assigned to the selected route, described by an itinerary.
     */
    @Test
    @Order(2)
    public void testRouting() throws Exception {
        LOGGER.log(Level.INFO, "assign to route::tracking id: {0}", trackingId);
        doInTx(() -> {
            Cargo cargo = findCargo();
            List<Itinerary> itineraries = bookingService.requestPossibleRoutesForCargo(trackingId);
            Itinerary itinerary = selectPreferredItinerary(itineraries);
            cargo.assignToRoute(itinerary);
        });

        var result = findCargo();
        LOGGER.log(
                Level.INFO,
                "after route is assigned, the itinerary is : {0}",
                result.getItinerary());
        LOGGER.log(
                Level.INFO,
                "after route is assigned, the delivery status: {0}",
                result.getDelivery());

        assertThat(result.getDelivery().transportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
        assertThat(result.getDelivery().routingStatus()).isEqualTo(RoutingStatus.ROUTED);
        assertThat(result.getDelivery().estimatedTimeOfArrival()).isNotNull();
        assertThat(result.getDelivery().nextExpectedActivity())
                .isEqualTo(
                        new HandlingActivity(HandlingEvent.Type.RECEIVE, SampleLocations.HONGKONG));
    }

    /*
     * Use case 3: handling
     *
     * A handling event registration attempt will be formed from parsing the data
     * coming in as a handling report either via the web service interface or as an
     * uploaded CSV file.
     *
     * The handling event factory tries to create a HandlingEvent from the attempt,
     * and if the factory decides that this is a plausible handling event, it is
     * stored. If the attempt is invalid, for example if no cargo exists for the
     * specfied tracking id, the attempt is rejected.
     *
     * Handling begins: cargo is received in Hongkong.
     */
    @Test
    @Order(3)
    public void testReceiveInHongKong() throws Exception {
        LOGGER.log(Level.INFO, "receive in HONGKONG::tracking id: {0}", trackingId);

        doInTx(() -> {
            try {
                handlingEventService.registerHandlingEvent(
                        LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(1),
                        trackingId,
                        null,
                        SampleLocations.HONGKONG.getUnLocode(),
                        HandlingEvent.Type.RECEIVE);
            } catch (CannotCreateHandlingEventException e) {
                throw new RuntimeException(e);
            }
        });

        Cargo cargo = findCargo();
        assertThat(cargo.getDelivery().transportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.HONGKONG);
        assertThat(cargo.getDelivery().nextExpectedActivity())
                .isEqualTo(
                        new HandlingActivity(
                                HandlingEvent.Type.LOAD,
                                SampleLocations.HONGKONG,
                                SampleVoyages.v100));
    }

    /// Next event: Load onto voyage SampleVoyages.v100 in Hongkong
    @Test
    @Order(4)
    public void testLoadInHongKong() throws Exception {
        LOGGER.log(Level.INFO, "load in HONGKONG::tracking id: {0}", trackingId);
        handlingEventService.registerHandlingEvent(
                LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(3),
                trackingId,
                SampleVoyages.v100.getVoyageNumber(),
                SampleLocations.HONGKONG.getUnLocode(),
                HandlingEvent.Type.LOAD);

        commitTransaction();

        // verify in new tx
        startTransaction();
        Cargo cargo = findCargo();
        // Check current state - should be ok
        assertThat(cargo.getDelivery().currentVoyage()).isEqualTo(SampleVoyages.v100);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.HONGKONG);
        assertThat(cargo.getDelivery().transportStatus())
                .isEqualTo(TransportStatus.ONBOARD_CARRIER);
        assertThat(cargo.getDelivery().misdirected()).isFalse();
        assertThat(cargo.getDelivery().nextExpectedActivity())
                .isEqualTo(
                        new HandlingActivity(
                                HandlingEvent.Type.UNLOAD,
                                SampleLocations.NEWYORK,
                                SampleVoyages.v100));
    }

    /*
     * Here's an attempt to register a handling event that's not valid because there
     * is no voyage with the specified voyage number, and there's no location with
     * the specified UN Locode either.
     *
     * This attempt will be rejected and will not affect the cargo delivery in any
     * way.
     */
    @Test
    @Order(5)
    public void testCannotCreateHandlingEventException() throws Exception {
        LOGGER.log(
                Level.INFO,
                "test CannotCreateHandlingEventException::tracking id: {0}",
                trackingId);
        VoyageNumber noSuchVoyageNumber = new VoyageNumber("XX000");
        UnLocode noSuchUnLocode = new UnLocode("ZZZZZ");

        assertThatThrownBy(
                        () ->
                                handlingEventService.registerHandlingEvent(
                                        LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(5),
                                        trackingId,
                                        noSuchVoyageNumber,
                                        noSuchUnLocode,
                                        HandlingEvent.Type.LOAD),
                        "Should not be able to register a handling event with invalid location and"
                                + " voyage")
                .isInstanceOf(CannotCreateHandlingEventException.class);
    }

    // Cargo is now (incorrectly) unloaded in Tokyo
    @Test
    @Order(6)
    public void testUnloadedIncorrectlyInTokyo() throws Exception {
        LOGGER.log(Level.INFO, "unload in Tokyo incorrectly, tracking id: {0}", trackingId);

        doInTx(() ->
        {
            try {
                handlingEventService.registerHandlingEvent(
                        LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(5),
                        trackingId,
                        SampleVoyages.v100.getVoyageNumber(),
                        SampleLocations.TOKYO.getUnLocode(),
                        HandlingEvent.Type.UNLOAD);
            } catch (CannotCreateHandlingEventException e) {
                throw new RuntimeException(e);
            }
        });

        // verify in new tx
        Cargo cargo = findCargo();
        // Check current state - cargo is misdirected!
        assertThat(cargo.getDelivery().currentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.TOKYO);
        assertThat(cargo.getDelivery().transportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().misdirected()).isTrue();
        assertThat(cargo.getDelivery().nextExpectedActivity()).isEqualTo(HandlingActivity.EMPTY);
    }

    // -- Cargo needs to be rerouted --
    // TODO [TDD] cleaner reroute from "earliest location from where the new route
    // originates"
    // Specify a new route, this time from Tokyo (where it was incorrectly unloaded)
    // to SampleLocations.STOCKHOLM
    @Test
    @Order(7)
    public void testNewRoute() throws Exception {
        LOGGER.log(Level.INFO, "specify new route spec, tracking id: {0}", trackingId);
        doInTx(() -> {
            Cargo cargo = findCargo();
            RouteSpecification fromTokyo =
                    new RouteSpecification(
                            locationRepository.find(SampleLocations.TOKYO.getUnLocode()),
                            locationRepository.find(SampleLocations.STOCKHOLM.getUnLocode()),
                            arrivalDeadline);
            cargo.specifyNewRoute(fromTokyo);

            cargoRepository.store(cargo);
        });


        var result = findCargo();

        LOGGER.log(
                Level.INFO,
                "after assigned to new route spec, route spec is : {0}",
                result.getRouteSpecification());
        LOGGER.log(
                Level.INFO,
                "after assigned to new route spec, the itinerary is : {0}",
                result.getItinerary());
        LOGGER.log(
                Level.INFO,
                "after assigned to new route spec, the delivery status: {0}",
                result.getDelivery());

        // The old itinerary does not satisfy the new specification
        assertThat(result.getDelivery().routingStatus()).isEqualTo(RoutingStatus.MISROUTED);
        assertThat(result.getDelivery().nextExpectedActivity()).isEqualTo(HandlingActivity.EMPTY);
    }

    // Repeat procedure of selecting one out of a number of possible routes
    // satisfying the route spec
    @Test
    @Order(8)
    public void testNewItinerary() throws Exception {
        LOGGER.log(Level.INFO, "assign to new itinerary, tracking id: {0}", trackingId);

        doInTx(() -> {
            Cargo cargo = findCargo();

            List<Itinerary> newItineraries =
                    bookingService.requestPossibleRoutesForCargo(cargo.getTrackingId());
            Itinerary newItinerary = selectPreferredItinerary(newItineraries);
            cargo.assignToRoute(newItinerary);

            cargoRepository.store(cargo);
        });

        var result = findCargo();

        LOGGER.log(
                Level.INFO,
                "after assigned to new itinerary, route spec is : {0}",
                result.getRouteSpecification());
        LOGGER.log(
                Level.INFO,
                "after assigned to new itinerary, the itinerary is : {0}",
                result.getItinerary());
        LOGGER.log(
                Level.INFO,
                "after assigned to new itinerary, the delivery status: {0}",
                result.getDelivery());

        // New itinerary should satisfy new route
        assertThat(result.getDelivery().routingStatus()).isEqualTo(RoutingStatus.ROUTED);

        // TODO we can't handle the face that after a reroute, the cargo isn't misdirected anymore
        // assertThat(cargo.isMisdirected()).isFalse();
        // assertThat(cargo.nextExpectedActivity())
        // .isEqualTo(new HandlingActivity(HandlingEvent.Type.LOAD, SampleLocations.TOKYO,
        // SampleVoyages.v300));
        //
        // When the Cargo is assigned to a new itinerary, but the `misredirected` status is still
        // true.
        // it will set NO_ACTIVITY as next expected activity, but logically it should work as new
        // route
        // assigned the Cargo
        // and has a next expected activity.
        // assertThat(cargo.getDelivery().getNextExpectedActivity())
        //        .isEqualTo(new HandlingActivity(HandlingEvent.Type.LOAD, SampleLocations.TOKYO,
        // SampleVoyages.v300));

    }

    // -- Cargo has been rerouted, shipping continues --
    // Load in Tokyo
    @Test
    @Order(9)
    public void testLoadInTokyo() throws Exception {
        LOGGER.log(Level.INFO, "load in Tokyo now, tracking id: {0}", trackingId);

        doInTx(() -> {
            try {
                handlingEventService.registerHandlingEvent(
                        LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(8),
                        trackingId,
                        SampleVoyages.v300.getVoyageNumber(),
                        SampleLocations.TOKYO.getUnLocode(),
                        HandlingEvent.Type.LOAD);
            } catch (CannotCreateHandlingEventException e) {
                throw new RuntimeException(e);
            }
        });

        Cargo cargo = findCargo();
        // Check current state - should be ok
        assertThat(cargo.getDelivery().currentVoyage()).isEqualTo(SampleVoyages.v300);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.TOKYO);
        assertThat(cargo.getDelivery().transportStatus())
                .isEqualTo(TransportStatus.ONBOARD_CARRIER);
        assertThat(cargo.getDelivery().misdirected()).isFalse();
        assertThat(cargo.getDelivery().nextExpectedActivity())
                .isEqualTo(
                        new HandlingActivity(
                                HandlingEvent.Type.UNLOAD,
                                SampleLocations.HAMBURG,
                                SampleVoyages.v300));
    }

    // Unload in Hamburg
    @Test
    @Order(10)
    public void testUnloadInHamburg() throws Exception {
        LOGGER.log(Level.INFO, "unload in Hamburg, tracking id: {0}", trackingId);
        doInTx(() -> {
            try {
        handlingEventService.registerHandlingEvent(
                LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(12),
                trackingId,
                SampleVoyages.v300.getVoyageNumber(),
                SampleLocations.HAMBURG.getUnLocode(),
                HandlingEvent.Type.UNLOAD);

            } catch (CannotCreateHandlingEventException e) {
                throw new RuntimeException(e);
            }
        });


        Cargo cargo = findCargo();
        // Check current state - should be ok
        assertThat(cargo.getDelivery().currentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.HAMBURG);
        assertThat(cargo.getDelivery().transportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().misdirected()).isFalse();
        assertThat(cargo.getDelivery().nextExpectedActivity())
                .isEqualTo(
                        new HandlingActivity(
                                HandlingEvent.Type.LOAD,
                                SampleLocations.HAMBURG,
                                SampleVoyages.v400));
    }

    // Load in Hamburg
    @Test
    @Order(11)
    public void testLoadInHamburg() throws Exception {
        LOGGER.log(Level.INFO, "load in Hamburg,  tracking id: {0}", trackingId);
        doInTx(() -> {
            try {
        handlingEventService.registerHandlingEvent(
                LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(14),
                trackingId,
                SampleVoyages.v400.getVoyageNumber(),
                SampleLocations.HAMBURG.getUnLocode(),
                HandlingEvent.Type.LOAD);

    } catch (CannotCreateHandlingEventException e) {
        throw new RuntimeException(e);
    }
});
        Cargo cargo = findCargo();
        // Check current state - should be ok
        assertThat(cargo.getDelivery().currentVoyage()).isEqualTo(SampleVoyages.v400);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.HAMBURG);
        assertThat(cargo.getDelivery().transportStatus())
                .isEqualTo(TransportStatus.ONBOARD_CARRIER);
        assertThat(cargo.getDelivery().misdirected()).isFalse();
        assertThat(cargo.getDelivery().nextExpectedActivity())
                .isEqualTo(
                        new HandlingActivity(
                                HandlingEvent.Type.UNLOAD,
                                SampleLocations.STOCKHOLM,
                                SampleVoyages.v400));
    }

    // Unload in SampleLocations.STOCKHOLM
    @Test
    @Order(12)
    public void testUnload_in_STOCKHOLM() throws Exception {
        LOGGER.log(Level.INFO, "unload in STOCKHOLM, tracking id: {0}", trackingId);
        doInTx(() -> {
            try {
        handlingEventService.registerHandlingEvent(
                LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(15),
                trackingId,
                SampleVoyages.v400.getVoyageNumber(),
                SampleLocations.STOCKHOLM.getUnLocode(),
                HandlingEvent.Type.UNLOAD);

            } catch (CannotCreateHandlingEventException e) {
                throw new RuntimeException(e);
            }
        });


        Cargo cargo = findCargo();
        // Check current state - should be ok
        assertThat(cargo.getDelivery().currentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.STOCKHOLM);
        assertThat(cargo.getDelivery().transportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().misdirected()).isFalse();
        assertThat(cargo.getDelivery().nextExpectedActivity())
                .isEqualTo(
                        new HandlingActivity(HandlingEvent.Type.CLAIM, SampleLocations.STOCKHOLM));
    }

    // Finally, cargo is claimed in SampleLocations.STOCKHOLM. This ends the cargo
    // lifecycle from our perspective.
    @Test
    @Order(13)
    public void testClaim_in_STOCKHOLM() throws Exception {
        LOGGER.log(
                Level.INFO,
                "claim in STOCKHOLM, the cargo is arrived, tracking id: {0}",
                trackingId);
        doInTx(() -> {
            try {
        handlingEventService.registerHandlingEvent(
                LocalDateTime.now().minusYears(1).plusMonths(3).plusDays(16),
                trackingId,
                null,
                SampleLocations.STOCKHOLM.getUnLocode(),
                HandlingEvent.Type.CLAIM);
    } catch (CannotCreateHandlingEventException e) {
        throw new RuntimeException(e);
    }
});

        Cargo cargo = findCargo();
        // Check current state - should be ok
        assertThat(cargo.getDelivery().currentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().lastKnownLocation()).isEqualTo(SampleLocations.STOCKHOLM);
        assertThat(cargo.getDelivery().transportStatus()).isEqualTo(TransportStatus.CLAIMED);
        assertThat(cargo.getDelivery().misdirected()).isFalse();
        assertThat(cargo.getDelivery().nextExpectedActivity()).isEqualTo(HandlingActivity.EMPTY);
    }

    /*
     * Utility stubs below.
     */
    private Itinerary selectPreferredItinerary(List<Itinerary> itineraries) {
        return itineraries.getFirst();
    }

    private Cargo findCargo() {
        var cargo = cargoRepository.find(trackingId);
        LOGGER.log(Level.INFO, "cargo itinerary: {0}", cargo.getItinerary());
        LOGGER.log(Level.INFO, "cargo delivery: {0}", cargo.getDelivery());

        return cargo;
    }

    //    @Before
    //    public void setUp() throws Exception {
}
