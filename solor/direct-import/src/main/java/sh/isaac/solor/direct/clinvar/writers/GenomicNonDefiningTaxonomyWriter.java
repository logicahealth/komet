package sh.isaac.solor.direct.clinvar.writers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.solor.direct.clinvar.model.NonDefiningTaxonomyArtifact;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public class GenomicNonDefiningTaxonomyWriter extends TimedTaskWithProgressTracker<Void> {

    private final Set<NonDefiningTaxonomyArtifact> nonDefiningTaxonomy;
    private final Semaphore writeSemaphore;
    private final AssemblageService assemblageService;
    private final IdentifierService identifierService;
    private final StampService stampService;
    private final List<IndexBuilderService> indexers;
    private final int batchSize = 10000;
    private static final Logger LOG = LogManager.getLogger();


    public GenomicNonDefiningTaxonomyWriter(Set<NonDefiningTaxonomyArtifact> nonDefiningTaxonomy, Semaphore writeSemaphore) {
        this.nonDefiningTaxonomy = nonDefiningTaxonomy;
        this.writeSemaphore = writeSemaphore;

        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();
        this.stampService = Get.stampService();
        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);

        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing non-defining relationships batch of size: " + this.nonDefiningTaxonomy.size());
        updateMessage("Solorizing non-defining relationships");
        addToTotalWork(this.nonDefiningTaxonomy.size() / this.batchSize);
        Get.activeTasks().add(this);
    }


    @Override
    protected Void call() throws Exception {

        final AtomicInteger batchProgressCounter = new AtomicInteger(0);

        try{

            this.nonDefiningTaxonomy.stream()
                    .forEach(nonDefiningTaxonomyArtifact -> {

                        if(this.identifierService.hasUuid(nonDefiningTaxonomyArtifact.getReferencedComponent()) &&
                                this.identifierService.hasUuid(nonDefiningTaxonomyArtifact.getSemanticComponentUUID())) {

                            batchProgressCounter.incrementAndGet();

                            int versionStamp = stampService.getStampSequence(
                                    nonDefiningTaxonomyArtifact.getStatus(),
                                    nonDefiningTaxonomyArtifact.getTime(),
                                    nonDefiningTaxonomyArtifact.getAuthorNid(),
                                    nonDefiningTaxonomyArtifact.getModuleNid(),
                                    nonDefiningTaxonomyArtifact.getPathNid()
                            );

                            SemanticChronologyImpl nidComponentSemantic = new SemanticChronologyImpl(
                                    VersionType.COMPONENT_NID,
                                    nonDefiningTaxonomyArtifact.getComponentUUID(),
                                    this.identifierService.getNidForUuids(nonDefiningTaxonomyArtifact.getNidSemanticAssemblageUUID()),
                                    this.identifierService.getNidForUuids(nonDefiningTaxonomyArtifact.getReferencedComponent()));

                            ComponentNidVersionImpl brittleVersion = nidComponentSemantic.createMutableVersion(versionStamp);
                            brittleVersion.setComponentNid(
                                    this.identifierService.getNidForUuids(nonDefiningTaxonomyArtifact.getSemanticComponentUUID())
                            );

                            index(nidComponentSemantic);
                            assemblageService.writeSemanticChronology(nidComponentSemantic);

                            if (batchProgressCounter.get() % this.batchSize == 0)
                                completedUnitOfWork();
                        } else {
                            LOG.info("Couldn't write non-defining taxonomy for referenced: " + nonDefiningTaxonomyArtifact.getReferencedComponent() +
                                    " semantic component: " + nonDefiningTaxonomyArtifact.getSemanticComponentUUID());
                        }
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
