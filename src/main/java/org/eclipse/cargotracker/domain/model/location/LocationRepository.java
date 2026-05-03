package org.eclipse.cargotracker.domain.model.location;

import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository(dataStore = "CargoTrackerUnit")
@Transactional
public interface LocationRepository {

    @Find
    Location find(UnLocode unLocode);

    @Find
    List<Location> findAll();
}
