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

	private static final String ARCHIVE_DIRECTORY = "archive_directory";

	@Inject
	private JobContext jobContext;

	@Inject
	private ApplicationEvents applicationEvents;

	@Inject
	private Logger logger;

	@Override
	public void open(Serializable checkpoint) throws Exception {
		Path archiveDirectory = Paths.get(jobContext.getProperties().getProperty(ARCHIVE_DIRECTORY));

		if (Files.notExists(archiveDirectory)) {
			Files.createDirectories(archiveDirectory);
		}
	}

	@Override
	@Transactional
	public void writeItems(List<Object> items) throws Exception {
		Path archivePath = Paths.get(jobContext.getProperties().getProperty(ARCHIVE_DIRECTORY),
				"archive_" + jobContext.getJobName() + "_" + jobContext.getInstanceId() + ".csv");

		List<String> lines = new ArrayList<>();
		items.forEach(item -> {
			if (item instanceof HandlingEventRegistrationAttempt attempt) {
				applicationEvents.receivedHandlingEventRegistrationAttempt(attempt);
				lines.add(DateUtil.toString(attempt.getRegistrationTime()) + ","
						+ DateUtil.toString(attempt.getCompletionTime()) + "," + attempt.getTrackingId() + ","
						+ attempt.getVoyageNumber() + "," + attempt.getUnLocode() + "," + attempt.getType());
			}
		});

		Files.write(archivePath, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		logger.log(Level.INFO, "write to file: {}", archivePath);
	}

}
