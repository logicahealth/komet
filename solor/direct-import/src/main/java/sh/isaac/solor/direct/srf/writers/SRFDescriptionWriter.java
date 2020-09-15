package sh.isaac.solor.direct.srf.writers;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.utility.LanguageMap;

public class SRFDescriptionWriter extends TimedTaskWithProgressTracker<Void> {

    private static final int SRF_ID_INDEX = 0;
    private static final int SRF_STATUS_INDEX = 1;
    private static final int SRF_TIME_INDEX = 2;
    private static final int SRF_AUTHOR_INDEX = 3;
    private static final int SRF_MODULE_INDEX = 4;
    private static final int SRF_PATH_INDEX = 5;
    private static final int SRF_REFERENCED_CONCEPT_ID_INDEX = 6;
    private static final int SRF_LANGUAGE_CODE_INDEX = 7;
    private static final int SRF_DESCRIPTION_TYPE_ID_INDEX = 8;
    private static final int SRF_TERM_INDEX = 9;
    private static final int SRF_CASE_SIGNIFICANCE_ID_INDEX = 10;

    private final List<String[]> descriptionRecords;
    private final Semaphore writeSemaphore;
    private final List<IndexBuilderService> indexers;
    private final ImportType importType;

    public SRFDescriptionWriter(List<String[]> descriptionRecords, Semaphore writeSemaphore, String message, ImportType importType) {
        this.descriptionRecords = descriptionRecords;
        this.writeSemaphore = writeSemaphore;
        this.importType = importType;

        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing description batch of size: " + descriptionRecords.size());
        updateMessage(message);
        addToTotalWork(descriptionRecords.size());
        Get.activeTasks().add(this);
    }

    private void index(Chronology chronicle) {
        for (IndexBuilderService indexer: indexers) {
            indexer.indexNow(chronicle);
        }
    }

    @Override
    protected Void call() throws Exception {

        try {

            AssemblageService assemblageService = Get.assemblageService();
            IdentifierService identifierService = Get.identifierService();
            StampService stampService = Get.stampService();

            int identifierAssemblageNid = TermAux.SNOMED_IDENTIFIER.getNid();

            for (String[] descriptionRecord : descriptionRecords) {

                final Status state = Status.fromZeroOneToken(descriptionRecord[SRF_STATUS_INDEX]);
                if (state == Status.INACTIVE && importType == ImportType.SNAPSHOT_ACTIVE_ONLY) {
                    continue;
                }

                UUID referencedConceptUuid = UUID.fromString(descriptionRecord[SRF_REFERENCED_CONCEPT_ID_INDEX]);

                if (importType == ImportType.SNAPSHOT_ACTIVE_ONLY) {
                    if (!identifierService.hasUuid(referencedConceptUuid)) {
                        // if concept was not imported because inactive, then skip
                        continue;
                    }
                }

                int authorNid = identifierService.getNidForUuids(UUID.fromString(descriptionRecord[SRF_AUTHOR_INDEX]));
                int pathNid = identifierService.getNidForUuids(UUID.fromString(descriptionRecord[SRF_PATH_INDEX]));
                int descriptionAssemblageNid = LanguageMap.iso639toDescriptionAssemblageNid(descriptionRecord[SRF_LANGUAGE_CODE_INDEX]);
                int languageNid = LanguageMap.iso639toConceptNid(descriptionRecord[SRF_LANGUAGE_CODE_INDEX]);
                UUID descriptionUuid = UUID.fromString(descriptionRecord[SRF_ID_INDEX]);
                UUID moduleUuid = UUID.fromString(descriptionRecord[SRF_MODULE_INDEX]);
                UUID caseSignificanceUuid = UUID.fromString(descriptionRecord[SRF_CASE_SIGNIFICANCE_ID_INDEX]);
                UUID descriptionTypeUuid = UUID.fromString(descriptionRecord[SRF_DESCRIPTION_TYPE_ID_INDEX]);
                TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(descriptionRecord[SRF_TIME_INDEX]));

                long time = accessor.getLong(INSTANT_SECONDS) * 1000;

                // add to description assemblage
                int moduleNid = identifierService.getNidForUuids(moduleUuid);
                int referencedConceptNid = identifierService.getNidForUuids(referencedConceptUuid);
                int caseSignificanceNid = identifierService.getNidForUuids(caseSignificanceUuid);
                int descriptionTypeNid = identifierService.getNidForUuids(descriptionTypeUuid);

                SemanticChronologyImpl descriptionToWrite =
                        new SemanticChronologyImpl(VersionType.DESCRIPTION, descriptionUuid, descriptionAssemblageNid, referencedConceptNid);
                int conceptStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
                DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(conceptStamp);
                descriptionVersion.setCaseSignificanceConceptNid(caseSignificanceNid);
                descriptionVersion.setDescriptionTypeConceptNid(descriptionTypeNid);
                descriptionVersion.setLanguageConceptNid(languageNid);
                descriptionVersion.setText(descriptionRecord[SRF_TERM_INDEX]);

                index(descriptionToWrite);
                assemblageService.writeSemanticChronology(descriptionToWrite);

                // add to sct identifier assemblage
                UUID identifierUuid;

                identifierUuid = UuidT5Generator.get(MetaData.UUID____SOLOR.getPrimordialUuid(), descriptionRecord[SRF_ID_INDEX]);

                SemanticChronologyImpl sctIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                        identifierUuid,
                        identifierAssemblageNid,
                        descriptionToWrite.getNid());

                StringVersionImpl idVersion = sctIdentifierToWrite.createMutableVersion(conceptStamp);
                idVersion.setString(descriptionRecord[SRF_ID_INDEX]);
                index(sctIdentifierToWrite);
                assemblageService.writeSemanticChronology(sctIdentifierToWrite);
                completedUnitOfWork();
            }

        } catch (DateTimeParseException dtepE){
            dtepE.printStackTrace();
        }finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;

    }
}
