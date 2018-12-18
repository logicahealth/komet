package sh.isaac.solor.rf2.config;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.model.configuration.LanguageCoordinates;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.*;

public class RF2Configuration {

    private String fileHeader;
    private String filePath;
    private RF2ConfigType rf2ConfigType;
    private String message;
    private LocalDateTime localDateTime;
    private Stream<? extends Chronology> chronologyStream;

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.chronologyStream = rf2ConfigType.getChronologystreamSupplier().get();
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.setFilePath();
    }

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime,
                            Supplier<Stream<? extends Chronology>> chronologyStreamSupplier, int languageNid) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.chronologyStream = chronologyStreamSupplier.get();
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.setFilePath(languageNid);
        this.setMessage(rf2ConfigType.getMessage() + " - " + LanguageCoordinates.conceptNidToIso639(languageNid));
    }

    public void addHeaderToExport(List<String> listToExport){
        listToExport.add(0, this.fileHeader);
    }

    private void setFilePath(){
        this.filePath = this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime));
    }

    private void setFilePath(int languageNid){
        this.filePath = this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                .replace("LANGUAGE1", LanguageCoordinates.conceptNidToIso639(languageNid));
    }

    private void setMessage(String message){
        this.message = message;
    }

    public String getFilePath() {
        return this.filePath ;
    }

    public RF2ConfigType getRf2ConfigType() {
        return rf2ConfigType;
    }

    public String getMessage() {
        return this.rf2ConfigType.getMessage();
    }

    public Stream<? extends Chronology> getChronologyStream() {
        return chronologyStream;
    }
}
