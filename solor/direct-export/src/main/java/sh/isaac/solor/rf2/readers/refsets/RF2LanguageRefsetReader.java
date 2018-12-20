package sh.isaac.solor.rf2.readers.refsets;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class RF2LanguageRefsetReader extends TimedTaskWithProgressTracker<List<String>> {

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;

    public RF2LanguageRefsetReader(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, String message) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + message + " batch of size: " + chronologies.size());
        updateMessage("Processing batch of descriptions for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() {
        ArrayList<String> returnList = new ArrayList<>();

        try{

            for(Chronology chronology : chronologies){
                StringBuilder stringBuilder = new StringBuilder();

                for(SemanticChronology dialect : chronology.getSemanticChronologyList()) {

                    if(dialect.getVersionType() == VersionType.COMPONENT_NID) {//Assuming a dialect semantic
                        ObservableComponentNidVersion descriptionDialect =
                                ((LatestVersion<ObservableComponentNidVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(dialect.getNid()))
                                        .get();
                        int stampNid = rf2ExportHelper.getSnapshotService()
                                .getObservableSemanticVersion(dialect.getNid()).getStamps().findFirst().getAsInt();

                        stringBuilder.append(descriptionDialect.getPrimordialUuid().toString() + "\t")
                                .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                                .append(rf2ExportHelper.getIdString(descriptionDialect.getAssemblageNid()) + "\t")
                                .append(rf2ExportHelper.getIdString(chronology) + "\t")
                                .append(rf2ExportHelper.getIdString(descriptionDialect.getComponentNid()))
                                .append("\r");
                    }
                }

                returnList.add(stringBuilder.toString());
                completedUnitOfWork();
            }

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
