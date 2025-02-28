package org.eclipse.cargotracker.scenario;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.domain.service.RoutingService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

////        routingService = routeSpecification -> {
//            if (routeSpecification.getOrigin().equals(SampleLocations.HONGKONG)) {
//                // Hongkong - NYC - Chicago - SampleLocations.STOCKHOLM, initial routing
//                return Arrays.asList(new Itinerary(Arrays.asList(
//                        new Leg(SampleVoyages.v100, SampleLocations.HONGKONG,
// SampleLocations.NEWYORK,
//                                DateUtil.toDateTime("2014-03-03", "00:00"),
// DateUtil.toDateTime("2014-03-09", "00:00")),
//                        new Leg(SampleVoyages.v200, SampleLocations.NEWYORK,
// SampleLocations.CHICAGO,
//                                DateUtil.toDateTime("2014-03-10", "00:00"),
// DateUtil.toDateTime("2014-03-14", "00:00")),
//                        new Leg(SampleVoyages.v200, SampleLocations.CHICAGO,
// SampleLocations.STOCKHOLM,
//                                DateUtil.toDateTime("2014-03-07", "00:00"),
// DateUtil.toDateTime("2014-03-11", "00:00")))));
//            } else {
//                // Tokyo - Hamburg - SampleLocations.STOCKHOLM, rerouting misdirected cargo
// from
//                // Tokyo
//                return Arrays.asList(new Itinerary(Arrays.asList(
//                        new Leg(SampleVoyages.v300, SampleLocations.TOKYO,
// SampleLocations.HAMBURG,
//                                DateUtil.toDateTime("2014-03-08", "00:00"),
// DateUtil.toDateTime("2014-03-12", "00:00")),
//                        new Leg(SampleVoyages.v400, SampleLocations.HAMBURG,
// SampleLocations.STOCKHOLM,
//                                DateUtil.toDateTime("2014-03-14", "00:00"),
// DateUtil.toDateTime("2014-03-15", "00:00")))));
//            }
//        };

//        applicationEvents = new SynchronousApplicationEventsStub();
// In-memory implementations of the repositories
//        handlingEventRepository = new HandlingEventRepositoryInMem();
//        cargoRepository = new CargoRepositoryInMem();
//        locationRepository = new LocationRepositoryInMem();
//        voyageRepository = new VoyageRepositoryInMem();
// Actual factories and application services, wired with stubbed or in-memory
// infrastructure
//        handlingEventFactory = new HandlingEventFactory(cargoRepository, voyageRepository,
// locationRepository);
//        cargoInspectionService = new CargoInspectionServiceImpl(applicationEvents,
// cargoRepository, handlingEventRepository);
//        handlingEventService = new DefaultHandlingEventService(handlingEventRepository,
// applicationEvents, handlingEventFactory);
//        bookingService = new BookingServiceImpl(cargoRepository, locationRepository,
// routingService);
// Circular dependency when doing synchrounous calls
//        ((SynchronousApplicationEventsStub)
// applicationEvents).setCargoInspectionService(cargoInspectionService);
//    }

@ApplicationScoped
public class RoutingServiceStub implements RoutingService {

    private static final Logger LOGGER = Logger.getLogger(RoutingServiceStub.class.getName());

    private LocationRepository locationRepository;

    private VoyageRepository voyageRepository;

    public RoutingServiceStub() {
    }

    @Inject
    public RoutingServiceStub(LocationRepository locationRepository, VoyageRepository voyageRepository) {
        this.locationRepository = locationRepository;
        this.voyageRepository = voyageRepository;
    }

    @Override
    public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
        LOGGER.log(Level.INFO, "fetchRoutesForSpecification:: {0}", routeSpecification);
        if (routeSpecification.origin().equals(SampleLocations.HONGKONG)) {
            // Hongkong - NYC - Chicago - SampleLocations.STOCKHOLM, initial routing
            return Arrays.asList(
                    new Itinerary(
                            Arrays.asList(
                                    new Leg(
                                            voyageRepository.find(
                                                    SampleVoyages.v100.getVoyageNumber()),
                                            locationRepository.find(
                                                    SampleLocations.HONGKONG.getUnLocode()),
                                            locationRepository.find(
                                                    SampleLocations.NEWYORK.getUnLocode()),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(3),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(9)),
                                    new Leg(
                                            voyageRepository.find(
                                                    SampleVoyages.v200.getVoyageNumber()),
                                            locationRepository.find(
                                                    SampleLocations.NEWYORK.getUnLocode()),
                                            locationRepository.find(
                                                    SampleLocations.CHICAGO.getUnLocode()),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(10),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(14)),
                                    new Leg(
                                            voyageRepository.find(
                                                    SampleVoyages.v200.getVoyageNumber()),
                                            locationRepository.find(
                                                    SampleLocations.CHICAGO.getUnLocode()),
                                            locationRepository.find(
                                                    SampleLocations.STOCKHOLM.getUnLocode()),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(7),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(11)))));
        } else {
            // Tokyo - Hamburg - SampleLocations.STOCKHOLM, rerouting misdirected cargo from
            // Tokyo
            return Arrays.asList(
                    new Itinerary(
                            Arrays.asList(
                                    new Leg(
                                            voyageRepository.find(
                                                    SampleVoyages.v300.getVoyageNumber()),
                                            locationRepository.find(
                                                    SampleLocations.TOKYO.getUnLocode()),
                                            locationRepository.find(
                                                    SampleLocations.HAMBURG.getUnLocode()),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(8),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(12)),
                                    new Leg(
                                            voyageRepository.find(
                                                    SampleVoyages.v400.getVoyageNumber()),
                                            locationRepository.find(
                                                    SampleLocations.HAMBURG.getUnLocode()),
                                            locationRepository.find(
                                                    SampleLocations.STOCKHOLM.getUnLocode()),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(14),
                                            LocalDateTime.now()
                                                    .minusYears(1)
                                                    .plusMonths(3)
                                                    .plusDays(15)))));
        }
    }
}
