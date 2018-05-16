package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.util.*;

import static sh.isaac.api.Get.concept;

/*
 * aks8m - 5/15/18
 */
public class ExportContentAndZipTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final ExportTypes exportType;
    private final Manifold manifold;

    public ExportContentAndZipTask(Manifold manifold, File exportDirectory, ExportTypes exportType){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportType = exportType;
    }

    @Override
    protected Void call() throws Exception {

        List<String> linesToWrite = new ArrayList<>();

        System.out.println("Concept Cronology Count: " + Get.conceptService().getConceptChronologyStream().count());
        System.out.println("Concept NID Count: " + Get.conceptService().getConceptNidStream().count());







        return null;
    }
}
