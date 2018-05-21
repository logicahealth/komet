package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.util.List;
import java.util.stream.Collectors;

/*
 * aks8m - 5/20/18
 */
public class ChronologyReader extends TimedTaskWithProgressTracker<List<String>> implements PersistTaskResult {

    private final ReaderSpecification readerSpecification;
    private List<? extends Chronology> chronologiesToRead;

    public ChronologyReader(ReaderSpecification readerSpecification, List<? extends Chronology> chronologiesToRead) {
        this.readerSpecification = readerSpecification;
        this.chronologiesToRead = chronologiesToRead;

        updateTitle("Reading " + this.chronologiesToRead.size() + " " + this.readerSpecification.getExportComponentType().toString() +"s");
        addToTotalWork(3);
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() throws Exception {

        final List<String> returnList;

        try{
            completedUnitOfWork();
            returnList = this.chronologiesToRead
                    .stream()
                    .map(chronology -> this.readerSpecification.createExportString(chronology))
                    .collect(Collectors.toList());
            completedUnitOfWork();
            this.readerSpecification.addColumnHeaders(returnList);
            completedUnitOfWork();
        }finally {
            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
