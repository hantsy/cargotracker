package org.eclipse.cargotracker.interfaces.booking.socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

import com.jayway.jsonpath.JsonPath;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
@Tag("arqtest")
public class RealtimeCargoTrackingWebSocketServiceTest {

    private static final Logger LOGGER =
            Logger.getLogger(RealtimeCargoTrackingWebSocketServiceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive war =
                ShrinkWrap.create(WebArchive.class, "test-RealtimeCargoTrackingServiceTest.war");

        addExtraJars(war);
        addDomainModels(war);
        addInfraBase(war);
        addApplicationBase(war);
        war.addClass(RealtimeCargoTrackingWebSocketService.class)
                // .addClass(TestClient.class)
                // EJB to raise a CDI event
                .addClass(CargoInspectedStub.class)
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

    @Test
    @RunAsClient
    void testOnCargoInspected() throws Exception {
        LOGGER.log(Level.INFO, "run test RealtimeCargoTrackingServiceTest# testOnCargoInspected");
        TestClient.latch = new CountDownLatch(1);
        var session = connectToServer();
        assertThat(session).isNotNull();
        TestClient.latch.await(10, TimeUnit.SECONDS);
        assertThat(TestClient.response).isNotNull();
        var json = JsonPath.parse(TestClient.response);
        LOGGER.log(Level.INFO, "response json string: {0}", json);
        assertThat(json.read("$.trackingId", String.class)).isEqualTo("AAA");
    }

    private Session connectToServer() throws DeploymentException, IOException, URISyntaxException {
        var container = ContainerProvider.getWebSocketContainer();
        URI uri =
                new URI(
                        "ws://"
                                + base.getHost()
                                + ":"
                                + base.getPort()
                                + base.getPath()
                                + "tracking");

        LOGGER.log(Level.INFO, "connected to url: {0}", uri);
        return container.connectToServer(TestClient.class, uri);
    }
}
