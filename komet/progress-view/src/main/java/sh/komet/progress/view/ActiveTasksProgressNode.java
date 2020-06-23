package sh.komet.progress.view;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import sh.isaac.api.Get;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;

import java.util.Optional;

public class ActiveTasksProgressNode extends TaskProgressNode {
    {
        titleProperty.setValue("Tasks");
        toolTipProperty.setValue("Information about active tasks. ");
        menuIconProperty.setValue(Iconography.SPINNER0.getIconographic());
    }

    final Node progressIcon = Iconography.SPINNER0.getIconographic();

    final RotateTransition rotation;

    public ActiveTasksProgressNode(ViewProperties manifold) {
        super(manifold);
        ActiveTasks activeTasks = Get.activeTasks();
        activeTasks.addListener(this::taskListener);
        taskProgressView.getTasks()
                .addAll(activeTasks.get());


        this.rotation = new RotateTransition(Duration.seconds(2), progressIcon);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setByAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        progressIcon.setTranslateZ(progressIcon.getBoundsInLocal().getWidth() / 2.0);
        progressIcon.setRotationAxis(Rotate.Z_AXIS);
        if (!taskProgressView.getTasks().isEmpty()) {
            Platform.runLater(() -> rotation.play());
        }
     }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.SPINNER0.getIconographic();
    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Node> getTitleNode() {
        titleLabel = new Label();
        titleLabel.graphicProperty().setValue(progressIcon);
        titleLabel.textProperty().bind(titledNodeTitle);
        title.setValue("");
        return Optional.of(titleLabel);
    }

    protected void taskListener(SetChangeListener.Change<? extends Task<?>> change) {
        Platform.runLater(() -> {
            if (change.wasAdded()) {
                taskProgressView.getTasks()
                        .add(0, change.getElementAdded());
            } else if (change.wasRemoved()) {
                taskProgressView.getTasks()
                        .remove(change.getElementRemoved());
            }

            if (change.getSet()
                    .isEmpty()) {
                rotation.stop();

                activeTasksTooltip.set("No tasks");
                if (titleLabel == null) {
                    title.set(TaskProgressNodeFactory.TITLE_BASE);
                }
                titledNodeTitle.set(TaskProgressNodeFactory.TITLE_BASE);
            } else {
                rotation.play();
                int taskCount = change.getSet()
                        .size();
                if (taskCount == 1) {
                    activeTasksTooltip.set(taskCount + " task");
                    if (titleLabel == null) {
                        title.set(taskCount + " " + TaskProgressNodeFactory.TITLE_BASE_SINGULAR);
                    }
                    titledNodeTitle.set(taskCount + " " + TaskProgressNodeFactory.TITLE_BASE_SINGULAR);
                } else {
                    activeTasksTooltip.set(taskCount + " tasks");
                    if (titleLabel == null) {
                        title.set(taskCount + " " + TaskProgressNodeFactory.TITLE_BASE);
                    }
                    titledNodeTitle.set(taskCount + " " + TaskProgressNodeFactory.TITLE_BASE);
                }
            }
        });

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
