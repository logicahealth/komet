package org.ihtsdo.otf.tcc.lookup.tasks;

import javafx.concurrent.Task;

import java.util.UUID;

/**
 * Created by kec on 9/13/14.
 */
public interface TaskTickHandler {
    public void tick(Task task);
    public UUID getHandlerUuid();

}
