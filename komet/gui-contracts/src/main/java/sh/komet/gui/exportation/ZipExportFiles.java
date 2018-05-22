package sh.komet.gui.exportation;

import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import javax.xml.soap.Text;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 * aks8m - 5/19/18
 */
public class ZipExportFiles extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final ExportFormatType exportFormatType;
    private final Manifold manifold;
    private final File exportDirectory;
    private final Map<ExportComponentType, List<String>>  linesToZipMap;

    private final Path terminologyPath;
    private final Path refsetPath;

    public ZipExportFiles(ExportFormatType exportFormatType, Manifold manifold, File exportDirectory, Map linesToZipMap) {
        this.exportFormatType = exportFormatType;
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.linesToZipMap = linesToZipMap;
        this.terminologyPath = Paths.get(this.exportDirectory.getAbsolutePath()+ "/Snapshot/Terminology");
        this.refsetPath = Paths.get(this.exportDirectory.getAbsolutePath() + "/Snapshot/Refset");
    }

    @Override
    protected Void call() throws Exception {



        ZipOutputStream zipOut = new ZipOutputStream(
                new FileOutputStream(this.exportDirectory.getAbsolutePath()
                        + "/SOLOR_SnapshotRF2_PRODUCTION_"
                        + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(LocalDateTime.now())
                        + ".zip"),
                StandardCharsets.UTF_8);

        for(Map.Entry<ExportComponentType, List<String>> entry : this.linesToZipMap.entrySet()){
            ZipEntry zipEntry;

            switch (entry.getKey()){
                case CONCEPT:
                    zipEntry = new ZipEntry("Snapshot/Terminology/Concepts.txt");//TODO follow RF2 Naming Convention
                    zipOut.putNextEntry(zipEntry);
                    break;
                case DESCRIPTION:
                    zipEntry = new ZipEntry("Snapshot/Terminology/Descriptions.txt");//TODO follow RF2 Naming Convention
                    zipOut.putNextEntry(zipEntry);
                    break;
                default:
                    break;
            }

            entry.getValue().stream()
                    .map(s -> {

                        byte[] bytes = new byte[1024];

                        try {
                            bytes = s.getBytes("UTF-8");
                        }catch (UnsupportedEncodingException uEE){
                            uEE.printStackTrace();
                        }
                        return bytes;
                    })
                    .forEach(bytes -> {

                        try {
                            zipOut.write(bytes, 0, bytes.length);
                        }catch (IOException ioE){
                            ioE.printStackTrace();
                        }
                    });
        }

        zipOut.close();

        return null;
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }


}
