package sh.isaac.solor.rf2.exporters.core;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2IdentifierExporter extends RF2DefaultExporter {

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

                        List<Version> versions = new ArrayList<>();
                        linesToWrite.setLength(0);

                        switch (Get.identifierService().getObjectTypeForComponent(nid)) {
                            case CONCEPT:
                                versions = Get.concept(nid).getVersionList();
                                break;
                            case SEMANTIC:
                                versions = Get.assemblageService().getSemanticChronology(nid).getVersionList();
                        }

                        versions.stream()
                                .forEach(version -> {

                                    linesToWrite
                                            .append("900000000000002006" + "\t")
                                            .append(version.getPrimordialUuid() + "\t")
                                            .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                            .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(version.getNid()))
                                            .append("\r");

                                    if(version instanceof StringVersion){

                                        linesToWrite
                                                .append(this.rf2ExportHelper.getIdString(version.getAssemblageNid()) + "\t")
                                                .append(((StringVersion)version).getString() + "\t")
                                                .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                                .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                                .append(this.rf2ExportHelper.getIdString(version.getNid()))
                                                .append("\r");
                                    }

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
