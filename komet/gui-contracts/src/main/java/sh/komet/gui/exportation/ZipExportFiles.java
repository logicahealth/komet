package sh.komet.gui.exportation;

import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final File exportDirectory;
    private final Map<ExportComponentType, List<String>>  linesToZipMap;


    public ZipExportFiles(ExportFormatType exportFormatType, File exportDirectory, Map linesToZipMap) {
        this.exportFormatType = exportFormatType;
        this.exportDirectory = exportDirectory;
        this.linesToZipMap = linesToZipMap;
    }

    @Override
    protected Void call() throws Exception {

        LocalDateTime localDateTime = LocalDateTime.now();
        String dateTimeFormattedString = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime);
        String dateFormattedString = DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime);

        ZipOutputStream zipOut = new ZipOutputStream(
                new FileOutputStream(this.exportDirectory.getAbsolutePath()
                        + "/SnomedCT_SnapshotRF2_PRODUCTION_"
                        + dateTimeFormattedString
                        + ".zip"),
                StandardCharsets.UTF_8);

        for(Map.Entry<ExportComponentType, List<String>> entry : this.linesToZipMap.entrySet()){
            ZipEntry zipEntry;

            switch (entry.getKey()){
                case CONCEPT:
                    zipEntry = new ZipEntry("Snapshot/Terminology/sct2_Concept_Snapshot_"
                            + dateFormattedString + ".txt");
                    zipOut.putNextEntry(zipEntry);
                    break;
                case DESCRIPTION:
                    zipEntry = new ZipEntry("Snapshot/Terminology/sct2_Description_Snapshot_"
                            + dateFormattedString + ".txt");
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
}
