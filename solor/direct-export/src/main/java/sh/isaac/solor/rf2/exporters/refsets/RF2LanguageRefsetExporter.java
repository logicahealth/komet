package sh.isaac.solor.rf2.exporters.refsets;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
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

                        super.clearLineOutput();
                        super.incrementProgressCount();

                        Get.assemblageService().getSemanticChronology(nid).getSemanticChronologyList().stream()
                                .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.COMPONENT_NID)
                                .flatMap(semanticChronology -> semanticChronology.getVersionList().stream())
                                .forEach(version ->

                                    super.outputToWrite
                                            .append(version.getPrimordialUuid().toString() + "\t")
                                            .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                            .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(version.getAssemblageNid()) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(((ComponentNidVersion)version).getComponentNid()))
                                            .append("\r\n")
                                );

                        super.writeToFile();
                        super.tryAndUpdateProgressTracker();
                    });

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
