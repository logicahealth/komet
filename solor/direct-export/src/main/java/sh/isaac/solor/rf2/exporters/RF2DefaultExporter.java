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
    private final int progressStep = 10000;
    private int progressCount =  0;
    protected final StringBuilder outputToWrite;

    public RF2DefaultExporter(RF2Configuration rf2Configuration) {
        this.rf2Configuration = rf2Configuration;
        this.outputToWrite = new StringBuilder();

        initDirectoryAndFile();
        this.clearLineOutput();
        this.outputToWrite.append(rf2Configuration.getFileHeader());
        this.writeToFile();
        this.clearLineOutput();

        updateTitle("Exporting " + rf2Configuration.getMessage());
        updateMessage(rf2Configuration.getFilePath().getFileName().toString());
        addToTotalWork(this.rf2Configuration.getExportCount() / progressStep);
    }

    protected void clearLineOutput(){
        this.outputToWrite.setLength(0);
    }

    protected void incrementProgressCount(){
        this.progressCount++;
    }

    protected void tryAndUpdateProgressTracker(){
        if(this.progressCount % this.progressStep == 0){
            this.completedUnitOfWork();
        }
    }

    private void initDirectoryAndFile(){
        try {
            Files.createDirectories(rf2Configuration.getFilePath().getParent());
            Files.createFile(rf2Configuration.getFilePath());
        }catch (IOException ioE){
            ioE.printStackTrace();
            LOG.warn("Can't init directory and file: " + this.rf2Configuration.getFilePath());
        }
    }

    protected void writeToFile(){

        if(this.outputToWrite != null) {
            try {
                Files.write(rf2Configuration.getFilePath(), this.outputToWrite.toString().getBytes(Charset.forName("UTF-8")), StandardOpenOption.APPEND);
            } catch (Exception ioE) {
                ioE.printStackTrace();
            }
        }else{
            LOG.warn("Can't write NULL string to file: " + this.rf2Configuration.getFilePath());
        }
    }

    protected void writeToFile(List<String> strings){
        if(strings.size() > 0){
            strings.stream()
                    .forEach(string -> {
                        this.clearLineOutput();
                        this.outputToWrite.append(string);
                        this.writeToFile();
                    });
        }else {
            LOG.warn("Can't write NULL List<String> to file: " + this.rf2Configuration.getFilePath());
        }
    }
}
