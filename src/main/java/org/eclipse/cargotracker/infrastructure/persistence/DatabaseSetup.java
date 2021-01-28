package org.eclipse.cargotracker.infrastructure.persistence;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// H2 will cause deployment failed on payara server.
//
// @DataSourceDefinition(
//        name = "java:app/jdbc/CargoTrackerDatabase",
//        className = "org.h2.jdbcx.JdbcDataSource",
//        url = "jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE"
// )
@DataSourceDefinition(
        name = "java:app/jdbc/CargoTrackerDatabase",
        className = "org.postgresql.xa.PGXADataSource",
        url = "jdbc:postgresql://localhost:5432/cargotracker",
        user = "user",
        password = "password")
@Singleton
@Startup
public class DatabaseSetup {
    private static final Logger LOGGER = Logger.getLogger(DatabaseSetup.class.getName());

    @Resource(lookup = "java:app/jdbc/CargoTrackerDatabase")
    DataSource dataSource;

    @PostConstruct
    public void init() {
        LOGGER.info("calling DatabaseSetup...");
        LOGGER.log(Level.INFO, "dataSource is not null: {0}", dataSource != null);

        try (Connection connection = dataSource.getConnection()) {
            LOGGER.log(
                    Level.INFO,
                    "connect to: {0}",
                    connection.getMetaData().getDatabaseProductName()
                            + "-"
                            + connection.getCatalog());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
