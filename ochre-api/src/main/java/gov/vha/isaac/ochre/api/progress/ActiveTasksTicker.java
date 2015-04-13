package gov.vha.isaac.ochre.api.progress;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.ticker.Ticker;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Created by kec on 4/9/15.
 */
public class ActiveTasksTicker {
    private static final Logger log = LogManager.getLogger();

    private static final Ticker ticker = new Ticker();

    public static void stop() {
        ticker.stop();
    }

    public static void start(int intervalInSeconds) {
        ticker.start(intervalInSeconds, (tick) -> {
            Set<Task> taskSet = LookupService.get().getService(ActiveTasks.class).get();
            taskSet.stream().forEach((task) -> {
                double percentProgress = task.getProgress() * 100;
                if (percentProgress < 0) {
                    percentProgress = 0;
                }
                log.printf(org.apache.logging.log4j.Level.INFO, "%n    %s%n    %s%n    %.1f%% complete",
                        task.getTitle(), task.getMessage(), percentProgress);
            });
        });
     }

}
