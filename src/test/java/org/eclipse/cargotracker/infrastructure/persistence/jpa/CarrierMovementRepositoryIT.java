package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import org.eclipse.cargotracker.TxUtil;
import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.CarrierMovement;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.Schedule;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.interfaces.RestActivator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.addApplicationBase;
import static org.eclipse.cargotracker.Deployments.addDomainModels;
import static org.eclipse.cargotracker.Deployments.addDomainRepositories;
import static org.eclipse.cargotracker.Deployments.addExtraJars;
import static org.eclipse.cargotracker.Deployments.addInfraBase;
import static org.eclipse.cargotracker.Deployments.addInfraPersistence;

<<<<<<<< HEAD:src/test/java/org/eclipse/cargotracker/infrastructure/persistence/jpa/CarrierMovementRepositoryIT.java
@ArquillianTest
public class CarrierMovementRepositoryIT {
    private static final Logger LOGGER =
            Logger.getLogger(CarrierMovementRepositoryIT.class.getName());
========
@ExtendWith(ArquillianExtension.class)
@Tag("arqtest")
public class VoyageRepositoryTest {
    private static final Logger LOGGER = Logger.getLogger(VoyageRepositoryTest.class.getName());
>>>>>>>> master:src/test/java/org/eclipse/cargotracker/infrastructure/persistence/jpa/VoyageRepositoryTest.java
    @Inject
    VoyageRepository voyageRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    UserTransaction utx;

    String voyageNumberIdString = "007";
    Voyage voyage;
    Location from = SampleLocations.HONGKONG;
    Location to = SampleLocations.CHICAGO;
    LocalDateTime fromDate = LocalDateTime.now();
    LocalDateTime toDate = LocalDateTime.now().plusDays(1);

    @Deployment
    public static WebArchive createDeployment() {
<<<<<<<< HEAD:src/test/java/org/eclipse/cargotracker/infrastructure/persistence/jpa/CarrierMovementRepositoryIT.java
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-CarrierMovementRepositoryIT.war");
========
        WebArchive war = ShrinkWrap.create(WebArchive.class, "VoyageRepositoryTest.war");
>>>>>>>> master:src/test/java/org/eclipse/cargotracker/infrastructure/persistence/jpa/VoyageRepositoryTest.java

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
                // add TxUtil
                .addClass(TxUtil.class)
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

    TxUtil tx = null;

    @BeforeEach
    public void setup() {
        tx = new TxUtil(utx, entityManager);
        tx.runInTx(() -> {
            voyage = new Voyage(
                    new VoyageNumber(voyageNumberIdString),
                    Schedule.of(List.of(new CarrierMovement(from, to, fromDate, toDate)))
            );
            this.entityManager.persist(voyage);
            this.entityManager.flush();
        });
    }

    @AfterEach
    public void teardown() {
        tx = null;
    }

    @Test
    public void testFind() {
        tx.runInTx(() -> {
            Voyage result = voyageRepository.find(new VoyageNumber(voyageNumberIdString));
            assertThat(result).isNotNull();
            assertThat(result.getVoyageNumber().number()).isEqualTo(voyageNumberIdString);

            var movements = result.getSchedule().carrierMovements();
            assertThat(movements).hasSize(1);

            var m = movements.getFirst();
            assertThat(m.getDepartureLocation()).isEqualTo(from);
            assertThat(m.getArrivalLocation()).isEqualTo(to);
            assertThat(m.getDepartureTime().truncatedTo(ChronoUnit.SECONDS))
                    .isEqualTo(fromDate.truncatedTo(ChronoUnit.SECONDS));
            assertThat(m.getArrivalTime().truncatedTo(ChronoUnit.SECONDS))
                    .isEqualTo(toDate.truncatedTo(ChronoUnit.SECONDS));
        });
    }
}
