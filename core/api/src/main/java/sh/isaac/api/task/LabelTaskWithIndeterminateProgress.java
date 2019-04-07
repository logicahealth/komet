package sh.isaac.api.task;

import javafx.application.Platform;
import sh.isaac.api.Get;

public class LabelTaskWithIndeterminateProgress extends TimedTaskWithProgressTracker {
    boolean finished = false;
    public LabelTaskWithIndeterminateProgress(String label) {
        Platform.runLater(() -> {
            updateTitle(label);
            Get.activeTasks().add(this);
        });
    }

    public void finished() {
        this.finished = true;
    }

    @Override
    protected Object call() throws Exception {
        try {
            while (!finished) {
                Thread.sleep(1000);
            }
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }
}
