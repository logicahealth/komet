package sh.isaac.solor.direct.umls.writers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Str2_VersionImpl;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 4/10/2019
 *
 * @author kmaulden
 */
public class EquivalencyWriter extends TimedTaskWithProgressTracker<Void> {

    private final Map<Integer, Integer> nidSet;
    private final Semaphore writeSemaphore;
    private long time = System.currentTimeMillis();


    private final AssemblageService assemblageService;
    private final IdentifierService identifierService;
    private final StampService stampService;
    private final int batchSize = 1000;
    private static final Logger LOG = LogManager.getLogger();

    public EquivalencyWriter(Map<Integer, Integer> nidSet, Semaphore writeSemaphore) {

        this.nidSet = nidSet;
        this.writeSemaphore = writeSemaphore;

        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();
        this.stampService = Get.stampService();

        this.writeSemaphore.acquireUninterruptibly();

        updateTitle("Creating umls assemblage based on asserted equivalencies. Size: " + this.nidSet.size());
        updateMessage("Writing umls assemblage");

        addToTotalWork(this.nidSet.size() / this.batchSize );
        Get.activeTasks().add(this);

    }

    @Override
    protected Void call() throws Exception {

        final AtomicInteger batchProgressCounter = new AtomicInteger(0);

        try {

            this.nidSet.entrySet().stream()
                    .forEach(nidSetEntry -> {
                        batchProgressCounter.incrementAndGet();

//                        EquivalencyArtifact equivalencyArtifact = new EquivalencyArtifact(
//                            Status.ACTIVE,
//                            this.time,
//                            MetaData.UMLS_USER____SOLOR.getNid(),
//                            MetaData.SOLOR_UMLS_MODULE____SOLOR.getNid(),
//                            MetaData.DEVELOPMENT_PATH____SOLOR.getNid(),
//                            nidSetEntry.getKey(),
//                            nidSetEntry.getValue(),
//                            MetaData.UMLS_EQUIVALENCY_ASSEMBLAGE____SOLOR.getPrimordialUuid(),
//                            "");
//
//                        int versionStamp = stampService.getStampSequence(
//                                equivalencyArtifact.getStatus(),
//                                equivalencyArtifact.getTime(),
//                                equivalencyArtifact.getAuthorNid(),
//                                equivalencyArtifact.getModuleNid(),
//                                equivalencyArtifact.getPathNid()
//                        );
//
//                        SemanticChronologyImpl nidStrSemantic = new SemanticChronologyImpl(
//                                VersionType.Nid1_Str2,
//                                equivalencyArtifact.getComponentUUID(),
//                                this.identifierService.getNidForUuids(equivalencyArtifact.getEquivalencyAssemblageUUID()),
//                                nidSetEntry.getKey());
//
//                        Nid1_Str2_VersionImpl brittleVersion = nidStrSemantic.createMutableVersion(versionStamp);
//                        brittleVersion.setNid1(nidSetEntry.getValue());
//                        brittleVersion.setStr2(equivalencyArtifact.getCui());

                        int versionStamp = stampService.getStampSequence(
                                Status.ACTIVE,
                                this.time,
                                MetaData.UMLS_AUTHOR____SOLOR.getNid(),
                                MetaData.SOLOR_UMLS_MODULE____SOLOR.getNid(),
                                MetaData.DEVELOPMENT_PATH____SOLOR.getNid()
                        );

                        SemanticChronologyImpl nidStrSemantic = new SemanticChronologyImpl(
                                VersionType.Nid1_Str2,
                                this.identifierService.getUuidPrimordialForNid(nidSetEntry.getKey()), //TODO what is the componenentUUID here?
                                this.identifierService.getNidForUuids(MetaData.UMLS_EQUIVALENCY_ASSEMBLAGE____SOLOR.getPrimordialUuid()),
                                nidSetEntry.getKey());

                        Nid1_Str2_VersionImpl brittleVersion = nidStrSemantic.createMutableVersion(versionStamp);
                        brittleVersion.setNid1(nidSetEntry.getValue());
                        brittleVersion.setStr2(""); //TODO set string value, CUI?

                        index(nidStrSemantic);
                        assemblageService.writeSemanticChronology(nidStrSemantic);

                        if (batchProgressCounter.get() % this.batchSize == 0) {
                            completedUnitOfWork();
                        }

                    });

        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }

    //TODO need to implement this.. what is it for?
    private void index(Chronology chronology) {
    }
}
