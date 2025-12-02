package org.eclipse.cargotracker.interfaces.handling.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent.Type;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.interfaces.RestActivator;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@ArquillianTest
@Tag("arqtest")
public class HandlingReportServiceTest {

	private static final Logger LOGGER = Logger.getLogger(HandlingReportServiceTest.class.getName());

	@Deployment()
	public static WebArchive createDeployment() {

		WebArchive war = ShrinkWrap.create(WebArchive.class, "test-HandlingReportServiceTest.war");

		addExtraJars(war);
		addDomainModels(war);
		addInfraBase(war);
		addApplicationBase(war);
		war.addClass(HandlingReportService.class)
			.addClass(HandlingReport.class)
			.addClass(ApplicationEventsStub.class)
			.addClass(ApplicationEvents.class)
			.addClass(HandlingEventRegistrationAttempt.class)
			// rest config.
			.addClass(RestActivator.class)
			// add samples.
			.addClass(SampleLocations.class)
			.addClass(SampleVoyages.class)
			.addAsResource("test-jboss-logging.properties", "jboss-logging.properties")
			// add web xml
			.addAsWebInfResource("test-web.xml", "web.xml")
			// add Wildfly specific deployment descriptor
			.addAsWebInfResource("test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

		LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

		return war;
	}

	@ArquillianResource
	URL base;

	@Inject
	ApplicationEventsStub applicationEventsStub;

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
	public void submitReport() throws MalformedURLException {
		HandlingReport report = new HandlingReport();
		report.setCompletionTime(DateUtil.toString(LocalDateTime.now()));
		report.setEventType("LOAD");
		report.setTrackingId("A001");
		report.setVoyageNumber(SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber().number());
		report.setUnLocode(SampleLocations.HONGKONG.getUnLocode().value());

		final WebTarget postReportTarget = client
			.target(URI.create(base + "rest/handling/reports").toURL().toExternalForm());

		// Response is an autocloseable resource.
		try (final Response postReportResponse = postReportTarget.request().post(Entity.json(report))) {
			assertThat(postReportResponse.getStatus()).isEqualTo(202);
			LOGGER.log(Level.INFO, "response of POST rest/handling/reports: {0}", postReportResponse.getEntity());

			assertThat(applicationEventsStub.getAttempt()).isNotNull();
			var attempt = applicationEventsStub.getAttempt();

			assertThat(attempt.getTrackingId()).isEqualTo(new TrackingId("A001"));
			assertThat(attempt.getType()).isEqualTo(Type.LOAD);
			assertThat(attempt.getVoyageNumber()).isEqualTo(SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber());
		}
	}

}
