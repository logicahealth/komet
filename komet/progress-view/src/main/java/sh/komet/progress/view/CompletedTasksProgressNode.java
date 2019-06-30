package sh.komet.progress.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import sh.isaac.api.Get;
import sh.isaac.api.progress.CompletedTasks;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

import javax.swing.*;
import java.util.Optional;

public class CompletedTasksProgressNode extends TaskProgressNode {

    public CompletedTasksProgressNode(Manifold manifold) {
        super(manifold);
        CompletedTasks completedTasks = Get.completedTasks();
        completedTasks.addListener(this::taskListener);
        taskProgressView.getTasks()
                .addAll(completedTasks.get());
        this.title.setValue(TasksCompletedNodeFactory.TITLE_BASE);
        this.titledNodeTitle.setValue(TasksCompletedNodeFactory.TITLE_BASE);
    }

    @Override
    public Optional<Node> getTitleNode() {
        titleLabel = new Label();
        titleLabel.graphicProperty().setValue(Iconography.CHECKERED_FLAG.getIconographic());
        titleLabel.textProperty().setValue("Completions");
        title.setValue("");
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
    public Node getMenuIcon() {
        return Iconography.CHECKERED_FLAG.getIconographic();
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
