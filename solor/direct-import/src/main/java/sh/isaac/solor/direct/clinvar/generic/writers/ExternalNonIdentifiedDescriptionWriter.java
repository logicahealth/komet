package sh.isaac.solor.direct.clinvar.generic.writers;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.solor.direct.clinvar.generic.model.DescriptionArtifact;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public class ExternalNonIdentifiedDescriptionWriter extends ExternalWriter {

    private final List<DescriptionArtifact> descriptionArtifacts;
    private final Semaphore writeSemaphore;
    private final StampService stampService;
    private final AssemblageService assemblageService;
    private final IdentifierService identifierService;
    private final int authorNid;
    private final int pathNid;

    public ExternalNonIdentifiedDescriptionWriter(List<DescriptionArtifact> descriptionArtifacts, Semaphore writeSemaphore, String message,
                                                  int authorNid, int pathNid) {
        this.descriptionArtifacts = descriptionArtifacts;
        this.writeSemaphore = writeSemaphore;
        this.authorNid = authorNid;
        this.pathNid = pathNid;

        this.stampService = Get.stampService();
        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();

        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing description batch of size: " + this.descriptionArtifacts.size());
        updateMessage(message);
        addToTotalWork(this.descriptionArtifacts.size());
        Get.activeTasks().add(this);


    }

    @Override
    protected Void call() throws Exception {

        try{

            this.descriptionArtifacts.stream()
                    .forEach(descriptionArtifact -> {





                        // add to description assemblage
                        int moduleNid = identifierService.getNidForUuids(UUID.fromString(descriptionArtifact.getModule()));
                        int referencedConceptNid = identifierService.getNidForUuids(UUID.fromString(descriptionArtifact.getConcept()));
                        int caseSignificanceNid = identifierService.getNidForUuids(UUID.fromString(descriptionArtifact.getCaseSignificance()));
                        int descriptionTypeNid = identifierService.getNidForUuids(UUID.fromString(descriptionArtifact.getType()));


                        SemanticChronologyImpl descriptionToWrite =
                                new SemanticChronologyImpl(
                                        VersionType.DESCRIPTION,
                                        descriptionUuid,
                                        LanguageCoordinates.iso639toDescriptionAssemblageNid(descriptionArtifact.getLanguageCode()),
                                        referencedConceptNid
                                );

                        int conceptStamp = this.stampService.getStampSequence(
                                Status.fromZeroOneToken(descriptionArtifact.getStatus()),
                                DateTimeFormatter.ISO_INSTANT.parse(getIsoInstant(descriptionArtifact.getTime())).getLong(INSTANT_SECONDS) * 1000,
                                authorNid,
                                moduleNid,
                                pathNid
                        );

                        DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(conceptStamp);
                        descriptionVersion.setCaseSignificanceConceptNid(caseSignificanceNid);
                        descriptionVersion.setDescriptionTypeConceptNid(descriptionTypeNid);
                        descriptionVersion.setLanguageConceptNid(LanguageCoordinates.iso639toConceptNid(descriptionArtifact.getLanguageCode()));
                        descriptionVersion.setText(descriptionArtifact.getTerm());

                        index(descriptionToWrite);
                        assemblageService.writeSemanticChronology(descriptionToWrite);
                    });


        }finally {

            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }



        return null;
    }
}
