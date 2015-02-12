package org.ihtsdo.otf.tcc.lookup.tasks;

import org.ihtsdo.otf.lookup.contracts.contracts.ActiveTaskSet;
import javafx.concurrent.Task;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.reactfx.EventStreams;
import org.reactfx.Subscription;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by kec on 9/13/14.
 */
public class TaskTicker implements Comparator<WeakReference<TaskTickHandler>> {


    private static final ConcurrentSkipListSet<WeakReference<TaskTickHandler>> subscriberSet = 
            new ConcurrentSkipListSet(new TaskTicker());
    
    private static final Subscription tickSubscription = EventStreams.ticks(Duration.ofSeconds(10))
            .subscribe(tick -> {

        Set<Task> taskSet = Hk2Looker.get().getService(ActiveTaskSet.class).get();
        HashSet<WeakReference<TaskTickHandler>> toRemove = new HashSet<>();

        taskSet.stream().map((task) -> {
            subscriberSet.stream().forEach((handlerReference) -> {
                TaskTickHandler handler = handlerReference.get();
                if (handler != null) {
                    handler.tick(task);
                } else {
                    toRemove.add(handlerReference);
                }
            });
            return task;
        }).forEach((_item) -> {
            toRemove.stream().forEach((handlerReference) -> {
                subscriberSet.remove(handlerReference);
            });
        });

    });

    public static void addTaskTickHandler(TaskTickHandler handler) {
        subscriberSet.add(new WeakReference(handler));
    }

    public static void removeTaskTickHandler(TaskTickHandler handler) {
        subscriberSet.remove(new WeakReference(handler));
    }

    @Override
    public int compare(WeakReference<TaskTickHandler> o1, WeakReference<TaskTickHandler> o2) {
            TaskTickHandler h1 = o1.get();
            TaskTickHandler h2 = o2.get();
            if (h1 == h2) {
                return 0;
            }
            if (h1 == null) {
                return -1;
            }
            if (h2 == null) {
                return 1;
            }

            return h1.getHandlerUuid().compareTo(h2.getHandlerUuid());
    }
}
