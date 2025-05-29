package org.eclipse.cargotracker.infrastructure.routing.client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.pathfinder.api.TransitPath;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class GraphTraversalResourceClient {

	private static final Logger LOGGER = Logger.getLogger(GraphTraversalResourceClient.class.getName());

	@Resource(lookup = "java:app/configuration/GraphTraversalUrl")
	private String graphTraversalUrl;

	private Client jaxrsClient = null;

	@PostConstruct
	public void init() {
		this.jaxrsClient = ClientBuilder.newClient();
	}

	@PreDestroy
	public void destroy() {
		this.jaxrsClient.close();
	}

	public List<TransitPath> findShortestPath(String origin, String destination) {
		LOGGER.log(Level.FINE, "fetch the shortest paths from external resource: {0}", graphTraversalUrl);
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
