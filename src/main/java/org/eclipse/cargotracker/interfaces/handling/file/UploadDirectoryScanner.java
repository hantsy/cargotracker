package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.operations.JobOperator;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 *
 * <p>Files that fail to parse are moved into a separate directory, successful files are deleted.
 */
@ApplicationScoped
public class UploadDirectoryScanner {

    @Inject private ManagedScheduledExecutorService scheduler;

    @Inject private JobOperator jobOperator;

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::processFiles, 0, 15, TimeUnit.MINUTES);
    }

    @Transactional
    public void processFiles() {
        jobOperator.start("EventFilesProcessorJob", null);
    }
}
