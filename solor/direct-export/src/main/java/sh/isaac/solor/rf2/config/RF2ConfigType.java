package sh.isaac.solor.rf2.config;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;

import java.util.function.Supplier;
import java.util.stream.Stream;

public enum RF2ConfigType {

    CONCEPT("id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId\r",
            "Snapshot/Terminology/sct2_Concept_Snapshot_TIME2.txt",
            "Concept",
            () -> Get.conceptService().getConceptChronologyStream()
    ),

    DESCRIPTION("id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode\r" +
            "\ttypeId\tterm\tcaseSignificanceId\r",
            "Snapshot/Terminology/sct2_Description_Snapshot_TIME2.txt",
            "Description",
            () -> Get.assemblageService().getSemanticChronologyStream()
            .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.DESCRIPTION)
    ),

    RELATIONSHIP("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\r" +
            "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r",
            "Snapshot/Terminology/sct2_Relationship_Snapshot_TIME2.txt",
            "Relationship",
            () -> Get.assemblageService().getSemanticChronologyStream()
            .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH)
            .filter(semanticChronology -> semanticChronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())
    ),

    STATED_RELATIONSHIP("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\r" +
            "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r",
            "Snapshot/Terminology/sct2_StatedRelationship_Snapshot_TIME2.txt",
            "Stated Relationship",
            () -> Get.assemblageService().getSemanticChronologyStream()
                    .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH)
                    .filter(semanticChronology -> semanticChronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid())
    ),

    IDENTIFIER("identifierSchemeId\talternateIdentifier\teffectiveTime\tactive\tmoduleId\treferencedComponentId\r",
            "Snapshot/Terminology/sct2_Identifier_Snapshot_TIME2.txt",
            "Identifier",
            () -> Stream.concat(
                    Get.conceptService().getConceptChronologyStream(),
                    Get.conceptService().getConceptChronologyStream()
                    .flatMap(conceptChronology -> conceptChronology.getSemanticChronologyList().stream()))
    ),

    TRANSITIVE_CLOSURE("subtypeId\tsupertypeId\r",
            "Snapshot/Terminology/sct2_TransitiveClosure_Snapshot_TIME2.txt",
            "Transitive Closure",
            () -> Get.assemblageService().getSemanticChronologyStream()
                    .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH)
                    .filter(semanticChronology -> semanticChronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())
    ),

    VERSIONED_TRANSITIVE_CLOSURE("subtypeId\tsupertypeId\teffectiveTime\tactive\r",
            "Snapshot/Terminology/sct2_VersionedTransitiveClosure_Snapshot_TIME2.txt",
            "Versioned Transitive Closure",
            () -> Get.assemblageService().getSemanticChronologyStream()
                    .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH)
                    .filter(semanticChronology -> semanticChronology.getAssemblageNid() == TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid())
    ),

    LANGUAGE_REFSET("id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\tacceptabilityId\r",
            "Snapshot/Refset/Language/der2_cRefset_LanguageSnapshot-LANGUAGE1_TIME2.txt",
            "Language Refset",
            () -> Get.assemblageService().getSemanticChronologyStream()
                    .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.DESCRIPTION)
    ),

    REFSET("id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\r",
            "Snapshot/Refset/der2_PATTERNRefset_SUMMARYSnapshot_TIME2.txt",
            "Refset",
            () -> Get.assemblageService().getSemanticChronologyStream()
                    .filter(semanticChronology ->
                            RF2Configuration.GetVersionTypesToExportAsRefsets()
                                    .contains(semanticChronology.getVersionType()))
    );


    private String fileHeader;
    private String filePath;
    private String message;
    private Supplier<Stream<? extends Chronology>> chronologyStreamSupplier;

    RF2ConfigType(String fileHeader, String filePath, String message, Supplier<Stream<? extends Chronology>> chronologyStreamSupplier) {
        this.fileHeader = fileHeader;
        this.filePath = filePath;
        this.message = message;
        this.chronologyStreamSupplier = chronologyStreamSupplier;
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

    protected Supplier<Stream<? extends Chronology>> getChronologyStreamSupplier() {
        return chronologyStreamSupplier;
    }
}
