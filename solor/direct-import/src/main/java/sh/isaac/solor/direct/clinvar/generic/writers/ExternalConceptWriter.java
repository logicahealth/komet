package sh.isaac.solor.direct.clinvar.generic.writers;

import sh.isaac.api.*;
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
import sh.isaac.solor.direct.clinvar.generic.model.ConceptArtifact;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public class ExternalConceptWriter extends ExternalWriter {

    private final List<ConceptArtifact> conceptArtifacts;
    private final Semaphore writeSemaphore;
    private final StampService stampService;
    private final ConceptService conceptService;
    private final IdentifierService identifierService;
    private final AssemblageService assemblageService;
    private final UUID conceptNamespaceUUID;
    private final ConceptSpecification identifierAssemblageConceptSpec;
    private final int authorNid;
    private final int pathNid;

    public ExternalConceptWriter(List<ConceptArtifact> conceptArtifacts, Semaphore writeSemaphore, String message,
                                 UUID conceptNamespaceUUID, ConceptSpecification identifierAssemblageConceptSpec,
                                 int authorNid, int pathNid) {
        this.conceptArtifacts = conceptArtifacts;
        this.writeSemaphore = writeSemaphore;
        this.conceptNamespaceUUID = conceptNamespaceUUID;
        this.identifierAssemblageConceptSpec = identifierAssemblageConceptSpec;
        this.authorNid = authorNid;
        this.pathNid = pathNid;

        this.stampService = Get.stampService();
        this.conceptService = Get.conceptService();
        this.identifierService = Get.identifierService();
        this.assemblageService = Get.assemblageService();

        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing concept batch of size: " + this.conceptArtifacts.size());
        updateMessage(message);
        addToTotalWork(this.conceptArtifacts.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try{

            this.conceptArtifacts.stream().parallel()
                    .forEach(conceptArtifact -> {

                        UUID conceptUuid = UuidT5Generator.get(this.conceptNamespaceUUID, conceptArtifact.getID());
                        UUID identifierUuid = UuidT5Generator.get(this.identifierAssemblageConceptSpec.getPrimordialUuid(),
                                conceptArtifact.getID());

                        ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(conceptUuid,
                                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
                        index(conceptToWrite);
                        int conceptStamp = stampService.getStampSequence(
                                Status.fromZeroOneToken(conceptArtifact.getStatus()),
                                DateTimeFormatter.ISO_INSTANT.parse(
                                        getIsoInstant(conceptArtifact.getTime())).getLong(INSTANT_SECONDS) * 1000,
                                this.authorNid,
                                this.identifierService.getNidForUuids(UUID.fromString(conceptArtifact.getModule())),
                                this.pathNid);
                        conceptToWrite.createMutableVersion(conceptStamp);
                        conceptService.writeConcept(conceptToWrite);

                        SemanticChronologyImpl identifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                                identifierUuid,
                                this.identifierAssemblageConceptSpec.getNid(),
                                conceptToWrite.getNid());
                        StringVersionImpl idVersion = identifierToWrite.createMutableVersion(conceptStamp);
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
}
