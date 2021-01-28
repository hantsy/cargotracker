package org.eclipse.cargotracker.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/** JAX-RS configuration. */
@ApplicationPath("rest")
public class BookingServiceTestRestConfiguration extends Application {

    //    public BookingServiceTestRestConfiguration() {
    //        // Resources
    //        packages(new String[]{GraphTraversalService.class.getPackage().getName()});
    //        // Providers - JSON.
    //        register(new MoxyJsonFeature());
    //        register(new JsonMoxyConfigurationContextResolver()); // TODO [Jakarta EE 8] See if
    // this
    // can be removed.
    //    }
}
