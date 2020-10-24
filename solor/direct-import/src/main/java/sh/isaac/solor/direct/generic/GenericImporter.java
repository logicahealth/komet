package sh.isaac.solor.direct.generic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.solor.direct.generic.artifact.GenericArtifact;

/**
 * 2019-06-03
 * aks8m - https://github.com/aks8m
 */
public abstract class GenericImporter<T> {

    private final Semaphore writeSemaphore;
    private final int WRITE_PERMITS;
    protected static final Logger LOG = LogManager.getLogger();
    private final Set<UUID> uniquenessSet = new HashSet<>();

    public GenericImporter(Semaphore writeSemaphore, int WRITE_PERMITS) {
        this.writeSemaphore = writeSemaphore;
        this.WRITE_PERMITS = WRITE_PERMITS;
    }

    public abstract void runImport(T data);


    protected void checkForArtifactUniqueness(GenericArtifact componentFields, ArrayList artifactList){

        if(!Get.identifierService().hasUuid(componentFields.getComponentUUID())
                && uniquenessSet.add(componentFields.getComponentUUID())){
            artifactList.add(componentFields);
        } else{
            LOG.warn("Uniqueness conflict identified for Artifact " + componentFields.getComponentUUID());
        }
    }
    
    protected void waitForAll(){
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        this.writeSemaphore.release(WRITE_PERMITS);
    }
}
