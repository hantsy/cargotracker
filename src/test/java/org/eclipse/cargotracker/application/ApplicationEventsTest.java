package org.eclipse.cargotracker.application;

import org.eclipse.cargotracker.IntegrationTests;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.infrastructure.messaging.JMSResourcesSetup;
import org.eclipse.cargotracker.infrastructure.messaging.jms.CargoHandledConsumer;
import org.eclipse.cargotracker.infrastructure.messaging.jms.HandlingEventRegistrationAttemptConsumer;
import org.eclipse.cargotracker.infrastructure.messaging.jms.JmsApplicationEvents;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.transaction.UserTransaction;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

@RunWith(Arquillian.class)
@Category(IntegrationTests.class)
public class ApplicationEventsTest {
    private static final Logger LOGGER = Logger.getLogger(ApplicationEventsTest.class.getName());
    private static TrackingId trackingId;
    private static List<Itinerary> candidates;

    //    @Inject
    //    private DeliveredCargoConsumer deliveredCargoConsumer;
    private static LocalDate deadline;
    private static Itinerary assigned;
    @Inject UserTransaction utx;
    @Inject CargoInspectionServiceStub cargoInspectionService;
    @Inject HandlingEventServiceStub handlingEventService;
    @Inject JMSContext jmsContext;
    // MSB can not be injected as normal CDI beans.
    // https://rmannibucau.metawerx.net/post/cdi-proxy-interceptor-support-with-interceptionfactory
    //
    // And it can not be spied like simple POJO.
    // https://github.com/topikachu/arquillian-extension-mockito
    //    @Test
    //    public void testCargoHasArrived() throws InterruptedException {
    //        var deliveredCargoConsumerSpy = spy(deliveredCargoConsumer);
    //        doNothing().when(deliveredCargoConsumerSpy).onMessage(any(Message.class));
    //
    //        var testCargo = new Cargo(new TrackingId("AAA"), new
    // RouteSpecification(SampleLocations.HONGKONG, SampleLocations.NEWYORK,
    // LocalDate.now().plusMonths(6)));
    //        applicationEvents.cargoHasArrived(testCargo);
    //
    //        Thread.sleep(5000);
    //
    //        ArgumentCaptor<Message> argumentCaptor = ArgumentCaptor.forClass(Message.class);
    //        verify(deliveredCargoConsumerSpy, atLeastOnce()).onMessage(argumentCaptor.capture());
    //
    //        assertThat(argumentCaptor.getValue().toString()).contains("AAA");
    //    }
    @Inject private ApplicationEvents applicationEvents;

    @Resource(lookup = "java:app/jms/MisdirectedCargoQueue")
    private Destination misdirectedCargoQueue;

    @Resource(lookup = "java:app/jms/DeliveredCargoQueue")
    private Destination deliveredCargoQueue;

    @Deployment
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-ApplicationEventsTest.war");

        File[] extraJars =
                Maven.resolver()
                        .loadPomFromFile("pom.xml")
                        .importCompileAndRuntimeDependencies()
                        .resolve(
                                "org.apache.commons:commons-lang3",
                                "org.assertj:assertj-core",
                                "org.hamcrest:hamcrest-core",
                                "org.mockito:mockito-core")
                        .withTransitivity()
                        .asFile();
        war.addAsLibraries(extraJars);
        addDomainModels(war);
        addInfraBase(war);
        // addInfraPersistence(war);
        // addInfraMessaging(war);
        war.addClass(JMSResourcesSetup.class)
                .addClass(CargoHandledConsumer.class)
                .addClass(HandlingEventRegistrationAttemptConsumer.class);
        addApplicationBase(war);
        // addApplicationService(war);

        // JmsApplicationEvents deps.
        war.addClass(ApplicationEvents.class).addClass(JmsApplicationEvents.class);
        // Stubbing Jms consumer deps.
        war.addClass(CargoInspectionService.class).addClass(CargoInspectionServiceStub.class);
        war.addClass(HandlingEventService.class).addClass(HandlingEventServiceStub.class);
        war.addClass(HandlingEventRegistrationAttempt.class);
        //
        // war.addClass(RestConfiguration.class);

        // addDomainService(war);
        // addInfraRouting(war);

        // addGraphTraversalModels(war);
        // addGraphTraversalService(war);
        war
                // .addClass(ApplicationEventsTestDataGenerator.class)// use custom data generator
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class);

        // add persistence unit descriptor
        // war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
        // add wildfly/jboss specific logging.properties
        war.addAsResource("test-jboss-logging.properties", "jboss-logging.properties");
        // add beans.xml
        war.addAsWebInfResource("test-beans.xml", "beans.xml");
        // add Wildfly specific deployment descriptor
        war.addAsWebInfResource(
                "test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");
        // add web xml
        war.addAsWebInfResource("test-web.xml", "web.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    @Test
    public void testCargoWasHandled() throws InterruptedException {
        CargoInspectionServiceStub.latch = new CountDownLatch(1);
        var trackingId = new TrackingId("AAA");
        var cargo =
                new Cargo(
                        trackingId,
                        new RouteSpecification(
                                SampleLocations.HONGKONG,
                                SampleLocations.NEWYORK,
                                LocalDate.now()));
        var event =
                new HandlingEvent(
                        cargo,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        HandlingEvent.Type.RECEIVE,
                        SampleLocations.HONGKONG);
        applicationEvents.cargoWasHandled(event);

        CargoInspectionServiceStub.latch.await(1, TimeUnit.SECONDS);

        assertThat(cargoInspectionService.getTrackingId()).isEqualTo(trackingId);
    }

    @Test
    public void testReceivedHandlingEventRegistrationAttempt() throws Exception {
        HandlingEventServiceStub.latch = new CountDownLatch(1);
        var trackingId = new TrackingId("AAA");
        var attempt =
                new HandlingEventRegistrationAttempt(
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        trackingId,
                        SampleVoyages.v100.getVoyageNumber(),
                        HandlingEvent.Type.RECEIVE,
                        SampleLocations.HONGKONG.getUnLocode());
        applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);

        HandlingEventServiceStub.latch.await(1, TimeUnit.SECONDS);

        assertThat(handlingEventService.getTrackingId()).isEqualTo(trackingId);
        assertThat(handlingEventService.getType()).isEqualTo(HandlingEvent.Type.RECEIVE);
        assertThat(handlingEventService.getVoyageNumber())
                .isEqualTo(SampleVoyages.v100.getVoyageNumber());
        assertThat(handlingEventService.getUnLocode())
                .isEqualTo(SampleLocations.HONGKONG.getUnLocode());
    }

    @Test
    public void testCargoWasArrived() throws InterruptedException, JMSException {
        var consumer = jmsContext.createConsumer(deliveredCargoQueue);
        var trackingId = new TrackingId("AAA");
        var cargo =
                new Cargo(
                        trackingId,
                        new RouteSpecification(
                                SampleLocations.HONGKONG,
                                SampleLocations.NEWYORK,
                                LocalDate.now()));
        applicationEvents.cargoHasArrived(cargo);

        var message = consumer.receive(1000);
        assertThat(message.getBody(String.class)).isEqualTo("AAA");
        consumer.close();
    }

    @Test
    public void testCargoWasMisdirected() throws InterruptedException, JMSException {
        var consumer = jmsContext.createConsumer(misdirectedCargoQueue);

        var trackingId = new TrackingId("AAA");
        var cargo =
                new Cargo(
                        trackingId,
                        new RouteSpecification(
                                SampleLocations.HONGKONG,
                                SampleLocations.NEWYORK,
                                LocalDate.now()));
        applicationEvents.cargoWasMisdirected(cargo);
        var message = consumer.receive(1000);
        assertThat(message.getBody(String.class)).isEqualTo("AAA");
        consumer.close();
    }

    @ApplicationScoped
    public static class CargoInspectionServiceStub implements CargoInspectionService {
        public static CountDownLatch latch;
        @Inject Logger logger;

        private TrackingId trackingId;

        @Override
        public void inspectCargo(TrackingId trackingId) {
            logger.log(Level.INFO, "tracking id: {0}", trackingId);
            this.trackingId = trackingId;
            latch.countDown();
        }

        public TrackingId getTrackingId() {
            return this.trackingId;
        }
    }

    @ApplicationScoped
    public static class HandlingEventServiceStub implements HandlingEventService {
        public static CountDownLatch latch;
        @Inject Logger logger;
        private LocalDateTime completionTime;
        private TrackingId trackingId;
        private VoyageNumber voyageNumber;
        private UnLocode unLocode;
        private HandlingEvent.Type type;

        @Override
        public void registerHandlingEvent(
                LocalDateTime completionTime,
                TrackingId trackingId,
                VoyageNumber voyageNumber,
                UnLocode unLocode,
                HandlingEvent.Type type)
                throws CannotCreateHandlingEventException {

            logger.log(
                    Level.INFO,
                    "completionTime: {0}\n"
                            + "tracking id: {1}\n"
                            + "voyageNumber: {2}\n"
                            + "unLocode: {3}\n"
                            + "type: {4}",
                    new Object[] {completionTime, trackingId, voyageNumber, unLocode, type});
            this.completionTime = completionTime;
            this.trackingId = trackingId;
            this.voyageNumber = voyageNumber;
            this.unLocode = unLocode;
            this.type = type;
            latch.countDown();
        }

        public LocalDateTime getCompletionTime() {
            return completionTime;
        }

        public TrackingId getTrackingId() {
            return trackingId;
        }

        public VoyageNumber getVoyageNumber() {
            return voyageNumber;
        }

        public UnLocode getUnLocode() {
            return unLocode;
        }

        public HandlingEvent.Type getType() {
            return type;
        }
    }
}
