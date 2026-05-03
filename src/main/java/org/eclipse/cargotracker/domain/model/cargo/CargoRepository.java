package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.data.repository.By;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Repository(dataStore = "CargoTrackerUnit")
@Transactional
public interface CargoRepository {

    @Find
    Cargo find(TrackingId trackingId);

    @Find
    List<Cargo> findAll();

    @Save
    void store(Cargo cargo);

    default TrackingId nextTrackingId(){
        String random = UUID.randomUUID().toString().toUpperCase();

        return new TrackingId(random.substring(0, random.indexOf("-")));
    }
}
