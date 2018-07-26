package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/*
 * aks8m - 5/20/18
 */
public class BatchReader extends TimedTaskWithProgressTracker<List<String>> implements PersistTaskResult {


    private final ReaderSpecification readerSpecification;
    private final int batchSize;
    private final Semaphore readSemaphore;

    public BatchReader(ReaderSpecification readerSpecification, int batchSize, Semaphore readSemaphore) {
        this.readerSpecification = readerSpecification;
        this.batchSize = batchSize;
        this.readSemaphore = readSemaphore;
        this.readSemaphore.acquireUninterruptibly();

        updateTitle("Managing " + this.readerSpecification.getReaderUIText() + " Readers");
        addToTotalWork(4);
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() throws Exception {

        final List<Chronology> chronologyBatches = new ArrayList<>();
        final List<Future<List<String>>> futures = new ArrayList<>();
        final List<String> returnList = new ArrayList<>();

        try {

            completedUnitOfWork();

            final List<Chronology> chronologies = this.readerSpecification.createChronologyList();

            completedUnitOfWork();
            for (int i = 0; i < chronologies.size(); i++) {
                if (chronologyBatches.size() < this.batchSize) {

                    chronologyBatches.add(chronologies.get(i));
                } else if (chronologyBatches.size() == this.batchSize) {

                    futures.add(Get.executor().submit(
                            new ChronologyReader(this.readerSpecification, new ArrayList<>(chronologyBatches), readSemaphore), new ArrayList<>()));
                    chronologyBatches.clear();
                }

                if (i == chronologies.size() - 1) {

                    futures.add(Get.executor().submit(
                            new ChronologyReader(this.readerSpecification, new ArrayList<>(chronologyBatches), readSemaphore), new ArrayList<>()));
                }
            }
            completedUnitOfWork();

            for (Future<List<String>> future : futures)
                returnList.addAll(future.get());

            completedUnitOfWork();
        } finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }
        return returnList;
    }
}
