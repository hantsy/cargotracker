package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;

@Repository(dataStore = "CargoTrackerUnit")
@Transactional
public interface VoyageRepository {

    default Voyage find(VoyageNumber voyageNumber){
        return findByVoyageNumber(voyageNumber).orElse(null);
    }

    @Find
    Optional<Voyage> findByVoyageNumber(VoyageNumber voyageNumber);

    @Find
    List<Voyage> findAll();
}
