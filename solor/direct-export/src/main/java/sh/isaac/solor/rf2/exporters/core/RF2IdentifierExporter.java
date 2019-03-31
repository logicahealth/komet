package sh.isaac.solor.rf2.exporters.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2IdentifierExporter extends RF2DefaultExporter {

    protected static final Logger LOG = LogManager.getLogger();
    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;

    public RF2IdentifierExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper, IntStream intStream, Semaphore readSemaphore) {
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

            final StringBuilder linesToWrite = new StringBuilder();

            this.intStream
                    .forEach(nid -> {

                        linesToWrite.setLength(0);

                        Get.concept(nid).getVersionList().stream()
                                .forEach(version -> {

                                    version.getChronology().getSemanticChronologyList().stream()
                                            .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.STRING)
                                            .filter(semanticChronology -> semanticChronology.getAssemblageNid() != TermAux.SNOMED_IDENTIFIER.getNid())
                                            .forEach(semanticChronology -> {

                                                linesToWrite
                                                        .append(this.rf2ExportHelper.getIdString(semanticChronology.getAssemblageNid()) + "\t")
                                                        .append(((StringVersion)semanticChronology.getVersionList().get(0)).getString() + "\t")
                                                        .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                                        .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                                        .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                                        .append(this.rf2ExportHelper.getIdString(version.getNid()))
                                                        .append("\r\n");
                                            });
                                });

                        super.writeStringToFile(linesToWrite.toString());
                    });

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
