package gov.vha.isaac.ochre.progress.provider;

import gov.vha.isaac.ochre.api.progress.ActiveTasks;
import javafx.concurrent.Task;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by kec on 1/2/16.
 */
@Service
@Singleton
public class ActiveTasksProvider implements ActiveTasks {
    ConcurrentSkipListSet<Task<?>> taskSet = new ConcurrentSkipListSet<>();

    @Override
    public Set<Task<?>> get() {
        return taskSet;
    }

    @Override
    public void add(Task<?> task) {
        taskSet.add(task);
    }

    @Override
    public void remove(Task<?> task) {
        taskSet.remove(task);
    }
}
