package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/*
 * aks8m - 5/15/18
 */
public class ExportContentAndZipTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final ExportType exportType;
    private final Manifold manifold;

    public ExportContentAndZipTask(Manifold manifold, File exportDirectory, ExportType exportType){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportType = exportType;
        updateTitle("Export " + this.exportType.toString());

        addToTotalWork(2);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        //Concepts
        completedUnitOfWork();
        List<String> conceptsToWrite = new ArrayList<>();
        conceptsToWrite.add("id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId");
        updateMessage("Exporting Concepts...");
        ConceptExporter conceptExporter = new ConceptExporter(this.exportType, this.manifold);
        Future<List<String>> conceptExportTask = Get.executor().submit(conceptExporter, conceptsToWrite);
        conceptsToWrite.addAll(conceptExportTask.get());
        completedUnitOfWork();

        //Add zipping algorithm
        Files.write(Paths.get(this.exportDirectory.getAbsolutePath() + "/concepts.txt"), conceptsToWrite);

        String b = "break";


        return null;
    }
}
