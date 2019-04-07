package sh.isaac.solor.direct.ho;

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class HoModuleExporter extends TimedTaskWithProgressTracker<Void> {


    @Override
    protected Void call() throws Exception {

        writeModule(HoDirectImporter.HUMAN_DX_MODULE, "hdx_module.ibdf");
        writeModule(HoDirectImporter.LEGACY_HUMAN_DX_MODULE, "hdx_legacy_module.ibdf");

        return null;
    }

    private void writeModule(ConceptProxy moduleNid, String fileName) throws IOException {
        File rootFile = new File(System.getProperty("user.home") + "/solor/export");
        DataWriterService writer = Get.binaryDataWriter(new File(rootFile, fileName).toPath());
        Get.conceptService().getConceptChronologyStream().forEach(conceptChronology -> {
            List<Version> versions = conceptChronology.getVersionList();
            if (!versions.isEmpty()  && versions.get(0).getModuleNid() == moduleNid.getNid()) {
                writer.put(conceptChronology);
            }
        });

        Get.assemblageService().getSemanticChronologyStream().forEach(semanticChronology -> {
            List<Version> versions = semanticChronology.getVersionList();
            if (!versions.isEmpty()  && versions.get(0).getModuleNid() == moduleNid.getNid()) {
                writer.put(semanticChronology);
            }
        });
        writer.flush();
        writer.close();
    }
}
