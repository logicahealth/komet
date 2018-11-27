package sh.isaac.solor.direct;

import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class GenomicDescriptionWriter extends TimedTaskWithProgressTracker<Void> {


    private final Semaphore writeSemaphore;
    private final Map<String, Set<String>> genomicDescriptionMap;

    private static final String ENCODING_FOR_UUID_GENERATION = "8859_1";
    private final long commitTime = System.currentTimeMillis();
    private final List<IndexBuilderService> indexers;
    private final Status state = Status.ACTIVE;
    private final int authorNid = TermAux.USER.getNid();
    private final int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
    private final int moduleNid = MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid();
    private final int descriptionAssemblageNid = LanguageCoordinates.iso639toDescriptionAssemblageNid("en");
    private final int languageNid = LanguageCoordinates.iso639toConceptNid("en");

    public GenomicDescriptionWriter(Map<String, Set<String>> genomicDescriptionMap, Semaphore writeSemaphore,
                                    String message, GenomicConceptType genomicConceptType) {
        this.genomicDescriptionMap = genomicDescriptionMap;
        this.writeSemaphore = writeSemaphore;
        this.writeSemaphore.acquireUninterruptibly();
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing " + genomicConceptType.toString() + " description batch of size: " + genomicDescriptionMap.size());
        updateMessage(message);
        addToTotalWork(genomicDescriptionMap.size());
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
            StampService stampService = Get.stampService();
            AssemblageService assemblageService = Get.assemblageService();
            IdentifierService identifierService = Get.identifierService();

            for(Map.Entry<String, Set<String>> entry : this.genomicDescriptionMap.entrySet()){

                String conceptString = entry.getKey();

                for(String descriptionString : entry.getValue()){

                    UUID conceptUUID, descriptionUUID;

                    try {
                        final String conceptID = "gov.nih.nlm.ncbi." + conceptString;
                        final String descriptionID = "gov.nih.nlm.ncbi." + descriptionString;
                        conceptUUID = UUID.nameUUIDFromBytes(conceptID.getBytes(ENCODING_FOR_UUID_GENERATION));
                        descriptionUUID = UUID.nameUUIDFromBytes(descriptionID.getBytes(ENCODING_FOR_UUID_GENERATION));
                    } catch (final UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }

                    /**
                     * Write variant description semantic
                     */

                    try {
                        int genomicConceptNID = identifierService.getNidForUuids(conceptUUID);
                        SemanticChronologyImpl genomicDescription =
                                new SemanticChronologyImpl(VersionType.DESCRIPTION, descriptionUUID, descriptionAssemblageNid, genomicConceptNID);
                        int conceptStamp = stampService.getStampSequence(this.state, this.commitTime, this.authorNid, this.moduleNid, this.pathNid);
                        DescriptionVersionImpl genomicDescriptionVersion = genomicDescription.createMutableVersion(conceptStamp);
                        genomicDescriptionVersion.setCaseSignificanceConceptNid(TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getNid());
                        genomicDescriptionVersion.setDescriptionTypeConceptNid(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid());
                        genomicDescriptionVersion.setLanguageConceptNid(this.languageNid);
                        genomicDescriptionVersion.setText(descriptionString);
                        index(genomicDescription);
                        assemblageService.writeSemanticChronology(genomicDescription);
                    }catch (NoSuchElementException nseE){
                        System.out.println("Can't find: " + conceptString + ": " + conceptUUID.toString());
                    }
                }

                completedUnitOfWork();
            }

            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }
}
