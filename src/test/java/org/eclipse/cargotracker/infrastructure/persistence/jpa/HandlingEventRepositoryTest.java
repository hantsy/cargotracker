package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("arqtest")
public class HandlingEventRepositoryTest {
    private static final Logger LOGGER =
            Logger.getLogger(HandlingEventRepositoryTest.class.getName());
    @Inject UserTransaction utx;
    @Inject private LocationRepository locationRepository;

    @Inject private HandlingEventRepository handlingEventRepository;

    @Inject private CargoRepository cargoRepository;

    @PersistenceContext private EntityManager entityManager;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war =
                ShrinkWrap.create(WebArchive.class, "test-HandlingEventRepositoryTest.war");

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

    @Test
    public void testSave() throws Exception {
        startTransaction();
        Location location = locationRepository.find(new UnLocode("SESTO"));

        Cargo cargo = cargoRepository.find(new TrackingId("ABC123"));
        LocalDateTime completionTime = LocalDateTime.now().minusDays(20);
        LocalDateTime registrationTime = LocalDateTime.now().minusDays(10);
        HandlingEvent event =
                new HandlingEvent(
                        cargo,
                        completionTime,
                        registrationTime,
                        HandlingEvent.Type.CLAIM,
                        location);

        handlingEventRepository.store(event);

        this.entityManager.flush();

        // Payara/EclipseLink issue:
        // In a native query, the named parameters like `:id` does not work on Payara/EclipseLink.
        // eg.
        // HandlingEvent result = (HandlingEvent) this.entityManager.createNativeQuery("select *
        // from
        // HandlingEvent where id = :id", HandlingEvent.class)
        //
        // revert to use JPQL, it is standard and portable.
        HandlingEvent result =
                this.entityManager
                        .createQuery(
                                "select e from HandlingEvent e where e.id = :id",
                                HandlingEvent.class)
                        .setParameter("id", getLongId(event))
                        .getSingleResult();
        assertThat(result.getCargo()).isEqualTo(cargo);
        assertThat(result.getCompletionTime().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(completionTime.truncatedTo(ChronoUnit.SECONDS));
        assertThat(result.getRegistrationTime().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(registrationTime.truncatedTo(ChronoUnit.SECONDS));
        assertThat(result.getType()).isEqualTo(HandlingEvent.Type.CLAIM);
        commitTransaction();
    }

    private Long getLongId(Object o) {

        try {
            Field id = o.getClass().getDeclaredField("id");
            id.setAccessible(true);
            return (Long) id.get(o);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    //    private void flush() {
    //        sessionFactory.getCurrentSession().flush();
    //    }
    //
    //    private Long getLongId(Object o) {
    //        final Session session = sessionFactory.getCurrentSession();
    //        if (session.contains(o)) {
    //            return (Long) session.getIdentifier(o);
    //        } else {
    //            try {
    //                Field id = o.getClass().getDeclaredField("id");
    //                id.setAccessible(true);
    //                return (Long) id.get(o);
    //            } catch (Exception e) {
    //                throw new RuntimeException();
    //            }
    //        }
    //    }

    @Test
    public void testFindEventsForCargo() {
        TrackingId trackingId = new TrackingId("XYZ"); // non-existing cargo
        List<HandlingEvent> handlingEvents =
                handlingEventRepository
                        .lookupHandlingHistoryOfCargo(trackingId)
                        .getDistinctEventsByCompletionTime();
        assertThat(handlingEvents).hasSize(0);

        TrackingId existingTrackingId = new TrackingId("MNO456"); // existing cargo
        List<HandlingEvent> existingHandlingEvents =
                handlingEventRepository
                        .lookupHandlingHistoryOfCargo(trackingId)
                        .getDistinctEventsByCompletionTime();
        assertThat(existingHandlingEvents).hasSize(0);
    }
}
