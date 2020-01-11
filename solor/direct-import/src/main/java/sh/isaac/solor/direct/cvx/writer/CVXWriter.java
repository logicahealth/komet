package sh.isaac.solor.direct.cvx.writer;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-05-28
 * aks8m - https://github.com/aks8m
 */
public class CVXWriter extends TimedTaskWithProgressTracker<Void> {



    private final List<String[]> valuesToWrite;
    private final Semaphore writeSemaphore;
    private final int batchSize = 10000;
    private final List<IndexBuilderService> indexers;
    private final StampService stampService = Get.stampService();
    private final long time = System.currentTimeMillis();
    private int versionStamp;
    private final AssemblageService assemblageService = Get.assemblageService();



    public CVXWriter(List<String[]> valuesToWrite, Semaphore writeSemaphore) {
        this.valuesToWrite = valuesToWrite;
        this.writeSemaphore = writeSemaphore;

        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);


        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing CVX batch of size: " + this.valuesToWrite.size());
        updateMessage("Solorizing CVX Data");
        addToTotalWork(this.valuesToWrite.size() / this.batchSize);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        final AtomicInteger batchCount = new AtomicInteger(0);

        try {

            this.valuesToWrite.stream()
                    .forEach(columns -> {






                    });

        }finally {

            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }

    private void index(Chronology chronicle)
    {
        for (IndexBuilderService indexer : indexers)
        {
            indexer.indexNow(chronicle);
        }
    }
}
