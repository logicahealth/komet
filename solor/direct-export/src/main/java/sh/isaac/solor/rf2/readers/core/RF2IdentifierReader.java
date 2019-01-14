package sh.isaac.solor.rf2.readers.core;

import org.apache.commons.lang.ArrayUtils;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.observable.semantic.version.brittle.ObservableLoincVersion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.isaac.solor.rf2.utility.RF2FileWriter;
import sh.komet.gui.manifold.Manifold;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class RF2IdentifierReader extends TimedTaskWithProgressTracker<Void>{

    private final RF2ExportHelper rf2ExportHelper;
    private final Stream<? extends Chronology> streamPage;
    private final Semaphore readSemaphore;
    private final Manifold manifold;
    private final RF2Configuration rf2Configuration;
    private final RF2FileWriter rf2FileWriter = new RF2FileWriter();

    public RF2IdentifierReader(Stream streamPage, Semaphore readSemaphore, Manifold manifold,
                               RF2Configuration rf2Configuration, long pageSize) {
        this.streamPage = streamPage;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        this.rf2Configuration = rf2Configuration;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + rf2Configuration.getMessage() + " batch of size: " + pageSize);
        updateMessage("Processing batch of identifiers for RF2 Export");
        addToTotalWork(pageSize + 1);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {
        final ArrayList<Byte[]> writeBytes = new ArrayList<>();

        try{

            this.streamPage
                    .forEach(chronology -> {

                        if(chronology instanceof ConceptChronology){

                            int stampNid = rf2ExportHelper.getSnapshotService()
                                    .getObservableConceptVersion(chronology.getNid()).getStamps().findFirst().getAsInt();

                            final StringBuilder conceptUUIDStringBuilder = new StringBuilder();

                            conceptUUIDStringBuilder.append("900000000000002006" + "\t")
                                    .append(chronology.getPrimordialUuid().toString() + "\t")
                                    .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getIdString(chronology.getNid()))
                                    .append("\r");

                            writeBytes.add(
                                    ArrayUtils.toObject(
                                            conceptUUIDStringBuilder.toString().getBytes(Charset.forName("UTF-8"))
                                    )
                            );


                        }else if(chronology instanceof SemanticChronology){

                            final StringBuilder semanticUUIDStringBuilder = new StringBuilder();
                            int stampNid = rf2ExportHelper.getSnapshotService().
                                    getObservableSemanticVersion(chronology.getNid()).getStamps().findFirst().getAsInt();

                            semanticUUIDStringBuilder.append("900000000000002006" + "\t")
                                    .append(chronology.getPrimordialUuid().toString() + "\t")
                                    .append(rf2ExportHelper.getTimeString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getActiveString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getModuleString(stampNid) + "\t")
                                    .append(rf2ExportHelper.getIdString(chronology.getNid()))
                                    .append("\r");

                            writeBytes.add(
                                    ArrayUtils.toObject(
                                            semanticUUIDStringBuilder.toString().getBytes(Charset.forName("UTF-8"))
                                    )
                            );

                            switch (chronology.getVersionType()){
                                case LOINC_RECORD:
                                    final StringBuilder loincRecordStringBuilder = new StringBuilder();
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

                                    writeBytes.add(
                                            ArrayUtils.toObject(
                                                    loincRecordStringBuilder.toString().getBytes(Charset.forName("UTF-8"))
                                            )
                                    );

                                    break;
                                case LONG:
                                    final StringBuilder longStringBuilder = new StringBuilder();
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

                                    writeBytes.add(
                                            ArrayUtils.toObject(
                                                    longStringBuilder.toString().getBytes(Charset.forName("UTF-8"))
                                            )
                                    );

                                    break;
                                case STRING:
                                    final StringBuilder stringStringBuilder = new StringBuilder();
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

                                    writeBytes.add(
                                            ArrayUtils.toObject(
                                                    stringStringBuilder.toString().getBytes(Charset.forName("UTF-8"))
                                            )
                                    );

                                    break;
                            }
                        }
                    });

            updateTitle("Writing " + rf2Configuration.getMessage() + " RF2 file");
            updateMessage("Writing to " + rf2Configuration.getFilePath());

            rf2FileWriter.writeToFile(writeBytes, rf2Configuration);
            completedUnitOfWork();

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
