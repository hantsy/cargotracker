package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 *
 * <p>Files that fail to parse are moved into a separate directory, successful files are deleted.
 * <p>
 * An example using ManagedScheduledExecutorService
 * <code>
 *
 * @ApplicationScoped public class UploadDirectoryScanner {
 * private final JobOperator jobOperator = BatchRuntime.getJobOperator();
 * private ManagedScheduledExecutorService scheduler;
 * <p>
 * // No-arg constructor required by CDI
 * public UploadDirectoryScanner() {
 * }
 * @Inject public UploadDirectoryScanner(ManagedScheduledExecutorService scheduler) {
 * this.scheduler = scheduler;
 * }
 * <p>
 * public void init(@Observes Startup startup) {
 * scheduler.scheduleAtFixedRate(this::processFiles, 100, 120, TimeUnit.SECONDS);
 * }
 * <p>
 * public void processFiles() {
 * jobOperator.start("EventFilesProcessorJob", null);
 * }
 * }
 * </code>
 */
@ApplicationScoped
public class UploadDirectoryScanner {
    private final JobOperator jobOperator = BatchRuntime.getJobOperator();

    public void init(@Observes Startup startup) {
        // The concurrency schedule requires a manual call to start up scheduled task.
        this.processFiles();
    }

    // see: https://github.com/jakartaee/concurrency/issues/624
    // The @Asynchronous(runAt...) not started automatically when the bean is initialized.
    @Asynchronous(runAt = {@Schedule(cron = "*/2 * * * *")})
    public void processFiles() {
        jobOperator.start("EventFilesProcessorJob", null);
    }
}