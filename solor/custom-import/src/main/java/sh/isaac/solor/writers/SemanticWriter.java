package sh.isaac.solor.writers;

import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 2019-03-10
 * aks8m - https://github.com/aks8m
 */
public class SemanticWriter extends TimedTaskWithProgressTracker<Void> {

    private final List<Integer[][]> nonDefiningTaxonomy;
    private final Semaphore writeSemaphore;
    private final AssemblageService assemblageService;
    private final StampService stampService;
    private final List<IndexBuilderService> indexers;


    public SemanticWriter(List<Integer[][]> nonDefiningTaxonomy, Semaphore writeSemaphore ) {
        this.nonDefiningTaxonomy = nonDefiningTaxonomy;
        this.writeSemaphore = writeSemaphore;

        this.assemblageService = Get.assemblageService();
        this.stampService = Get.stampService();
        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);


        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing non-defining relationships batch of size: " + this.nonDefiningTaxonomy.size());
        updateMessage("Solorizing non-defining relationships");
        addToTotalWork(this.nonDefiningTaxonomy.size());
        Get.activeTasks().add(this);
    }


    @Override
    protected Void call() throws Exception {

        try{

            final Status status = Status.ACTIVE;
            final long time = System.currentTimeMillis();
            int authorNid = MetaData.USER____SOLOR.getNid();
            int moduleNid = MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid();
            int pathNid = TermAux.DEVELOPMENT_PATH.getNid();

            this.nonDefiningTaxonomy.stream()
                    .forEach(integers -> {

                        int versionStamp = stampService.getStampSequence(status, time, authorNid, moduleNid, pathNid);


//
//                        SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(
//                                VersionType.COMPONENT_NID,
//                                UuidT5Generator.get(integers[0][0].toString() + integers[0][1].toString()),
//                                MetaData.CLINVAR_NON_DEFINING_TAXONOMY____SOLOR.getNid(),
//                                integers[0][0]);
//
//                        ComponentNidVersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
//                        brittleVersion.setComponentNid(integers[0][1]);
//
//                        index(refsetMemberToWrite);
//                        assemblageService.writeSemanticChronology(refsetMemberToWrite);
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
