package sh.komet.gui.exportation.batching;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.exportation.batching.specification.BatchSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class Batcher<T, U> extends TimedTaskWithProgressTracker<List<U>> implements PersistTaskResult{

    private final BatchSpecification<T, U> batchSpecification;
    private final Semaphore batchSemaphore;
    private final int batchSize;

    public Batcher(BatchSpecification<T, U> batchSpecification, Semaphore batchSemaphore, int batchSize) {
        this.batchSpecification = batchSpecification;
        this.batchSemaphore = batchSemaphore;
        this.batchSize = batchSize;

        updateTitle("Batching " + this.batchSpecification.getReaderUIText() + " Process");
        this.batchSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected List<U> call() throws Exception {

        List<T> batch = new ArrayList<>(this.batchSize);
        final List<Future<List<U>>> futures = new ArrayList<>();
        final List<U> returnList = new ArrayList<>();

        try {
            for (T t : this.batchSpecification.createItemListToBatch()) {
                batch.add(t);

                if (batch.size() % this.batchSize == 0) {

                    futures.add(Get.executor().submit(new BatchProcess<T, U>(
                            this.batchSpecification,
                            batch, batchSemaphore), new ArrayList<>()));
                    batch = new ArrayList<>(this.batchSize);
                }
            }

            futures.add(Get.executor().submit(new BatchProcess<T, U>(
                    this.batchSpecification,
                    batch, batchSemaphore), new ArrayList<>()));


            for (Future<List<U>> future : futures) {
                returnList.addAll(future.get());
            }

        } finally {
            this.batchSemaphore.release();
            Get.activeTasks().remove(this);
        }
        return returnList;
    }
}
