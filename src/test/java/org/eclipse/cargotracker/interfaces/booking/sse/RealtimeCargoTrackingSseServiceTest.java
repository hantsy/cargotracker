package org.eclipse.cargotracker.interfaces.booking.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

import com.jayway.jsonpath.JsonPath;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.sse.SseEventSource;

import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.interfaces.booking.socket.RealtimeCargoTrackingWebSocketService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
@Tag("arqtest")
public class RealtimeCargoTrackingSseServiceTest {

    private static final Logger LOGGER =
            Logger.getLogger(RealtimeCargoTrackingSseServiceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");

        addExtraJars(war);
        addDomainModels(war);
        addInfraBase(war);
        addApplicationBase(war);
        war.addClasses(
                        RealtimeCargoTrackingWebSocketService.class,
                        SseRequest.class,
                        SseHandler.class)
                // stub bean to raise a CDI event
                .addClass(CargoInspectedSseEventStub.class)
                // add samples.
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class)
                // add web xml
                .addAsWebInfResource("test-web.xml", "web.xml")
                // add Wildfly specific deployment descriptor
                .addAsWebInfResource(
                        "test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    @ArquillianResource URL base;
    private Client client;

    @BeforeEach
    public void setup() {
        this.client = ClientBuilder.newClient();
    }

    @AfterEach
    public void teardown() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test
    @RunAsClient
    public void testOnCargoInspected() throws Exception {
        LOGGER.log(Level.INFO, " Running test:: RealtimeCargoTrackingServiceTest#testCargoStatus ... ");
        final var trackingTarget =
                client.target(URI.create(base.toExternalForm() + "rest/tracking"));

        var latch = new CountDownLatch(1);
        try (final var trackingEventSource = SseEventSource.target(trackingTarget)
                .build()) {
            trackingEventSource.register(inboundSseEvent -> {
                var eventData = inboundSseEvent.readData();
                assertThat(eventData).isNotNull();
                var json = JsonPath.parse(eventData);
                LOGGER.log(Level.INFO, "response json string: {0}", json);
                assertThat(json.read("$.trackingId", String.class)).isEqualTo("AAA");
                latch.countDown();
            });
            trackingEventSource.open();
        }
        latch.await(6000, TimeUnit.MILLISECONDS);
    }

}
