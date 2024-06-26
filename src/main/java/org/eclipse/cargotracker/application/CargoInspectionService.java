package org.eclipse.cargotracker.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

public interface CargoInspectionService {

    /**
     * Inspect cargo and send relevant notifications to interested parties, for example if a cargo
     * has been misdirected, or unloaded at the final destination.
     */
    void inspectCargo(@NotNull(message = "Tracking ID is required") @Valid TrackingId trackingId);
}
