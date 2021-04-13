package org.eclipse.cargotracker.infrastructure.routing.client;

import org.eclipse.pathfinder.api.TransitPath;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class GraphTraversalResourceClient {
    private static final Logger LOGGER =
            Logger.getLogger(GraphTraversalResourceClient.class.getName());

    @Resource(lookup = "java:app/configuration/GraphTraversalUrl")
    private String graphTraversalUrl;

    private Client jaxrsClient = null;

    @PostConstruct
    public void init() {
        this.jaxrsClient = ClientBuilder.newClient();
        try {
            Class<?> clazz =
                    Class.forName(
                            "org.eclipse.cargotracker.infrastructure.routing.client.JacksonObjectMapperContextResolver");
            jaxrsClient.register(clazz);
        } catch (ClassNotFoundException e) {
            LOGGER.log(
                    Level.WARNING,
                    "registering JacksonObjectMapperContextResolver failed: {0}",
                    e.getMessage());
            LOGGER.log(Level.INFO, "Skip this error for non-WildFly application servers.");
        }
    }

    @PreDestroy
    public void destroy() {
        this.jaxrsClient.close();
    }

    public List<TransitPath> findShortestPath(String origin, String destination) {
        LOGGER.log(
                Level.FINE,
                "fetch the shortest paths from external resource: {0}",
                graphTraversalUrl);
        WebTarget graphTraversalResource = jaxrsClient.target(graphTraversalUrl);
        // @formatter:off
        return graphTraversalResource
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<>() {});
        // @formatter:on
    }
}
