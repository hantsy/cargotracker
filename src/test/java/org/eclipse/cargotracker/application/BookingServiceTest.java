package org.eclipse.cargotracker.application;

import org.eclipse.cargotracker.IntegrationTests;
import org.eclipse.cargotracker.application.internal.DefaultBookingService;
import org.eclipse.cargotracker.application.util.RestConfiguration;
import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.infrastructure.routing.ExternalRoutingService;
import org.eclipse.cargotracker.infrastructure.routing.client.GraphTraversalResourceClient;
import org.eclipse.pathfinder.api.GraphTraversalService;
import org.eclipse.pathfinder.api.TransitEdge;
import org.eclipse.pathfinder.api.TransitPath;
import org.eclipse.pathfinder.internal.GraphDao;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;
import static org.junit.Assert.*;

/**
 * Application layer integration test covering a number of otherwise fairly trivial components that
 * largely do not warrant their own tests.
 *
 * <p>Ensure a Payara instance is running locally before this test is executed, with the default
 * user name and password.
 */
// TODO [Jakarta EE 8] Move to the Java Date-Time API for date manipulation. Also avoid hard-coded
// dates.
@RunWith(Arquillian.class)
@Category(IntegrationTests.class)
public class BookingServiceTest {
    private static final Logger LOGGER = Logger.getLogger(BookingServiceTest.class.getName());
    private static TrackingId trackingId;
    private static List<Itinerary> candidates;
    private static LocalDate deadline;
    private static Itinerary assigned;
    @Inject UserTransaction utx;
    @Inject private BookingService bookingService;
    @PersistenceContext private EntityManager entityManager;

    @Deployment
    public static WebArchive createDeployment() {

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
                .addClass(RestConfiguration.class)

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
    @Before
    public void setUp() throws Exception {
        startTransaction();
    }

    @After
    public void tearDown() throws Exception {
        commitTransaction();
    }

    public void startTransaction() throws Exception {
        utx.begin();
        entityManager.joinTransaction();
    }

    public void commitTransaction() throws Exception {
        utx.commit();
    }

    @Test
    @InSequence(1)
    // The `Transactional` annotation does not work in Arquillian test.
    // @Transactional
    public void testRegisterNew() {
        UnLocode fromUnlocode = new UnLocode("USCHI");
        UnLocode toUnlocode = new UnLocode("SESTO");

        deadline = LocalDate.now().plusMonths(6);

        trackingId = bookingService.bookNewCargo(fromUnlocode, toUnlocode, deadline);

        Cargo cargo =
                entityManager
                        .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                        .setParameter("trackingId", trackingId)
                        .getSingleResult();

        assertEquals(SampleLocations.CHICAGO, cargo.getOrigin());
        assertEquals(SampleLocations.STOCKHOLM, cargo.getRouteSpecification().getDestination());
        assertTrue(deadline.isEqual(cargo.getRouteSpecification().getArrivalDeadline()));
        assertEquals(TransportStatus.NOT_RECEIVED, cargo.getDelivery().getTransportStatus());
        assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
        assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
        assertFalse(cargo.getDelivery().isMisdirected());
        assertEquals(Delivery.ETA_UNKOWN, cargo.getDelivery().getEstimatedTimeOfArrival());
        assertEquals(Delivery.NO_ACTIVITY, cargo.getDelivery().getNextExpectedActivity());
        assertFalse(cargo.getDelivery().isUnloadedAtDestination());
        assertEquals(RoutingStatus.NOT_ROUTED, cargo.getDelivery().getRoutingStatus());
        assertEquals(Itinerary.EMPTY_ITINERARY, cargo.getItinerary());
    }

    @Test
    @InSequence(2)
    // @Transactional
    public void testRouteCandidates() {
        candidates = bookingService.requestPossibleRoutesForCargo(trackingId);

        assertFalse(candidates.isEmpty());
    }

    @Test
    @InSequence(3)
    // @Transactional
    public void testAssignRoute() {
        assigned = candidates.get(new Random().nextInt(candidates.size()));

        bookingService.assignCargoToRoute(assigned, trackingId);

        Cargo cargo =
                entityManager
                        .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                        .setParameter("trackingId", trackingId)
                        .getSingleResult();

        assertThat(cargo.getItinerary()).isEqualTo(assigned);
        assertThat(cargo.getDelivery().getTransportStatus())
                .isEqualTo(TransportStatus.NOT_RECEIVED);
        assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
        assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(
                        cargo.getDelivery()
                                .getEstimatedTimeOfArrival()
                                .isBefore(deadline.atStartOfDay()))
                .isTrue();
        assertEquals(
                HandlingEvent.Type.RECEIVE,
                cargo.getDelivery().getNextExpectedActivity().getType());
        assertEquals(
                SampleLocations.CHICAGO,
                cargo.getDelivery().getNextExpectedActivity().getLocation());
        assertThat(cargo.getDelivery().getNextExpectedActivity().getVoyage()).isNull();
        assertThat(cargo.getDelivery().isUnloadedAtDestination()).isFalse();
        assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.ROUTED);
    }

    @Test
    @InSequence(4)
    // @Transactional
    public void testChangeDestination() {
        bookingService.changeDestination(trackingId, new UnLocode("FIHEL"));

        Cargo cargo =
                entityManager
                        .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                        .setParameter("trackingId", trackingId)
                        .getSingleResult();

        assertEquals(SampleLocations.CHICAGO, cargo.getOrigin());
        assertEquals(SampleLocations.HELSINKI, cargo.getRouteSpecification().getDestination());
        assertTrue(deadline.isEqual(cargo.getRouteSpecification().getArrivalDeadline()));
        assertThat(cargo.getItinerary()).isEqualTo(assigned);
        assertEquals(TransportStatus.NOT_RECEIVED, cargo.getDelivery().getTransportStatus());
        assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
        assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
        assertFalse(cargo.getDelivery().isMisdirected());
        assertEquals(Delivery.ETA_UNKOWN, cargo.getDelivery().getEstimatedTimeOfArrival());
        assertEquals(Delivery.NO_ACTIVITY, cargo.getDelivery().getNextExpectedActivity());
        assertFalse(cargo.getDelivery().isUnloadedAtDestination());
        assertEquals(RoutingStatus.MISROUTED, cargo.getDelivery().getRoutingStatus());
    }

    @Test
    @InSequence(5)
    // @Transactional
    public void testChangeDeadline() {
        LocalDate newDeadline = deadline.plusMonths(1);
        bookingService.changeDeadline(trackingId, newDeadline);

        Cargo cargo =
                entityManager
                        .createNamedQuery("Cargo.findByTrackingId", Cargo.class)
                        .setParameter("trackingId", trackingId)
                        .getSingleResult();

        assertEquals(SampleLocations.CHICAGO, cargo.getOrigin());
        assertEquals(SampleLocations.HELSINKI, cargo.getRouteSpecification().getDestination());
        assertTrue(newDeadline.isEqual(cargo.getRouteSpecification().getArrivalDeadline()));
        assertThat(cargo.getItinerary()).isEqualTo(assigned);
        assertEquals(TransportStatus.NOT_RECEIVED, cargo.getDelivery().getTransportStatus());
        assertEquals(Location.UNKNOWN, cargo.getDelivery().getLastKnownLocation());
        assertEquals(Voyage.NONE, cargo.getDelivery().getCurrentVoyage());
        assertFalse(cargo.getDelivery().isMisdirected());
        assertEquals(Delivery.ETA_UNKOWN, cargo.getDelivery().getEstimatedTimeOfArrival());
        assertEquals(Delivery.NO_ACTIVITY, cargo.getDelivery().getNextExpectedActivity());
        assertFalse(cargo.getDelivery().isUnloadedAtDestination());
        assertEquals(RoutingStatus.MISROUTED, cargo.getDelivery().getRoutingStatus());
    }
    
}
