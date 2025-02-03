package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.*;
import org.eclipse.cargotracker.interfaces.RestActivator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
@Tag("arqtest")
public class CarrierMovementRepositoryTest {
    private static final Logger LOGGER =
            Logger.getLogger(CarrierMovementRepositoryTest.class.getName());
    @Inject VoyageRepository voyageRepository;
    @Inject EntityManager entityManager;
    @Inject UserTransaction utx;
    String voyageNumberIdString = "007";
    Voyage voyage;
    Location from = SampleLocations.HONGKONG;
    Location to = SampleLocations.CHICAGO;
    LocalDateTime fromDate = LocalDateTime.now();
    LocalDateTime toDate = LocalDateTime.now();

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war =
                ShrinkWrap.create(WebArchive.class, "test-CarrierMovementRepositoryTest.war");

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

    @BeforeEach
    public void setup() throws Exception {
        startTransaction();
        voyage =
                new Voyage(
                        new VoyageNumber(voyageNumberIdString),
                        new Schedule(
                                Collections.singletonList(
                                        new CarrierMovement(from, to, fromDate, toDate))));
        this.entityManager.persist(voyage);
        this.entityManager.flush();
        commitTransaction();
    }

    @Test
    public void testFind() throws Exception {
        startTransaction();
        Voyage result = voyageRepository.find(new VoyageNumber(voyageNumberIdString));
        assertThat(result).isNotNull();
        assertThat(result.getVoyageNumber().number()).isEqualTo(voyageNumberIdString);

        var movements = result.getSchedule().getCarrierMovements();
        assertThat(movements).hasSize(1);

        var m = movements.get(0);
        assertThat(m.getDepartureLocation()).isEqualTo(from);
        assertThat(m.getArrivalLocation()).isEqualTo(to);
        assertThat(m.getDepartureTime().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(fromDate.truncatedTo(ChronoUnit.SECONDS));
        assertThat(m.getArrivalTime().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(toDate.truncatedTo(ChronoUnit.SECONDS));
        commitTransaction();
    }
}
