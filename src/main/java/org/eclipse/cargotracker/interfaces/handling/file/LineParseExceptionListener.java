package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.api.chunk.listener.SkipReadListener;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
@Named("LineParseExceptionListener")
public class LineParseExceptionListener implements SkipReadListener {

    private static final Logger LOGGER = Logger.getLogger(LineParseExceptionListener.class.getName());
    private static final String FAILED_DIRECTORY = "failed_directory";

    @Inject
    private JobContext jobContext;

    @Override
    public void onSkipReadItem(Exception e) throws Exception {
        Path failedDirectory = Paths.get(jobContext.getProperties().getProperty(FAILED_DIRECTORY));

        if (!Files.exists(failedDirectory)) {
            Files.createDirectories(failedDirectory);
        }

        EventLineParseException parseException = (EventLineParseException) e;

        LOGGER.log(Level.WARNING, "Problem parsing event file line", parseException);

        Path failedFile = failedDirectory.resolve(
                "failed_"
                        + jobContext.getJobName()
                        + "_"
                        + jobContext.getInstanceId()
                        + ".csv");

        try {
            Files.writeString(failedFile, parseException.getLine(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to write file:{0}, root cause: {1}", new Object[]{failedFile.toString(), exception.getMessage()});
        }
    }
}
