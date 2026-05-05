package org.eclipse.cargotracker.domain.model.location;

import jakarta.annotation.Nullable;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Repository(dataStore = "CargoTrackerUnit")
@Transactional
public interface LocationRepository {

    default Location find(UnLocode unLocode) {
        return findByUnLocode(unLocode).orElse(null);
    }

    @Find
    Optional<Location> findByUnLocode(UnLocode unLocode);

    @Find
    List<Location> findAll();
}
