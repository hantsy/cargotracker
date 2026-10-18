package org.eclipse.cargotracker.domain.model.cargo;

import jakarta.annotation.Nullable;

import java.util.List;

public interface CargoRepository {

   @Nullable
   Cargo find(TrackingId trackingId);

    List<Cargo> findAll();

    void store(Cargo cargo);

    TrackingId nextTrackingId();
}
