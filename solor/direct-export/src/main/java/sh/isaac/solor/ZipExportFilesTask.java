package sh.isaac.solor;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.ExportComponentType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 * aks8m - 5/19/18
 */
public class ZipExportFilesTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final Map<ExportComponentType, List<String>> linesToWriteMap;
    private final Semaphore readSemaphore;
    private final String zipFileName;

    public ZipExportFilesTask(File exportDirectory, Map<ExportComponentType, List<String>> linesToWriteMap, Semaphore readSemaphore, String zipFileName) {
        this.exportDirectory = exportDirectory;
        this.linesToWriteMap = linesToWriteMap;
        this.readSemaphore = readSemaphore;
        this.zipFileName = zipFileName;
    }

    @Override
    protected Void call() throws Exception {

        try {

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(this.exportDirectory + this.zipFileName), StandardCharsets.UTF_8);

            for (Map.Entry<ExportComponentType, List<String>> entry : this.linesToWriteMap.entrySet()) {

                ZipEntry zipEntry = new ZipEntry(entry.getKey().getFilePath());
                try {
                    zipOut.putNextEntry(zipEntry);

                    entry.getValue().stream()
                            .forEach(s -> {

                                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

                                try {
                                    zipOut.write(bytes, 0, bytes.length);
                                } catch (IOException ioE) {
                                    ioE.printStackTrace();
                                }
                            });
                } catch (IOException ioE) {
                    ioE.printStackTrace();
                }
            }

            zipOut.close();

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
