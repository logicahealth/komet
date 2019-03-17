package sh.isaac.solor.direct.clinvar.writers;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.solor.direct.clinvar.model.DescriptionArtifact;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public class GenomicDescriptionWriter extends TimedTaskWithProgressTracker<Void> {

    private final Set<DescriptionArtifact> descriptionArtifacts;
    private final Semaphore writeSemaphore;
    private final StampService stampService;
    private final IdentifierService identifierService;
    private final AssemblageService assemblageService;
    private final List<IndexBuilderService> indexers;
    private final int batchSize = 10000;


    public GenomicDescriptionWriter(Set<DescriptionArtifact> descriptionArtifacts, Semaphore writeSemaphore) {
        this.descriptionArtifacts = descriptionArtifacts;
        this.writeSemaphore = writeSemaphore;

        this.stampService = Get.stampService();
        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();
        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);


        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing description batch of size: " + this.descriptionArtifacts.size());
        updateMessage("Solorizing descriptions");
        addToTotalWork(this.descriptionArtifacts.size() / this.batchSize);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        final AtomicInteger batchProgressCounter = new AtomicInteger(0);

        try{

            this.descriptionArtifacts.stream()
                    .forEach(descriptionArtifact -> {

                        batchProgressCounter.incrementAndGet();

                        //Create description semantic
                        SemanticChronologyImpl descriptionToWrite =
                                new SemanticChronologyImpl(
                                        VersionType.DESCRIPTION,
                                        descriptionArtifact.getComponentUUID(),
                                        descriptionArtifact.getDescriptionAssemblageNid(),
                                        this.identifierService.getNidForUuids(descriptionArtifact.getReferencedComponentUUID())
                                );

                        int stamp = this.stampService.getStampSequence(
                                descriptionArtifact.getStatus(),
                                descriptionArtifact.getTime(),
                                descriptionArtifact.getAuthorNid(),
                                descriptionArtifact.getModuleNid(),
                                descriptionArtifact.getPathNid()
                        );

                        DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(stamp);
                        descriptionVersion.setCaseSignificanceConceptNid(descriptionArtifact.getCaseSignificanceNid());
                        descriptionVersion.setDescriptionTypeConceptNid(descriptionArtifact.getTypeNid());
                        descriptionVersion.setLanguageConceptNid(descriptionArtifact.getLanguageConceptNid());
                        descriptionVersion.setText(descriptionArtifact.getTerm());

                        index(descriptionToWrite);
                        assemblageService.writeSemanticChronology(descriptionToWrite);

                        //Create description String identifier semantic
                        SemanticChronologyImpl sctIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                                descriptionArtifact.getIdentifierComponentUUID(),
                                this.identifierService.getNidForUuids(descriptionArtifact.getIdentifierAssemblageUUID()),
                                descriptionToWrite.getNid());

                        StringVersionImpl idVersion = sctIdentifierToWrite.createMutableVersion(stamp);
                        idVersion.setString(descriptionArtifact.getIdentifierValue());
                        index(sctIdentifierToWrite);
                        assemblageService.writeSemanticChronology(sctIdentifierToWrite);

                        if(batchProgressCounter.get() % this.batchSize == 0)
                            completedUnitOfWork();
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
