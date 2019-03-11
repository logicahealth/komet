package sh.isaac.solor.direct.clinvar.generic.writers;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.solor.direct.clinvar.generic.model.DescriptionArtifact;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public class DescriptionWriter extends TimedTaskWithProgressTracker<Void> {

    private final List<DescriptionArtifact> descriptionArtifacts;
    private final Semaphore writeSemaphore;
    private final StampService stampService;
    private final AssemblageService assemblageService;
    private final UUID descriptionNamespaceUUID;
    private final ConceptSpecification identifierAssemblageConceptSpec;
    private final List<IndexBuilderService> indexers;


    public DescriptionWriter(List<DescriptionArtifact> descriptionArtifacts, Semaphore writeSemaphore,
                             UUID descriptionNamespaceUUID, ConceptSpecification identifierAssemblageConceptSpec) {
        this.descriptionArtifacts = descriptionArtifacts;
        this.writeSemaphore = writeSemaphore;
        this.descriptionNamespaceUUID = descriptionNamespaceUUID;
        this.identifierAssemblageConceptSpec = identifierAssemblageConceptSpec;

        this.stampService = Get.stampService();
        this.assemblageService = Get.assemblageService();
        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);


        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing description batch of size: " + this.descriptionArtifacts.size());
        updateMessage("Solorizing descriptions");
        addToTotalWork(this.descriptionArtifacts.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try{

            this.descriptionArtifacts.stream()
                    .forEach(descriptionArtifact -> {

                        //Create description semantic
                        SemanticChronologyImpl descriptionToWrite =
                                new SemanticChronologyImpl(
                                        VersionType.DESCRIPTION,
                                        UuidT5Generator.get(this.descriptionNamespaceUUID, descriptionArtifact.getID()),
                                        descriptionArtifact.getLanguageCode(),
                                        descriptionArtifact.getConcept()
                                );

                        int stamp = this.stampService.getStampSequence(
                                descriptionArtifact.getStatus(),
                                descriptionArtifact.getTime(),
                                descriptionArtifact.getAuthor(),
                                descriptionArtifact.getModule(),
                                descriptionArtifact.getPath()
                        );

                        DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(stamp);
                        descriptionVersion.setCaseSignificanceConceptNid(descriptionArtifact.getCaseSignificance());
                        descriptionVersion.setDescriptionTypeConceptNid(descriptionArtifact.getType());
                        descriptionVersion.setLanguageConceptNid(descriptionArtifact.getLanguageCode());
                        descriptionVersion.setText(descriptionArtifact.getTerm());

                        index(descriptionToWrite);
                        assemblageService.writeSemanticChronology(descriptionToWrite);

                        //Create description String identifier semantic
                        SemanticChronologyImpl sctIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                                UuidT5Generator.get(this.identifierAssemblageConceptSpec.getPrimordialUuid(), descriptionArtifact.getID()),
                                this.identifierAssemblageConceptSpec.getNid(),
                                descriptionToWrite.getNid());

                        StringVersionImpl idVersion = sctIdentifierToWrite.createMutableVersion(stamp);
                        idVersion.setString(descriptionArtifact.getID());
                        index(sctIdentifierToWrite);
                        assemblageService.writeSemanticChronology(sctIdentifierToWrite);
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
