package sh.isaac.komet.batch;

import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.progress.CompletedTasks;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.komet.batch.fxml.ListViewNodeController;
import sh.komet.gui.control.property.ViewProperties;

import java.util.concurrent.atomic.AtomicInteger;

public class AddConceptsInModule extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final int moduleNid;
    private final ViewProperties viewProperties;
    private final ListViewNodeController listViewNodeController;

    public AddConceptsInModule(int moduleNid, ViewProperties viewProperties,
                               ListViewNodeController listViewNodeController) {
        this.moduleNid = moduleNid;
        this.viewProperties = viewProperties;
        this.listViewNodeController = listViewNodeController;
        Platform.runLater(() -> Get.activeTasks().add(this));
        this.updateTitle("Adding concepts in module " + viewProperties.getPreferredDescriptionText(moduleNid)
        + " to list view. ");
    }

    @Override
    protected Void call() throws Exception {
        try {
            AtomicInteger conceptsProcessed = new AtomicInteger();
            AtomicInteger conceptsAdded = new AtomicInteger();
            addToTotalWork(Get.conceptService().getConceptCount());
            Get.conceptService().getConceptChronologyStream().forEach(conceptChronology -> {
                LatestVersion<Version> latest = conceptChronology.getLatestVersion(this.viewProperties.getManifoldCoordinate().getVertexStampFilter());
                if (latest.get().getModuleNid() == moduleNid) {
                    conceptsAdded.getAndIncrement();
                    Platform.runLater(() -> {
                        this.listViewNodeController.addIdentifiedObject(conceptChronology);
                    });
                }
                if (conceptsProcessed.get() % 5000 == 0) {
                    updateMessage("Added " + conceptsAdded + " concepts out of " + conceptsProcessed + " processed.");
                }
                conceptsProcessed.getAndIncrement();
                this.completedUnitOfWork();
            });


        } finally {
            Platform.runLater(() -> Get.activeTasks().remove(this));
        }
        return null;
    }
}
