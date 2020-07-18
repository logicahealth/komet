package sh.isaac.komet.batch;

import javafx.application.Platform;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.komet.batch.fxml.ListViewNodeController;
import sh.komet.gui.control.property.ViewProperties;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AddConceptsInModule extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private static final int ITEM_UPDATE_INCREMENT = 20;
    private static final int PROGRESS_UPDATE_INCREMENT = 5000;
    private final int moduleNid;
    private final ViewProperties viewProperties;
    private final ListViewNodeController listViewNodeController;
    private final AtomicReference<MutableList<IdentifiedObject>> objectsToAddReference = new AtomicReference<>(Lists.mutable.ofInitialCapacity(ITEM_UPDATE_INCREMENT));
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
                if (latest.isPresent()) {
                    if (latest.get().getModuleNid() == moduleNid) {
                        conceptsAdded.getAndIncrement();
                        objectsToAddReference.get().add(conceptChronology);
                    }
                    if (conceptsAdded.get() % ITEM_UPDATE_INCREMENT == 0) {
                        MutableList<IdentifiedObject> objectsToAdd = objectsToAddReference.getAndSet(Lists.mutable.ofInitialCapacity(ITEM_UPDATE_INCREMENT));
                        Platform.runLater(() -> {
                            this.listViewNodeController.addIdentifiedObjects(objectsToAdd);
                        });
                    }
                    if (conceptsProcessed.get() % PROGRESS_UPDATE_INCREMENT == 0) {
                        updateMessage("Added " + conceptsAdded + " concepts out of " + conceptsProcessed + " processed.");
                    }
                }
                conceptsProcessed.getAndIncrement();
                this.completedUnitOfWork();
            });
            Platform.runLater(() -> {
                this.listViewNodeController.addIdentifiedObjects(objectsToAddReference.get());
            });

        } finally {
            Platform.runLater(() -> Get.activeTasks().remove(this));
        }
        return null;
    }
}
