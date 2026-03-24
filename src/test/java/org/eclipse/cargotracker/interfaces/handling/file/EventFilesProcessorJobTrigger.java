package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.operations.JobOperator;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import org.eclipse.cargotracker.interfaces.booking.sse.CargoInspectedSseEventStub;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class EventFilesProcessorJobTrigger {
    private static final Logger LOGGER = Logger.getLogger(EventFilesProcessorJobTrigger.class.getName());

    @Inject
    private JobOperator jobOperator;

    @Inject
    ManagedScheduledExecutorService scheduledExecutorService;

    public void init(@Observes Startup startup) {
        LOGGER.log(Level.INFO, "Jakarta Batch Job: EventFilesProcessorJob was launched after 5 seconds: {0}", startup);
        scheduledExecutorService.schedule(this::processFiles, 5000, TimeUnit.MILLISECONDS);
    }

    public void processFiles() {
        jobOperator.start("EventFilesProcessorJob", null);
    }
}
