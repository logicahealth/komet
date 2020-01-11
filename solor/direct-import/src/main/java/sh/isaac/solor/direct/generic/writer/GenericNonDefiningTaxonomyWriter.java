package sh.isaac.solor.direct.generic.writer;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.solor.direct.generic.artifact.NonDefiningTaxonomyArtifact;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public class GenericNonDefiningTaxonomyWriter extends GenericWriter {

    private final ArrayList<NonDefiningTaxonomyArtifact> nonDefiningTaxonomy;
    private final Semaphore writeSemaphore;
    private final AssemblageService assemblageService;
    private final IdentifierService identifierService;
    private final StampService stampService;

    public GenericNonDefiningTaxonomyWriter(ArrayList<NonDefiningTaxonomyArtifact> nonDefiningTaxonomy, Semaphore writeSemaphore) {

        super(
                "Importing non-defining relationships batch of size: " + nonDefiningTaxonomy.size(),
                "Solorizing non-defining relationships",
                nonDefiningTaxonomy.size()
        );

        this.nonDefiningTaxonomy = nonDefiningTaxonomy;
        this.writeSemaphore = writeSemaphore;
        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();
        this.stampService = Get.stampService();
        this.writeSemaphore.acquireUninterruptibly();
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
}
