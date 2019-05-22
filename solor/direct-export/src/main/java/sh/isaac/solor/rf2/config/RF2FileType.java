package sh.isaac.solor.rf2.config;

public enum RF2FileType {

    CONCEPT("id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId\r\n",
            "RELEASETYPE/Terminology/sct2_Concept_RELEASETYPE__TIME2.txt",
            "Concept"
    ),

    DESCRIPTION("id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode" +
            "\ttypeId\tterm\tcaseSignificanceId\r\n",
            "RELEASETYPE/Terminology/sct2_Description_RELEASETYPE__TIME2.txt",
            "Description"
    ),

    RELATIONSHIP("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\t" +
            "relationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r\n",
            "RELEASETYPE/Terminology/sct2_Relationship_RELEASETYPE__TIME2.txt",
            "Relationship"
    ),

    STATED_RELATIONSHIP("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\t" +
            "relationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r\n",
            "RELEASETYPE/Terminology/sct2_StatedRelationship_RELEASETYPE__TIME2.txt",
            "Stated Relationship"
    ),

    IDENTIFIER("identifierSchemeId\talternateIdentifier\teffectiveTime\tactive\tmoduleId\treferencedComponentId\r\n",
            "RELEASETYPE/Terminology/sct2_Identifier_RELEASETYPE__TIME2.txt",
            "Identifier"
    ),

    LANGUAGE_REFSET("id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\tacceptabilityId\r\n",
            "RELEASETYPE/Refset/Language/der2_cRefset_LanguageRELEASETYPE-LANGUAGE1__TIME2.txt",
            "Language Refset"
    ),

    REFSET("id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\t",
            "RELEASETYPE/Refset/der2_PATTERNRefset_SUMMARYRELEASETYPE__TIME2.txt",
            "Refset"
    );

    private String fileHeader;
    private String filePath;
    private String message;

    RF2FileType(String fileHeader, String filePath, String message) {
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
