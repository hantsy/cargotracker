package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
@Named("EventItemWriter")
public class EventItemWriter extends AbstractItemWriter {
    private static final Logger LOGGER = Logger.getLogger(EventItemWriter.class.getName());
    private static final String ARCHIVE_DIRECTORY = "archive_directory";

    @Inject
    private JobContext jobContext;

    @Inject
    private ApplicationEvents applicationEvents;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        Path archiveDirectory = Paths.get(jobContext.getProperties().getProperty(ARCHIVE_DIRECTORY));

        if (!Files.exists(archiveDirectory)) {
            Files.createDirectories(archiveDirectory);
        }
    }

    @Override
    @Transactional
    public void writeItems(List<Object> items) throws Exception {
        Path archiveFile = Paths.get(
                jobContext.getProperties().getProperty(ARCHIVE_DIRECTORY),
                "archive_" + jobContext.getJobName() + "_" + jobContext.getInstanceId() + ".csv");

        List<String> lines = new ArrayList<>();
        items.stream().map(item -> (HandlingEventRegistrationAttempt) item)
                .forEachOrdered(attempt -> {
                    applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
                    var line = DateUtil.toString(attempt.registrationTime())
                            + ","
                            + DateUtil.toString(attempt.completionTime())
                            + ","
                            + attempt.trackingId()
                            + ","
                            + attempt.voyageNumber()
                            + ","
                            + attempt.unLocode()
                            + ","
                            + attempt.type();
                    lines.add(line);
                });

        try {
            Files.write(archiveFile, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write file:{0}, root cause: {1}", new Object[]{archiveFile.toString(), e.getMessage()});
        }
    }
}
