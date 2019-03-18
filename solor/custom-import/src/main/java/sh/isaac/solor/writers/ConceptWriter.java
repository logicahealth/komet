package sh.isaac.solor.writers;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
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
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.solor.model.ConceptArtifact;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public class ConceptWriter extends TimedTaskWithProgressTracker<Void> {

    private final List<ConceptArtifact> conceptArtifacts;
    private final Semaphore writeSemaphore;
    private final StampService stampService;
    private final ConceptService conceptService;
    private final AssemblageService assemblageService;
    private final UUID conceptNamespaceUUID;
    private final ConceptSpecification identifierAssemblageConceptSpec;
    private final ConceptSpecification definitionStatusAssemblageConceptSpec;
    private final List<IndexBuilderService> indexers;

    public ConceptWriter(List<ConceptArtifact> conceptArtifacts, Semaphore writeSemaphore,
                         UUID conceptNamespaceUUID, ConceptSpecification identifierAssemblageConceptSpec,
                         ConceptSpecification definitionStatusAssemblageConceptSpec) {

        this.conceptArtifacts = conceptArtifacts;
        this.writeSemaphore = writeSemaphore;
        this.conceptNamespaceUUID = conceptNamespaceUUID;
        this.identifierAssemblageConceptSpec = identifierAssemblageConceptSpec;
        this.definitionStatusAssemblageConceptSpec = definitionStatusAssemblageConceptSpec;

        this.stampService = Get.stampService();
        this.conceptService = Get.conceptService();
        this.assemblageService = Get.assemblageService();
        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);

        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing concept batch of size: " + this.conceptArtifacts.size());
        updateMessage("Solorizing concepts");
        addToTotalWork(this.conceptArtifacts.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try{

            this.conceptArtifacts.stream()
                    .forEach(conceptArtifact -> {


                        //Create concept
                        ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(
                                UuidT5Generator.get(this.conceptNamespaceUUID, conceptArtifact.getID()),
                                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid()
                        );

                        index(conceptToWrite);

                        int stamp = stampService.getStampSequence(
                               conceptArtifact.getStatus(),
                                conceptArtifact.getTime(),
                                conceptArtifact.getAuthor(),
                                conceptArtifact.getModule(),
                                conceptArtifact.getPath()
                        );

                        conceptToWrite.createMutableVersion(stamp);
                        conceptService.writeConcept(conceptToWrite);

                        //Create definition status semantic
                        SemanticChronologyImpl defStatusToWrite = new SemanticChronologyImpl(VersionType.COMPONENT_NID,
                                UuidT5Generator.get(this.definitionStatusAssemblageConceptSpec.getPrimordialUuid(), String.valueOf(conceptArtifact.getDefinitionStatus())),
                                this.definitionStatusAssemblageConceptSpec.getNid(),
                                conceptToWrite.getNid()
                        );

                        ComponentNidVersionImpl defStatusVersion = defStatusToWrite.createMutableVersion(stamp);
                        defStatusVersion.setComponentNid(conceptArtifact.getDefinitionStatus());
                        index(defStatusToWrite);
                        assemblageService.writeSemanticChronology(defStatusToWrite);

                        //Create concept identifier semantic
                        SemanticChronologyImpl identifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                                UuidT5Generator.get(
                                        this.identifierAssemblageConceptSpec.getPrimordialUuid(), conceptArtifact.getID()),
                                this.identifierAssemblageConceptSpec.getNid(),
                                conceptToWrite.getNid()
                        );

                        StringVersionImpl idVersion = identifierToWrite.createMutableVersion(stamp);
                        idVersion.setString(conceptArtifact.getID());
                        index(identifierToWrite);
                        this.assemblageService.writeSemanticChronology(identifierToWrite);
                    });

        }finally {

            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }

    private void index(Chronology chronicle) {
        for (IndexBuilderService indexer: indexers) {
            indexer.indexNow(chronicle);
        }
    }
}
