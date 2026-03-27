package org.eclipse.cargotracker.interfaces.handling.file;

import jakarta.inject.Inject;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.ApplicationEventsStub;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.cargotracker.Deployments.addApplicationBase;
import static org.eclipse.cargotracker.Deployments.addDomainModels;
import static org.eclipse.cargotracker.Deployments.addExtraJars;
import static org.eclipse.cargotracker.Deployments.addInfraBase;

@ExtendWith(ArquillianExtension.class)
@Tag("arqtest")
public class EventFilesProcessorJobTest {

    private static final Logger LOGGER = Logger.getLogger(EventFilesProcessorJobTest.class.getName());

    @Inject
    private ApplicationEventsStub applicationEventsStub;

    private final Path uploadDir = Paths.get("/tmp/uploads");
    private final Path archiveDir = Paths.get("/tmp/archive");
    private final Path failedDir = Paths.get("/tmp/failed");

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-EventFilesProcessorJobTest.war");

        addExtraJars(war);
        addDomainModels(war);
        addInfraBase(war);
        addApplicationBase(war);

        war.addClass(EventItemReader.class)
                .addClass(EventItemWriter.class)
                .addClass(EventFilesCheckpoint.class)
                .addClass(EventLineParseException.class)
                .addClass(LineParseExceptionListener.class)
                .addClass(FileProcessorJobListener.class)
                .addClass(EventFilesProcessorJobTrigger.class)
                .addClass(ApplicationEventsStub.class)
                .addClass(ApplicationEvents.class)
                .addClass(HandlingEventRegistrationAttempt.class)
                .addAsResource("META-INF/batch-jobs/EventFilesProcessorJob.xml")
                .addAsResource("test-jboss-logging.properties", "jboss-logging.properties")
                .addAsWebInfResource("test-web.xml", "web.xml")
                .addAsWebInfResource("test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    @BeforeEach
    public void setup() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        if (!Files.exists(archiveDir)) {
            Files.createDirectories(archiveDir);
        }
        if (!Files.exists(failedDir)) {
            Files.createDirectories(failedDir);
        }

        // Clean directories
        cleanDirectory(uploadDir);
        cleanDirectory(archiveDir);
        cleanDirectory(failedDir);

        applicationEventsStub.clear();
    }

    private void cleanDirectory(Path path) throws IOException {
        try (var stream = Files.list(path)) {
            stream.forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to delete file: " + p, e);
                }
            });
        }
    }

    @Test
    public void testProcessValidFile() throws Exception {
        String completionTime = DateUtil.toString(LocalDateTime.now());
        List<String> lines = List.of(
                completionTime + ",A001,V001,CNSHA,LOAD",
                completionTime + ",A002,V002,CNCAN,UNLOAD",
                completionTime + ",A003,V003,SESTO,RECEIVE"
        );
        Files.write(uploadDir.resolve("events.csv"), lines);

        await().atMost(15, TimeUnit.SECONDS).until(() -> applicationEventsStub.getAttempts().size() == 3);

        List<HandlingEventRegistrationAttempt> attempts = applicationEventsStub.getAttempts();
        assertThat(attempts).hasSize(3);
        assertThat(attempts.get(0).trackingId()).isEqualTo(new TrackingId("A001"));
        assertThat(attempts.get(0).type()).isEqualTo(HandlingEvent.Type.LOAD);
        assertThat(attempts.get(1).trackingId()).isEqualTo(new TrackingId("A002"));
        assertThat(attempts.get(1).type()).isEqualTo(HandlingEvent.Type.UNLOAD);
        assertThat(attempts.get(2).trackingId()).isEqualTo(new TrackingId("A003"));
        assertThat(attempts.get(2).type()).isEqualTo(HandlingEvent.Type.RECEIVE);
    }
}
