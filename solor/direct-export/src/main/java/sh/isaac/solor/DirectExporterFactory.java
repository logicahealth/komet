package sh.isaac.solor;

import sh.isaac.solor.rf2.RF2DirectExporter;
import sh.komet.gui.manifold.Manifold;

import java.io.File;

public class DirectExporterFactory {

    public static RF2DirectExporter GetRF2DirectExporter(Manifold manifold, File exportDirectory, String exportMessage){
        return new RF2DirectExporter(manifold, exportDirectory, exportMessage);
    }


}
