package sh.isaac.solor.direct;

import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class GenomicConceptWriter extends TimedTaskWithProgressTracker<Void> {


    private final Semaphore writeSemaphore;
    private final List<String> genomicConcepts;

    private static final String ENCODING_FOR_UUID_GENERATION = "8859_1";
    private final long commitTime = System.currentTimeMillis();
    private final List<IndexBuilderService> indexers;
    private final Status state = Status.ACTIVE;
    private final int authorNid = TermAux.USER.getNid();
    private final int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
    private final int moduleNid = MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid();
    private final GenomicConceptType genomicConceptType;


    public GenomicConceptWriter(List<String> genomicConcepts, Semaphore writeSemaphore,
                                String message, GenomicConceptType genomicConceptType) {
        this.genomicConcepts = genomicConcepts;
        this.writeSemaphore = writeSemaphore;
        this.genomicConceptType = genomicConceptType;
        this.writeSemaphore.acquireUninterruptibly();
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing " + genomicConceptType.toString() + " concept batch of size: " + genomicConcepts.size());
        updateMessage(message);
        addToTotalWork(genomicConcepts.size());
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
            ConceptService conceptService = Get.conceptService();
            StampService stampService = Get.stampService();
            AssemblageService assemblageService = Get.assemblageService();


            for (String conceptStringValue : this.genomicConcepts) {

                UUID conceptUUID;
                ConceptSpecification conceptSpecificAssemblage;

                try {
                    String conceptID = "gov.nih.nlm.ncbi." + conceptStringValue;
                    conceptUUID = UUID.nameUUIDFromBytes(conceptID.getBytes(ENCODING_FOR_UUID_GENERATION));
                } catch (final UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

                switch (this.genomicConceptType) {

                    case VARIANT:
                        conceptSpecificAssemblage = MetaData.CLINVAR_VARIANT_ID____SOLOR;
                        break;

                    case GENE:
                        conceptSpecificAssemblage = MetaData.NCBI_GENE_ID____SOLOR;
                        break;

                    default:
                        conceptSpecificAssemblage = TermAux.SOLOR_CONCEPT_ASSEMBLAGE;
                        break;
                }

                    /**
                     * Write concept
                     */
                    ConceptChronologyImpl concept = new ConceptChronologyImpl(conceptUUID, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
                    index(concept);
                    int conceptStamp = stampService.getStampSequence(this.state, this.commitTime, this.authorNid, this.moduleNid, this.pathNid);
                    concept.createMutableVersion(conceptStamp);
                    conceptService.writeConcept(concept);

                    /**
                     * Write concept identifier semantic
                     */
                    UUID conceptIdentifierSemanticUUID = UuidT5Generator.get(conceptSpecificAssemblage.getPrimordialUuid(),
                            conceptStringValue);
                    SemanticChronologyImpl conceptIdentifierSemantic = new SemanticChronologyImpl(VersionType.STRING,
                            conceptIdentifierSemanticUUID,
                            conceptSpecificAssemblage.getNid(),
                            concept.getNid());
                    StringVersionImpl conceptIdentifierSemanticVersion = conceptIdentifierSemantic.createMutableVersion(conceptStamp);
                    conceptIdentifierSemanticVersion.setString(conceptStringValue);
                    index(conceptIdentifierSemantic);
                    assemblageService.writeSemanticChronology(conceptIdentifierSemantic);

                completedUnitOfWork();

            }


            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }
}
