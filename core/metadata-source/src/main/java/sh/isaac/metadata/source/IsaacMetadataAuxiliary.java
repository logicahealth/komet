/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.metadata.source;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.Rank;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.IsaacTaxonomy;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;

import static sh.isaac.api.bootstrap.TermAux.*;
import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import static sh.isaac.model.observable.ObservableFields.*;

import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 * The Class IsaacMetadataAuxiliary.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Rank(value = 10)
@Singleton
public class IsaacMetadataAuxiliary extends IsaacTaxonomy {

   

   /**
    * METADATA_VERSION will be added to MetaData.java, IsaacMetadataAuxiliary.json, IsaacMetadataAuxiliary.yaml when built.
    * 
    * Format of the version number is major.minor.release. First digit indicates a major change i.e. a string values changes in a concept which breaks backwards
    * compatibility Second digit indicates a minor change i.e. a new concept is created which does not affect backwards compatibility Third digit indicates a minor
    * change that does not affect backwards compatibility - typically things like spelling corrections, etc.
    * 
    * DO NOT update if no change to concepts this file. This version number is independent of release versioning.
    * 
    * If you change this value, let the KOMET developers know that they must also update the same in their module AuxililaryMetadata.
    * 
    * 1.2.0 Merging VA Metadata with solor.io metadata, adding this version here.
    * 
    **/
   public static final String AUXILIARY_METADATA_VERSION = "1.2.0";

   // ~--- constructors --------------------------------------------------------

   /**
    * If you are looking for the code that creates / uses this, see the class {@link ExportTaxonomy} To override this class with a different taxonomy, provide
    * another implementation with a higher rank.
    *
    * @throws NoSuchAlgorithmException
    *             the no such algorithm exception
    * @throws UnsupportedEncodingException
    *             the unsupported encoding exception
    */
   public IsaacMetadataAuxiliary() throws NoSuchAlgorithmException, UnsupportedEncodingException, Exception {
      super(TermAux.DEVELOPMENT_PATH, TermAux.USER, TermAux.CORE_METADATA_MODULE, ConceptProxy.METADATA_SEMANTIC_TAG, AUXILIARY_METADATA_VERSION, TermAux.CORE_METADATA_MODULE.getPrimordialUuid());

//J-
      createConcept(TermAux.SOLOR_ROOT);
      pushParent(current());
         createConcept("Health concept").setPrimordialUuid("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")
            .addUuids(UUID.fromString("a892950a-0847-300c-b477-4e3cbb945225"));
         pushParent(current());
            createConcept(TermAux.BODY_STRUCTURE);
            createConcept(TermAux.EVENT);
            createConcept(TermAux.FORCE);
            createConcept(TermAux.MEDICATION);
            createConcept(TermAux.PHENOMENON);
            pushParent(current());
               createConcept("Uncategorized phenomenon");
               createConcept("Finding").addUuids(UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c"));
               createConcept("Observation").addUuids(UUID.fromString("d678e7a6-5562-3ff1-800e-ab070e329824"));
            popParent();
            createConcept(TermAux.ORGANISM);
            createConcept(TermAux.OBJECT);
            createConcept(TermAux.PROCEDURE);
            createConcept(TermAux.SPECIMEN);
            createConcept(TermAux.SUBSTANCE);
         popParent();
         createConcept(TermAux.SOLOR_METADATA).addDescription("version:" + AUXILIARY_METADATA_VERSION, TermAux.DEFINITION_DESCRIPTION_TYPE);
         pushParent(current());
            // order (int), field type (concept) 
            // order (int), field concept (concept)
            createConcept(TermAux.ANY_ASSEMBLAGE);
            createConcept(TermAux.SEMANTIC_TYPE);
            pushParent(current());
                createConcept(TermAux.MEMBERSHIP_SEMANTIC);
                createConcept(TermAux.DYNAMIC_SEMANTIC);
                createConcept(TermAux.CONCEPT_SEMANTIC).addComponentIntSemantic(UUID.fromString("4262f3b1-1e11-5258-a414-1d7c7d60ee6b"), TermAux.CONCEPT_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.COMPONENT_SEMANTIC).addComponentIntSemantic(UUID.fromString("ca0ae831-128b-5130-a647-9006cf42b24d"), TermAux.COMPONENT_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.LOGICAL_EXPRESSION_SEMANTIC).addComponentIntSemantic(UUID.fromString("145c5c77-7484-5250-950b-3015b0592e0c"), TermAux.LOGICAL_EXPRESSION_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.INTEGER_SEMANTIC).addComponentIntSemantic(UUID.fromString("55ca0d0d-95e7-50d9-b871-d16c251e2632"), TermAux.INTEGER_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.STRING_SEMANTIC).addComponentIntSemantic(UUID.fromString("562af392-6ff2-5c01-82f5-381b3a6a332b"), TermAux.STRING_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.DESCRIPTION_SEMANTIC)
                        .addComponentIntSemantic(UUID.fromString("b3f0c79e-4dbb-54cc-a461-c09b054d768e"), TermAux.CONCEPT_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE)
                        .addComponentIntSemantic(UUID.fromString("95cd1755-b789-51e7-a706-8de4e382b00a"), TermAux.CONCEPT_FIELD, 1, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE)
                        .addComponentIntSemantic(UUID.fromString("4eabf660-571c-5424-86c0-33a30fe537d5"), TermAux.CONCEPT_FIELD, 2, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE)
                        .addComponentIntSemantic(UUID.fromString("94b6759d-f6f3-5d5c-a37c-b7c78cabbf07"), TermAux.STRING_FIELD, 3, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.IMAGE_SEMANTIC)
                        .addComponentIntSemantic(UUID.fromString("e2ebbb96-ae98-52fb-99c5-a0437bfa8f88"), TermAux.IMAGE_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE)
                ;
                popParent();
            createConcept(TermAux.SEMANTIC_FIELD_TYPE);
            pushParent(current());
                createConcept(TermAux.ARRAY_FIELD);
                createConcept(TermAux.BOOLEAN_FIELD);
                createConcept(TermAux.BYTE_ARRAY_FIELD);
                createConcept(TermAux.DOUBLE_FIELD);
                createConcept(TermAux.FLOAT_FIELD);
                createConcept(TermAux.INTEGER_FIELD);
                createConcept(TermAux.LOGICAL_EXPRESSION_FIELD);
                createConcept(TermAux.LONG_FIELD);
                createConcept(TermAux.COMPONENT_FIELD);
                pushParent(current());
                    createConcept(TermAux.CONCEPT_FIELD);
                    popParent();
                createConcept(TermAux.STRING_FIELD);
                createConcept(TermAux.IMAGE_FIELD);
                createConcept(TermAux.POLYMORPHIC_FIELD);
                createConcept(TermAux.UUID_FIELD);
                popParent();
            createConcept("Module").mergeFromSpec(TermAux.UNSPECIFIED_MODULE);
            pushParent(current());
               createConcept(TermAux.METADATA_MODULES);
               pushParent(current());
                  createConcept(TermAux.CORE_METADATA_MODULE);
                  createConcept(TermAux.KOMET_MODULE).setModule(TermAux.KOMET_MODULE);
                  popParent();
               createConcept(TermAux.SOLOR_MODULE).addDescription("SOLOR", TermAux.REGULAR_NAME_DESCRIPTION_TYPE);
               pushParent(current());
                  createConcept("Solor genomic module");
                  createConcept("SOLOR quality assurance rule module");
                  createConcept("SOLOR temporary concept module")
                          .addDescription("The temporary module is used for concepts such as those that represent feature paths, that are never part of a release.", TermAux.DEFINITION_DESCRIPTION_TYPE);
                  createConcept("SOLOR automation rule module");
                  // The second UUID here was the old value from the TermAux - but this was an orphan. to best fix the bug that resulted,
                  // the type5 UUID from here was moved to TermAux, and the old UUID was added here as an additional.
                  createConcept(TermAux.SOLOR_OVERLAY_MODULE).addDescription("SOLOR overlay", TermAux.REGULAR_NAME_DESCRIPTION_TYPE)
                     .addUuids(UUID.fromString("1f2016a6-960e-11e5-8994-feff819cdc9f"));
                  createConcept("Solor umls module");
               
                  popParent();

               createConcept(TermAux.SCT_CORE_MODULE);
               createConcept("US Extension modules", "US Extension");
               createConcept("LOINC® modules", "LOINC").addDescription("Logical Observation Identifiers Names and Codes", TermAux.DEFINITION_DESCRIPTION_TYPE);
               createConcept("RxNorm modules", "RxNorm");
               createConcept("Generated administration of module");

               //The second UUID here was the old value from the TermAux - but this was an orphan.  to best fix the bug that resulted, 
               // the type5 UUID from here was moved to TermAux, and the old UUID was added here as an additional.
               createConcept(TermAux.VHAT_MODULES).addDescription("VHAT", TermAux.REGULAR_NAME_DESCRIPTION_TYPE).addDescription("VHA Terminology", TermAux.DEFINITION_DESCRIPTION_TYPE)
                     .addUuids(UUID.fromString("1f201520-960e-11e5-8994-feff819cdc9f"));
               createConcept("HL7® v3 modules", "HL7v3").addDescription("Health Level 7 version 3", TermAux.DEFINITION_DESCRIPTION_TYPE);
               createConcept("NUCC modules", "NUCC").addDescription("National Uniform Claim Committee", TermAux.DEFINITION_DESCRIPTION_TYPE);
               createConcept("CVX modules", "CVX").addDescription("Vaccines Administered", TermAux.DEFINITION_DESCRIPTION_TYPE);
               createConcept("MVX modules", "MVX").addDescription("Manufacturers of Vaccines", TermAux.DEFINITION_DESCRIPTION_TYPE);
               createConcept("CPT® modules", "CPT").addDescription("Current Procedural Terminology", TermAux.DEFINITION_DESCRIPTION_TYPE);
               createConcept("SOPT modules", "SOPT").addDescription("Source of Payment Typology", TermAux.DEFINITION_DESCRIPTION_TYPE);
               createConcept("ICD10 modules", "ICD10").addDescription("Procedure Coding System Tenth Revision", TermAux.DEFINITION_DESCRIPTION_TYPE);
               popParent();
            createConcept(TermAux.USER);
            pushParent(current());
               createConcept(TermAux.KEITH_CAMPBELL);
               createConcept(TermAux.DELOITTE_USER);
               createConcept("Bootstrap administrator", "admin");
               createConcept("Clinvar User");
               createConcept("umls User");
               popParent();
            createConcept(TermAux.PATH);
            pushParent(current());

               final ConceptBuilder developmentPath = createConcept(TermAux.DEVELOPMENT_PATH);
               final ConceptBuilder masterPath = createConcept(TermAux.MASTER_PATH);
               masterPath.addUuids(UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")); // UUID from WB_AUX_PATH
               popParent();
            createConcept("Sufficient concept definition operator");
               pushParent(current());
               createConcept(TermAux.SUFFICIENT_CONCEPT_DEFINITION);
               createConcept(TermAux.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION);
               popParent();
               
            createConcept(TermAux.STATEMENT_TYPE);
                pushParent(current());
                createConcept(TermAux.REQUEST_STATEMENT);
                createConcept(TermAux.PERFORMANCE_STATEMENT);
                popParent();

            createConcept("Priority").setPrimordialUuid(UUID.fromString("61c1f346-2103-3032-8066-2add812a5b74"));
                pushParent(current());
                createConcept("Routine").setPrimordialUuid(UUID.fromString("90581618-c1c5-3e6e-ab21-80b18ded492c"));
                createConcept("Immediate").setPrimordialUuid(UUID.fromString("bffcefca-d520-3d4a-ac37-ce8376376136"));
                popParent();
            
            createConcept(TermAux.SUBJECT_OF_INFORMATION);
                pushParent(current());
                createConcept(TermAux.SUBJECT_OF_RECORD);
                createConcept(TermAux.MATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD);
                    pushParent(current());
                    createConcept(TermAux.MOTHER_OF_SUBJECT_OF_RECORD);
                    popParent();
                createConcept(TermAux.PATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD);
                    pushParent(current());
                    createConcept(TermAux.FATHER_OF_SUBJECT_OF_RECORD);
                    popParent();
                popParent();

                createConcept(TermAux.MODE);
                pushParent(current());
                createConcept(TermAux.TEMPLATE);
                createConcept(TermAux.INSTANCE);
                popParent();
            createConcept("EL profile set operator");
            pushParent(current());
               createConcept("Sufficient set").setPrimordialUuid(NodeSemantic.SUFFICIENT_SET.getSemanticUuid());
               createConcept("Necessary set").setPrimordialUuid(NodeSemantic.NECESSARY_SET.getSemanticUuid());
               createConcept("Property set").setPrimordialUuid(NodeSemantic.PROPERTY_SET.getSemanticUuid()).setModule(TermAux.KOMET_MODULE);
               popParent();
            createConcept(TermAux.IDENTIFIER_SOURCE)
               .addDescription("A parent concept and membership assemblage used to group identifiers", TermAux.DEFINITION_DESCRIPTION_TYPE);
            pushParent(current());
               createConcept(TermAux.RXNORM_CUI).addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("SCTID").mergeFromSpec(TermAux.SNOMED_IDENTIFIER).addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept(TermAux.ISAAC_UUID).addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("VUID", "Vets Unique Identifier").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("Code").setPrimordialUuid("803af596-aea8-5184-b8e1-45f801585d17")
                  .addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);// UUID comes from the algorithm in the VHAT econ loader
               ConceptBuilder loincBuilder = createConcept("LOINC ID assemblage");
               loincBuilder.addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               loincBuilder.getPreferredDescriptionBuilder().setDescriptionText("LOINC ID");
               createConcept("Clinvar Variant ID").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("NCBI Gene ID").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("Clinvar Description ID").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               popParent();

            createConcept("Assemblage membership type");
               pushParent(current());
               createConcept("Normal member").setPrimordialUuid("bebbda5d-2fa4-5106-8f02-f2d4673fb1c9");
               createConcept("Marked parent").setPrimordialUuid("5b5adb62-6ced-5013-b849-cad9d1bd34f3");
               popParent();
            createConcept(TermAux.CONTENT_LICENSE);
            pushParent(current());
               createConcept(TermAux.SCT_AFFILIATES_LICENSE);
               createConcept(TermAux.LOINC_LICENSE);
               createConcept(TermAux.RXNORM_LICENSE);
               createConcept(TermAux.APACHE_2_LICENSE);
               createConcept(TermAux.CC_BY_LICENSE);
               createConcept(TermAux.US_GOVERNMENT_WORK);
               popParent();
            createConcept(TermAux.COPYRIGHT);
            pushParent(current());
               createConcept(TermAux.SNOMED_COPYRIGHT);
               createConcept(TermAux.COPYRIGHT_FREE_WORK);
               createConcept(TermAux.REGENSTRIEF_AND_LOINC_COPYRIGHT);
               createConcept(TermAux.INFORMATICS_INC_COPYRIGHT);
               popParent();
            createConcept("Annotation type");
            pushParent(current());
               createConcept("Content issue");
               createConcept("Komet issue");
               createConcept("Quality assurance rule issue");
               createConcept("Automation issue");
               popParent();
            createConcept(TermAux.SEMANTIC_FIELD_CONCEPTS);
            createConcept(TermAux.ASSEMBLAGE);
            pushParent(current());
               createConcept(TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
               createConcept(TermAux.REFLECTION_CLASS_ASSEMBLAGE);
               createConcept(TermAux.PROVIDER_CLASS_ASSEMBLAGE);
               createConcept(TermAux.ASSEMBLAGE_SEMANTIC_FIELDS);
               createConcept("RxNorm Assemblages").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                    createConcept("Active ingredient is different").setModule(TermAux.KOMET_MODULE);
                    createConcept("Dose form is different").setModule(TermAux.KOMET_MODULE);
                    createConcept("Veterinary medicine only").setModule(TermAux.KOMET_MODULE);
                    createConcept("NDC codes available").setModule(TermAux.KOMET_MODULE);
                    createConcept("Substance does not exist").setModule(TermAux.KOMET_MODULE);
                    createConcept("Vaccine").setModule(TermAux.KOMET_MODULE);
                    createConcept("Boss substances are different").setModule(TermAux.KOMET_MODULE);
                    createConcept("Allergen").setModule(TermAux.KOMET_MODULE);
                    createConcept("Values different").setModule(TermAux.KOMET_MODULE);
                    createConcept("RxNorm Asserted").setModule(TermAux.KOMET_MODULE);
                    createConcept("Prescribable").setModule(TermAux.KOMET_MODULE);

                    popParent();
               createConcept("Issue management assemblage").addComponentSemantic(UUID.fromString("6b0a4de8-e2db-54a1-9c87-fb365df15662"), STRING_SEMANTIC, SEMANTIC_TYPE);
               pushParent(current());
                  createConcept("Content issue assemblage").addComponentSemantic(UUID.fromString("0b50bab6-8b0a-51d6-ad03-caf478ab5040"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("KOMET issue assemblage").addComponentSemantic(UUID.fromString("2d97a6b5-7fbf-581f-b9ab-5c61c1d9bc60"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Quality assurance rule issue assemblage").addComponentSemantic(UUID.fromString("ea165bab-5e1d-53fd-a439-42d30e82c563"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Automation issue assemblage").addComponentSemantic(UUID.fromString("77a6ca0e-24b5-5aa2-8adb-972e72a6c990"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Clinical statement issue assemblage").addComponentSemantic(UUID.fromString("5f546442-1f45-5766-a804-7527fccd1750"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("SNOMED® issue assemblage").addComponentSemantic(UUID.fromString("76c751bd-2700-517e-8414-2626e197a781"),STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("LOINC® issue assemblage").addComponentSemantic(UUID.fromString("68d50701-a289-530a-99bc-785a3a79a310"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("RxNorm issue assemblage").addComponentSemantic(UUID.fromString("347ea828-41e2-50cb-bbf5-ab8cd7506c3e"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("SOLOR issue assemblage").addComponentSemantic(UUID.fromString("ef4e2854-cff5-5a52-bc26-d74b3c5d652c"), STRING_SEMANTIC, SEMANTIC_TYPE);
                  popParent();
               createConcept(TermAux.DESCRIPTION_ASSEMBLAGE);
               createConcept("Dialect assemblage");
               pushParent(current());
                  createConcept(TermAux.ENGLISH_DIALECT_ASSEMBLAGE).addComponentSemantic(UUID.fromString("1efad98f-1448-53bd-b0bf-168b788f0428"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  pushParent(current());
                     createConcept("GB English dialect").mergeFromSpec(TermAux.GB_DIALECT_ASSEMBLAGE).addComponentSemantic(UUID.fromString("d34e3933-797a-55a5-8425-36dca1e99c90"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     createConcept("US English dialect").mergeFromSpec(TermAux.US_DIALECT_ASSEMBLAGE).addComponentSemantic(UUID.fromString("1c54bec0-ded0-5121-bd93-62929c4b329b"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     pushParent(current());
                        createConcept("US Nursing dialect").addComponentSemantic(UUID.fromString("f061d440-513b-53d1-ada5-700550614561"), CONCEPT_SEMANTIC, SEMANTIC_TYPE).setPrimordialUuid("6e447636-1085-32ff-bc36-6748a45255de");
                        popParent();
                     popParent();
                  createConcept(TermAux.SPANISH_DIALECT_ASSEMBLAGE).addComponentSemantic(UUID.fromString("3a701ef9-6c03-5b02-ae4f-190785518c27"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  pushParent(current());
                     createConcept(TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE).addComponentSemantic(UUID.fromString("5a334acb-0d79-529f-8224-a171d6a55fca"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     popParent();
                  createConcept("French dialect").addComponentSemantic(UUID.fromString("2abccc59-c8d8-5b2f-b39d-211598fbf972"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Korean dialect").addComponentSemantic(UUID.fromString("ab213d1f-d5cf-52bb-a178-b01dd3313814"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  pushParent(current());
                     createConcept("Standard Korean dialect").addComponentSemantic(UUID.fromString("2f31bf6b-61d0-5b1c-aed3-199aca16d5a5"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     popParent();
                  createConcept("Polish dialect").addComponentSemantic(UUID.fromString("511d12fd-dac6-5f0e-a206-e1affadcf102"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Irish dialect").addComponentSemantic(UUID.fromString("5c8a229a-78f8-545c-8ed6-9ec81c007688"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Czech dialect").addComponentSemantic(UUID.fromString("b06f811f-843f-5bef-931e-5a893903ffc1"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Russian dialect").addComponentSemantic(UUID.fromString("97c37520-b021-522d-b7bd-6d0833b27755"), CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  popParent();
               createConcept("Logic assemblage");
               pushParent(current());
                  createConcept(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE);
                  createConcept(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE);
                  createConcept(TermAux.SRF_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE);
                  ConceptBuilder builder = createConcept(TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE);
                  builder.getPreferredDescriptionBuilder().setDescriptionText("SNOMED legacy implication");
                  createConcept("Clinvar Definition Assemblage");
                  popParent();
               createConcept(TermAux.CONCEPT_ASSEMBLAGE);
               pushParent(current());
                  createConcept(TermAux.SOLOR_CONCEPT_ASSEMBLAGE);
                  createConcept(TermAux.LOINC_CONCEPT_ASSEMBLAGE);
                  createConcept(TermAux.RXNORM_CONCEPT_ASSEMBLAGE);
                  popParent();
               createConcept("External data assemblage");
               pushParent(current());
                  createConcept(TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE);
                  createConcept(TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE);
                  createConcept(TermAux.SRF_INFERRED_RELATIONSHIP_ASSEMBLAGE);
                  createConcept(TermAux.SRF_STATED_RELATIONSHIP_ASSEMBLAGE);
                  createConcept(TermAux.LOINC_RECORD_ASSEMBLAGE);
                  createConcept("Clinvar Variant to Gene Non-Defining Taxonomy");
                  createConcept("Clinvar Gene to Phenotype Non-Defining Taxonomy");
                  createConcept("umls equivalency assemblage");
                  popParent();
               createConcept("Rule assemblage");
               pushParent(current());
                  createConcept("Module assemblage");
                  createConcept("Quality assurance rule assemblage");
                  createConcept("Automation rule assemblage");
                  popParent();
               createConcept("Image assemblage");
               pushParent(current());
                  createConcept("Concept image")
                          .addComponentSemantic(UUID.fromString("f6f48a4d-befd-5806-936e-6238d7c912b3"), IMAGE_SEMANTIC, SEMANTIC_TYPE)
                          .addComponentIntSemantic(UUID.fromString("42ae3aa2-b41f-509d-bd62-141964f58058"), IMAGE_DATA_FOR_SEMANTIC, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS);
                  popParent();
               createConcept("Assemblage related to path management");
               pushParent(current());

                  final ConceptBuilder paths = createConcept("Paths assemblage");
                  paths.mergeFromSpec(TermAux.PATH_ASSEMBLAGE);
                  addPath(paths, masterPath, TermAux.MASTER_PATH_SEMANTIC_UUID);
                  addPath(paths, developmentPath, TermAux.DEVELOPMENT_PATH_SEMANTIC_UUID);

                  final ConceptBuilder pathOrigins = createConcept("Path origins assemblage");
                  pathOrigins.mergeFromSpec(TermAux.PATH_ORIGIN_ASSEMBLAGE);

                  // addPathOrigin(pathOrigins, developmentPath, masterPath);
                  popParent();
               popParent();
            createConcept("Content Metadata");
            pushParent(current());
               createConcept(TermAux.DATABASE_UUID);
               createConcept("Source Artifact Version");
               createConcept("Source Release Date");
               createConcept("Converter Version");
               createConcept("Converted IBDF Artifact Version");
               createConcept("Converted IBDF Artifact Classifier");
               popParent();
            createConcept(TermAux.LANGUAGE);
            pushParent(current());  //Adding the UUIDs from the retired "assemblage" only concept, which just made the metadata far more 
            //confusing than necessary, also, making 2 parents, one of language, the other under assemblage.
               createConcept(TermAux.ENGLISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentSemantic(UUID.fromString("16fd6c1f-e4d6-50ad-b77a-8b57eaae16cb"), DESCRIPTION_SEMANTIC, SEMANTIC_TYPE)
                       .addComponentIntSemantic(UUID.fromString("704ebb91-f713-5485-ae2e-ddcf315f70f8"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("423cd6bd-d939-57cc-adfa-3e0a748a3c25"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("68563e65-b39e-5604-af38-864127870429"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("68563e65-b39e-5604-af38-864127870429"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021920-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.SPANISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("3294978d-15b0-598e-b6c6-819d65d23960"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("f4b423a7-e548-5b8a-be3b-59ed29dc5ef2"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("340e18fc-3528-5b32-ba27-670ce9dd9dcf"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("55b9b3b5-f720-5cea-aac4-e01a33470105"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021c36-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.FRENCH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("a52da1b9-a923-5869-99d2-b4b5bc47c87c"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("b6be17f5-4389-526e-9a94-1f1dd7d303b1"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("c3fbd2dd-a2a3-5d7a-a58b-6a68462e009b"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("8f156027-ba6f-54cc-b327-9abc8fb0bb28"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021dbc-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.DANISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("48eab53d-7d9e-593d-a006-1918a6a14440"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("0e509d15-4aad-548a-b423-65ceebf26ee0"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("b55a4395-cfbd-55c4-8512-c8944e8e5509"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("af9aac66-3244-5622-8b66-2e1f028b5497"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021f10-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.POLISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("c790ee35-72ec-5685-8ad2-f768b6de00fb"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("e974688d-05af-5791-8a59-9fb2cdeaeedd"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("d75ee68e-c0e5-5243-8c63-c7f48fa7eaea"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("51652b05-02f6-5eac-84bb-9a08c0020c5a"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022140-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.DUTCH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("6ced8ded-7e67-518f-9050-a876782f9ce7"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("8e4cebc4-b9d9-523b-9fc3-2c331bc964fc"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("0191b805-c94d-5d95-a36d-6de06625561f"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("7371683b-10d4-5b6e-997e-df5e1914670f"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022280-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.LITHUANIAN_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("a53357a0-9ae0-5e2e-a18a-9e69efbf682f"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("37cdcc04-a793-55f0-9424-2e32e1b2c4bb"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("e3882496-d970-5c06-8b8c-33398c211e58"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("e844638b-6cc5-5062-96f1-3087cc14f837"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022410-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.CHINESE_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("c182c189-9b66-5a7d-a834-d278efbfaf45"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("58a9fc07-3d6a-5d7e-9fc6-d74aecf92772"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("dcf1d02e-c729-54eb-86bb-aba2688f39dc"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("566d0fe0-2728-5941-9822-0e38bd6a4b5c"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022532-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.JAPANESE_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("1fcb21be-3958-5914-93f0-9d73cebe1d10"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("56991166-7c20-5cce-8506-7c168aaa2e2b"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("1e825368-49f6-5adc-ba20-27fd8b033cf0"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("753feb91-af58-5b26-9edc-3cb7329d8da9"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("450226cc-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.SWEDISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(UUID.fromString("f4f2bbcf-75ce-5fae-ac4c-53bb5b4de9b1"), TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("d602c3de-b49c-5a79-8927-2ca88759fef0"), LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("d74f25a4-a543-58f4-811d-fb58c22406a4"), DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(UUID.fromString("c48e9061-1296-5228-a671-5025e7221cd4"), CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022848-9567-11e5-8994-feff819cdc9f"));
               createConcept("Korean language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               createConcept("Russian language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               createConcept("Irish language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               createConcept("Czech language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               popParent();
            createConcept("Measurement semantic");
            pushParent(current());
                createConcept(TermAux.EXISTENTIAL_MEASUREMENT_SEMANTIC);
                createConcept(TermAux.TIME_MEASUREMENT_SEMANTIC);
                pushParent(current());
                    createConcept(TermAux.ISO_8601);
                    createConcept(TermAux.ISO_8601_AFTER);
                    createConcept(TermAux.ISO_8601_PRIOR);
                popParent();
                createConcept(TermAux.DISCREATE_MEASURE_SEMANTICS);
                pushParent(current());
                    createConcept(TermAux.ITEM_COUNT);
                popParent();
                createConcept(TermAux.PRESSURE_MEASURE_SEMANTICS);
                pushParent(current());
                    createConcept(TermAux.MM_HG);
                popParent();
                createConcept(TermAux.MASS_MEASUREMENT_SEMANTIC);
                pushParent(current());
                    createConcept(TermAux.MILLIGRAM);
                popParent();
                createConcept(TermAux.TEXT_COMPARISON_SEMANTIC);
                pushParent(current());
                    createConcept(TermAux.CASE_SENSITIVE_EVAL);
                    createConcept(TermAux.CASE_INSENSITIVE_EVAL);
                    createConcept(TermAux.UNICODE_OPERATION_EVAL);
                popParent();
            popParent();
                
            createConcept("Axiom origin");
            pushParent(current());
               createConcept(TermAux.STATED_PREMISE_TYPE).addUuids(UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));
               createConcept(TermAux.INFERRED_PREMISE_TYPE).addUuids(UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"));
               popParent();
            createConcept("Description type");
            pushParent(current());

               final ConceptBuilder fsn = createConcept(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
               fsn.addUuids(UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66")); // RF1 FSN
               
               final ConceptBuilder syn = createConcept(TermAux.REGULAR_NAME_DESCRIPTION_TYPE);
               syn.addUuids(UUID.fromString("d6fad981-7df6-3388-94d8-238cc0465a79"));
               syn.addDescription("Synonym", TermAux.REGULAR_NAME_DESCRIPTION_TYPE);
               createConcept(TermAux.DEFINITION_DESCRIPTION_TYPE);
               popParent();
            createConcept(TermAux.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY); // SOLOR extended description types are created under this node
            createConcept(TermAux.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY); // SOLOR extended relationship types are created under this node
            createConcept("Description case significance");
            pushParent(current());
               createConcept(TermAux.DESCRIPTION_CASE_SENSITIVE);
               createConcept(TermAux.DESCRIPTION_NOT_CASE_SENSITIVE);
               createConcept(TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE);
               createConcept("Not Applicable");
               popParent();
            createConcept("Description acceptability");
            pushParent(current());
               createConcept(TermAux.ACCEPTABLE);
               createConcept(TermAux.PREFERRED);
               popParent();
            createConcept("Tree amalgam properties").setModule(TermAux.KOMET_MODULE);
            pushParent(current());
               createConcept("Include defining taxonomy").setModule(TermAux.KOMET_MODULE);
               createConcept("Tree list").setModule(TermAux.KOMET_MODULE);
               createConcept("Inverse tree list").setModule(TermAux.KOMET_MODULE);
               popParent();
            createConcept("Taxonomy operator");
            pushParent(current());
               createConcept(TermAux.CHILD_OF);
               final ConceptBuilder isa = createConcept("Is-a");
               isa.setPrimordialUuid(TermAux.IS_A.getPrimordialUuid());
               isa.addUuids(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")); // merge with "Is a (attribute)" //SCTID 116680003
                createConcept("Logically equivalent to");
                popParent();
            createConcept("Connective operator");
            pushParent(current());
               createConcept("And").setPrimordialUuid(NodeSemantic.AND.getSemanticUuid());
               createConcept("Or").setPrimordialUuid(NodeSemantic.OR.getSemanticUuid());
               createConcept("Disjoint with").setPrimordialUuid(NodeSemantic.DISJOINT_WITH.getSemanticUuid());
               createConcept("Definition root").setPrimordialUuid(NodeSemantic.DEFINITION_ROOT.getSemanticUuid());
               popParent();
            createConcept("Node operator");
            pushParent(current());
               createConcept("Template merge");
               createConcept("Field substitution");
               pushParent(current());
                  createConcept("Concept substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_CONCEPT.getSemanticUuid());
                  createConcept("Boolean substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_BOOLEAN.getSemanticUuid());
                  createConcept("Float substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_FLOAT.getSemanticUuid());
                  createConcept("Instant substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_INSTANT.getSemanticUuid());
                  createConcept("Integer substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_INTEGER.getSemanticUuid());
                  createConcept("string substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_STRING.getSemanticUuid());
                  popParent();
               createConcept("Concept reference").setPrimordialUuid(NodeSemantic.CONCEPT.getSemanticUuid());
               popParent();
            createConcept("Template concept").setPrimordialUuid(NodeSemantic.TEMPLATE.getSemanticUuid());
            pushParent(current());
               createConcept("Skin of region template");
               popParent();
            
            // add annotations for order and labels
            // create template
            createConcept("Role operator");
            pushParent(current());
               createConcept("Universal restriction").setPrimordialUuid(NodeSemantic.ROLE_ALL.getSemanticUuid());
               createConcept("Existential restriction").setPrimordialUuid(NodeSemantic.ROLE_SOME.getSemanticUuid());
               popParent();
            createConcept("Logical feature").addDescription("A property is a characteristic of a class - a directed binary relation that specifies some attribute which is true for instances of that class. Properties sometimes act as data values, or links to other instances. Properties may exhibit logical features, for example, by being transitive, symmetric, inverse and functional. Properties may also have domains and ranges.",
                DEFINITION_DESCRIPTION_TYPE
            ).setModule(TermAux.KOMET_MODULE);
            pushParent(current());
                createConcept("Transitive feature").setModule(TermAux.KOMET_MODULE);
                createConcept("Symmetric feature").setModule(TermAux.KOMET_MODULE);
                createConcept("Inverse feature").setModule(TermAux.KOMET_MODULE);
                createConcept("Functional feature").setModule(TermAux.KOMET_MODULE);
                createConcept("Reflexive feature").setModule(TermAux.KOMET_MODULE);
                popParent();
            createConcept("Status value");
            pushParent(current());
               createConcept(TermAux.INACTIVE_STATUS);
               createConcept(TermAux.ACTIVE_STATUS);
               createConcept(TermAux.PRIMORDIAL_STATUS);
               createConcept(TermAux.CANCELED_STATUS);
               createConcept(TermAux.WITHDRAWN_STATUS);
                popParent();
            createConcept("Precedence");
            pushParent(current());
               createConcept(TermAux.TIME_PRECEDENCE);
               createConcept(TermAux.PATH_PRECEDENCE);
                popParent();
            createConcept("Literal value");
            pushParent(current());
               createConcept("Boolean literal").setPrimordialUuid(NodeSemantic.LITERAL_BOOLEAN.getSemanticUuid());
               createConcept("Float literal").setPrimordialUuid(NodeSemantic.LITERAL_DOUBLE.getSemanticUuid());
               createConcept("Instant literal").setPrimordialUuid(NodeSemantic.LITERAL_INSTANT.getSemanticUuid());
               createConcept("Integer literal").setPrimordialUuid(NodeSemantic.LITERAL_INTEGER.getSemanticUuid());
               createConcept("String literal").setPrimordialUuid(NodeSemantic.LITERAL_STRING.getSemanticUuid());
               popParent();
            createConcept("Concrete domain operator");
            pushParent(current());
               createConcept("Greater than");
               createConcept("Greater than or equal to");
               createConcept("Equal to");
               createConcept("Less than or equal to");
               createConcept("Less than");
               popParent();
            createConcept("Description-logic profile");
            pushParent(current());
               createConcept("EL++ profile").mergeFromSpec(TermAux.EL_PLUS_PLUS_LOGIC_PROFILE);
               createConcept("SH profile");
               popParent();
            createConcept("Description-logic classifier");
            pushParent(current());
               createConcept(TermAux.IHTSDO_CLASSIFIER);
               createConcept("SnoRocket classifier").mergeFromSpec(TermAux.SNOROCKET_CLASSIFIER);
               createConcept("ConDOR classifier");
               popParent();
            createConcept("Property pattern implication").setPrimordialUuid(NodeSemantic.PROPERTY_PATTERN_IMPLICATION.getSemanticUuid()).setModule(TermAux.KOMET_MODULE);
            createConcept("Feature").setPrimordialUuid(NodeSemantic.FEATURE.getSemanticUuid());
            pushParent(current());
               createConcept("Ingredient strength");
            popParent();
            createConcept("Role").setPrimordialUuid("6155818b-09ed-388e-82ce-caa143423e99");
            pushParent(current());
               createConcept("Intrinsic role");
               pushParent(current());
               // Added UUID corresponding to SNOMED role group UUID.
                  createConcept(TermAux.ROLE_GROUP).addUuids(UUID.fromString("051fbfed-3c40-3130-8c09-889cb7b7b5b6"));

                  popParent();
               createConcept(TermAux.PART_OF);  //TODO [KEC] not sure if I put these 4 (previously) missing concepts in the right place in the hierarchy
               createConcept(TermAux.LATERALITY);
               createConcept(TermAux.HAS_ACTIVE_INGREDIENT);
               createConcept(TermAux.HAS_DOSE_FORM);
               popParent();
            createConcept("Unmodeled concept");
            pushParent(current());
               createConcept("Anonymous concept");
               createConcept("Unmodeled role concept");
               createConcept("Unmodeled feature concept");
               createConcept("Unmodeled taxonomic concept");
               createConcept(TermAux.UNINITIALIZED_COMPONENT_ID);
               popParent();
            createConcept("Object properties");
            pushParent(current());
               createConcept("Action properties");
               pushParent(current());
                  createConcept("Action name");
                  createConcept(ObservableFields.ASSEMBLAGE_FOR_ACTION);
                  createConcept(ObservableFields.CONCEPT_CONSTRAINTS);
                  createConcept(ObservableFields.VERSION_TYPE_FOR_ACTION);
                  createConcept(ObservableFields.ROLE_TYPE_TO_ADD);
                  createConcept(ObservableFields.ASSEMBLAGE_FOR_CONSTRAINT);
                  popParent();
               createConcept("Configuration properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Configuration name", "Configuration name").setModule(TermAux.KOMET_MODULE);
                  createConcept("Datastore location", "Datastore location").setModule(TermAux.KOMET_MODULE);
                  popParent();
               createConcept("Window configuration properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Window configuration name").getPreferredDescriptionBuilder().setDescriptionText("Window name").setModule(TermAux.KOMET_MODULE);
                  createConcept("Left tab nodes").getPreferredDescriptionBuilder().setDescriptionText("Left tab").setModule(TermAux.KOMET_MODULE);
                  createConcept("Right tab nodes").getPreferredDescriptionBuilder().setDescriptionText("Right tab").setModule(TermAux.KOMET_MODULE);
                  createConcept("Center tab nodes").getPreferredDescriptionBuilder().setDescriptionText("Center tab nodes").setModule(TermAux.KOMET_MODULE);
                  createConcept("Window x position").setModule(TermAux.KOMET_MODULE);
                  createConcept("Window y position").setModule(TermAux.KOMET_MODULE);
                  createConcept("Window width").setModule(TermAux.KOMET_MODULE);
                  createConcept("Window height").setModule(TermAux.KOMET_MODULE);
                  popParent();
               createConcept("Komet panels").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                     createConcept("Exploration nodes").setModule(TermAux.KOMET_MODULE);
                     pushParent(current());
                     createConcept("Simple search panel")
                             .addStringSemantic(UUID.fromString("4d45c20c-cf25-5280-b422-fad245e12899"),
                                     "sh.komet.gui.search.simple.SimpleSearchViewFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Simple search")
                             .setModule(TermAux.KOMET_MODULE);

                     createConcept("Classification results panel")
                             .addStringSemantic(UUID.fromString("8f76efd0-4978-56a9-90cc-66937e9b36fc"),
                                     "sh.komet.gui.provider.classification.ClassificationResultsProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Classification results")
                             .setModule(TermAux.KOMET_MODULE);

                     createConcept("Extended search panel").addStringSemantic(UUID.fromString("d8301130-5bb6-52d6-b85a-04ac36a3b70c"), "sh.komet.gui.search.extended.ExtendedSearchViewFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Extended search").setModule(TermAux.KOMET_MODULE);

                     createConcept("FLWOR query panel").addStringSemantic(UUID.fromString("2468daa1-27fe-5332-907d-85f3ca5c2f89"), "sh.komet.gui.search.flwor.FLWORQueryViewFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("FLWOR query").setModule(TermAux.KOMET_MODULE);

                     createConcept("Activities panel").addStringSemantic(UUID.fromString("627abd2f-dd49-5bbb-b2af-1b47feaa38be"), "sh.komet.progress.view.TaskProgressNodeFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Activities").setModule(TermAux.KOMET_MODULE);

                     createConcept("Completion panel").addStringSemantic( UUID.fromString("49e15039-76e5-5a16-bfcd-49aa4b555781"), "sh.komet.progress.view.TasksCompletedNodeFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Completions").setModule(TermAux.KOMET_MODULE);

                     createConcept("Assemblage panel").addStringSemantic(UUID.fromString("c89d0881-a09d-5938-a287-4f475c90f98b"), "sh.komet.assemblage.view.AssemblageViewProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Assemblage").setModule(TermAux.KOMET_MODULE);

                     createConcept("Taxonomy panel").addStringSemantic(UUID.fromString("c78b6598-8188-5696-8bbb-afa9b2c2326c"), "sh.isaac.komet.gui.treeview.TreeViewExplorationNodeFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Taxonomy").setModule(TermAux.KOMET_MODULE);

                     createConcept("Dynamic assemblage definition panel").addStringSemantic(UUID.fromString("3e4cbb69-19e9-56fe-80d0-e69ff3a59beb"), "sh.komet.?", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Dynamic assemblage definition").setModule(TermAux.KOMET_MODULE);

                     createConcept("System dashboard panel").addStringSemantic(UUID.fromString("d62a8a77-4d96-5c6e-a040-5b74b412f616"), "sh.komet.gui.action.dashboard.DashboardNodeFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("System dashboard").setModule(TermAux.KOMET_MODULE);

                     createConcept("Groovy scripting panel").addStringSemantic(UUID.fromString("eafc507e-f7ef-5198-9dff-cee1f98bf2fb"), "sh.komet.scripting.groovy.GroovyViewFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Groovy").setModule(TermAux.KOMET_MODULE);

                     createConcept("Export specification panel").addStringSemantic(UUID.fromString("088cabd6-47d2-57aa-8726-2ec91f287ce4"), "sh.komet.gui.exportation.ExportSpecificationFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                        .getPreferredDescriptionBuilder().setDescriptionText("Export specification").setModule(TermAux.KOMET_MODULE);

       createConcept("Component list panel").addStringSemantic(UUID.fromString("6b8fa23c-358e-5335-ac17-9239ea821842"), "sh.isaac.komet.batch.ListViewFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
               .getPreferredDescriptionBuilder().setDescriptionText("Component list").setModule(TermAux.KOMET_MODULE);

       createConcept("Composite action panel").addStringSemantic(UUID.fromString("1123d55e-75f6-502f-80a1-d9f0fe86eb7a"), "sh.isaac.komet.batch.CompositeActionFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
               .getPreferredDescriptionBuilder().setDescriptionText("Composite action").setModule(TermAux.KOMET_MODULE);

       popParent();
                  createConcept("Detail nodes").getPreferredDescriptionBuilder().setModule(TermAux.KOMET_MODULE);
                  pushParent(current());
                     createConcept("Concept details panel").addStringSemantic(UUID.fromString("de7b7946-d001-56de-919b-0dba9459a28a"), "sh.komet.gui.provider.concept.detail.panel.ConceptDetailPanelProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Concept details").setModule(TermAux.KOMET_MODULE);

                     createConcept("Concept details search-linked panel")
               .addStringSemantic(UUID.fromString("99a62de4-b0d7-5781-87a5-02580aa6ed87"),
                       "sh.komet.gui.provider.concept.detail.panel.ConceptDetailSearchLinkedPanelProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
               .getPreferredDescriptionBuilder().setDescriptionText("Concept details - search").setModule(TermAux.KOMET_MODULE);

       createConcept("Concept details classification-results-linked panel")
               .addStringSemantic(UUID.fromString("e274d5b2-4db8-558c-be73-82a181b074f1"),
                       "sh.komet.gui.provider.concept.detail.panel.ConceptDetailClassificationResultsLinkedPanelProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
               .getPreferredDescriptionBuilder().setDescriptionText("Concept details - classification").setModule(TermAux.KOMET_MODULE);

       createConcept("Concept details list-view-linked panel")
               .addStringSemantic(UUID.fromString("de1ebfaa-e94c-5533-99c7-9309133eef2b"),
                       "sh.komet.gui.provider.concept.detail.panel.ConceptDetailListLinkedPanelProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
               .getPreferredDescriptionBuilder().setDescriptionText("Concept details - list").setModule(TermAux.KOMET_MODULE);


       createConcept("Concept details taxonomy-linked panel").addStringSemantic(UUID.fromString("4155e905-57fb-5805-b9cd-55cfcf538e33"), "sh.komet.gui.provider.concept.detail.panel.ConceptDetailTaxonomyLinkedPanelProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                            .getPreferredDescriptionBuilder().setDescriptionText("Concept details - taxonomy").setModule(TermAux.KOMET_MODULE);

                     createConcept("Concept details tree table").addStringSemantic(UUID.fromString("394e925e-3928-52f6-a0a6-b6b22813f1b5"), "sh.komet.gui.provider.concept.detail.treetable.ConceptDetailTreeTableProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Concept details tree table").setModule(TermAux.KOMET_MODULE);
                     createConcept("Semantic tree table panel").addStringSemantic(UUID.fromString("bbf42dbe-050c-53c5-847b-03d704f2aef9"), "sh.isaac.komet.gui.semanticViewer.SemanticViewer", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Semantic table").setModule(TermAux.KOMET_MODULE);
                     createConcept("Concept builder panel").addStringSemantic(UUID.fromString("282a5bbe-8c16-5145-9c51-ae75a3193775"), "sh.komet.gui.provider.concept.builder.ConceptBuilderProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Concept builder").setModule(TermAux.KOMET_MODULE);
                     createConcept("Logic details panel").addStringSemantic(UUID.fromString("d06f1dc7-8d1e-56a4-9932-2d3d656405b9"), "sh.komet.gui.provider.concept.detail.logic.LogicDetailProviderFactory", TermAux.PROVIDER_CLASS_ASSEMBLAGE)
                             .getPreferredDescriptionBuilder().setDescriptionText("Logic details").setModule(TermAux.KOMET_MODULE);
                     popParent();
                  popParent();
               createConcept("Taxonomy configuration properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Taxonomy configuration name").getPreferredDescriptionBuilder().setDescriptionText("Configuration name").setModule(TermAux.KOMET_MODULE);
                  createConcept("Taxonomy configuration roots").getPreferredDescriptionBuilder().setDescriptionText("Taxonomy roots").setModule(TermAux.KOMET_MODULE);
                  popParent();
               createConcept("Synchronization item properties");
               pushParent(current());
                  createConcept("Item name");
                  createConcept("Item active", "Active");
                  createConcept(GIT_URL);
                  createConcept(GIT_USER_NAME);
                  createConcept(GIT_PASSWORD);
                  popParent();
               createConcept("Stamp coordinate properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Stamp coordinate name");
                  popParent();
               createConcept("Language coordinate properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Language coordinate name");
                  popParent();
               createConcept("Logic coordinate properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Logic coordinate name");
                   popParent();
               createConcept("Manifold coordinate properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Manifold name").setModule(TermAux.KOMET_MODULE);
                  createConcept("Manifold focus").setModule(TermAux.KOMET_MODULE);
                  createConcept("Manifold history").setModule(TermAux.KOMET_MODULE);
                  popParent();
               createConcept("Persona properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept("Persona name").setModule(TermAux.KOMET_MODULE);
                  createConcept("Persona instance name").addDescription("Instance name", TermAux.REGULAR_NAME_DESCRIPTION_TYPE).setModule(TermAux.KOMET_MODULE);
                  createConcept("Enable left pane").setModule(TermAux.KOMET_MODULE);
                  createConcept("Enable center pane").setModule(TermAux.KOMET_MODULE);
                  createConcept("Enable right pane").setModule(TermAux.KOMET_MODULE);
                  createConcept("Left pane options", "Left options").setModule(TermAux.KOMET_MODULE);
                  createConcept("Center pane options", "Center options").setModule(TermAux.KOMET_MODULE);
                  createConcept("Right pane options", "Right options").setModule(TermAux.KOMET_MODULE);
                  createConcept("Left pane dafaults", "Left defaults").setModule(TermAux.KOMET_MODULE);
                  createConcept("Center pane defaults", "Center defaults").setModule(TermAux.KOMET_MODULE);
                  createConcept("Right pane defaults", "Right defaults").setModule(TermAux.KOMET_MODULE);
                  popParent();
               createConcept("Coordinate properties").setModule(TermAux.KOMET_MODULE);
               pushParent(current());
                  createConcept(AUTHOR_NID_FOR_EDIT_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(MODULE_NID_FOR_EDIT_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(MODULE_OPTIONS_FOR_EDIT_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(PATH_NID_FOR_EDIT_CORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(PATH_OPTIONS_FOR_EDIT_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(LANGUAGE_NID_FOR_LANGUAGE_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(LANGUAGE_FOR_LANGUAGE_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(DIALECT_ASSEMBLAGE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(MODULE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(MODULE_NID_PREFERENCE_LIST_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(MODULE_SPECIFICATION_PREFERENCE_LIST_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(CLASSIFIER_NID_FOR_LOGIC_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(STAMP_PRECEDENCE_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(STAMP_POSITION_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(ALLOWED_STATES_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(MODULE_NID_ARRAY_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(AUTHOR_SPECIFICATION_SET_FOR_STAMP_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(PATH_NID_FOR_STAMP_PATH).setModule(TermAux.KOMET_MODULE);
                  createConcept(PATH_ORIGIN_LIST_FOR_STAMP_PATH).setModule(TermAux.KOMET_MODULE);
                  createConcept(TIME_FOR_STAMP_POSITION).setModule(TermAux.KOMET_MODULE);
                  createConcept(PATH_NID_FOR_STAMP_POSITION).setModule(TermAux.KOMET_MODULE);
                  createConcept(PREMISE_TYPE_FOR_TAXONOMY_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(UUID_FOR_TAXONOMY_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  createConcept(LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE).setModule(TermAux.KOMET_MODULE);
                  popParent();
               createConcept(DESCRIPTION_DIALECT);
               createConcept("Description/dialect properties");
               pushParent(current());
                  createConcept(DESCRIPTION_DIALECT_DESCRIPTION);
                  createConcept(DESCRIPTION_DIALECT_DIALECT);
                  popParent();
               createConcept("Version properties");
               pushParent(current());
                  createConcept(STATUS_FOR_VERSION);
                  createConcept(TIME_FOR_VERSION);
                  createConcept(AUTHOR_NID_FOR_VERSION);
                  createConcept(MODULE_NID_FOR_VERSION);
                  createConcept(PATH_NID_FOR_VERSION);
                  createConcept(COMMITTED_STATE_FOR_VERSION);
                  createConcept(STAMP_SEQUENCE_FOR_VERSION);
                  createConcept("Description version properties");
                  pushParent(current());
                     createConcept(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION);
                     createConcept(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION);
                     createConcept(TEXT_FOR_DESCRIPTION);
                     createConcept(DESCRIPTION_TYPE_FOR_DESCRIPTION);
                     popParent();
                  popParent();
               createConcept("Chronicle properties");
               pushParent(current());
                  createConcept(VERSION_LIST_FOR_CHRONICLE);
                  createConcept(NATIVE_ID_FOR_COMPONENT);
                  createConcept(ENTRY_SEQUENCE_FOR_COMPONENT);
                  createConcept(PRIMORDIAL_UUID_FOR_COMPONENT);
                  createConcept(UUID_LIST_FOR_COMPONENT);
                  createConcept(COMMITTED_STATE_FOR_CHRONICLE);
                  createConcept(SEMANTIC_LIST_FOR_CHRONICLE);
                  createConcept(ASSEMBLAGE_NID_FOR_COMPONENT);
                  createConcept(REFERENCED_COMPONENT_NID_FOR_SEMANTIC);
                  popParent();
               createConcept("Concept properties");
               pushParent(current());
                  createConcept(DESCRIPTION_LIST_FOR_CONCEPT);
                  createConcept(CONCEPT_VERSION);
                  createConcept(ObservableFields.CONCEPT_IS_ASSEMBLAGE);
                  popParent();
               createConcept("Corelation properties");
               pushParent(current());
                  createConcept(CORELATION_REFERENCE_EXPRESSION);
                  createConcept(CORELATION_COMPARISON_EXPRESSION);
                  createConcept(CORELATION_EXPRESSION);
                  popParent();
               createConcept("KOMET preference properties");
               pushParent(current());
                  createConcept(ObservableFields.GIT_LOCAL_FOLDER);
                  createConcept(ObservableFields.ENABLE_EDIT);
                  createConcept(ObservableFields.KOMET_USER);
                  createConcept(ObservableFields.KOMET_USER_LIST);
                  createConcept(ObservableFields.MODULE_FOR_USER);
                  createConcept(ObservableFields.PATH_FOR_USER);
                  popParent();
               createConcept("Semantic properties");
               pushParent(current());
                  createConcept(ObservableFields.SEMANTIC_FIELD_NAME);
                  createConcept(ObservableFields.STRING_VALUE_FOR_SEMANTIC);
                  createConcept(ObservableFields.COMPONENT_NID_FOR_SEMANTIC);
                  createConcept(ObservableFields.LOGIC_GRAPH_FOR_SEMANTIC);
                  createConcept(ObservableFields.LONG_VALUE_FOR_SEMANTIC);
                  createConcept(ObservableFields.IMAGE_DATA_FOR_SEMANTIC);
                   createConcept(ObservableFields.TYPE_NID_FOR_RF2_REL);
                   createConcept(ObservableFields.DESTINATION_NID_FOR_RF2_REL);
                   createConcept(ObservableFields.REL_GROUP_FOR_RF2_REL);
                   createConcept(ObservableFields.CHARACTERISTIC_NID_FOR_RF2_REL);
                   createConcept(ObservableFields.MODIFIER_NID_FOR_RF2_REL);
                   createConcept(ObservableFields.NID1);
                   createConcept(ObservableFields.NID2);
                   createConcept(ObservableFields.NID3);
                   createConcept(ObservableFields.NID4);
                   createConcept(ObservableFields.NID5);
                   createConcept(ObservableFields.NID6);
                   createConcept(ObservableFields.NID7);
                   createConcept(ObservableFields.STR1);
                   createConcept(ObservableFields.STR2);
                   createConcept(ObservableFields.STR3);
                   createConcept(ObservableFields.STR4);
                   createConcept(ObservableFields.STR5);
                   createConcept(ObservableFields.STR6);
                   createConcept(ObservableFields.STR7);
                   createConcept(ObservableFields.INT1);
                   createConcept(ObservableFields.INT2);
                   createConcept(ObservableFields.INT3);
                   createConcept(ObservableFields.INT4);
                   createConcept(ObservableFields.INT5);
                   createConcept(ObservableFields.INT6);
                   createConcept(ObservableFields.INT7);
                        pushParent(current());
                        createConcept(ObservableFields.LOINC_NUMBER);
                        createConcept(ObservableFields.LOINC_COMPONENT);
                        createConcept(ObservableFields.LOINC_PROPERTY);
                        createConcept(ObservableFields.LOINC_TIME_ASPECT);
                        createConcept(ObservableFields.LOINC_SYSTEM);
                        createConcept(ObservableFields.LOINC_SCALE_TYPE);
                        createConcept(ObservableFields.LOINC_METHOD_TYPE);
                        createConcept(ObservableFields.LOINC_STATUS);
                        createConcept(ObservableFields.LOINC_SHORT_NAME);
                        createConcept(ObservableFields.LOINC_LONG_COMMON_NAME);
                        popParent();
                   
                   popParent();
            createConcept("Clinical statement properties");
            pushParent(current());
               createConcept("Circumstance properties");
               pushParent(current());
                    createConcept(ObservableFields.CIRCUMSTANCE_PURPOSE_LIST);
                    createConcept(ObservableFields.CIRCUMSTANCE_TIMING);
                    
                    createConcept("Performance circumstance properties");
                    pushParent(current());
                        createConcept(ObservableFields.PERFORMANCE_CIRCUMSTANCE_RESULT);
                        createConcept(ObservableFields.PERFORMANCE_CIRCUMSTANCE_PARTICIPANTS);
                        popParent();
                    
                    
                    createConcept("Request circumstance properties");
                    pushParent(current());
                        createConcept(ObservableFields.REQUEST_CIRCUMSTANCE_CONDITIONAL_TRIGGERS);
                        createConcept(ObservableFields.REQUEST_CIRCUMSTANCE_REQUESTED_PARTICIPANTS);
                        createConcept(ObservableFields.REQUEST_CIRCUMSTANCE_PRIORITY);
                        createConcept(ObservableFields.REQUEST_CIRCUMSTANCE_REPETITIONS);
                        createConcept(ObservableFields.REQUEST_CIRCUMSTANCE_REQUESTED_RESULT);
                        popParent();
                        
                    createConcept("Unstructured circumstance properties");
                    pushParent(current());
                        createConcept(ObservableFields.UNSTRUCTURED_CIRCUMSTANCE_TEXT);
                        popParent();
                    popParent();
               createConcept("Statement properties");
               pushParent(current());
                   createConcept(ObservableFields.STATEMENT_STAMP_COORDINATE);
                   createConcept(ObservableFields.STATEMENT_NARRATIVE);
                   createConcept(ObservableFields.STATEMENT_TIME);
                   createConcept(ObservableFields.STATEMENT_ID);
                   createConcept(ObservableFields.STATEMENT_SOR);
                   createConcept(ObservableFields.STATEMENT_AUTHORS);
                   createConcept(ObservableFields.STATEMENT_SOI);
                   createConcept(ObservableFields.STATEMENT_TYPE);
                   createConcept(ObservableFields.STATEMENT_MODE);
                   createConcept(ObservableFields.STATEMENT_TOPIC);
                   createConcept(ObservableFields.STATEMENT_CIRCUMSTANCE);
                   createConcept(ObservableFields.STATEMENT_ASSOCIATIONS);
                   popParent();
               createConcept("Interval properties");
               pushParent(current());
                   createConcept(ObservableFields.INTERVAL_LOWER_BOUND);
                   createConcept(ObservableFields.INTERVAL_UPPER_BOUND);
                   createConcept(ObservableFields.INTERVAL_INCLUDE_UPPER_BOUND);
                   createConcept(ObservableFields.INTERVAL_INCLUDE_LOWER_BOUND);
                   popParent();
               createConcept("Result properties");
               pushParent(current());
                    createConcept(ObservableFields.INTERVENTION_RESULT_STATUS);
                    popParent();

               createConcept("Measure properties");
               pushParent(current());
                   createConcept(ObservableFields.MEASURE_NARRATIVE);
                   createConcept(ObservableFields.MEASURE_RESOLUTION);
                   createConcept(ObservableFields.MEASURE_SEMANTIC);
                   createConcept(ObservableFields.OBSERVATION_RESULT_HEALTH_RISK);
                   createConcept(ObservableFields.MEASURE_NORMAL_RANGE);
                   popParent();
               
               createConcept("Participant properties");
               pushParent(current());
                    createConcept(ObservableFields.PARTICIPANT_ID);   
                    createConcept(ObservableFields.PARTICIPANT_ROLE);
                    popParent();
                    
 
               createConcept("Repetition properties");
               pushParent(current());
                    createConcept(ObservableFields.REPETITION_PERIOD_START);
                    createConcept(ObservableFields.REPETITION_PERIOD_DURATION);
                    createConcept(ObservableFields.REPETITION_EVENT_FREQUENCY);
                    createConcept(ObservableFields.REPETITION_EVENT_SEPARATION);
                    createConcept(ObservableFields.REPETITION_EVENT_DURATION);
                    popParent();

               createConcept("Statement association properties");
               pushParent(current());
                    createConcept(ObservableFields.STATEMENT_ASSOCIATION_SEMANTIC);
                    createConcept(ObservableFields.STATEMENT_ASSOCIATION_ID);
                    popParent();
                popParent();

            createConcept("Query clauses");
            pushParent(current());
               createConcept(TermAux.ACTIVE_QUERY_CLAUSE);
               createConcept(TermAux.INACTIVE_QUERY_CLAUSE);
               createConcept(TermAux.REFERENCED_COMPONENT_IS_ACTIVE);
               createConcept(TermAux.REFERENCED_COMPONENT_IS_INACTIVE);
               
               createConcept(TermAux.REFERENCED_COMPONENT_IS);
               createConcept(TermAux.REFERENCED_COMPONENT_IS_MEMBER_OF);
               createConcept(TermAux.REFERENCED_COMPONENT_IS_NOT_MEMBER_OF);

               createConcept(TermAux.COMPONENT_IS_MEMBER_OF);
               createConcept(TermAux.COMPONENT_IS_NOT_MEMBER_OF);
               createConcept(TermAux.REFERENCED_COMPONENT_IS_KIND_OF);
               createConcept(TermAux.REFERENCED_COMPONENT_IS_NOT_KIND_OF);
               
               createConcept(TermAux.AND_QUERY_CLAUSE);
         
               createConcept(TermAux.NOT_QUERY_CLAUSE);
               createConcept(TermAux.AND_NOT_QUERY_CLAUSE);
               createConcept(TermAux.OR_QUERY_CLAUSE);
               createConcept(TermAux.XOR_QUERY_CLAUSE);
               createConcept(TermAux.CHANGED_FROM_PREVIOUS_VERSION_QUERY_CLAUSE);
               createConcept(TermAux.CONCEPT_IS_QUERY_CLAUSE);
               createConcept(TermAux.CONCEPT_IS_KIND_OF_QUERY_CLAUSE);
               createConcept(TermAux.DESCRIPTION_LUCENE_MATCH_QUERY_CLAUSE);
               createConcept(TermAux.DESCRIPTION_LUCENE_ACTIVE_ONLY_MATCH_QUERY_CLAUSE);
               createConcept(TermAux.PREFERRED_NAME_FOR_CONCEPT_QUERY_CLAUSE);
               createConcept(TermAux.RELATIONSHIP_IS_CIRCULAR_QUERY_CLAUSE);
               createConcept(TermAux.CONCEPT_IS_CHILD_OF_QUERY_CLAUSE);
               createConcept(TermAux.DESCRIPTION_REGEX_MATCH_QUERY_CLAUSE);
               createConcept(TermAux.DESCRIPTION_REGEX_ACTIVE_ONLY_MATCH_QUERY_CLAUSE);
               createConcept(TermAux.CONCEPT_FOR_COMPONENT_QUERY_CLAUSE);
               createConcept(TermAux.CONCEPT_IS_DESCENDENT_OF_QUERY_CLAUSE);
               createConcept(TermAux.FULLY_QUALIFIED_NAME_FOR_CONCEPT_QUERY_CLAUSE);
               createConcept(TermAux.ENCLOSING_CONCEPT_QUERY_CLAUSE);

               createConcept(TermAux.ASSEMBLAGE_CONTAINS_STRING_QUERY_CLAUSE);
               createConcept(TermAux.ASSEMBLAGE_CONTAINS_CONCEPT_QUERY_CLAUSE);
               createConcept(TermAux.ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE);
               createConcept(TermAux.ASSEMBLAGE_LUCENE_MATCH_QUERY_CLAUSE);
               createConcept(TermAux.ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE);
               createConcept(TermAux.REL_RESTRICTION_QUERY_CLAUSE);
               createConcept(TermAux.REL_TYPE_QUERY_CLAUSE);
               createConcept(TermAux.ASSOCIATED_PARAMETER_QUERY_CLAUSE).setModule(TermAux.KOMET_MODULE);
               createConcept(TermAux.JOIN_QUERY_CLAUSE).setModule(TermAux.KOMET_MODULE);
               createConcept(ASSEMBLAGE_LIST_FOR_QUERY).setModule(TermAux.KOMET_MODULE);
               createConcept(STAMP_COORDINATE_KEY_FOR_MANIFOLD).setModule(TermAux.KOMET_MODULE);
               createConcept(LANGUAGE_COORDINATE_KEY_FOR_MANIFOLD).setModule(TermAux.KOMET_MODULE);
               createConcept(LOGIC_COORDINATE_KEY_FOR_MANIFOLD).setModule(TermAux.KOMET_MODULE);
               createConcept(PREMISE_TYPE_FOR_MANIFOLD).setModule(TermAux.KOMET_MODULE);
               createConcept(TermAux.CONCEPT_HAS_TAXONOMY_DISTANCE_FROM).setModule(TermAux.KOMET_MODULE);
               createConcept(TermAux.INTEGER_REFERENCE).setModule(TermAux.KOMET_MODULE);
               createConcept(TermAux.BOOLEAN_REFERENCE).setModule(TermAux.KOMET_MODULE);
               popParent();
            createConcept("Query clause parameters");
            pushParent(current());
               createConcept("For assemblage");
               createConcept("Query string");
               createConcept("Query string is regex", "Is regex");
               createConcept("Let item key");
               createConcept("Assemblage 1 to join", "Join assemblage 1");
               createConcept("Assemblage 2 to join", "Join assemblage 2");
               createConcept("Field 1 to join", "Join field 1");
               createConcept("Field 2 to join", "Join field 2");
               createConcept(MANIFOLD_COORDINATE_REFERENCE);
               popParent();
            popParent(); 
         popParent(); // ISAAC root should still be parent on stack...
         //createConcept("Clinical statement");
         //pushParent(current());
            //createConcept("Request statement");
            //createConcept("Action statement");
            //popParent();

         if (false) {
            addMultiparentTestConcepts();
         }

         popParent();

      // Note that we leave this method with the root concept set as parent (on purpose) - we don't call popParent the last time.
      // This way, if createConcept(...) is called again, the new concepts go under root.
         
      // this nasty oversight took _far_ too long to recognize.
      // MetaData concepts (and generated Semantics) must have CONSISTENT UUIDs. The default concept builder creates random
      // UUIDs for anything that doesn't have a UUID listed here, causing them to be random, which breaks things in interesting ways when we 
      // have ibdf files that references the UUIDs from a MetaData file....
      generateStableUUIDs();
//J+
   }

   private void addMultiparentTestConcepts() throws IllegalStateException {
      createConcept("test concept");
      pushParent(current());
         ConceptBuilder parentOneBuilder = createConcept("parent one");
         pushParent(current());
            ConceptBuilder multiParentBuilder = createConcept("multi-parent");
            popParent();
         ConceptBuilder parentTwoBuilder = createConcept("parent two");
         final LogicalExpressionBuilderService expressionBuilderService = LookupService.getService(LogicalExpressionBuilderService.class);
         final LogicalExpressionBuilder defBuilder = expressionBuilderService.getLogicalExpressionBuilder();
         NecessarySet(And(ConceptAssertion(parentOneBuilder.getNid(), defBuilder), ConceptAssertion(parentTwoBuilder.getNid(), defBuilder)));
         final LogicalExpression logicalExpression = defBuilder.build();
         multiParentBuilder.setLogicalExpression(logicalExpression);
         popParent();
   }

   // ~--- methods -------------------------------------------------------------

   /**
    * The main method.
    *
    * @param args
    *            the arguments
    */
   public static void main(String[] args) {
      try {
         final IsaacMetadataAuxiliary aux = new IsaacMetadataAuxiliary();

         aux.export(new DataOutputStream(new ByteArrayOutputStream(10240)));
      } catch (Exception ex) {
         Logger.getLogger(IsaacMetadataAuxiliary.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
