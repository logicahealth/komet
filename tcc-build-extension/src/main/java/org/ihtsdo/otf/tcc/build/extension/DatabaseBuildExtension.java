package org.ihtsdo.otf.tcc.build.extension;

import javafx.concurrent.Task;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.ihtsdo.otf.tcc.lookup.tasks.TaskTickHandler;
import org.ihtsdo.otf.tcc.lookup.tasks.TaskTicker;

import java.util.Formatter;
import java.util.UUID;

/**
 * Created by kec on 9/13/14.
 */
public class DatabaseBuildExtension extends AbstractMavenLifecycleParticipant implements TaskTickHandler {


    @Requirement
    private Logger log;

    UUID handlerUuid = UUID.randomUUID();

    String buildExtensionName;

    public DatabaseBuildExtension(String buildExtensionName) {
        this.buildExtensionName = buildExtensionName;
    }

    @Override
    public void afterProjectsRead(MavenSession session)
            throws MavenExecutionException {
        TaskTicker.addTaskTickHandler(this);
        log.info(buildExtensionName + " extension loaded");
    }

    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        TaskTicker.removeTaskTickHandler(this);
        log.info(buildExtensionName + " extension session ended");

    }

    @Override
    public void tick(Task task) {
        log.info((new Formatter().format("%n    %s%n    %s%n    %.1f%% complete",
                task.getTitle(), task.getMessage(), task.getProgress() * 100)).toString());
    }

    @Override
    public UUID getHandlerUuid() {
        return handlerUuid;
    }
}

