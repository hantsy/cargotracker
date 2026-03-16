package org.eclipse.cargotracker.application;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import org.eclipse.cargotracker.TxUtil;
import org.eclipse.cargotracker.application.internal.DefaultBookingService;
import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.infrastructure.routing.ExternalRoutingService;
import org.eclipse.cargotracker.infrastructure.routing.client.GraphTraversalResourceClient;
import org.eclipse.cargotracker.interfaces.RestActivator;
import org.eclipse.pathfinder.api.GraphTraversalService;
import org.eclipse.pathfinder.api.TransitEdge;
import org.eclipse.pathfinder.api.TransitPath;
import org.eclipse.pathfinder.internal.GraphDao;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

/**
 * Application layer integration test covering a number of otherwise fairly trivial components that
 * largely do not warrant their own tests.
 *
 * <p>Ensure a Payara instance is running locally before this test is executed, with the default
 * user name and password.
 */
@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("arqtest")
public class BookingServiceTest {
    private static final Logger LOGGER = Logger.getLogger(BookingServiceTest.class.getName());
    private static TrackingId trackingId;
    private static List<Itinerary> candidates;
    private static LocalDate deadline;
    private static Itinerary assigned;

    @Inject
    UserTransaction utx;

    @Inject
    private BookingService bookingService;

    @PersistenceContext
    private EntityManager entityManager;

    private TxUtil txUtil;

    @Deployment
    public static WebArchive createDeployment() {
        // use the fixed cargo-tracker-test as part of the routing service url defined in the test-web.xml.
        WebArchive war = ShrinkWrap.create(WebArchive.class, "cargo-tracker-test.war");

        addExtraJars(war);
        addDomainModels(war);
        addDomainRepositories(war);
        addInfraBase(war);
        addInfraPersistence(war);
        addApplicationBase(war);

        // add target BookingService for test
        war.addClass(BookingService.class).addClass(DefaultBookingService.class);

        addDomainService(war);
        war.addClass(ExternalRoutingService.class)
                .addClass(GraphTraversalResourceClient.class)

                // .addClass(JsonMoxyConfigurationContextResolver.class)
                // Interface components
                .addClass(TransitPath.class)
                .addClass(TransitEdge.class)
                // Third-party system simulator
                .addClass(GraphTraversalService.class)
                .addClass(GraphDao.class)
                // Sample data.
                .addClass(BookingServiceTestDataGenerator.class)
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class)
                .addClass(RestActivator.class)

                // add TxUtil
                .addClass(TxUtil.class)

                // add persistence unit descriptor
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")

                // add web xml
                .addAsWebInfResource("test-web.xml", "web.xml")

                // add Wildfly specific deployment descriptor
                .addAsWebInfResource(
                        "test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    // Wildfly/Hibernate issue:
    // use a UserTransaction to wrap the tests and avoid the Hibernate lazy initialization exception
    // in test.
    @BeforeEach
    public void setUp() throws Exception {
        this.txUtil = new TxUtil(utx, entityManager);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.txUtil = null;
    }


    @Test
    @Order(1)
    // The `Transactional` annotation does not work in Arquillian test.
    // @Transactional
    public void testRegisterNew() {
        UnLocode fromUnlocode = new UnLocode("USCHI");
        UnLocode toUnlocode = new UnLocode("SESTO");

        deadline = LocalDate.now().plusMonths(6);

        txUtil.runInTx(() -> {
            trackingId = bookingService.bookNewCargo(fromUnlocode, toUnlocode, deadline);
        });


        txUtil.runInTx(() -> {
            Cargo cargo = entityManager
                    .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                    .setParameter("trackingId", trackingId)
                    .getSingleResult();

            assertThat(cargo.getOrigin()).isEqualTo(SampleLocations.CHICAGO);
            assertThat(cargo.getRouteSpecification().getDestination()).isEqualTo(SampleLocations.STOCKHOLM);
            assertThat(cargo.getRouteSpecification().getArrivalDeadline()).isEqualTo(deadline);
            assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
            assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(Location.UNKNOWN);
            assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
            assertThat(cargo.getDelivery().isMisdirected()).isFalse();
            assertThat(cargo.getDelivery().getEstimatedTimeOfArrival()).isEqualTo(Delivery.ETA_UNKOWN);
            assertThat(cargo.getDelivery().getNextExpectedActivity()).isEqualTo(Delivery.NO_ACTIVITY);
            assertThat(cargo.getDelivery().isUnloadedAtDestination()).isFalse();
            assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.NOT_ROUTED);
            assertThat(cargo.getItinerary()).isEqualTo(Itinerary.EMPTY_ITINERARY);
        });
    }

    @Test
    @Order(2)
    public void testRouteCandidates() {
        txUtil.runInTx(() -> {
            candidates = bookingService.requestPossibleRoutesForCargo(trackingId);
        });

        assertThat(candidates).isNotEmpty();
    }

    @Test
    @Order(3)
    public void testAssignRoute() {
        assigned = candidates.get(new Random().nextInt(candidates.size()));

        txUtil.runInTx(() -> {
            bookingService.assignCargoToRoute(assigned, trackingId);
        });

        txUtil.runInTx(() -> {
            Cargo cargo = entityManager
                    .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                    .setParameter("trackingId", trackingId)
                    .getSingleResult();

            assertThat(cargo.getItinerary()).isEqualTo(assigned);
            assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
            assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(Location.UNKNOWN);
            assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
            assertThat(cargo.getDelivery().isMisdirected()).isFalse();
            assertThat(cargo.getDelivery().getEstimatedTimeOfArrival().isBefore(deadline.atStartOfDay())).isTrue();
            assertThat(cargo.getDelivery().getNextExpectedActivity().getType()).isEqualTo(HandlingEvent.Type.RECEIVE);
            assertThat(cargo.getDelivery().getNextExpectedActivity().getLocation()).isEqualTo(SampleLocations.CHICAGO);
            assertThat(cargo.getDelivery().getNextExpectedActivity().getVoyage()).isNull();
            assertThat(cargo.getDelivery().isUnloadedAtDestination()).isFalse();
            assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.ROUTED);
        });
    }

    @Test
    @Order(4)
    public void testChangeDestination() {
        txUtil.runInTx(() -> {
            bookingService.changeDestination(trackingId, new UnLocode("FIHEL"));
        });

        txUtil.runInTx(() -> {
            Cargo cargo = entityManager
                    .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                    .setParameter("trackingId", trackingId)
                    .getSingleResult();

            assertThat(cargo.getOrigin()).isEqualTo(SampleLocations.CHICAGO);
            assertThat(cargo.getRouteSpecification().getDestination()).isEqualTo(SampleLocations.HELSINKI);
            assertThat(cargo.getRouteSpecification().getArrivalDeadline()).isEqualTo(deadline);
            assertThat(cargo.getItinerary()).isEqualTo(assigned);
            assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
            assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(Location.UNKNOWN);
            assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
            assertThat(cargo.getDelivery().isMisdirected()).isFalse();
            assertThat(cargo.getDelivery().getEstimatedTimeOfArrival()).isEqualTo(Delivery.ETA_UNKOWN);
            assertThat(cargo.getDelivery().getNextExpectedActivity()).isEqualTo(Delivery.NO_ACTIVITY);
            assertThat(cargo.getDelivery().isUnloadedAtDestination()).isFalse();
            assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.MISROUTED);
        });
    }

    @Test
    @Order(5)
    public void testChangeDeadline() {
        LocalDate newDeadline = deadline.plusMonths(1);
        txUtil.runInTx(() -> {
            bookingService.changeDeadline(trackingId, newDeadline);
        });

        txUtil.runInTx(() -> {
            Cargo cargo = entityManager
                    .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                    .setParameter("trackingId", trackingId)
                    .getSingleResult();

            assertThat(cargo.getOrigin()).isEqualTo(SampleLocations.CHICAGO);
            assertThat(cargo.getRouteSpecification().getDestination()).isEqualTo(SampleLocations.HELSINKI);
            assertThat(cargo.getRouteSpecification().getArrivalDeadline()).isEqualTo(newDeadline);
            assertThat(cargo.getItinerary()).isEqualTo(assigned);
            assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
            assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(Location.UNKNOWN);
            assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
            assertThat(cargo.getDelivery().isMisdirected()).isFalse();
            assertThat(cargo.getDelivery().getEstimatedTimeOfArrival()).isEqualTo(Delivery.ETA_UNKOWN);
            assertThat(cargo.getDelivery().getNextExpectedActivity()).isEqualTo(Delivery.NO_ACTIVITY);
            assertThat(cargo.getDelivery().isUnloadedAtDestination()).isFalse();
            assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.MISROUTED);
        });
    }
}
