package org.eclipse.cargotracker.interfaces.tracking.web;

import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.omnifaces.util.Messages;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

/**
 * Backing bean for tracking cargo. This interface sits immediately on top of the domain layer,
 * unlike the booking interface which has a facade and supporting DTOs in between.
 *
 * <p>An adapter class, designed for the tracking use case, is used to wrap the domain model to make
 * it easier to work with in a web page rendering context. We do not want to apply view rendering
 * constraints to the design of our domain model and the adapter helps us shield the domain model
 * classes where needed.
 *
 * <p>In some very simplistic cases, it is fine to not use even an adapter.
 */
@Named("track")
@ViewScoped
public class Track implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private CargoRepository cargoRepository;
    @Inject private HandlingEventRepository handlingEventRepository;

    private String trackingId;
    private CargoTrackingViewAdapter cargo;

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        if (trackingId != null) {
            trackingId = trackingId.trim();
        }

        this.trackingId = trackingId;
    }

    public CargoTrackingViewAdapter getCargo() {
        return cargo;
    }

    public void setCargo(CargoTrackingViewAdapter cargo) {
        this.cargo = cargo;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void onTrackById() {
        Cargo cargo = cargoRepository.find(new TrackingId(trackingId));

        if (cargo != null) {
            List<HandlingEvent> handlingEvents =
                    handlingEventRepository
                            .lookupHandlingHistoryOfCargo(new TrackingId(trackingId))
                            .getDistinctEventsByCompletionTime();
            this.cargo = new CargoTrackingViewAdapter(cargo, handlingEvents);
        } else {
            Messages.addFlashGlobalError("Cargo with tracking ID: {0} not found.", trackingId);
            //            FacesContext context = FacesContext.getCurrentInstance();
            //            FacesMessage message =
            //                    new FacesMessage("Cargo with tracking ID: " + trackingId + " not
            // found.");
            //            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            //            context.addMessage(null, message);
            this.cargo = null;
        }
    }
}
