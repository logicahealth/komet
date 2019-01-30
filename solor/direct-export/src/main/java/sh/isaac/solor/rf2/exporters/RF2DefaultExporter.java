package sh.isaac.solor.rf2.exporters;

import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2Configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * 2019-01-23
 * aks8m - https://github.com/aks8m
 */
public abstract class RF2DefaultExporter extends TimedTaskWithProgressTracker<Void> {

    private final RF2Configuration rf2Configuration;

    public RF2DefaultExporter(RF2Configuration rf2Configuration) {
        this.rf2Configuration = rf2Configuration;

        //Initial RF2 export file setup
        initDirectoryAndFile();
        writeToFile(rf2Configuration.getFileHeader());

        updateTitle("Exporting " + rf2Configuration.getMessage());
        updateMessage(rf2Configuration.getFilePath().getFileName().toString());
    }

    private void initDirectoryAndFile(){
        try {
            //Create Directories, File, and initial File Header
            Files.createDirectories(rf2Configuration.getFilePath().getParent());
            Files.createFile(rf2Configuration.getFilePath());
        }catch (IOException ioE){
            ioE.printStackTrace();
        }
    }

    protected void writeToFile(String stringToWrite){
        try {
            Files.write(rf2Configuration.getFilePath(), stringToWrite.getBytes(Charset.forName("UTF-8")), StandardOpenOption.APPEND);
        }catch (IOException ioE){
            ioE.printStackTrace();
        }
    }
}
