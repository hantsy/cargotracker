package org.eclipse.cargotracker;

import org.eclipse.cargotracker.application.BookingService;
import org.eclipse.cargotracker.application.internal.DefaultBookingService;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.handling.*;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.*;
import org.eclipse.cargotracker.domain.service.RoutingService;
import org.eclipse.cargotracker.domain.shared.*;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;
import org.eclipse.cargotracker.infrastructure.logging.LoggerProducer;
import org.eclipse.cargotracker.infrastructure.messaging.JMSResourcesSetup;
import org.eclipse.cargotracker.infrastructure.persistence.DatabaseSetup;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaCargoRepository;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaHandlingEventRepository;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaLocationRepository;
import org.eclipse.cargotracker.infrastructure.persistence.jpa.JpaVoyageRepository;
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
                                "org.hamcrest:hamcrest-core",
                                "org.mockito:mockito-core")
                        .withTransitivity()
                        .asFile();
        LOGGER.log(
                Level.FINE, "add test libs to deployment archive: {0}", new Object[] {extraJars});
        war.addAsLibraries(extraJars);
    }

    public static void addInfraBase(WebArchive war) {
        war.addPackage(CargoInspected.class.getPackage()).addClass(LoggerProducer.class);
    }

    // Infrastructure layer components.
    // Add persistence/JPA components.
    public static void addInfraPersistence(WebArchive war) {
        war.addClass(DatabaseSetup.class)
                .addClass(JpaCargoRepository.class)
                .addClass(JpaVoyageRepository.class)
                .addClass(JpaHandlingEventRepository.class)
                .addClass(JpaLocationRepository.class);
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
                .addClass(Location.class)
                .addClass(UnLocode.class)

                // voyage
                .addClass(Voyage.class)
                .addClass(VoyageNumber.class)
                .addClass(Schedule.class)
                .addClass(CarrierMovement.class)

                // cargo models
                .addClass(Cargo.class)
                .addClass(Delivery.class)
                .addClass(HandlingActivity.class)
                .addClass(Itinerary.class)
                .addClass(Leg.class)
                .addClass(RouteSpecification.class)
                .addClass(RoutingStatus.class)
                .addClass(TrackingId.class)
                .addClass(TransportStatus.class)

                // handling models
                .addClass(HandlingEvent.class)
                // .addClass(HandlingEventFactory.class)
                .addClass(HandlingHistory.class)
                .addClass(CannotCreateHandlingEventException.class)
                .addClass(UnknownCargoException.class)
                .addClass(UnknownVoyageException.class)
                .addClass(UnknownLocationException.class)

                // shared classes
                .addClass(AbstractSpecification.class)
                .addClass(Specification.class)
                .addClass(AndSpecification.class)
                .addClass(OrSpecification.class)
                .addClass(NotSpecification.class)
                .addClass(DomainObjectUtils.class);
    }

    public static void addDomainRepositories(WebArchive war) {
        war.addClass(HandlingEventFactory.class); // depends on repos
        // add repos
        war.addClass(CargoRepository.class)
                .addClass(LocationRepository.class)
                .addClass(VoyageRepository.class)
                .addClass(HandlingEventRepository.class);
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
