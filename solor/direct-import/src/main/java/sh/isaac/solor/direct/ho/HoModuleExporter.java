package sh.isaac.solor.direct.ho;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.nio.file.Path;
import java.util.List;

public class HoModuleExporter extends TimedTaskWithProgressTracker<Void> {


    @Override
    protected Void call() throws Exception {
        DataWriterService writer = Get.binaryDataWriter(Path.of("hdx_module"));
        int humanDxModuleNid = HoDirectImporter.HUMAN_DX_MODULE.getNid();
        Get.conceptService().getConceptChronologyStream().forEach(conceptChronology -> {
            List<Version> versions = conceptChronology.getVersionList();
            if (!versions.isEmpty()  && versions.get(0).getModuleNid() == humanDxModuleNid) {
                writer.put(conceptChronology);
            }
        });

        Get.assemblageService().getSemanticChronologyStream().forEach(semanticChronology -> {
            List<Version> versions = semanticChronology.getVersionList();
            if (!versions.isEmpty()  && versions.get(0).getModuleNid() == humanDxModuleNid) {
                writer.put(semanticChronology);
            }
        });
        writer.flush();
        writer.close();
        return null;
    }
}
