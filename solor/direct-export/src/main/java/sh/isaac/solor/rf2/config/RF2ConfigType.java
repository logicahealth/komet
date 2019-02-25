package sh.isaac.solor.rf2.config;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum RF2ConfigType {

    CONCEPT("id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId\r",
            "Full/Terminology/sct2_Concept_Full_TIME2.txt",
            "Concept"
    ),

    DESCRIPTION("id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode" +
            "\ttypeId\tterm\tcaseSignificanceId\r",
            "Full/Terminology/sct2_Description_Full_TIME2.txt",
            "Description"
    ),

    RELATIONSHIP("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\t" +
            "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r",
            "Full/Terminology/sct2_Relationship_Full_TIME2.txt",
            "Relationship"
    ),

    STATED_RELATIONSHIP("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\r" +
            "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r",
            "Full/Terminology/sct2_StatedRelationship_Full_TIME2.txt",
            "Stated Relationship"
    ),

    IDENTIFIER("identifierSchemeId\talternateIdentifier\teffectiveTime\tactive\tmoduleId\treferencedComponentId\r",
            "Full/Terminology/sct2_Identifier_Full_TIME2.txt",
            "Identifier"
    ),

    TRANSITIVE_CLOSURE("subtypeId\tsupertypeId\r",
            "Full/Terminology/sct2_TransitiveClosure_Full_TIME2.txt",
            "Transitive Closure"
    ),

    VERSIONED_TRANSITIVE_CLOSURE("subtypeId\tsupertypeId\teffectiveTime\tactive\r",
            "Full/Terminology/sct2_VersionedTransitiveClosure_Full_TIME2.txt",
            "Versioned Transitive Closure"
    ),

    LANGUAGE_REFSET("id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\tacceptabilityId\r",
            "Full/Refset/Language/der2_cRefset_LanguageFull-LANGUAGE1_TIME2.txt",
            "Language Refset"
    ),

    REFSET("id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\t",
            "Full/Refset/der2_PATTERNRefset_SUMMARYFull_TIME2.txt",
            "Refset"
    );


    private String fileHeader;
    private String filePath;
    private String message;

    RF2ConfigType(String fileHeader, String filePath, String message) {
        this.fileHeader = fileHeader;
        this.filePath = filePath;
        this.message = message;
    }

    protected String getFileHeader() {
        return fileHeader;
    }

    protected String getFilePath(){
        return filePath;
    }

    protected String getMessage() {
        return message;
    }
}
