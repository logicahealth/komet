package sh.isaac.solor.rf2.config;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.model.configuration.LanguageCoordinates;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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
        this.chronologyStream = rf2ConfigType.getChronologyStream();
        this.fileHeader = rf2ConfigType.getFileHeader();
    }

    public void addHeaderToExport(List<String> listToExport){
        listToExport.add(0, this.fileHeader);
    }

    public String getFilePath() {
        return this.rf2ConfigType.getFilePathWithDateTime(this.localDateTime, true);
    }

    public String getLanguageRefsetFilePath(int languageNid){
        return getFilePath()
                .replace("LANGUAGE1", LanguageCoordinates.conceptNidToIso639(languageNid));
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
