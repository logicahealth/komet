package sh.isaac.api.task;

import org.apache.commons.lang3.StringUtils;
import javafx.application.Platform;
import sh.isaac.api.Get;

public class LabelTaskWithIndeterminateProgress extends TimedTaskWithProgressTracker<Void> {
    boolean finished = false;
    public LabelTaskWithIndeterminateProgress(String label) {
        Platform.runLater(() -> {
            updateTitle(label);
            Get.activeTasks().add(this);
        });
    }

    public void finished() {
        this.finished = true;
        this.cancel(true);  //send interrupt to thread below
    }

    @Override
    protected Void call() throws Exception {
        try {
            while (!finished) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    if (finished) {
                        break;
                    }
                    else {
                        LOG.warn("Interupted, but I'm not finished?  Ignoring");
                    }
                }
            }
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }

    protected String getSimpleName() {
        return StringUtils.isBlank(simpleTitle) ? super.getSimpleName() : simpleTitle;
    }
}
