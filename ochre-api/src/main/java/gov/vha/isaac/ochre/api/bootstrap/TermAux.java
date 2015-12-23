package gov.vha.isaac.ochre.api.bootstrap;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import java.util.UUID;

public class TermAux {

    public static ConceptSpecification USER
            = new ConceptProxy("user",
                    UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
    public static ConceptSpecification IS_A
            = new ConceptProxy("is a (relationship type)",
                    UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpecification CURRENT
            = new ConceptProxy("current (active status type)",
                    UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));
    public static ConceptSpecification RETIRED
            = new ConceptProxy("retired (inactive status type)",
                    UUID.fromString("e1956e7b-08b4-3ad0-ab02-b411869f1c09"));
    public static ConceptSpecification INACTIVE_STATUS
            = new ConceptProxy("inactive (inactive status type)",
                    UUID.fromString("1464ec56-7118-3051-9d21-0f95c1a39080"));
    public static ConceptSpecification MOVED_TO
            = new ConceptProxy("moved elsewhere (inactive status type)",
                    UUID.fromString("76367831-522f-3250-83a4-8609ab298436"));
    public static ConceptSpecification REL_QUALIFIER_CHAR
            = new ConceptProxy("qualifier (characteristic type)",
                    UUID.fromString("416ad0e4-b6bc-386c-900e-121c58b20f55"));
    public static ConceptSpecification REL_HISTORIC
            = new ConceptProxy("historical (characteristic type)",
                    UUID.fromString("1d054ca3-2b32-3004-b7af-2701276059d5"));
    public static ConceptSpecification REL_STATED_CHAR
            = new ConceptProxy("stated (defining characteristic type)",
                    UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));
    public static ConceptSpecification REL_INFERED_CHAR
            = new ConceptProxy("defining",
                    UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpecification REL_NOT_REFINABLE
            = new ConceptProxy("not refinable (refinability type)",
                    UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpecification REL_OPTIONALLY_REFINABLE
            = new ConceptProxy("optional (refinability type)",
                    UUID.fromString("c3d997d3-b0a4-31f8-846f-03fa874f5479"));
    public static ConceptSpecification WB_AUX_PATH
            = new ConceptProxy("master", //new name from isaacmetadata
                    UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66"));
    public static ConceptSpecification IHTSDO_CLASSIFIER
            = new ConceptProxy("IHTSDO Classifier",
                    UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
    public static ConceptSpecification PATH
            = new ConceptProxy("path",
                    UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));
    public static ConceptSpecification PATH_ORIGIN_ASSEMBLAGE
            = new ConceptProxy("Path origin reference set",
                    UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b"));
    public static ConceptSpecification PATH_ASSEMBLAGE
            = new ConceptProxy("paths",
                    UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f"));
    public static ConceptSpecification GENERATED_UUID
            = new ConceptProxy("generated UUID",
                    UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66"));
    public static ConceptSpecification SNOMED_IDENTIFIER
            = new ConceptProxy("SNOMED integer id",
                    UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"));
    public static ConceptSpecification SNOMED_RELEASE_PATH
            = new ConceptProxy("SNOMED Release path",
                    UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2"));
    public static ConceptSpecification SNOMED_CT_CORE_MODULE
            = new ConceptProxy("SNOMED CT core module",
                    UUID.fromString("1b4f1ba5-b725-390f-8c3b-33ec7096bdca"));

    public static ConceptSpecification REFSET_IDENTITY
            = new ConceptProxy("refset identity",
                    UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da"));
    public static ConceptSpecification UNSPECIFIED_MODULE
            = new ConceptProxy("Module (core metadata concept)",
                    UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));
    public static ConceptSpecification ISAAC_MODULE
            = new ConceptProxy("ISAAC Module",
                    UUID.fromString("f680c868-f7e5-5d0e-91f2-615eca8f8fd2"));

    //ConceptSpecs for Description types
   // SCT ID:	900000000000013009
    public static ConceptSpecification SYNONYM_DESCRIPTION_TYPE
            = new ConceptProxy("synonym",
                    UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"));

     public static ConceptSpecification FULLY_SPECIFIED_DESCRIPTION_TYPE
            = new ConceptProxy("Fully specified name (core metadata concept)",
                    UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));
    public static ConceptSpecification DEFINITION_DESCRIPTION_TYPE
            = new ConceptProxy("Definition (core metadata concept)",
                    UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09"));
    //ConceptSpecs for language refsets
    public static ConceptSpecification US_DIALECT_ASSEMBLAGE
            = new ConceptProxy("United States of America English language reference set",
                    UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));
    public static ConceptSpecification GB_DIALECT_ASSEMBLAGE
            = new ConceptProxy("Great Britain English language reference set",
                    UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));

    public static ConceptSpecification ENGLISH_LANGUAGE
            = new ConceptProxy("English language",
                    UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558"));

    public static ConceptSpecification SPANISH_LANGUAGE
            = new ConceptProxy("Spanish language", "0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e");

    public static ConceptSpecification FRENCH_LANGUAGE
            = new ConceptProxy("French language", "8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e");

    public static ConceptSpecification DANISH_LANGUAGE
            = new ConceptProxy("Danish language", "7e462e33-6d94-38ae-a044-492a857a6853");

    public static ConceptSpecification POLISH_LANGUAGE
            = new ConceptProxy("Polish language", "c924b887-da88-3a72-b8ea-fa86990467c9");

    public static ConceptSpecification DUTCH_LANGUAGE
            = new ConceptProxy("Dutch language", "674ad858-0224-3f90-bcf0-bc4cab753d2d");

    public static ConceptSpecification LITHUANIAN_LANGUAGE
            = new ConceptProxy("Lithuanian language", "e9645d95-8a1f-3825-8feb-0bc2ee825694");

    public static ConceptSpecification CHINESE_LANGUAGE
            = new ConceptProxy("Chinese language", "ba2efe6b-fe56-3d91-ae0f-3b389628f74c");

    public static ConceptSpecification JAPANESE_LANGUAGE
            = new ConceptProxy("Japanese language", "b90a1097-29e3-42bc-8576-8e8eb6715c44");

    public static ConceptSpecification SWEDISH_LANGUAGE
            = new ConceptProxy("Swedish language", "9784a791-8fdb-32f7-88da-74ab135fe4e3");

    public static ConceptSpecification ENGLISH_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("English description assemblage", "45021920-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification SPANISH_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Spanish description assemblage", "45021c36-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification FRENCH_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("French description assemblage", "45021dbc-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification DANISH_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Danish description assemblage", "45021f10-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification POLISH_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Polish description assemblage", "45022140-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification DUTCH_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Dutch description assemblage", "45022280-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification LITHUANIAN_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Lithuanian description assemblage", "45022410-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification CHINESE_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Chinese description assemblage", "45022532-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification JAPANESE_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Japanese description assemblage", "450226cc-9567-11e5-8994-feff819cdc9f");
    
    public static ConceptSpecification SWEDISH_DESCRIPTION_ASSEMBLAGE
            = new ConceptProxy("Swedish description assemblage", "45022848-9567-11e5-8994-feff819cdc9f");

     // SCT ID:	900000000000548007
    public static ConceptSpecification PREFERRED
            = new ConceptProxy("preferred",
                    UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));
    // SCT ID:	900000000000549004
    public static ConceptSpecification ACCEPTABLE
            = new ConceptProxy("acceptable",
                    UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));
    // SCT ID:	900000000000011006
    public static ConceptSpecification INFERRED_RELATIONSHIP
            = new ConceptProxy("Inferred",
                    UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"));
    // SCT ID:	900000000000010007
    public static ConceptSpecification STATED_RELATIONSHIP
            = new ConceptProxy("stated elationship",
                    UUID.fromString("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"));
    
    public static ConceptSpecification DESCRIPTION_CASE_SENSITIVE
            = new ConceptProxy("description case sensitive",
                    UUID.fromString("0def37bc-7e1b-384b-a6a3-3e3ceee9c52e"));
    
   public static ConceptSpecification DESCRIPTION_NOT_CASE_SENSITIVE
            = new ConceptProxy("description not case sensitive",
                    UUID.fromString("ecea41a2-f596-3d98-99d1-771b667e55b8"));
    
   public static ConceptSpecification DESCRIPTION_INITIAL_CHARACTER_SENSITIVE
            = new ConceptProxy("description initial character sensitive",
                    UUID.fromString("17915e0d-ed38-3488-a35c-cda966db306a"));
    
   public static ConceptSpecification DEVELOPMENT_PATH
            = new ConceptProxy("development path",
                    UUID.fromString("1f200ca6-960e-11e5-8994-feff819cdc9f"));
    
   public static ConceptSpecification MASTER_PATH
            = new ConceptProxy("master path",
                    UUID.fromString("1f20134a-960e-11e5-8994-feff819cdc9f"));
    
   public static ConceptSpecification VHA_MODULE
            = new ConceptProxy("VHA module",
                    UUID.fromString("1f201520-960e-11e5-8994-feff819cdc9f"));
    
   public static ConceptSpecification SOLOR_OVERLAY_MODULE
            = new ConceptProxy("SOLOR overlay module",
                    UUID.fromString("1f2016a6-960e-11e5-8994-feff819cdc9f"));
   
  public static ConceptSpecification EL_PLUS_PLUS_INFERRED_ASSEMBLAGE
            = new ConceptProxy("EL++ inferred form assemblage",
                    UUID.fromString("1f20182c-960e-11e5-8994-feff819cdc9f"));
   
  public static ConceptSpecification EL_PLUS_PLUS_STATED_ASSEMBLAGE
            = new ConceptProxy("EL++ stated form assemblage",
                    UUID.fromString("1f201994-960e-11e5-8994-feff819cdc9f"));
   
  public static ConceptSpecification EL_PLUS_PLUS_LOGIC_PROFILE
            = new ConceptProxy("EL++ logic profile",
                    UUID.fromString("1f201e12-960e-11e5-8994-feff819cdc9f"));
   
  public static ConceptSpecification SNOROCKET_CLASSIFIER
            = new ConceptProxy("Snorocket classifier",
                    UUID.fromString("1f201fac-960e-11e5-8994-feff819cdc9f"));
   
 public static ConceptSpecification ROLE_GROUP
            = new ConceptProxy("role group",
                    UUID.fromString("a63f4bf2-a040-11e5-8994-feff819cdc9f"));
   
   public static int getDescriptionAssemblageConceptSequence(int languageConceptSequence) {
        if (languageConceptSequence == ENGLISH_LANGUAGE.getConceptSequence()) {
            return ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == SPANISH_LANGUAGE.getConceptSequence()) {
            return SPANISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == FRENCH_LANGUAGE.getConceptSequence()) {
            return FRENCH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == DANISH_LANGUAGE.getConceptSequence()) {
            return DANISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == POLISH_LANGUAGE.getConceptSequence()) {
            return POLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == DUTCH_LANGUAGE.getConceptSequence()) {
            return DUTCH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == LITHUANIAN_LANGUAGE.getConceptSequence()) {
            return LITHUANIAN_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == CHINESE_LANGUAGE.getConceptSequence()) {
            return CHINESE_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == JAPANESE_LANGUAGE.getConceptSequence()) {
            return JAPANESE_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       if (languageConceptSequence == SWEDISH_LANGUAGE.getConceptSequence()) {
            return SWEDISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence();
        }
       //TODO make an assemblage to link langauge to description assemblage, or an unspecified assemblage
        throw new RuntimeException("No description assemblage for: " + 
                Get.conceptDescriptionText(languageConceptSequence));
    }
    
    public static ConceptSpecification getConceptSpecificationForLanguageSequence(int languageConceptSequence) {
       if (languageConceptSequence == ENGLISH_LANGUAGE.getConceptSequence()) {
            return ENGLISH_LANGUAGE;
        }
       if (languageConceptSequence == SPANISH_LANGUAGE.getConceptSequence()) {
            return SPANISH_LANGUAGE;
        }
       if (languageConceptSequence == FRENCH_LANGUAGE.getConceptSequence()) {
            return FRENCH_LANGUAGE;
        }
       if (languageConceptSequence == DANISH_LANGUAGE.getConceptSequence()) {
            return DANISH_LANGUAGE;
        }
       if (languageConceptSequence == POLISH_LANGUAGE.getConceptSequence()) {
            return POLISH_LANGUAGE;
        }
       if (languageConceptSequence == DUTCH_LANGUAGE.getConceptSequence()) {
            return DUTCH_LANGUAGE;
        }
       if (languageConceptSequence == LITHUANIAN_LANGUAGE.getConceptSequence()) {
            return LITHUANIAN_LANGUAGE;
        }
       if (languageConceptSequence == CHINESE_LANGUAGE.getConceptSequence()) {
            return CHINESE_LANGUAGE;
        }
       if (languageConceptSequence == JAPANESE_LANGUAGE.getConceptSequence()) {
            return JAPANESE_LANGUAGE;
        }
       if (languageConceptSequence == SWEDISH_LANGUAGE.getConceptSequence()) {
            return SWEDISH_LANGUAGE;
        }
        return Get.conceptSpecification(languageConceptSequence);
    }
}
