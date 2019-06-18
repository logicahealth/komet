package sh.isaac.solor.direct.srf.writers;

import org.apache.logging.log4j.LogManager;
import org.apache.mahout.math.Arrays;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Rf2RelationshipImpl;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportStreamType;
import sh.isaac.solor.direct.ImportType;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

public class SRFRelationshipWriter extends TimedTaskWithProgressTracker<Void> {

    private static final int SRF_ID_INDEX = 0;
    private static final int SRF_STATUS_INDEX = 1;
    private static final int SRF_TIME_INDEX = 2;
    private static final int SRF_AUTHOR_INDEX = 3;
    private static final int SRF_MODULE_INDEX = 4;
    private static final int SRF_PATH_INDEX = 5;
    private static final int SRF_SOURCE_ID_INDEX = 6;
    private static final int SRF_DESTINATION_ID_INDEX = 7;
    private static final int SRF_RELATIONSHIP_GROUP_INDEX = 8;
    private static final int SRF_TYPE_ID_INDEX = 9;
    private static final int SRF_CHARACTERISTIC_TYPE_ID_INDEX = 10;
    private static final int SRF_MODIFIER_ID_INDEX = 11;

    private final List<String[]> relationshipRecords;
    private final Semaphore writeSemaphore;
    private final ImportType importType;
    private final ImportStreamType streamType;

    protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();


    public SRFRelationshipWriter(List<String[]> relationshipRecords, Semaphore writeSemaphore,  String message,
                                 ImportType importType, ImportStreamType streamType) {
        this.relationshipRecords = relationshipRecords;
        this.writeSemaphore = writeSemaphore;
        this.importType = importType;
        this.streamType = streamType;

        updateTitle( "Importing relationship batch of size: " + relationshipRecords.size());
        updateMessage(message);
        addToTotalWork(relationshipRecords.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try{

            AssemblageService assemblageService = Get.assemblageService();
            IdentifierService identifierService = Get.identifierService();
            StampService stampService = Get.stampService();
            int relAssemblageNid;

            if (this.streamType == ImportStreamType.SRF_STATED_RELATIONSHIP) {
                relAssemblageNid = TermAux.SRF_STATED_RELATIONSHIP_ASSEMBLAGE.getNid();
            } else{
                relAssemblageNid = TermAux.SRF_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid();
            }

            for (String[] relationshipRecord : relationshipRecords) {
                try {

                    final Status state = Status.fromZeroOneToken(relationshipRecord[SRF_STATUS_INDEX]);
                    if (state == Status.INACTIVE && importType == ImportType.ACTIVE_ONLY) {
                        continue;
                    }
                    UUID referencedConceptUuid = UUID.fromString(relationshipRecord[SRF_SOURCE_ID_INDEX]);
                    if (importType == ImportType.ACTIVE_ONLY) {
                        if (!identifierService.hasUuid(referencedConceptUuid)) {
                            // if concept was not imported because inactive then skip
                            continue;
                        }
                    }

                    int authorNid = identifierService.getNidForUuids(UUID.fromString(relationshipRecord[SRF_AUTHOR_INDEX]));
                    int pathNid = identifierService.getNidForUuids(UUID.fromString(relationshipRecord[SRF_PATH_INDEX]));
                    UUID betterRelUuid = UuidT5Generator.get(
                            relationshipRecord[SRF_ID_INDEX]
                                    + relationshipRecord[SRF_SOURCE_ID_INDEX]
                                    + relationshipRecord[SRF_TYPE_ID_INDEX]
                                    + relationshipRecord[SRF_DESTINATION_ID_INDEX]
                                    + relationshipRecord[SRF_CHARACTERISTIC_TYPE_ID_INDEX]
                                    + relationshipRecord[SRF_MODIFIER_ID_INDEX]
                                    + this.streamType
                    );
                    UUID moduleUuid = UUID.fromString(relationshipRecord[SRF_MODULE_INDEX]);
                    UUID destinationUuid = UUID.fromString(relationshipRecord[SRF_DESTINATION_ID_INDEX]);
                    UUID relTypeUuid = UUID.fromString(relationshipRecord[SRF_TYPE_ID_INDEX]);
                    UUID relCharacteristicUuid = UUID.fromString(relationshipRecord[SRF_CHARACTERISTIC_TYPE_ID_INDEX]);
                    UUID relModifierUuid = UUID.fromString(relationshipRecord[SRF_MODIFIER_ID_INDEX]);
                    TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(relationshipRecord[SRF_TIME_INDEX]));

                    long time = accessor.getLong(INSTANT_SECONDS) * 1000;

                    // add to rel assemblage
                    int destinationNid = identifierService.getNidForUuids(destinationUuid);
                    int moduleNid = identifierService.getNidForUuids(moduleUuid);
                    int referencedConceptNid = identifierService.getNidForUuids(referencedConceptUuid);
                    int relTypeNid = identifierService.getNidForUuids(relTypeUuid);
                    int relCharacteristicNid = identifierService.getNidForUuids(relCharacteristicUuid);
                    int relModifierNid = identifierService.getNidForUuids(relModifierUuid);

                    SemanticChronologyImpl relationshipToWrite = new SemanticChronologyImpl(VersionType.RF2_RELATIONSHIP, betterRelUuid, relAssemblageNid, referencedConceptNid);

                    int relStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
                    Rf2RelationshipImpl relVersion = relationshipToWrite.createMutableVersion(relStamp);
                    relVersion.setCharacteristicNid(relCharacteristicNid);
                    relVersion.setDestinationNid(destinationNid);
                    relVersion.setModifierNid(relModifierNid);
                    relVersion.setTypeNid(relTypeNid);
                    relVersion.setRelationshipGroup(Integer.parseInt(relationshipRecord[SRF_RELATIONSHIP_GROUP_INDEX]));

                    assemblageService.writeSemanticChronology(relationshipToWrite);

                } catch (NoSuchElementException noSuchElementException) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Error importing record: \n").append(Arrays.toString(relationshipRecord));
                    builder.append("\n");
                    LOG.error(builder.toString(), noSuchElementException);
                } catch (DateTimeParseException dtepE){
                        dtepE.printStackTrace();
                } finally {
                    completedUnitOfWork();
                }
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
