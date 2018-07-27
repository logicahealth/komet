package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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

        updateTitle("Batching " + this.readerSpecification.getReaderUIText() + " Readers");
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() throws Exception {

        final List<Chronology> chronologyBatch = new ArrayList<>();
        final List<Future<List<String>>> futures = new ArrayList<>();
        final List<String> returnList = new ArrayList<>();

        try {

            this.readerSpecification.createChronologyList().stream()
                    .forEach(chronology -> {

                        chronologyBatch.add(chronology);

                        if( (chronologyBatch.size() % this.batchSize) == 0 ){

                            futures.add(Get.executor().submit(
                                    new ChronologyReader(this.readerSpecification,
                                            new ArrayList<>(chronologyBatch), readSemaphore), new ArrayList<>()));
                            chronologyBatch.clear();
                        }
                    });

            futures.add(Get.executor().submit(
                    new ChronologyReader(this.readerSpecification,
                            new ArrayList<>(chronologyBatch), readSemaphore), new ArrayList<>()));


            for (Future<List<String>> future : futures)
                returnList.addAll(future.get());

        } finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }
        return returnList;
    }
}
