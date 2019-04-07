package sh.isaac.solor.rf2.exporters.core;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2ConceptExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;
    private final RF2Configuration rf2Configuration;

    public RF2ConceptExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper,
                              IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
        this.rf2Configuration = rf2Configuration;
        this.rf2ExportHelper = rf2ExportHelper;
        this.intStream = intStream;
        this.readSemaphore = readSemaphore;

        readSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {
        try {

            this.intStream
                    .forEach(nid ->{

                        super.clearLineOutput();
                        super.incrementProgressCount();

                        switch (this.rf2Configuration.getRf2ReleaseType()){
                            case FULL:

                                Get.concept(nid).getVersionList().stream()
                                        .forEach(version ->
                                                super.outputToWrite
                                                .append(this.rf2ExportHelper.getIdString(version) + "\t")
                                                .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                                .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                                .append(this.rf2ExportHelper.getConceptPrimitiveOrSufficientDefinedSCTID((ConceptVersion)version))
                                                .append("\r\n")
                                        );

                                break;
                            case SNAPSHOT:

                                super.outputToWrite
                                        .append(this.rf2ExportHelper.getIdString(nid) + "\t")
                                        .append(this.rf2ExportHelper.getTimeString(nid) + "\t")
                                        .append(this.rf2ExportHelper.getActiveString(nid) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(this.rf2ExportHelper.getModuleNid(nid)) + "\t")
                                        .append(this.rf2ExportHelper.getConceptPrimitiveOrSufficientDefinedSCTID(nid))
                                        .append("\r\n");

                                break;
                        }

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
