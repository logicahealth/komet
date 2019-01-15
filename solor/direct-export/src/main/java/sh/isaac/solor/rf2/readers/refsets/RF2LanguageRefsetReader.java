package sh.isaac.solor.rf2.readers.refsets;

import org.apache.commons.lang.ArrayUtils;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.isaac.solor.rf2.utility.RF2FileWriter;
import sh.komet.gui.manifold.Manifold;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class RF2LanguageRefsetReader extends TimedTaskWithProgressTracker<Void> {

    private final RF2ExportHelper rf2ExportHelper;
    private final Stream<? extends Chronology> streamPage;
    private final Semaphore readSemaphore;
    private final Manifold manifold;
    private final RF2Configuration rf2Configuration;
    private final RF2FileWriter rf2FileWriter;

    public RF2LanguageRefsetReader(Stream streamPage, Semaphore readSemaphore, Manifold manifold,
                                   RF2Configuration rf2Configuration, long pageSize) {
        this.streamPage = streamPage;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        this.rf2Configuration = rf2Configuration;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);
        this.rf2FileWriter = new RF2FileWriter();

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + this.rf2Configuration.getMessage() + " batch of size: " + pageSize);
        updateMessage("Processing batch of descriptions for RF2 Export");
        addToTotalWork(pageSize + 1);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {
        ArrayList<Byte[]> writeBytes = new ArrayList<>();

        try{

            this.streamPage
                    .forEach(chronology -> {
                        final StringBuilder stringBuilder = new StringBuilder();

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

                        writeBytes.add(ArrayUtils.toObject(stringBuilder.toString().getBytes(Charset.forName("UTF-8"))));

                        completedUnitOfWork();

                    });

            updateTitle("Writing " + rf2Configuration.getMessage() + " RF2 file");
            updateMessage("Writing to " + rf2Configuration.getFilePath());

            rf2FileWriter.writeToFile(writeBytes, this.rf2Configuration);

            completedUnitOfWork();


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
