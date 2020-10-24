package sh.isaac.solor.direct.srf.writers;

import org.apache.logging.log4j.LogManager;
import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

public class SRFConceptWriter extends TimedTaskWithProgressTracker<Void> {

    private static final int SRF_ID_INDEX = 0;
    private static final int SRF_STATUS_INDEX = 1;
    private static final int SRF_TIME_INDEX = 2;
    private static final int SRF_AUTHOR_INDEX = 3;
    private static final int SRF_MODULE_INDEX = 4;
    private static final int SRF_PATH_INDEX = 5;
    private static final int SRF_DEF_STATUS_INDEX = 6;

    private final List<String[]> conceptRecords;
    private final Semaphore writeSemaphore;
    private final List<IndexBuilderService> indexers;
    private final ImportType importType;

    public SRFConceptWriter(List<String[]> conceptRecords, Semaphore writeSemaphore,
                            String message, ImportType importType) {
        this.conceptRecords = conceptRecords;
        this.writeSemaphore = writeSemaphore;
        this.importType = importType;
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);

        updateTitle("Importing concept batch of size: " + conceptRecords.size());
        updateMessage(message);
        addToTotalWork(conceptRecords.size());
        Get.activeTasks().add(this);
    }

    protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();
    private void index(Chronology chronicle) {
        for (IndexBuilderService indexer: indexers) {
            indexer.indexNow(chronicle);
        }
    }

    @Override
    protected Void call() throws Exception {

        try{

            ConceptService conceptService = Get.conceptService();
            AssemblageService assemblageService = Get.assemblageService();
            IdentifierService identifierService = Get.identifierService();
            StampService stampService = Get.stampService();



            int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
            int identifierAssemblageNid = MetaData.UUID____SOLOR.getNid();
            int defStatusAssemblageNid = TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getNid();


            for (String[] conceptRecord : conceptRecords) {
                final Status state = Status.fromZeroOneToken(conceptRecord[SRF_STATUS_INDEX]);

                if (state == Status.INACTIVE && importType == ImportType.SNAPSHOT_ACTIVE_ONLY) {
                    continue;
                }

                UUID conceptUuid, moduleUuid, legacyDefStatus;
                TemporalAccessor accessor;

                int authorNid = identifierService.getNidForUuids(UUID.fromString(conceptRecord[SRF_AUTHOR_INDEX]));
                int pathNid = identifierService.getNidForUuids(UUID.fromString(conceptRecord[SRF_PATH_INDEX]));
                conceptUuid = UUID.fromString(conceptRecord[SRF_ID_INDEX]);
                moduleUuid = UUID.fromString(conceptRecord[SRF_MODULE_INDEX]);
                legacyDefStatus = UUID.fromString(conceptRecord[SRF_DEF_STATUS_INDEX]);
                accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(conceptRecord[SRF_TIME_INDEX]));

                long time = accessor.getLong(INSTANT_SECONDS) * 1000;

                // add to concept assemblage
                int moduleNid = identifierService.assignNid(moduleUuid);
                int legacyDefStatusNid = identifierService.assignNid(legacyDefStatus);

                ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(conceptUuid, conceptAssemblageNid);
                index(conceptToWrite);
                int conceptStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
                conceptToWrite.createMutableVersion(conceptStamp);
                conceptService.writeConcept(conceptToWrite);

                // add to legacy def status assemblage
                UUID defStatusPrimordialUuid;

                defStatusPrimordialUuid = UuidT5Generator.get(TermAux.SRF_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getPrimordialUuid(),
                        conceptRecord[SRF_ID_INDEX]);

                SemanticChronologyImpl defStatusToWrite = new SemanticChronologyImpl(VersionType.COMPONENT_NID,
                        defStatusPrimordialUuid,
                        defStatusAssemblageNid,
                        conceptToWrite.getNid());

                ComponentNidVersionImpl defStatusVersion = defStatusToWrite.createMutableVersion(conceptStamp);
                defStatusVersion.setComponentNid(legacyDefStatusNid);
                index(defStatusToWrite);
                assemblageService.writeSemanticChronology(defStatusToWrite);

                // add to sct identifier assemblage
                UUID identifierUuid = UuidT5Generator.get(MetaData.UUID____SOLOR.getPrimordialUuid(),
                        conceptRecord[SRF_ID_INDEX]);

                SemanticChronologyImpl identifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                        identifierUuid,
                        identifierAssemblageNid,
                        conceptToWrite.getNid());

                StringVersionImpl idVersion = identifierToWrite.createMutableVersion(conceptStamp);
                idVersion.setString(conceptRecord[SRF_ID_INDEX]);
                index(identifierToWrite);
                assemblageService.writeSemanticChronology(identifierToWrite);
                completedUnitOfWork();
            }


        } catch (DateTimeParseException dtepE){
            dtepE.printStackTrace();
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;

    }
}
