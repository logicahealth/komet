package sh.isaac.solor.rf2.utility;

import sh.isaac.api.Get;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 * aks8m - 5/19/18
 */
public class ZipExportDirectory extends TimedTaskWithProgressTracker<Void> {

    private final Path zipDirectoryPath;
    private final Path rootDirectoryPath;
    private final String rootDirectoryName;
    private final Semaphore taskSemaphore;
    private final int taskPermits;

    public ZipExportDirectory(Path zipDirectoryPath, Semaphore taskSemaphore, int taskPermits) {
        this.zipDirectoryPath = zipDirectoryPath;
        this.rootDirectoryPath = Paths.get(zipDirectoryPath.toString().replace(".zip",""));
        this.rootDirectoryName = zipDirectoryPath.getFileName().toString().replace(".zip", "");
        this.taskSemaphore = taskSemaphore;
        this.taskPermits = taskPermits;

        taskSemaphore.acquireUninterruptibly(taskPermits);
        updateTitle("Zipping Directory");
        updateMessage(this.rootDirectoryPath.toString());
        Get.activeTasks().add(this);
    }


    @Override
    protected Void call() {

       try {

           final FileOutputStream fos = new FileOutputStream(this.zipDirectoryPath.toString());
           final ZipOutputStream zos = new ZipOutputStream(fos);

           Files.walk(this.rootDirectoryPath).forEach(path -> {

               try {

                   if(!Files.isDirectory(path)) {

                       FileInputStream fis = new FileInputStream(path.toFile());

                       String zipFilePath = Paths.get(this.rootDirectoryName +
                               "/" +
                               path.toString().replace(this.rootDirectoryPath.toString(), "")).toString();

                       ZipEntry zipEntry = new ZipEntry(zipFilePath);
                       zos.putNextEntry(zipEntry);

                       byte[] bytes = new byte[1024];
                       int length;
                       while ((length = fis.read(bytes)) >= 0) {
                           zos.write(bytes, 0, length);
                       }

                       zos.closeEntry();
                       fis.close();
                   }

               } catch (IOException e) {
                   e.printStackTrace();
               }

           });

           zos.close();
           fos.close();

           Files.walk(this.rootDirectoryPath)
                   .sorted(Comparator.reverseOrder())
                   .map(Path::toFile)
                   .forEach(File::delete);

       } catch (IOException e) {
           e.printStackTrace();
       }finally {
           Get.activeTasks().remove(this);
           this.taskSemaphore.release(this.taskPermits);
       }

        return null;
    }
}
