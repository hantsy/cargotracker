package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

import java.io.BufferedReader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Dependent
@Named("EventItemReader")
public class EventItemReader extends AbstractItemReader {

    private static final Logger LOGGER = Logger.getLogger(EventItemReader.class.getName());
    private static final String UPLOAD_DIRECTORY = "upload_directory";

    @Inject
    private JobContext jobContext;
    private EventFilesCheckpoint checkpoint;
    private BufferedReader currentReader;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        Path uploadDirectory = Paths.get(jobContext.getProperties().getProperty(UPLOAD_DIRECTORY));

        if (checkpoint == null) {
            this.checkpoint = new EventFilesCheckpoint();
            LOGGER.log(Level.INFO, "Scanning upload directory: {0}", uploadDirectory);

            if (!Files.exists(uploadDirectory)) {
                LOGGER.log(Level.INFO, "Upload directory does not exist, creating it");
                Files.createDirectories(uploadDirectory);
            } else {
                try (Stream<Path> stream = Files.list(uploadDirectory)) {
                    List<Path> files = stream.filter(Files::isRegularFile).toList();
                    LOGGER.log(Level.INFO, "Found files in Upload directory: {0}", new Object[]{files});
                    this.checkpoint.setPaths(files);
                }
            }
        } else {
            LOGGER.log(Level.INFO, "Starting from previous checkpoint");
            this.checkpoint = (EventFilesCheckpoint) checkpoint;
        }

        Path file = this.checkpoint.currentFile();

        if (file == null) {
            LOGGER.log(Level.INFO, "No files to process");
            currentReader = null;
        } else {
            currentReader = Files.newBufferedReader(file);
            LOGGER.log(Level.INFO, "Processing file: {0}", file);
            long linesToSkip = this.checkpoint.getLineIndex();
            for (long i = 0; i < linesToSkip; i++) {
                currentReader.readLine();
            }
        }
    }

    @Override
    public Object readItem() throws Exception {
        if (currentReader != null) {
            String line = currentReader.readLine();

            if (line != null) {
                this.checkpoint.setLineIndex(this.checkpoint.getLineIndex() + 1);
                return parseLine(line);
            } else {
                LOGGER.log(
                        Level.INFO,
                        "Finished processing file, deleting: {0}",
                        this.checkpoint.currentFile());
                currentReader.close();
                Files.delete(this.checkpoint.currentFile());
                LOGGER.log(Level.INFO, "File was deleted");

                Path nextFile = this.checkpoint.nextFile();

                if (nextFile == null) {
                    LOGGER.log(Level.INFO, "No more files to process");
                    return null;
                } else {
                    currentReader = Files.newBufferedReader(nextFile);
                    LOGGER.log(Level.INFO, "Processing file: {0}", nextFile);
                    return readItem();
                }
            }
        } else {
            return null;
        }
    }

    private Object parseLine(String line) throws EventLineParseException {
        String[] result = line.split(",");

        if (result.length != 5) {
            throw new EventLineParseException("Wrong number of data elements", line);
        }

        LocalDateTime completionTime = null;

        try {
            completionTime = DateUtil.toDateTime(result[0]);
        } catch (DateTimeParseException e) {
            throw new EventLineParseException("Cannot parse completion time", e, line);
        }

        TrackingId trackingId = null;

        try {
            trackingId = new TrackingId(result[1]);
        } catch (NullPointerException e) {
            throw new EventLineParseException("Cannot parse tracking ID", e, line);
        }

        VoyageNumber voyageNumber = null;

        try {
            if (!result[2].isEmpty()) {
                voyageNumber = new VoyageNumber(result[2]);
            }
        } catch (NullPointerException e) {
            throw new EventLineParseException("Cannot parse voyage number", e, line);
        }

        UnLocode unLocode = null;

        try {
            unLocode = new UnLocode(result[3]);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new EventLineParseException("Cannot parse UN location code", e, line);
        }

        HandlingEvent.Type eventType = null;

        try {
            eventType = HandlingEvent.Type.valueOf(result[4]);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new EventLineParseException("Cannot parse event type", e, line);
        }

        HandlingEventRegistrationAttempt attempt =
                new HandlingEventRegistrationAttempt(
                        LocalDateTime.now(),
                        completionTime,
                        trackingId,
                        voyageNumber,
                        eventType,
                        unLocode);

        return attempt;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return this.checkpoint;
    }
}
