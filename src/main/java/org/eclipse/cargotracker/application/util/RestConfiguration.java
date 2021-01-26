package org.eclipse.cargotracker.application.util;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/** JAX-RS configuration. */
@ApplicationPath("rest")
public class RestConfiguration extends Application {

  //	public RestConfiguration() {
  //		// Resources
  //		packages(new String[] { HandlingReportService.class.getPackage().getName(),
  //				GraphTraversalService.class.getPackage().getName(),
  //				CargoMonitoringService.class.getPackage().getName() });
  //		// Enable Bean Validation error messages.
  //		property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
  //		// Providers - JSON.
  //		register(new MoxyJsonFeature());
  //		register(new JsonMoxyConfigurationContextResolver()); // TODO [Jakarta EE 8] See if this can
  // be removed.
  //	}
}
