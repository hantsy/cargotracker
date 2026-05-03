package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository(dataStore = "CargoTrackerUnit")
@Transactional
public interface VoyageRepository {

    @Find
    Voyage find(VoyageNumber voyageNumber);

    @Find
    List<Voyage> findAll();
}
