package sh.isaac.solor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum ExportComponentType {

    RF2CONCEPT("SnomedCT_SolorRF2_PRODUCTION_"
            + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(LocalDateTime.now())
            + "/Snapshot/Terminology/sct2_Concept_Snapshot_"
            + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt"),
    RF2DESCRIPTION("SnomedCT_SolorRF2_PRODUCTION_"
            + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(LocalDateTime.now())
            + "/Snapshot/Terminology/sct2_Description_Snapshot_"
            + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt"),
    RF2RELATIONSHIP("SnomedCT_SolorRF2_PRODUCTION_"
            + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(LocalDateTime.now())
            + "/Snapshot/Terminology/sct2_Relationship_Snapshot_"
            + DateTimeFormatter.ofPattern("uuuuMMdd").format(LocalDateTime.now()) + ".txt");

    private String filePath;

    ExportComponentType(String filePath){
        this.filePath = filePath;
    }

    public String getFilePath(){
        return this.filePath;
    }

}
