package sh.komet.gui.exportation;

import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final Map<ReaderSpecification, List<byte[]>> bytesToZip;


    public ZipExportFiles(ExportFormatType exportFormatType, File exportDirectory, Map<ReaderSpecification, List<byte[]>> bytesToZip) {
        this.exportFormatType = exportFormatType;
        this.exportDirectory = exportDirectory;
        this.bytesToZip = bytesToZip;
    }

    @Override
    protected Void call() throws Exception {

        final String rootDirName = "SnomedCT_SolorRF2_PRODUCTION_"
                + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(LocalDateTime.now());

        ZipOutputStream zipOut = new ZipOutputStream(
                new FileOutputStream(this.exportDirectory.getAbsolutePath() + "/" + rootDirName + ".zip"),
                StandardCharsets.UTF_8);

        for(Map.Entry<ReaderSpecification, List<byte[]>> entry : this.bytesToZip.entrySet()){

            ZipEntry zipEntry = new ZipEntry(entry.getKey().getFileName(rootDirName));
            try {
                zipOut.putNextEntry(zipEntry);

                entry.getValue().stream()
                        .forEach(bytes -> {
                            try {
                                zipOut.write(bytes, 0, bytes.length);
                            }catch (IOException ioE){
                                ioE.printStackTrace();
                            }
                        });
            }catch (IOException ioE){
                ioE.printStackTrace();
            }
        }

        zipOut.close();

        return null;
    }
}
