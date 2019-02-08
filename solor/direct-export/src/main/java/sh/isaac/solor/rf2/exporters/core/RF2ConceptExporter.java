package sh.isaac.solor.rf2.exporters.core;

import sh.isaac.api.Get;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2ConceptExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;

    public RF2ConceptExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper,
                              IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
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
                    .forEach(nid -> super.writeStringToFile(
                            this.rf2ExportHelper.getRF2CommonElements(nid)
                                    .append(this.rf2ExportHelper.getConceptPrimitiveOrSufficientDefinedSCTID(nid))
                                    .append("\r")
                                    .toString()
                    ));

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
