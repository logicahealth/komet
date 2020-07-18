package sh.komet.progress.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import sh.isaac.api.Get;
import sh.isaac.api.progress.CompletedTasks;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;

import java.util.Optional;

public class CompletedTasksProgressNode extends TaskProgressNode {
    {
        titleProperty.setValue("Completed tasks");
        toolTipProperty.setValue("Information about completed tasks. ");
        menuIconProperty.setValue(Iconography.CHECKERED_FLAG.getIconographic());
    }

    public CompletedTasksProgressNode(ViewProperties viewProperties) {
        super(viewProperties);
        CompletedTasks completedTasks = Get.completedTasks();
        completedTasks.addListener(this::taskListener);
        taskProgressView.getTasks()
                .addAll(completedTasks.get());
        this.titleProperty.setValue(TasksCompletedNodeFactory.TITLE_BASE);
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.CHECKERED_FLAG.getIconographic();
    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Node> getTitleNode() {
        titleLabel = new Label();
        titleLabel.graphicProperty().setValue(Iconography.CHECKERED_FLAG.getIconographic());
        titleLabel.textProperty().setValue("");
        return Optional.of(titleLabel);
    }

    protected void taskListener(SetChangeListener.Change<? extends Task<?>> change) {
        if (change.wasAdded()) {
            taskProgressView.getTasks()
                    .add(0, change.getElementAdded());
        } else if (change.wasRemoved()) {
            taskProgressView.getTasks()
                    .remove(change.getElementRemoved());
        }
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public ActivityFeed getActivityFeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
