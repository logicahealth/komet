package sh.isaac.solor.rf2.exporters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2Configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * 2019-01-23
 * aks8m - https://github.com/aks8m
 */
public abstract class RF2DefaultExporter extends TimedTaskWithProgressTracker<Void> {

    protected static final Logger LOG = LogManager.getLogger();
    private final RF2Configuration rf2Configuration;

    public RF2DefaultExporter(RF2Configuration rf2Configuration) {
        this.rf2Configuration = rf2Configuration;

        //Initial RF2 export file setup
        initDirectoryAndFile();
        writeStringToFile(rf2Configuration.getFileHeader());

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

    protected void writeStringToFile(String stringToWrite){

        if(stringToWrite != null) {
            try {
                Files.write(rf2Configuration.getFilePath(), stringToWrite.getBytes(Charset.forName("UTF-8")), StandardOpenOption.APPEND);
            } catch (Exception ioE) {
                ioE.printStackTrace();
            }
        }else{
            LOG.warn("Can't write NULL string to file: " + this.rf2Configuration.getFilePath());
        }
    }

    protected void writeStringsToFile(List<String> strings){
        if(strings.size() > 0) {
            strings.stream().forEach(this::writeStringToFile);
        }else {
            LOG.warn("Can't write NULL List<String> to file: " + this.rf2Configuration.getFilePath());
        }
    }
}
