package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/*
 * aks8m - 5/20/18
 */
public class BatchReader extends TimedTaskWithProgressTracker<List<String>> implements PersistTaskResult {


    private final ReaderSpecification readerSpecification;
    private final int BATCH_SIZE;

    public BatchReader(ReaderSpecification readerSpecification, int BATCH_SIZE) {
        this.readerSpecification = readerSpecification;
        this.BATCH_SIZE = BATCH_SIZE;

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
                if (chronologyBatches.size() < this.BATCH_SIZE) {

                    chronologyBatches.add(chronologies.get(i));
                } else if (chronologyBatches.size() == this.BATCH_SIZE) {

                    futures.add(Get.executor().submit(
                            new ChronologyReader(this.readerSpecification, new ArrayList<>(chronologyBatches)), new ArrayList<>()));
                    chronologyBatches.clear();
                }

                if (i == chronologies.size() - 1) {

                    futures.add(Get.executor().submit(
                            new ChronologyReader(this.readerSpecification, new ArrayList<>(chronologyBatches)), new ArrayList<>()));
                }
            }
            completedUnitOfWork();

            for (Future<List<String>> future : futures)
                returnList.addAll(future.get());

            completedUnitOfWork();
        } finally {
            Get.activeTasks().remove(this);

        }
        return returnList;
    }
}
