package org.eclipse.cargotracker;

import org.eclipse.cargotracker.application.BookingService;
import org.eclipse.cargotracker.application.internal.DefaultBookingService;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.Delivery;
import org.eclipse.cargotracker.domain.model.cargo.HandlingActivity;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.RoutingStatus;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.cargo.TransportStatus;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventFactory;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.domain.model.handling.UnknownCargoException;
import org.eclipse.cargotracker.domain.model.handling.UnknownLocationException;
import org.eclipse.cargotracker.domain.model.handling.UnknownVoyageException;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.CarrierMovement;
import org.eclipse.cargotracker.domain.model.voyage.Schedule;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.domain.service.RoutingService;
import org.eclipse.cargotracker.domain.shared.AndSpecification;
import org.eclipse.cargotracker.domain.shared.NotSpecification;
import org.eclipse.cargotracker.domain.shared.OrSpecification;
import org.eclipse.cargotracker.domain.shared.Specification;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;
import org.eclipse.cargotracker.infrastructure.logging.LoggerProducer;
import org.eclipse.cargotracker.infrastructure.messaging.JMSResourcesSetup;
import org.eclipse.cargotracker.infrastructure.persistence.DatabaseSetup;
import org.eclipse.cargotracker.infrastructure.routing.ExternalRoutingService;
import org.eclipse.pathfinder.api.GraphTraversalService;
import org.eclipse.pathfinder.api.TransitEdge;
import org.eclipse.pathfinder.api.TransitPath;
import org.eclipse.pathfinder.internal.GraphDao;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Deployments {
    private static final Logger LOGGER = Logger.getLogger(Deployments.class.getName());

    public static void addExtraJars(WebArchive war) {
        File[] extraJars =
                Maven.resolver()
                        .loadPomFromFile("pom.xml")
                        .importCompileAndRuntimeDependencies()
                        .resolve(
                                "org.assertj:assertj-core",
                                "org.hamcrest:hamcrest",
                                "org.mockito:mockito-core",
                                "org.awaitility:awaitility")
                        .withTransitivity()
                        .asFile();
        LOGGER.log(
                Level.FINE, "add test libs to deployment archive: {0}", new Object[]{extraJars});
        war.addAsLibraries(extraJars);
    }

    public static void addInfraBase(WebArchive war) {
        war.addPackage(CargoInspected.class.getPackage()).addClass(LoggerProducer.class);
    }

    // Infrastructure layer components.
    // Add persistence/JPA components.
    public static void addInfraPersistence(WebArchive war) {
        war.addClass(DatabaseSetup.class);
//                .addClass(JpaCargoRepository.class)
//                .addClass(JpaVoyageRepository.class)
//                .addClass(JpaHandlingEventRepository.class)
//                .addClass(JpaLocationRepository.class);
    }

    public static void addApplicationBase(WebArchive war) {
        war.addClass(DateUtil.class);
    }

    public static void addApplicationService(WebArchive war) {
        war.addPackage(BookingService.class.getPackage())
                .addPackage(DefaultBookingService.class.getPackage());
    }

    public static void addInfraMessaging(WebArchive war) {
        war.addPackages(true, JMSResourcesSetup.class.getPackage());
    }

    public static void addInfraRouting(WebArchive war) {
        war.addPackages(true, ExternalRoutingService.class.getPackage());
    }

    public static void addDomainModels(WebArchive war) {
        war
                // locations
                .addPackage(Location.class.getPackage())

                // voyage
                .addPackage(Voyage.class.getPackage())

                // cargo models
                .addPackage(Cargo.class.getPackage())

                // handling events
                .addPackage(HandlingEvent.class.getPackage())

                // shared classes
                .addPackage(Specification.class.getPackage());
    }

    public static void addDomainRepositories(WebArchive war) {
        // the repository interfaces are included in the domain models, do noting here
    }

    public static void addDomainService(WebArchive war) {
        war.addClass(RoutingService.class);
    }

    public static void addGraphTraversalModels(WebArchive war) {
        war.addClass(TransitPath.class).addClass(TransitEdge.class);
    }

    public static void addGraphTraversalService(WebArchive war) {
        war.addClass(GraphTraversalService.class).addClass(GraphDao.class);
    }
}
