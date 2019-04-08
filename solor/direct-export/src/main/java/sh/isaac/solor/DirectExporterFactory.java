package sh.isaac.solor;

import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.solor.rf2.RF2DirectExporter;

import java.io.File;

public class DirectExporterFactory {

    public static RF2DirectExporter GetRF2DirectExporter(ManifoldCoordinate manifold, File exportDirectory, String exportMessage){
        return new RF2DirectExporter(manifold, exportDirectory, exportMessage);
    }


}
