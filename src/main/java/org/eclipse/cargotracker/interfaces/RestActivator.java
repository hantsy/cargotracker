package org.eclipse.cargotracker.interfaces;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** JAX-RS configuration. */
@ApplicationPath("rest")
public class RestActivator extends Application {}
