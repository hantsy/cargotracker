package org.eclipse.cargotracker.infrastructure.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

@DataSourceDefinition(name = "java:app/jdbc/CargoTrackerDatabase", className = "org.postgresql.xa.PGXADataSource",
		url = "jdbc:postgresql://localhost:5432/cargotracker", user = "user", password = "password")
@ApplicationScoped
public class DatabaseResources {

	private static final Logger LOGGER = Logger.getLogger(DatabaseResources.class.getName());

	@Resource(lookup = "java:app/jdbc/CargoTrackerDatabase")
	DataSource dataSource;

	@PostConstruct
	public void init() {
		LOGGER.config("calling DatabaseSetup...");
		LOGGER.log(Level.INFO, "dataSource is available: {0}", dataSource != null);

		try (Connection connection = dataSource.getConnection()) {
			LOGGER.log(Level.INFO, "connect to: {0}",
					connection.getMetaData().getDatabaseProductName() + "-" + connection.getCatalog());
		}
		catch (SQLException e) {
			LOGGER.log(Level.WARNING, "database connection failed: {0}", e);
		}
	}

}
