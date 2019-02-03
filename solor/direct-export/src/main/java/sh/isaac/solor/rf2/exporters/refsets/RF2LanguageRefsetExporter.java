package sh.isaac.solor.rf2.exporters.refsets;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2LanguageRefsetExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;

    public RF2LanguageRefsetExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper, IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
        this.rf2ExportHelper = rf2ExportHelper;
        this.intStream = intStream;
        this.readSemaphore = readSemaphore;

        readSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {

        try{

            this.intStream
                    .forEach(nid -> {
                        final StringBuilder stringBuilder = new StringBuilder();

                        for(SemanticChronology dialect : Get.assemblageService().getSemanticChronology(nid).getSemanticChronologyList()) {

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
                                        .append(rf2ExportHelper.getIdString(nid) + "\t")
                                        .append(rf2ExportHelper.getIdString(descriptionDialect.getComponentNid()))
                                        .append("\r");
                            }

                            super.writeToFile(stringBuilder.toString());
                        }
                    });

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
