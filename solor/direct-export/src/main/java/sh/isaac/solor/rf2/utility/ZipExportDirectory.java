package sh.isaac.solor.rf2.utility;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.util.zip.ZipFile;

/*
 * aks8m - 5/19/18
 */
public class ZipExportDirectory {

    public static void zip(String rootDir, String zipDir) {

        try {

            // Initiate ZipFile object with the path/name of the zip file.
            ZipFile zipFile = new ZipFile(zipDir);

            // Folder to add
            String folderToAdd = rootDir;

            // Initiate Zip Parameters which define various properties such
            // as compression method, etc.
            ZipParameters parameters = new ZipParameters();

            // set compression method to store compression
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

            // Set the compression level
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            // Add folder to the zip file
//            zipFile addFolder(rootDir, parameters);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
