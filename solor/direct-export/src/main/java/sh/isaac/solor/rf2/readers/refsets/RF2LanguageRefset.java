package sh.isaac.solor.rf2.readers.refsets;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class RF2LanguageRefset extends TimedTaskWithProgressTracker<List<String>> {

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;

    public RF2LanguageRefset(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, String message) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + message + " batch of size: " + chronologies.size());
        updateMessage("Processing batch of descriptions for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() {
        ArrayList<String> returnList = new ArrayList<>();

        try{

            for(Chronology chronology : chronologies){
                returnList.add(this.rf2ExportHelper.getRF2CommonElements(chronology)
//                        .append(getConceptPrimitiveOrSufficientDefinedSCTID(chronology.getNid()))
                        .append("\r")
                        .toString());
                completedUnitOfWork();
            }

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
