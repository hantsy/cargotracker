package org.eclipse.cargotracker.domain.model.handling;

import jakarta.data.repository.By;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.transaction.Transactional;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;

import java.util.List;

@Repository(dataStore = "CargoTrackerUnit")
@Transactional
public interface HandlingEventRepository {

    @Save
    void store(HandlingEvent event);

    @Find
    List<HandlingEvent> findByCargoTrackingId(@By("cargo.trackingId") TrackingId trackingId);

    default HandlingHistory lookupHandlingHistoryOfCargo(TrackingId trackingId){
        return new HandlingHistory(findByCargoTrackingId(trackingId));
    }
}
