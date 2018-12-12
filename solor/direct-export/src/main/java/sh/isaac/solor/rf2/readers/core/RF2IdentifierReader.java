package sh.isaac.solor.rf2.readers.core;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.observable.semantic.version.brittle.ObservableLoincVersion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class RF2IdentifierReader extends TimedTaskWithProgressTracker<List<String>>{

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;

    public RF2IdentifierReader(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, String message) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + message + " batch of size: " + chronologies.size());
        updateMessage("Processing batch of identifiers for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() {
        ArrayList<String> returnList = new ArrayList<>();

        try{

            for(Chronology chronology : this.chronologies){

                if(chronology instanceof ConceptChronology){

                    int stampNid = rf2ExportHelper.getSnapshotService()
                            .getObservableConceptVersion(chronology.getNid()).getStamps().findFirst().getAsInt();

                    StringBuilder conceptUUIDStringBuilder = new StringBuilder();

                    conceptUUIDStringBuilder.append("900000000000002006" + "\t")
                            .append(chronology.getPrimordialUuid().toString() + "\t")
                            .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                            .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                            .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                            .append(rf2ExportHelper.getIdString(chronology.getNid()))
                            .append("\r");

                    returnList.add(conceptUUIDStringBuilder.toString());


                }else if(chronology instanceof SemanticChronology){

                    StringBuilder semanticUUIDStringBuilder = new StringBuilder();
                    int stampNid = rf2ExportHelper.getSnapshotService().
                        getObservableSemanticVersion(chronology.getNid()).getStamps().findFirst().getAsInt();

                    semanticUUIDStringBuilder.append("900000000000002006" + "\t")
                            .append(chronology.getPrimordialUuid().toString() + "\t")
                            .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                            .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                            .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                            .append(rf2ExportHelper.getIdString(chronology.getNid()))
                            .append("\r");

                    returnList.add(semanticUUIDStringBuilder.toString());

                    switch (chronology.getVersionType()){
                        case LOINC_RECORD:
                            StringBuilder loincRecordStringBuilder = new StringBuilder();
                            ObservableLoincVersion observableLoincVersion =
                                ((LatestVersion<ObservableLoincVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                            loincRecordStringBuilder.append("900000000000002006" + "\t") //TODO to work out a New Scheme Identifier per spec
                                    .append(observableLoincVersion.getLoincNum() + "\t")
                                    .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getIdString(chronology.getNid()))
                                    .append("\r");
                            returnList.add(loincRecordStringBuilder.toString());
                            break;
                        case LONG:
                            StringBuilder longStringBuilder = new StringBuilder();
                            ObservableLongVersion observableLongVersion =
                                ((LatestVersion<ObservableLongVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                            longStringBuilder.append("900000000000002006" + "\t") //TODO to work out a New Scheme Identifier per spec
                                    .append(observableLongVersion.getLongValue() + "\t")
                                    .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getIdString(chronology.getNid()))
                                    .append("\r");
                            returnList.add(longStringBuilder.toString());
                            break;
                        case STRING:
                            StringBuilder stringStringBuilder = new StringBuilder();
                            ObservableStringVersion observableStringVersion =
                                ((LatestVersion<ObservableStringVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                            stringStringBuilder.append("900000000000002006" + "\t") //TODO to work out a New Scheme Identifier per spec
                                    .append(observableStringVersion.getString() + "\t")
                                    .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getIdString(chronology.getNid()))
                                    .append("\r");
                            returnList.add(stringStringBuilder.toString());
                            break;
                    }
                }

                completedUnitOfWork();
            }

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
