package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;

import java.io.Serializable;

@ApplicationScoped
public class JpaHandlingEventRepository implements HandlingEventRepository, Serializable {

	private EntityManager entityManager;

	// no-args constructor required by CDI
	public JpaHandlingEventRepository() {
	}

	@Inject
	public JpaHandlingEventRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void store(HandlingEvent event) {
		entityManager.persist(event);
	}

	@Override
	public HandlingHistory lookupHandlingHistoryOfCargo(TrackingId trackingId) {
		return new HandlingHistory(entityManager.createNamedQuery("HandlingEvent.findByTrackingId", HandlingEvent.class)
			.setParameter("trackingId", trackingId)
			.getResultList());
	}

}
