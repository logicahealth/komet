package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * Selected sample ConceptSpecs, which can be used in test queries.
 *
 */
public class Snomed {

    public static ConceptSpec ABDOMINAL_WALL_STRUCTURE =
            new ConceptSpec("Abdominal wall structure (body structure)",
            UUID.fromString("c258b290-fabc-3e9d-86f4-9cf4dc0d6978"));
    public static ConceptSpec NEUROLOGICAL_SYMPTOM =
            new ConceptSpec("Neurological symptom (finding)",
            UUID.fromString("f40bbc47-9f5a-34d9-b3c8-3f58a08874fe"));
    public static ConceptSpec BODY_STRUCTURE_REFSET =
            new ConceptSpec("Body structure (domain) refset",
            UUID.fromString("0acd09e5-a5f1-4880-82b2-2a5c273dca29"));
    public static ConceptSpec BARANYS_SIGN =
            new ConceptSpec("Barany's sign (finding)",
            UUID.fromString("c8aa4892-6af9-3d70-acd9-bb249dc2ceaf"));
    public static ConceptSpec RESPIRATORY_DISORDER =
            new ConceptSpec("Respiratory disorder",
            UUID.fromString("275f19cb-83fc-3f8c-992d-9ad866602c88"));
    public static ConceptSpec BRONCHIAL_HYPERREACTIVITY =
            new ConceptSpec("BHR - Bronchial hyperreactivity",
            UUID.fromString("c265cf22-2a11-3488-b71e-296ec0317f96"));
    public static ConceptSpec ALLERGIC_ASTHMA =
            new ConceptSpec("Allergic asthma",
            UUID.fromString("531abe20-8324-3db9-9104-8bcdbf251ac7"));
    public static ConceptSpec BODY_STRUCTURE =
            new ConceptSpec("Body structures",
            UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790"));
    public static ConceptSpec IS_A =
            new ConceptSpec("Is a (attribute)",
            UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
    public static ConceptSpec FINDING_SITE =
            new ConceptSpec("Finding site (attribute)",
            UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928"));
    public static ConceptSpec PART_OF =
            new ConceptSpec("Part of (attribute)",
            UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73"));
    public static ConceptSpec CLINICAL_FINDING =
            new ConceptSpec("Clinical finding (finding)",
            UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c"));
    public static ConceptSpec FULLY_SPECIFIED_DESCRIPTION_TYPE =
            new ConceptSpec("fully specified name (description type)",
            UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));
    public static ConceptSpec CORE_NAMESPACE =
            new ConceptSpec("Core Namespace",
            UUID.fromString("d6bbe207-7b5c-3e32-a2a1-f9259a7260c1"));
    public static ConceptSpec CORE_MODULE =
            new ConceptSpec("SNOMED CT core module",
            UUID.fromString("1b4f1ba5-b725-390f-8c3b-33ec7096bdca"));
    public static ConceptSpec DISEASE =
            new ConceptSpec("Disease (disorder)",
            UUID.fromString("ab4e618b-b954-3d56-a44b-f0f29d6f59d3"));
    public static ConceptSpec DISORDER_OF_EYE =
            new ConceptSpec("Disorder of eye proper (disorder)",
            UUID.fromString("70ed9379-15e5-3716-a0d3-b9f5bef3d9a0"));
    public static ConceptSpec SNOMED_RELEASE_PATH =
            new ConceptSpec("SNOMED Core",
            UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2"));
    public static ConceptSpec ENTIRE_HEMATOPOIETIC_SYSTEM =
            new ConceptSpec("Entire hematopoietic system (body structure)",
            UUID.fromString("ec1d9f88-5de1-3891-b67f-e50f262cf63f"));
    public static ConceptSpec EXTENSION_0 =
            new ConceptSpec("Extension Namespace 1000000",
            UUID.fromString("18388bfd-9fab-3581-9e22-cbae53725ef2"));
    public static ConceptSpec EXTENSION_13 =
            new ConceptSpec("Extension Namespace 1000013",
            UUID.fromString("bb57db0f-def7-3fb7-b7f2-89fa7710bffa"));
    public static ConceptSpec LASER_SURGERY =
            new ConceptSpec("Laser surgery (procedure)",
            UUID.fromString("c8419d14-07f1-3c85-b320-47d91dd08181"));
    public static ConceptSpec LASER_SURGERY_TECHNIQUE =
            new ConceptSpec("Laser surgery technique",
            UUID.fromString("f23cff34-c2c8-32dd-865a-e08762018ded"));
    public static ConceptSpec CONCEPT_HISTORY_ATTRIB =
            new ConceptSpec("Concept history attribute",
            UUID.fromString("f323b5dd-1f97-3873-bcbc-3563663dda14"));
    public static ConceptSpec PRODUCT =
            new ConceptSpec("Pharmaceutical / biologic product (product)",
            UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449"));
    public static ConceptSpec INACTIVE_CONCEPT =
            new ConceptSpec("Inactive concept (inactive concept)",
            UUID.fromString("f267fc6f-7c4d-3a79-9f17-88b82b42709a"));
    public static ConceptSpec STRUCTURE_OF_ENDOCRINE_SYSTEM =
            new ConceptSpec("Structure of endocrine system (body structure)",
            UUID.fromString("9d902867-4e0e-39e0-b6dd-e4be495b6ab1"));
    //Concept Specs for context sensitive role relationships
    public static ConceptSpec ACCESS =
            new ConceptSpec("Access (attribute)",
            UUID.fromString("3f5a4b8c-923b-3df5-9362-67881b729394"));
    public static ConceptSpec APPROACH =
            new ConceptSpec("Procedural approach (qualifier value)",
            UUID.fromString("2209583c-de0b-376d-9aa0-850c37240788"));
    public static ConceptSpec ASSOCIATED_FINDING =
            new ConceptSpec("Associated finding (attribute)",
            UUID.fromString("b20b664d-2690-3092-a2ef-7f8013b2dad3"));
    public static ConceptSpec ASSOCIATED_MORPHOLOGY =
            new ConceptSpec("Associated morphology (attribute)",
            UUID.fromString("3161e31b-7d00-33d9-8cbd-9c33dc153aae"));
    public static ConceptSpec ASSOCIATED_WITH =
            new ConceptSpec("Associated with (attribute)",
            UUID.fromString("79e34041-f87c-3659-b033-41bdd35bd89e"));
    public static ConceptSpec ASSOCIATED_WITH_AFTER =
            new ConceptSpec("After (attribute)",
            UUID.fromString("fb6758e0-442c-3393-bb2e-ff536711cde7"));
    public static ConceptSpec ASSOCIATED_WITH_AGENT =
            new ConceptSpec("Causative agent (attribute)",
            UUID.fromString("f770e2d8-91e6-3c55-91be-f794ee835265"));
    public static ConceptSpec ASSOCIATED_WITH_DUE =
            new ConceptSpec("Due to (attribute)",
            UUID.fromString("6525dbf8-c839-3e45-a4bb-8bab7faf7cf9"));
    public static ConceptSpec CLINICAL_COURSE =
            new ConceptSpec("Clinical course (attribute)",
            UUID.fromString("0d8a9cbb-e21e-3de7-9aad-8223c000849f"));
    public static ConceptSpec COMPONENT =
            new ConceptSpec("Component (attribute)",
            UUID.fromString("8f0696db-210d-37ab-8fe1-d4f949892ac4"));
    public static ConceptSpec DIRECT_SUBSTANCE =
            new ConceptSpec("Direct substance (attribute)",
            UUID.fromString("49ee3912-abb7-325c-88ba-a98824b4c47d"));
    public static ConceptSpec ENVIRONMENT =
            new ConceptSpec("Environment (environment)",
            UUID.fromString("da439d54-0823-3b47-abed-f9ba50791335"));
    public static ConceptSpec EVENT =
            new ConceptSpec("Event (event)",
            UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698"));
    public static ConceptSpec FINDING_CONTEXT =
            new ConceptSpec("Finding context (attribute)",
            UUID.fromString("2dbbf50e-9e14-382d-80be-ec7a020cb436"));
    public static ConceptSpec FINDING_INFORMER =
            new ConceptSpec("Finding informer (attribute)",
            UUID.fromString("4990c973-2c08-3972-93ed-3ce9cd4e1776"));
    public static ConceptSpec FINDING_METHOD =
            new ConceptSpec("Finding method (attribute)",
            UUID.fromString("ee283805-ec23-3e22-8bd0-c739f8cbdd7d"));
    public static ConceptSpec HAS_ACTIVE_INGREDIENT =
            new ConceptSpec("Has active ingredient (attribute)",
            UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8"));
    public static ConceptSpec HAS_DEFINITIONAL_MANIFESTATION =
            new ConceptSpec("Has definitional manifestation (attribute)",
            UUID.fromString("545df979-75ea-3f82-939a-565d032bcdad"));
    public static ConceptSpec HAS_DOSE_FORM =
            new ConceptSpec("Has dose form (attribute)",
            UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63"));
    public static ConceptSpec HAS_FOCUS =
            new ConceptSpec("Has focus (attribute)",
            UUID.fromString("b610d820-4486-3b5e-a2c1-9b66bc718c6d"));
    public static ConceptSpec HAS_INTENT =
            new ConceptSpec("Has intent (attribute)",
            UUID.fromString("4e504dc1-c971-3e20-a4f9-b86d0c0490af"));
    public static ConceptSpec HAS_INTERPRETATION =
            new ConceptSpec("Has interpretation (attribute)",
            UUID.fromString("993a598d-a95a-3235-813e-59252c975070"));
    public static ConceptSpec HAS_SPECIMEN =
            new ConceptSpec("Has specimen (attribute)",
            UUID.fromString("5ce3e93b-8594-3d38-b410-b06039e63e3c"));
    public static ConceptSpec INTERPRETS =
            new ConceptSpec("Interprets (attribute)",
            UUID.fromString("75e0da0c-21ea-301f-a176-bf056788afe5"));
    public static ConceptSpec LATERALITY =
            new ConceptSpec("Laterality (attribute)",
            UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b"));
    public static ConceptSpec LINK_ASSERTION =
            new ConceptSpec("Link assertion (link assertion)",
            UUID.fromString("7f39edac-198d-366d-b8b9-4eab221ee144"));
    public static ConceptSpec MEASUREMENT_METHOD =
            new ConceptSpec("Measurement method (attribute)",
            UUID.fromString("a6e4f659-a4b4-33b7-a75d-4a810167b32a"));
    public static ConceptSpec METHOD =
            new ConceptSpec("Method (attribute)",
            UUID.fromString("d0f9e3b1-29e4-399f-b129-36693ba4acbc"));
    public static ConceptSpec MORPHOLOGIC_ABNORMALITY =
            new ConceptSpec("Morphologically abnormal structure (morphologic abnormality)",
            UUID.fromString("3d3c4a6a-98d6-3a7c-9e1b-7fabf61e5ca5"));
    public static ConceptSpec MOVED_ELSEWHERE =
            new ConceptSpec("Moved elsewhere (inactive concept)",
            UUID.fromString("e730d11f-e155-3482-a423-9637db3bc1a2"));
    public static ConceptSpec OBSERVABLE_ENTITY =
            new ConceptSpec("Observable entity (observable entity)",
            UUID.fromString("d678e7a6-5562-3ff1-800e-ab070e329824"));
    public static ConceptSpec OCCURRENCE =
            new ConceptSpec("Occurrence (attribute)",
            UUID.fromString("d99e2a70-243d-3bf2-967a-faee3265102b"));
    public static ConceptSpec ORGANISM =
            new ConceptSpec("Organism (organism)",
            UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072"));
    public static ConceptSpec PATHOLOGICAL_PROCESS =
            new ConceptSpec("Pathological process (attribute)",
            UUID.fromString("52542cae-017c-3fc4-bff0-97b7f620db28"));
    public static ConceptSpec PERSON =
            new ConceptSpec("Person (person)",
            UUID.fromString("37c4cc1d-b35c-3080-80ac-b5e3a14c8a4b"));
    public static ConceptSpec PHYSICAL_FORCE =
            new ConceptSpec("Physical force (physical force)",
            UUID.fromString("32213bf6-c073-3ce1-b0c7-9463e43af2f1"));
    public static ConceptSpec PHYSICAL_OBJECT =
            new ConceptSpec("Physical object (physical object)",
            UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));
    public static ConceptSpec PRIORITY =
            new ConceptSpec("Priority (attribute)",
            UUID.fromString("77d496f0-d56d-3ab1-b3c4-b58969ddd078"));
    public static ConceptSpec PROCEDURE =
            new ConceptSpec("Procedure (procedure)",
            UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b"));
    public static ConceptSpec PROCEDURE_CONTEXT =
            new ConceptSpec("Procedure context (attribute)",
            UUID.fromString("6d2e9614-a93f-3835-b278-01650b17743a"));
    public static ConceptSpec PROCEDURE_DEVICE =
            new ConceptSpec("Procedure device (attribute)",
            UUID.fromString("820447dc-ff12-3902-b752-6e5397d297ef"));
    public static ConceptSpec PROCEDURE_DEVICE_DIRECT =
            new ConceptSpec("Direct device (attribute)",
            UUID.fromString("102422d3-6b68-3d16-a756-1df791d91e7f"));
    public static ConceptSpec PROCEDURE_INDIRECT_DEVICE =
            new ConceptSpec("Indirect device (attribute)",
            UUID.fromString("9f4020b4-9949-3448-b43a-f3f5b0d44e2b"));
    public static ConceptSpec PROCCEDURE_ACCESS_DEVICE =
            new ConceptSpec("Using access device (attribute)",
            UUID.fromString("857b607c-bed8-3432-b474-1a65e613f242"));
    public static ConceptSpec PROCEDURE_USING_DEVICE =
            new ConceptSpec("Using device (attribute)",
            UUID.fromString("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"));
    public static ConceptSpec PROCEDURE_MORPHOLOGY =
            new ConceptSpec("Procedure morphology (attribute)",
            UUID.fromString("c6456f56-c088-34f5-a85d-39a5fcf62411"));
    public static ConceptSpec PROCEDURE_MORPHOLOGY_DIRECT =
            new ConceptSpec("Direct morphology (attribute)",
            UUID.fromString("f28dd2fb-7573-3c53-b42a-c8212c946738"));
    public static ConceptSpec PROCEDURE_INDIRECT_MORPHOLOGY =
            new ConceptSpec("Indirect morphology (attribute)",
            UUID.fromString("f941f564-f7b1-3a4f-a2ed-f8e1787ee082"));
    public static ConceptSpec PROCEDURE_SITE =
            new ConceptSpec("Procedure site (attribute)",
            UUID.fromString("78dd0334-4b9e-3c26-9266-356f8c5c43ed"));
    public static ConceptSpec PROCEDURE_SITE_DIRECT =
            new ConceptSpec("Procedure site - Direct (attribute)",
            UUID.fromString("472df387-0193-300f-9184-85b59aa85416"));
    public static ConceptSpec PROCEDURE_SITE_INDIRECT =
            new ConceptSpec("Procedure site - Indirect (attribute)",
            UUID.fromString("ac38de9e-2c97-37ed-a3e2-365a87ba1730"));
    public static ConceptSpec PROPERTY =
            new ConceptSpec("Property (attribute)",
            UUID.fromString("066462e2-f926-35d5-884a-4e276dad4c2c"));
    public static ConceptSpec QUALIFIER_VALUE =
            new ConceptSpec("Qualifier value (qualifier value)",
            UUID.fromString("ed6a9820-ba24-3917-b1b2-151e9c5a7a8d"));
    public static ConceptSpec RECIPIENT_CATEGORY =
            new ConceptSpec("Recipient category (attribute)",
            UUID.fromString("e4233cb6-6b8f-3ae5-85e5-dab691a81ecd"));
    public static ConceptSpec REVISION_STATUS =
            new ConceptSpec("Revision status (attribute)",
            UUID.fromString("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"));
    public static ConceptSpec ROUTE_OF_ADMIN =
            new ConceptSpec("Route of administration (attribute)",
            UUID.fromString("ddbb95e5-aaf6-38f3-b400-dcfb1f85be91"));
    public static ConceptSpec SCALE_TYPE =
            new ConceptSpec("Scale type (attribute)",
            UUID.fromString("087afdd2-23cd-34c3-93a4-09088dfd480c"));
    public static ConceptSpec SEVERITIES =
            new ConceptSpec("Severities (qualifier value)",
            UUID.fromString("ee50a583-4172-30f4-84dd-c61f83f0130e"));
    public static ConceptSpec SITUATION_WITH_EXPLICIT_CONTEXT =
            new ConceptSpec("Situation with explicit context (situation)",
            UUID.fromString("27d03723-07c3-3de9-828b-76aa05a23438"));
    public static ConceptSpec SOCIAL_CONTEXT =
            new ConceptSpec("Social context (social concept)",
            UUID.fromString("b89db478-21d5-3e51-972d-6c900f0ec436"));
    public static ConceptSpec SPECIMEN =
            new ConceptSpec("Specimen (specimen)",
            UUID.fromString("3680e12d-c14c-39cb-ac89-2ae1fa125d41"));
    public static ConceptSpec SPECIMEN_PROCEDURE =
            new ConceptSpec("Specimen procedure (attribute)",
            UUID.fromString("e81aa5e5-fcf6-3329-994d-3154576ac90d"));
    public static ConceptSpec SPECIMEN_SOURCE_ID =
            new ConceptSpec("Specimen source identity (attribute)",
            UUID.fromString("4ae2b18c-db93-339c-8a9f-35e027007bf5"));
    public static ConceptSpec SPECIMEN_SOURCE_MORPHOLOGY =
            new ConceptSpec("Specimen source morphology (attribute)",
            UUID.fromString("3dd1e927-005e-30ba-b3a4-0a67d538fefe"));
    public static ConceptSpec SPECIMEN_SOURCE_TOPOGRAPHY =
            new ConceptSpec("Specimen source topography (attribute)",
            UUID.fromString("4aafafbc-b21f-30a6-b676-0224e6b001ab"));
    public static ConceptSpec SPECIMEN_SUBSTANCE =
            new ConceptSpec("Specimen substance (attribute)",
            UUID.fromString("500b618d-2896-36ff-a020-c3c988f816f1"));
    public static ConceptSpec SUBJECT_REL_CONTEXT =
            new ConceptSpec("Subject relationship context (attribute)",
            UUID.fromString("cbd2a57c-a28d-3494-9193-2189f2b618a2"));
    public static ConceptSpec SUBSTANCE =
            new ConceptSpec("Substance (substance)",
            UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));
    public static ConceptSpec TEMPORAL_CONTEXT =
            new ConceptSpec("Temporal context (attribute)",
            UUID.fromString("2c6acb71-a375-30b4-952d-63916ed74084"));
    public static ConceptSpec TIME_ASPECT =
            new ConceptSpec("Time aspect (attribute)",
            UUID.fromString("350adfa7-8fd5-3b95-91f2-8119b500a464"));
    public static ConceptSpec USING_ENERGY =
            new ConceptSpec("Using energy (attribute)",
            UUID.fromString("3050f9ea-e811-37f2-b132-ffa06afcfbbe"));
    public static ConceptSpec USING_SUBSTANCE =
            new ConceptSpec("Using substance (attribute)",
            UUID.fromString("996261c3-3c12-3f09-8f14-e30e85e9e70d"));
    public static ConceptSpec ACCELERATION =
            new ConceptSpec("Acceleration (physical force)",
            UUID.fromString("6ef49616-e2c7-3557-b7f1-456a2c5a5e54"));
    public static ConceptSpec CENTRIFUGAL_FORCE =
            new ConceptSpec("Centrifugal force (physical force)",
            UUID.fromString("2b684fe1-8baf-34ef-9d2a-df03142c915a"));
    public static ConceptSpec CONTINUED_MOVEMENT =
            new ConceptSpec("Continued movement (physical force)",
            UUID.fromString("5a6abaa5-adac-3c31-a32e-acf4710dca9d"));
    public static ConceptSpec DECELERATION =
            new ConceptSpec("Deceleration (physical force)",
            UUID.fromString("02df5644-8644-3430-98de-f740ada3286b"));
    public static ConceptSpec MOMENTUM =
            new ConceptSpec("Momentum (physical force)",
            UUID.fromString("20fc8b77-9137-385c-92cc-ed4cec5b33a2"));
    public static ConceptSpec VIBRATION =
            new ConceptSpec("Vibration (physical force)",
            UUID.fromString("c3cfddd6-71bc-34f4-97bc-745f475c88ff"));
    public static ConceptSpec MOTION =
            new ConceptSpec("Motion (physical force)",
            UUID.fromString("45a8fde8-535d-3d2a-b76b-95ab67718b41"));
    public static ConceptSpec RECORD_OCCLUSAL =
            new ConceptSpec("Record occlusal registration (procedure)",
            UUID.fromString("d13e32a7-588d-323c-adb2-886e72be5c5e"));
    public static ConceptSpec STATUS =
            new ConceptSpec("status (status type)",
            UUID.fromString("d944af55-86d9-33f4-bebd-a10bf3f4712c"));
    public static ConceptSpec CLINICAL_FINDING_ABSENT =
            new ConceptSpec("Clinical finding absent (situation)",
            UUID.fromString("66387a1b-9bb6-361d-99f0-0a3147cad7f2"));
    public static ConceptSpec DIABETES_MELLITUS =
            new ConceptSpec("Diabetes mellitus (disorder)",
            UUID.fromString("dd25374d-b4a9-3622-bba5-c82ae028631c"));
    public static ConceptSpec EYE_STRUCTURE =
            new ConceptSpec("Structure of eye proper (body structure)",
            UUID.fromString("868b0f47-c952-3587-b0ae-ceafd4597a74"));
    public static ConceptSpec DISORDER_OF_CORNEA =
            new ConceptSpec("Disorder of cornea (disorder)",
            UUID.fromString("932dd33c-b806-3805-b8c9-8fd57f28315d"));
    public static ConceptSpec SURGICAL_PROCEDURE =
            new ConceptSpec("Surgical procedure (procedure)",
            UUID.fromString("85923350-9ead-323f-b2d9-8ade0ac4264c"));
    public static ConceptSpec DISCHARGE_FROM_HOSPICE =
            new ConceptSpec("Discharge from hospice (procedure)",
            UUID.fromString("ccb0fa22-4f01-3dba-88ab-75b2007325cc"));
    public static ConceptSpec GRADES =
            new ConceptSpec("Grades (qualifier value)",
            UUID.fromString("15384acd-0151-3973-841b-6c1f05c375b3"));
    public static ConceptSpec PATIENT_DISCHARGE =
            new ConceptSpec("Patient discharge (procedure)",
            UUID.fromString("d74af504-62d8-3b95-9e7c-0c18365bdcb7"));
    public static ConceptSpec SEVERITY_REFSET =
            new ConceptSpec("Severity (valid targets) refset",
            UUID.fromString("f0133e8f-547f-4fc6-8c10-6c6d1be0f531"));
    public static ConceptSpec SEVERITY_REFSET_SPEC =
            new ConceptSpec("Severity (valid targets) refset spec",
            UUID.fromString("897b6c64-fbdc-4a38-9e95-3eb100b730af"));
    public static ConceptSpec MILD =
            new ConceptSpec("Mild (qualifier value)",
            UUID.fromString("7a25d07f-ad54-3d93-b823-67eb30cf3a6f"));
    public static ConceptSpec PROCEDURE_REFSET =
            new ConceptSpec("Procedure (domain) refset",
            UUID.fromString("8d0f383e-fa0e-4492-843e-bc2003037dfa"));
    public static ConceptSpec PLACENTOGRAPHY =
            new ConceptSpec("Placentography-contrast (procedure)",
            UUID.fromString("c013929e-2fbb-3e24-ab8d-56dc654d5fcf"));
}
