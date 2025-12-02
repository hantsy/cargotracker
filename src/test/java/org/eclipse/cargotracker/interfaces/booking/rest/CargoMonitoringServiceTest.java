package org.eclipse.cargotracker.interfaces.booking.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.interfaces.RestActivator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@ArquillianTest
@Tag("arqtest")
public class CargoMonitoringServiceTest {

	private static final Logger LOGGER = Logger.getLogger(CargoMonitoringServiceTest.class.getName());

	@ArquillianResource
	private URL base;

	private Client client;

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		WebArchive war = ShrinkWrap.create(WebArchive.class, "test-CargoMonitoringServiceTest.war");

		addExtraJars(war);
		addDomainModels(war);
		addDomainRepositories(war);
		addInfraBase(war);
		addInfraPersistence(war);
		addApplicationBase(war);

		war.addClass(RestActivator.class).addClass(CargoMonitoringService.class);
		war.addClass(SampleDataGenerator.class)
			.addClass(SampleLocations.class)
			.addClass(SampleVoyages.class)
			// add persistence unit descriptor
			.addAsResource("test-persistence.xml", "META-INF/persistence.xml")

			// add web xml
			.addAsWebInfResource("test-web.xml", "web.xml")

			// add Wildfly specific deployment descriptor
			.addAsWebInfResource("test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

		LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

		return war;
	}

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
	public void testCargoStatus() throws Exception {
		LOGGER.log(Level.INFO, " Running test:: CargoMonitoringServiceTest#testCargoStatus ... ");
		final WebTarget getCargoStatus = client.target(URI.create(base.toExternalForm() + "rest/cargo"));

		// Response is an autocloseable resource.
		try (final Response getAllPostsResponse = getCargoStatus.request().accept(MediaType.APPLICATION_JSON).get()) {
			assertThat(getAllPostsResponse.getStatus()).isEqualTo(200);
			// TODO: use POJO to assert the response body.
		}
	}

}
