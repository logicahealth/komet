/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 *
 * @author marc
 */
public class SnomedMetadataRf2 {

    /*** DEFINITIONS ***/
    // SCT ID: 900000000000003001
    public static ConceptSpec FULLY_SPECIFIED_NAME_RF2 =
            new ConceptSpec("Fully specified name (core metadata concept)",
            UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));
    // SCT ID:	900000000000013009
    public static ConceptSpec SYNONYM_RF2 =
            new ConceptSpec("Synonym (core metadata concept)",
            UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
    
    // SCT ID:	900000000000548007
    public static ConceptSpec PREFERRED_RF2 =
            new ConceptSpec("Preferred (foundation metadata concept)",
            UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));
    // SCT ID:	900000000000549004
    public static ConceptSpec ACCEPTABLE_RF2 =
            new ConceptSpec("Acceptable (foundation metadata concept)",
            UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));
    
    // SCT ID:	900000000000509007
    public static ConceptSpec US_ENGLISH_REFSET_RF2 =
            new ConceptSpec("US English",
            UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));
    // SCT ID:	900000000000508004
    public static ConceptSpec GB_ENGLISH_REFSET_RF2 =
            new ConceptSpec("GB English",
            UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));
    
    // SCT ID:	900000000000017005
    public static ConceptSpec CASE_SENSITIVE_RF2 =
            new ConceptSpec("Case sensitive",
            UUID.fromString("0def37bc-7e1b-384b-a6a3-3e3ceee9c52e"));
    // SCT ID:	900000000000020002
    public static ConceptSpec INITIAL_CHARACTER_CASE_INSENSITIVE_RF2 =
            new ConceptSpec("Initial character case insensitive",
            UUID.fromString("17915e0d-ed38-3488-a35c-cda966db306a"));
    // SCT ID:	900000000000448009
    public static ConceptSpec CASE_INSENSITIVE_RF2 =
            new ConceptSpec("Case insensitive",
            UUID.fromString("ecea41a2-f596-3d98-99d1-771b667e55b8"));
    
    // SCT ID:	900000000000074008
    public static ConceptSpec PRIMITIVE_RF2 =
            new ConceptSpec("Primitive",
            UUID.fromString("e1a12059-3b01-3296-9532-d10e49d0afc3"));
    // SCT ID:	900000000000073002
    public static ConceptSpec DEFINED_RF2 =
            new ConceptSpec("Defined",
            UUID.fromString("6d9cd46e-8a8f-310a-a298-3e55dcf7a986"));
    
    // SCT ID:	447563008
    public static ConceptSpec ICD_9_CM_EQUIVALENCE_MAP_REFSET_RF2 =
            new ConceptSpec("ICD-9-CM equivalence complex map reference set (foundation metadata concept)",
            UUID.fromString("165a4026-1afd-39a5-91a3-b2d4279a083f"));
    // SCT ID:	446608001
    public static ConceptSpec ICD_O_SIMPLE_MAP_REFSET_RF2 =
            new ConceptSpec("ICD-O simple map reference set (foundation metadata concept)",
            UUID.fromString("5ef10e09-8f16-398e-99b5-55cff5bd820a"));
    
    // SCT ID:	900000000000007000
    public static ConceptSpec NOT_REFINABLE_RF2 =
            new ConceptSpec("Not refinable (foundation metadata concept)",
            UUID.fromString("ce30636d-bfc9-3a70-9678-abc6b542ab4c"));
    // SCT ID:	900000000000216007
    public static ConceptSpec OPTIONAL_REFINIBILITY_RF2 =
            new ConceptSpec("Optional refinability",
            UUID.fromString("7d2d6cd0-c727-397e-9bc8-da65562e9350"));
    // SCT ID:	900000000000218008
    public static ConceptSpec MANDATORY_REFINIBILITY_RF2 =
            new ConceptSpec("Mandatory refinability",
            UUID.fromString("67a79b5a-d56e-37f6-ad66-da712e39c453"));
    
    // SCT ID:	900000000000227009
    public static ConceptSpec ADDITIONAL_RELATIONSHIP_RF2 =
            new ConceptSpec("Additional relationship",
            UUID.fromString("85aba419-17fe-3033-a7c2-21df0af84176"));
    // SCT ID:	900000000000006009
    public static ConceptSpec DEFINING_RELATIONSHIP_RF2 =
            new ConceptSpec("Defining relationship (core metadata concept)",
            UUID.fromString("e607218d-7027-3058-ae5f-0d4ccd148fd0"));
    // SCT ID:	900000000000011006
    public static ConceptSpec INFERRED_RELATIONSHIP_RF2 =
            new ConceptSpec("Inferred relationship (core metadata concept)",
            UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"));
    // SCT ID:	900000000000010007
    public static ConceptSpec STATED_RELATIONSHIP_RF2 =
            new ConceptSpec("Stated relationship (core metadata concept)",
            UUID.fromString("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"));
    // SCT ID:	900000000000225001
    public static ConceptSpec QUALIFYING_RELATIONSSHIP_RF2 =
            new ConceptSpec("Qualifying relationship (core metadata concept)",
            UUID.fromString("569dac14-a8a5-3cf0-b608-5ae2f1c89461"));
    // SCT ID: NOT APPLICABLE,  USES WORKBENCH CONCEPT
    public static ConceptSpec HISTORICAL_RELATIONSSHIP_RF2 =
            new ConceptSpec("historical (characteristic type)",
            UUID.fromString("1d054ca3-2b32-3004-b7af-2701276059d5"));
    // SCT ID: 900000000000522004
    public static ConceptSpec HISTORICAL_REFSET_RF2 =
            new ConceptSpec("Historical association reference set (foundation metadata concept)",
            UUID.fromString("fd552da3-92fd-38c1-9982-d60ad6c9020b"));
    
    /** STATUS ***/
    // SCT ID:	900000000000486000
    public static ConceptSpec LIMITED_COMPONENT_RF2 =
            new ConceptSpec("Limited component (foundation metadata concept)",
            UUID.fromString("0d1278d5-3718-36de-91fd-7c6c8d2d2521"));
    // SCT ID:	900000000000482003
    public static ConceptSpec DUPLICATE_COMPONENT_RF2 =
            new ConceptSpec("Duplicate component (foundation metadata concept)",
            UUID.fromString("16500683-0760-3aa5-8ed7-9cb98562e755"));
    // SCT ID:	900000000000492006
    public static ConceptSpec PENDING_MOVE_RF2 =
            new ConceptSpec("Pending move (foundation metadata concept)",
            UUID.fromString("9906317a-f50f-30f6-8b59-a751ae1cdeb9"));
    // SCT ID:	900000000000484002
    public static ConceptSpec AMBIGUOUS_COMPONENT_RF2 =
            new ConceptSpec("Ambiguous component (foundation metadata concept)",
            UUID.fromString("8c852b81-6246-34b5-b882-81627aa404e4"));
    // SCT ID:	900000000000487009
    public static ConceptSpec COMPONENT_MOVED_ELSEWHERE_RF2 =
            new ConceptSpec("Component moved elsewhere (foundation metadata concept)",
            UUID.fromString("95028943-b11c-3509-b1c0-c4ae16aaad5c"));
    // SCT ID:	900000000000495008
    public static ConceptSpec CONCEPT_NON_CURRENT_RF2 =
            new ConceptSpec("Concept non-current (foundation metadata concept)",
            UUID.fromString("6cc3df26-661e-33cd-a93d-1c9e797c90e3"));
    // SCT ID:	900000000000483008
    public static ConceptSpec OUTDATED_COMPONENT_RF2 =
            new ConceptSpec("Outdated component (foundation metadata concept)",
            UUID.fromString("eab9334c-8269-344e-9db6-9189f991566e"));
    // SCT ID:	900000000000494007
    public static ConceptSpec INAPPROPRIATE_COMPONENT_RF2 =
            new ConceptSpec("Inappropriate component (foundation metadata concept)",
            UUID.fromString("bcb2ccda-d62a-3fc8-b158-10ad673823b6"));
    // SCT ID:	900000000000485001
    public static ConceptSpec ERRONEOUS_COMPONENT_RF2 =
            new ConceptSpec("Erroneous component (foundation metadata concept)",
            UUID.fromString("dde90dcf-8749-32ff-bdaa-4e5d17e505f2"));
    // SCT ID:	900000000000546006
    public static ConceptSpec INACTIVE_VALUE_RF2 =
            new ConceptSpec("Inactive value (foundation metadata concept)",
            UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
    // SCT ID: 900000000000545005
    public static ConceptSpec ACTIVE_VALUE_RF2 =
            new ConceptSpec("Active value (foundation metadata concept)",
            UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
    // SCT ID:	123005000
    public static ConceptSpec PART_OF_ATTRIBUTE_REFSET =
            new ConceptSpec("Part of (attribute)",
            UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73"));
    // SCT ID:	900000000000523009
    public static ConceptSpec POSSIBLY_EQUIVALENT_TO_REFSET_RF2 =
            new ConceptSpec("POSSIBLY EQUIVALENT TO association reference set (foundation metadata concept)",
            UUID.fromString("2cb0d803-0aa8-3c4a-a58c-949fa85a5016"));
    // SCT ID:	900000000000528000
    public static ConceptSpec WAS_A_REFSET_RF2 =
            new ConceptSpec("WAS A association reference set (foundation metadata concept)",
            UUID.fromString("0c4a8933-bf14-39b9-a743-a5dc07faa7bb"));
    // SCT ID:	900000000000527005
    public static ConceptSpec SAME_AS_REFSET_RF2 =
            new ConceptSpec("SAME AS association reference set (foundation metadata concept)",
            UUID.fromString("58d18fcb-02fd-3c2d-82e6-0ff2593d9df4"));
    // SCT ID:	900000000000526001
    public static ConceptSpec REPLACED_BY_REFSET_RF2 =
            new ConceptSpec("REPLACED BY association reference set (foundation metadata concept)",
            UUID.fromString("9cb37094-2be5-3b05-a586-73ae6727e9c2"));
    // SCT ID:	900000000000524003
    public static ConceptSpec MOVED_TO_REFSET_RF2 =
            new ConceptSpec("MOVED TO association reference set (foundation metadata concept)",
            UUID.fromString("53e5f8b2-2f4b-3268-8a5e-04f30c241c89"));
    // SCT ID:	900000000000525002
    public static ConceptSpec MOVED_FROM_REFSET_RF2 =
            new ConceptSpec("MOVED FROM association reference set (foundation metadata concept)",
            UUID.fromString("34a2171a-af64-3ab2-b27d-e19914f058e3"));
    // SCT ID:	900000000000526001
    public static ConceptSpec REPLACE_BY_REFSET_RF2 =
            new ConceptSpec("REPLACED BY association reference set (foundation metadata concept)",
            UUID.fromString("9cb37094-2be5-3b05-a586-73ae6727e9c2"));
    // SCT ID:	900000000000530003
    public static ConceptSpec ALTERNATIVE_REFSET_RF2 =
            new ConceptSpec("ALTERNATIVE association reference set (foundation metadata concept)",
            UUID.fromString("e116d83a-4402-3f4e-9628-6edafcd98ed7"));
    // SCT ID:	900000000000531004
    public static ConceptSpec REFERS_TO_REFSET_RF2 =
            new ConceptSpec("REFERS TO concept association reference set (foundation metadata concept)",
            UUID.fromString("d15fde65-ed52-3a73-926b-8981e9743ee9"));
    
    public static ConceptSpec DEGREE_OF_SYNONYMY_RF2 =
            new ConceptSpec("Degree of Synonymy Refset (RF2)",
            UUID.fromString("3bc6ad17-253e-53a9-bea6-049abfa467c8"));
    
    public static ConceptSpec VMP_RF2 =
            new ConceptSpec("Virtual medicinal product simple reference set",
            UUID.fromString("c259d808-8011-3772-bece-b4fbde18d375"));
    
    public static ConceptSpec VTM_RF2 =
            new ConceptSpec("Virtual therapeutic moiety simple reference set",
            UUID.fromString("1a090a21-28c4-3a87-9d04-766f04600494"));
    
    public static ConceptSpec NON_HUMAN_RF2 =
            new ConceptSpec("Non-human simple reference set",
            UUID.fromString("b1b1e773-3eb6-3bcc-a6c7-52ac5d0a53be"));
}
