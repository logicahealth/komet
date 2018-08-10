package sh.komet.gui.exportation.batching;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.exportation.batching.specification.BatchSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class BatchProcess<T, U> extends TimedTaskWithProgressTracker<List<U>> implements PersistTaskResult {

    private final BatchSpecification batchSpecification;
    private final List<T> batch;
    private final Semaphore batchSemaphore;

    public BatchProcess(BatchSpecification batchSpecification, List batch, Semaphore batchSemaphore) {
        this.batchSpecification = batchSpecification;
        this.batch = batch;
        this.batchSemaphore = batchSemaphore;

        this.batchSemaphore.acquireUninterruptibly();
        updateTitle("Running Batch of " + this.batch.size() + " " + this.batchSpecification.getReaderUIText());
        addToTotalWork(batch.size() + 2);
        Get.activeTasks().add(this);
    }

    @Override
    protected List<U> call() throws Exception {

            List<U> returnList = new ArrayList<>();

            try {
                completedUnitOfWork();

                this.batch.stream()
                        .forEach((item -> {

                            returnList.addAll(this.batchSpecification.performProcessOnItem(item));
                            completedUnitOfWork();
                        }));

                completedUnitOfWork();

            }finally {
                this.batchSemaphore.release();
                Get.activeTasks().remove(this);
            }

            return returnList;
    }
}
