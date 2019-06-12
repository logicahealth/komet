package sh.isaac.solor.direct.generic.writer;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.solor.direct.generic.artifact.ConceptArtifact;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public class GenericConceptWriter extends GenericWriter {

    private final ArrayList<ConceptArtifact> conceptArtifacts;
    private final Semaphore writeSemaphore;
    private final StampService stampService;
    private final ConceptService conceptService;
    private final AssemblageService assemblageService;

    private final IdentifierService identifierService;

    public GenericConceptWriter(ArrayList<ConceptArtifact> conceptArtifacts, Semaphore writeSemaphore) {

        super(
                "Importing concept batch of size: " + conceptArtifacts.size(),
                "Solorizing concepts",
                conceptArtifacts.size()
        );

        this.conceptArtifacts = conceptArtifacts;
        this.writeSemaphore = writeSemaphore;
        this.stampService = Get.stampService();
        this.conceptService = Get.conceptService();
        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();
        this.writeSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        final AtomicInteger batchCount = new AtomicInteger(0);

        try{

            this.conceptArtifacts.stream()
                    .forEach(conceptArtifact -> {

                        batchCount.incrementAndGet();

                        try {

                            //Create concept
                            ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(
                                    conceptArtifact.getComponentUUID(),
                                    conceptArtifact.getConceptAssemblageNid()
                            );

                            index(conceptToWrite);

                            int stamp = stampService.getStampSequence(
                                    conceptArtifact.getStatus(),
                                    conceptArtifact.getTime(),
                                    conceptArtifact.getAuthorNid(),
                                    conceptArtifact.getModuleNid(),
                                    conceptArtifact.getPathNid()
                            );

                            conceptToWrite.createMutableVersion(stamp);
                            conceptService.writeConcept(conceptToWrite);

                            //Create definition status semantic
                            SemanticChronologyImpl defStatusToWrite = new SemanticChronologyImpl(VersionType.COMPONENT_NID,
                                    conceptArtifact.getDefinitionStatusComponentUUID(),
                                    this.identifierService.getNidForUuids(conceptArtifact.getDefinitionStatusAssemblageUUID()),
                                    conceptToWrite.getNid()
                            );

                            ComponentNidVersionImpl defStatusVersion = defStatusToWrite.createMutableVersion(stamp);
                            defStatusVersion.setComponentNid(conceptArtifact.getDefinitionStatusNid());
                            super.index(defStatusToWrite);
                            assemblageService.writeSemanticChronology(defStatusToWrite);

                            //Create concept identifier semantic
                            SemanticChronologyImpl identifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                                    conceptArtifact.getIdentifierComponentUUID(),
                                    this.identifierService.getNidForUuids(conceptArtifact.getIdentifierAssemblageUUID()),
                                    conceptToWrite.getNid()
                            );

                            StringVersionImpl idVersion = identifierToWrite.createMutableVersion(stamp);
                            idVersion.setString(conceptArtifact.getIdentifierValue());
                            index(identifierToWrite);
                            this.assemblageService.writeSemanticChronology(identifierToWrite);

                            if (batchCount.get() % this.batchSize == 0)
                                completedUnitOfWork();

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });

        }finally {

            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }


}
