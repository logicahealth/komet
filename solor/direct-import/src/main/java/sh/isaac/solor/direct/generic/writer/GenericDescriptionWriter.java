package sh.isaac.solor.direct.generic.writer;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.solor.direct.generic.artifact.DescriptionArtifact;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public class GenericDescriptionWriter extends GenericWriter {

    private final ArrayList<DescriptionArtifact> descriptionArtifacts;
    private final Semaphore writeSemaphore;
    private final StampService stampService;
    private final IdentifierService identifierService;
    private final AssemblageService assemblageService;

    public GenericDescriptionWriter(ArrayList<DescriptionArtifact> descriptionArtifacts, Semaphore writeSemaphore) {

        super(
                "Importing description batch of size: " + descriptionArtifacts.size(),
                "Solorizing concepts",
                descriptionArtifacts.size()
        );

        this.descriptionArtifacts = descriptionArtifacts;
        this.writeSemaphore = writeSemaphore;
        this.stampService = Get.stampService();
        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();
        this.writeSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        final AtomicInteger batchProgressCounter = new AtomicInteger(0);

        try{

            this.descriptionArtifacts.stream()
                    .forEach(descriptionArtifact -> {

                        if(this.identifierService.hasUuid(descriptionArtifact.getReferencedComponentUUID())) {

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

                            if (batchProgressCounter.get() % this.batchSize == 0)
                                completedUnitOfWork();
                        } else {
                            LOG.info("Couldn't write non-defining taxonomy for referenced component: "
                                    + descriptionArtifact.getReferencedComponentUUID());
                        }
                    });

        }finally {

            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
