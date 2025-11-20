package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JpaVoyageRepository implements VoyageRepository, Serializable {

	private static final Logger logger = Logger.getLogger(JpaVoyageRepository.class.getName());

	private EntityManager entityManager;

	// no-args constructor required by CDI
	public JpaVoyageRepository() {
	}

	@Inject
	public JpaVoyageRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Voyage find(VoyageNumber voyageNumber) {
		return  entityManager.createNamedQuery("Voyage.findByVoyageNumber", Voyage.class)
				.setParameter("voyageNumber", voyageNumber)
				.getSingleResultOrNull();
	}

	@Override
	public List<Voyage> findAll() {
		return entityManager.createNamedQuery("Voyage.findAll", Voyage.class).getResultList();
	}

}
