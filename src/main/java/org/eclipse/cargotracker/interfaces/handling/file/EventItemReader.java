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

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
@Named("EventItemReader")
public class EventItemReader extends AbstractItemReader {

    private static final String UPLOAD_DIRECTORY = "upload_directory";

    @Inject private Logger logger;
    @Inject private JobContext jobContext;
    
    private EventFilesCheckpoint checkpoint;
    private RandomAccessFile currentFile;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        Path uploadDirectory = Paths.get(jobContext.getProperties().getProperty(UPLOAD_DIRECTORY));

        if (checkpoint == null) {
            this.checkpoint = new EventFilesCheckpoint();
            logger.log(Level.INFO, "Scanning upload directory: {0}", uploadDirectory);

            if (Files.notExists(uploadDirectory)) {
                logger.log(Level.INFO, "Upload directory does not exist, creating it");
                Files.createDirectories(uploadDirectory);
            } else {
                this.checkpoint.setFiles(Files.list(uploadDirectory).toList());
            }
        } else {
            logger.log(Level.INFO, "Starting from previous checkpoint");
            this.checkpoint = (EventFilesCheckpoint) checkpoint;
        }

        Path file = this.checkpoint.currentFile();

        if (file == null) {
            logger.log(Level.INFO, "No files to process");
            currentFile = null;
        } else {
            currentFile = new RandomAccessFile(file.toFile(), "r");
            logger.log(Level.INFO, "Processing file: {0}", file);
            currentFile.seek(this.checkpoint.getFilePointer());
        }
    }

    @Override
    public Object readItem() throws Exception {
        if (currentFile != null) {
            String line = currentFile.readLine();

            if (line != null) {
                this.checkpoint.setFilePointer(currentFile.getFilePointer());
                return parseLine(line);
            } else {
                logger.log(
                        Level.INFO,
                        "Finished processing file, deleting: {0}",
                        this.checkpoint.currentFile());
                currentFile.close();
                if (Files.deleteIfExists(this.checkpoint.currentFile())) {
                    logger.log(Level.INFO, "File was deleted");
                }
                Path nextFile = this.checkpoint.nextFile();

                if (nextFile == null) {
                    logger.log(Level.INFO, "No more files to process");
                    return null;
                } else {
                    currentFile = new RandomAccessFile(nextFile.toFile(), "r");
                    logger.log(Level.INFO, "Processing file: {0}", nextFile);
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

        LocalDateTime completionTime;
        try {
            completionTime = DateUtil.toDateTime(result[0]);
        } catch (DateTimeParseException e) {
            throw new EventLineParseException("Cannot parse completion time", e, line);
        }

        TrackingId trackingId;
        try {
            trackingId = new TrackingId(result[1]);
        } catch (NullPointerException e) {
            throw new EventLineParseException("Cannot parse tracking ID", e, line);
        }

        VoyageNumber voyageNumber = result[2].isEmpty() ? null : new VoyageNumber(result[2]);

        UnLocode unLocode;
        try {
            unLocode = new UnLocode(result[3]);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new EventLineParseException("Cannot parse UN location code", e, line);
        }

        HandlingEvent.Type eventType;
        try {
            eventType = HandlingEvent.Type.valueOf(result[4]);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new EventLineParseException("Cannot parse event type", e, line);
        }

        return new HandlingEventRegistrationAttempt(
                LocalDateTime.now(),
                completionTime,
                trackingId,
                voyageNumber,
                eventType,
                unLocode);
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return this.checkpoint;
    }
}
