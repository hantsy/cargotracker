## Summary

Extract test infrastructure improvements from the convert-embeddable-to-records branch into a focused PR against master. No domain model changes are included.

## Changes

### 1. Test naming convention (13 files)
Renamed all Arquillian-based integration tests from XXXTest to XXXIT to follow the Maven Failsafe naming convention:
- @ExtendWith(ArquillianExtension.class) + @Tag("arqtest") → @ArquillianTest
- Removed unused imports (ArquillianExtension, Tag, ExtendWith)
- Updated class names, LOGGER references, and deployment war names

### 2. Maven Surefire/Failsafe filter changes
- **Surefire (unit tests)**: <excludedGroups>arqtest</excludedGroups> → <excludes><exclude>**/*IT.java</exclude></excludes>
- **Failsafe (integration tests)**: <include>**/*Test.java</include> + <groups>arqtest</groups> → <include>**/*IT.java</include> (no groups)

### 3. TxUtil test helper (already on master)
The TxUtil.java class for explicit transaction management in integration tests already existed on master. Only the test files now use @ArquillianTest for cleaner Arquillian test setup.

## Files renamed (13)
ApplicationEventsTest → ApplicationEventsIT, BookingServiceTest → BookingServiceIT, CargoRepositoryTest → CargoRepositoryIT, HandlingEventRepositoryTest → HandlingEventRepositoryIT, LocationRepositoryTest → LocationRepositoryIT, VoyageRepositoryTest → VoyageRepositoryIT, CargoMonitoringServiceTest → CargoMonitoringServiceIT, RealtimeCargoTrackingWebSocketServiceTest → RealtimeCargoTrackingWebSocketServiceIT, RealtimeCargoTrackingSseServiceTest → RealtimeCargoTrackingSseServiceIT, EventFilesProcessorJobTest → EventFilesProcessorJobIT, EventFilesProcessorJobWithInvalidFileTest → EventFilesProcessorJobWithInvalidFileIT, HandlingReportServiceTest → HandlingReportServiceIT, CargoLifecycleScenarioTest → CargoLifecycleScenarioIT

Co-Authored-By: Oz <oz-agent@warp.dev>
