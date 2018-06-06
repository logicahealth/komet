package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/*
 * aks8m - 5/20/18
 */
public class ChronologyReader extends TimedTaskWithProgressTracker<List<byte[]>> implements PersistTaskResult {

    private final ReaderSpecification readerSpecification;
    private List<? extends Chronology> chronologiesToRead;

    public ChronologyReader(ReaderSpecification readerSpecification, List<? extends Chronology> chronologiesToRead) {
        this.readerSpecification = readerSpecification;
        this.chronologiesToRead = chronologiesToRead;



        updateTitle("Reading " + this.chronologiesToRead.size() + " " + this.readerSpecification.getReaderUIText());
        addToTotalWork(2);
        Get.activeTasks().add(this);
    }

    @Override
    protected List<byte[]> call() throws Exception {

        final List<byte[]> byteList = new ArrayList<>();

        try {
            completedUnitOfWork();

            this.chronologiesToRead.stream()
                    .forEach(chronology -> {

                        try {
                            byteList.addAll(this.readerSpecification.readExportData(chronology));
                        }catch (UnsupportedEncodingException uueE) {
                            uueE.printStackTrace();
                        }
                    });

            this.readerSpecification.addColumnHeaders(byteList);

            completedUnitOfWork();

        }finally {
            Get.activeTasks().remove(this);
        }

        return byteList;
    }
}
