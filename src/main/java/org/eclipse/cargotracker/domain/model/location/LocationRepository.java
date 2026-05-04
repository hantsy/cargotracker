package org.eclipse.cargotracker.domain.model.location;

import jakarta.annotation.Nullable;

import java.util.List;

public interface LocationRepository {

   @Nullable
   Location find(UnLocode unLocode);

    List<Location> findAll();
}
