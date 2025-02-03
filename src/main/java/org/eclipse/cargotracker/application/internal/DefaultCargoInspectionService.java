package org.eclipse.cargotracker.application.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Transactional
public class DefaultCargoInspectionService implements CargoInspectionService {

    private static final Logger LOGGER =
            Logger.getLogger(DefaultCargoInspectionService.class.getName());

    private ApplicationEvents applicationEvents;
    private CargoRepository cargoRepository;
    private HandlingEventRepository handlingEventRepository;
    private Event<Cargo> cargoInspected;

    // no-args constructor required by CDI
    public DefaultCargoInspectionService() {}

    @Inject
    public DefaultCargoInspectionService(
            ApplicationEvents applicationEvents,
            CargoRepository cargoRepository,
            HandlingEventRepository handlingEventRepository,
            @CargoInspected Event<Cargo> cargoInspected) {
        this.applicationEvents = applicationEvents;
        this.cargoRepository = cargoRepository;
        this.handlingEventRepository = handlingEventRepository;
        this.cargoInspected = cargoInspected;
    }

    @Override
    public void inspectCargo(TrackingId trackingId) {
        Cargo cargo = cargoRepository.find(trackingId);

        if (cargo == null) {
            LOGGER.log(Level.WARNING, "Can't inspect non-existing cargo {0}", trackingId);
            return;
        }

        HandlingHistory handlingHistory =
                handlingEventRepository.lookupHandlingHistoryOfCargo(trackingId);

        cargo.deriveDeliveryProgress(handlingHistory);

        if (cargo.getDelivery().misdirected()) {
            applicationEvents.cargoWasMisdirected(cargo);
        }

        if (cargo.getDelivery().isUnloadedAtDestination()) {
            applicationEvents.cargoHasArrived(cargo);
        }

        cargoRepository.store(cargo);

        cargoInspected.fire(cargo);
    }
}
