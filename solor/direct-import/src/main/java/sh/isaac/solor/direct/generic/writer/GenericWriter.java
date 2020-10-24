package sh.isaac.solor.direct.generic.writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.util.List;

/**
 * 2019-06-03
 * aks8m - https://github.com/aks8m
 */
public abstract class GenericWriter extends TimedTaskWithProgressTracker<Void> {

    private final List<IndexBuilderService> indexers;
    static final Logger LOG = LogManager.getLogger();
    protected final int batchSize = 10000;

    GenericWriter(String taskTitle, String taskMessage, int taskTotalSize) {

        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle(taskTitle);
        updateMessage(taskMessage);
        addToTotalWork(taskTotalSize / this.batchSize);
    }

    protected void index(Chronology chronicle) {

        for (IndexBuilderService indexer: indexers) {
            indexer.indexNow(chronicle);
        }
    }
}
