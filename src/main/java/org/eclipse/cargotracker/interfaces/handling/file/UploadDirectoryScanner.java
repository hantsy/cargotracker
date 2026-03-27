package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.operations.JobOperator;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 */
@ApplicationScoped
public class UploadDirectoryScanner {

    @Inject
    private JobOperator jobOperator;

    public void init(@Observes Startup startup) {
        // The concurrency schedule requires a manual call to start up scheduled task.
        this.processFiles();
    }

    // see: https://github.com/jakartaee/concurrency/issues/624
    // The @Asynchronous(runAt...) not started automatically when the bean is initialized.
    @Asynchronous(runAt = {
            @Schedule(cron = "*/2 * * * *"), //every 2 min
            @Schedule(cron = "*/5 * * * * *") // every 5 seconds for testing purpose
    })
    public void processFiles() {
        jobOperator.start("EventFilesProcessorJob", null);
    }
}
