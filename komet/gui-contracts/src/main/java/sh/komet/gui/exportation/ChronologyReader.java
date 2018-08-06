package sh.komet.gui.exportation;

import javafx.beans.property.SimpleIntegerProperty;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/*
 * aks8m - 5/20/18
 */
public class ChronologyReader extends TimedTaskWithProgressTracker<List<String>> implements PersistTaskResult {

    private final ReaderSpecification readerSpecification;
    private List<? extends Chronology> chronologiesToRead;
    private final Semaphore readSemaphore;

    public ChronologyReader(ReaderSpecification readerSpecification,
                            List<? extends Chronology> chronologiesToRead,
                            Semaphore readSemaphore) {
        this.readerSpecification = readerSpecification;
        this.chronologiesToRead = chronologiesToRead;
        this.readSemaphore = readSemaphore;

        this.readSemaphore.acquireUninterruptibly();
        updateTitle("Reading " + this.chronologiesToRead.size() + " " + this.readerSpecification.getReaderUIText());
        addToTotalWork(chronologiesToRead.size() + 2);
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() throws Exception {

        final List<String> returnList = new ArrayList<>();

        try {
            completedUnitOfWork();

            this.chronologiesToRead.stream()
                    .forEach(chronology -> {

                        returnList.addAll(this.readerSpecification.readExportData(chronology));
                        completedUnitOfWork();
                    });

            completedUnitOfWork();

        }finally {
            this.readSemaphore.release();

            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
