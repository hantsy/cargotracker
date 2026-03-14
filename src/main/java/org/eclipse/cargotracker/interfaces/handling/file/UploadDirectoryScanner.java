package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 *
 * <p>Files that fail to parse are moved into a separate directory, successful files are deleted.
 */
@ApplicationScoped
public class UploadDirectoryScanner {
    private final JobOperator jobOperator = BatchRuntime.getJobOperator();
    private ManagedScheduledExecutorService scheduler;

    // No-arg constructor required by CDI
    public UploadDirectoryScanner() {
    }

    @Inject
    public UploadDirectoryScanner( ManagedScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::processFiles, 100, 120, TimeUnit.SECONDS);
    }

    // see: https://github.com/jakartaee/concurrency/issues/624
    // The @Asynchronous(runAt...) not started automatically when the bean is initialized.
    // @Asynchronous(runAt = {@Schedule(cron = "*/2 * * * *")}) // In production, run every fifteen minutes
    public void processFiles() {
        jobOperator.start("EventFilesProcessorJob", null);
    }
}