package sh.isaac.solor.rf2.exporters.core;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2DescriptionExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;

    public RF2DescriptionExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper,
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
        try{

            final StringBuilder linesToWrite = new StringBuilder();

            this.intStream
                    .forEach(nid -> {

                        linesToWrite.setLength(0);

                        for(Version version : Get.assemblageService().getSemanticChronology(nid).getVersionList()){
                            linesToWrite
                                    .append(this.rf2ExportHelper.getIdString(version) + "\t")
                                    .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                    .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                    .append(this.rf2ExportHelper.getIdString(((SemanticChronology)version.getChronology()).getReferencedComponentNid()) + "\t")
                                    .append(this.rf2ExportHelper.getLanguageCode(version) + "\t")
                                    .append(this.rf2ExportHelper.getTypeId((DescriptionVersion)version) + "\t")
                                    .append(this.rf2ExportHelper.getTerm((DescriptionVersion)version) + "\t")
                                    .append(this.rf2ExportHelper.getCaseSignificanceId((DescriptionVersion) version))
                                    .append("\r");
                        }

                        super.writeStringToFile(linesToWrite.toString());
                    });

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }




}
