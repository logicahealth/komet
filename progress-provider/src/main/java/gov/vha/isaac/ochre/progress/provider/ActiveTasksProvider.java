package gov.vha.isaac.ochre.progress.provider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import gov.vha.isaac.ochre.api.progress.ActiveTasks;
import javafx.concurrent.Task;

/**
 * Created by kec on 1/2/16.
 */
@Service
@Singleton
public class ActiveTasksProvider implements ActiveTasks {
    Set<Task<?>> taskSet = ConcurrentHashMap.newKeySet();

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
