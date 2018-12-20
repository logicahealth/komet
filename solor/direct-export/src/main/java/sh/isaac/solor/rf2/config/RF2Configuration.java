package sh.isaac.solor.rf2.config;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
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
    private Supplier<Stream<? extends Chronology>> chronologyStreamSupplier;

    private static List<VersionType> versionTypesToExportAsRefsets = new ArrayList<>();
    static {
        versionTypesToExportAsRefsets.add(VersionType.MEMBER);
        versionTypesToExportAsRefsets.add(VersionType.LOINC_RECORD);
        versionTypesToExportAsRefsets.add(VersionType.COMPONENT_NID);
        versionTypesToExportAsRefsets.add(VersionType.STRING);
        versionTypesToExportAsRefsets.add(VersionType.LONG);
        versionTypesToExportAsRefsets.add(VersionType.Nid1_Int2);
        versionTypesToExportAsRefsets.add(VersionType.Nid1_Nid2);
        versionTypesToExportAsRefsets.add(VersionType.Nid1_Str2);
        versionTypesToExportAsRefsets.add(VersionType.Str1_Str2);
        versionTypesToExportAsRefsets.add(VersionType.Nid1_Nid2_Int3);
        versionTypesToExportAsRefsets.add(VersionType.Nid1_Nid2_Str3);
        versionTypesToExportAsRefsets.add(VersionType.Str1_Nid2_Nid3_Nid4);
        versionTypesToExportAsRefsets.add(VersionType.Str1_Str2_Nid3_Nid4);
        versionTypesToExportAsRefsets.add(VersionType.Str1_Str2_Nid3_Nid4_Nid5);
        versionTypesToExportAsRefsets.add(VersionType.Nid1_Int2_Str3_Str4_Nid5_Nid6);
        versionTypesToExportAsRefsets.add(VersionType.Int1_Int2_Str3_Str4_Str5_Nid6_Nid7);
        versionTypesToExportAsRefsets.add(VersionType.Str1_Str2_Str3_Str4_Str5_Str6_Str7);
    }

    public static Supplier<Stream<? extends Chronology>> GetLanguageStreamSupplier(){
        return RF2ConfigType.LANGUAGE_REFSET.getChronologystreamSupplier();
    }

    public static Supplier<Stream<? extends Chronology>> GetRefsetStreamSupplier(){
        return RF2ConfigType.REFSET.getChronologystreamSupplier();
    }

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.chronologyStreamSupplier = rf2ConfigType.getChronologystreamSupplier();
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.setFilePath();
        this.message = rf2ConfigType.getMessage();
    }

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime,
                            Supplier<Stream<? extends Chronology>> chronologyStreamSupplier, int languageNid) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.chronologyStreamSupplier = chronologyStreamSupplier;
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.setFilePath(languageNid);
        this.message = rf2ConfigType.getMessage() + " - " + LanguageCoordinates.conceptNidToIso639(languageNid);
    }

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime,
                            Supplier<Stream<? extends Chronology>> chronologyStreamSupplier, VersionType versionType, String assemblageFQN) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.chronologyStreamSupplier = chronologyStreamSupplier;
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.setFilePath(versionType, assemblageFQN);
        this.message = rf2ConfigType.getMessage() + " " + assemblageFQN;
    }

    public void addHeaderToExport(List<String> listToExport){
        listToExport.add(0, this.fileHeader);
    }

    private void setFilePath(){//Core Files
        this.filePath = this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime));
    }

    private void setFilePath(int languageNid){//Language Refsets
        this.filePath = this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                .replace("LANGUAGE1", LanguageCoordinates.conceptNidToIso639(languageNid));
    }

    private void setFilePath(VersionType versionType, String assemblageFQN){//Refsets
        String pattern = "";


        switch (versionType){
            case MEMBER:
            case LOINC_RECORD:
                break;
            case COMPONENT_NID:
                pattern = "c";
                break;
            case STRING:
            case LONG:
                pattern = "s";
                break;
            case Nid1_Int2:
                pattern = "ci";
                break;
            case Nid1_Nid2:
                pattern = "cc";
                break;
            case Nid1_Str2:
                pattern = "cs";
                break;
            case Str1_Str2:
                pattern = "ss";
                break;
            case Nid1_Nid2_Int3:
                pattern = "cci";
                break;
            case Nid1_Nid2_Str3:
                pattern = "ccs";
                break;
            case Str1_Nid2_Nid3_Nid4:
                pattern = "sccc";
                break;
            case Str1_Str2_Nid3_Nid4:
                pattern = "sscc";
                break;
            case Str1_Str2_Nid3_Nid4_Nid5:
                pattern = "ssccc";
                break;
            case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                pattern = "cisscc";
                break;
            case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                pattern = "iissscc";
                break;
            case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                pattern = "sssssss";
                break;
        }

        this.filePath = this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                .replace("PATTERN", pattern)
                .replace("SUMMARY", assemblageFQN).replace(" ", "");
    }

    public static List<VersionType> GetVersionTypesToExportAsRefsets() {
        return versionTypesToExportAsRefsets;
    }

    public String getFilePath() {
        return this.filePath ;
    }

    public RF2ConfigType getRf2ConfigType() {
        return rf2ConfigType;
    }

    public String getMessage() {
        return this.message;
    }

    public Stream<? extends Chronology> getChronologyStream() {
        return this.chronologyStreamSupplier.get();
    }

    public Supplier<Stream<? extends Chronology>> getChronologyStreamSupplier() {
        return chronologyStreamSupplier;
    }

    public void updateFileHeader(String headerUpdate){
        this.fileHeader = this.fileHeader + headerUpdate;
    }
}
