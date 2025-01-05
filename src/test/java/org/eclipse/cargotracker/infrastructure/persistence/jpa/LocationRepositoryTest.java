package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

import jakarta.inject.Inject;

import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.interfaces.RestActivator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("arqtest")
public class LocationRepositoryTest {
    private static final Logger LOGGER = Logger.getLogger(LocationRepositoryTest.class.getName());
    @Inject private LocationRepository locationRepository;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-LocationRepositoryTest.war");

        addExtraJars(war);
        addDomainModels(war);
        addDomainRepositories(war);
        addInfraBase(war);
        addInfraPersistence(war);
        addApplicationBase(war);

        war.addClass(RestActivator.class);
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

    @BeforeEach
    public void setup() {}

    @Test
    public void testFind() {
        final UnLocode melbourne = new UnLocode("AUMEL");
        Location location = locationRepository.find(melbourne);
        assertThat(location).isNotNull();
        assertThat(location.getName()).isEqualTo("Melbourne");
        assertThat(location.getUnLocode()).isEqualTo(melbourne);

        assertThat(locationRepository.find(new UnLocode("NOLOC"))).isNull();
    }

    @Test
    public void testFindAll() {
        List<Location> allLocations = locationRepository.findAll();

        assertThat(allLocations).isNotNull();
        assertThat(allLocations).hasSize(13);
    }
}
