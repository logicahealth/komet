package sh.komet.gui.exportation;

import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.util.Map;

/*
 * aks8m - 5/19/18
 */
public class ExportDirectoryZipper extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final ExportFormatType exportFormatType;
    private final Manifold manifold;
    private final File exportDirectory;
    private final Map pathsAndLinesToWrite;

    public ExportDirectoryZipper(ExportFormatType exportFormatType, Manifold manifold, File exportDirectory, Map pathsAndLinesToWrite) {
        this.exportFormatType = exportFormatType;
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.pathsAndLinesToWrite = pathsAndLinesToWrite;
    }

    @Override
    protected Void call() throws Exception {
        return null;
    }
}
