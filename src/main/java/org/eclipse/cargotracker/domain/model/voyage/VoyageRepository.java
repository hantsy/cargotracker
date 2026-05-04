package org.eclipse.cargotracker.domain.model.voyage;

import jakarta.annotation.Nullable;

import java.util.List;

public interface VoyageRepository {

    @Nullable
    Voyage find(VoyageNumber voyageNumber);

    List<Voyage> findAll();
}
