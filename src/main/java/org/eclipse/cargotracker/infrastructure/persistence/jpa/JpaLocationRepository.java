package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JpaLocationRepository implements LocationRepository, Serializable {

	private static final Logger logger = Logger.getLogger(JpaLocationRepository.class.getName());

	private EntityManager entityManager;

	// no-args constructor required by CDI
	public JpaLocationRepository() {
	}

	@Inject
	public JpaLocationRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Location find(UnLocode unLocode) {
		return entityManager.createNamedQuery("Location.findByUnLocode", Location.class)
				.setParameter("unLocode", unLocode)
				.getSingleResultOrNull();
	}

	@Override
	public List<Location> findAll() {
		return entityManager.createNamedQuery("Location.findAll", Location.class).getResultList();
	}

}
