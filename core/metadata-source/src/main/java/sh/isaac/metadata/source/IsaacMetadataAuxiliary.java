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
import static sh.isaac.api.bootstrap.TermAux.ASSEMBLAGE_NID_FOR_COMPONENT;
import static sh.isaac.api.bootstrap.TermAux.AUTHOR_NID_FOR_VERSION;
import static sh.isaac.api.bootstrap.TermAux.COMMITTED_STATE_FOR_VERSION;
import static sh.isaac.api.bootstrap.TermAux.CONCEPT_SEMANTIC;
import static sh.isaac.api.bootstrap.TermAux.DESCRIPTION_SEMANTIC;
import static sh.isaac.api.bootstrap.TermAux.LANGUAGE_COORDINATE_KEY_FOR_MANIFOLD;
import static sh.isaac.api.bootstrap.TermAux.LOGIC_COORDINATE_KEY_FOR_MANIFOLD;
import static sh.isaac.api.bootstrap.TermAux.MODULE_NID_FOR_VERSION;
import static sh.isaac.api.bootstrap.TermAux.PATH_NID_FOR_VERSION;
import static sh.isaac.api.bootstrap.TermAux.PREMISE_TYPE_FOR_MANIFOLD;
import static sh.isaac.api.bootstrap.TermAux.REFERENCED_COMPONENT_NID_FOR_SEMANTIC;
import static sh.isaac.api.bootstrap.TermAux.SEMANTIC_TYPE;
import static sh.isaac.api.bootstrap.TermAux.STAMP_COORDINATE_KEY_FOR_MANIFOLD;
import static sh.isaac.api.bootstrap.TermAux.STAMP_SEQUENCE_FOR_VERSION;
import static sh.isaac.api.bootstrap.TermAux.STATUS_FOR_VERSION;
import static sh.isaac.api.bootstrap.TermAux.STRING_SEMANTIC;
import static sh.isaac.api.bootstrap.TermAux.TIME_FOR_VERSION;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.observable.ObservableFields;

import static sh.isaac.model.observable.ObservableFields.ALLOWED_STATES_FOR_STAMP_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.ASSEMBLAGE_LIST_FOR_QUERY;
import static sh.isaac.model.observable.ObservableFields.COMMITTED_STATE_FOR_CHRONICLE;
import static sh.isaac.model.observable.ObservableFields.DESCRIPTION_LIST_FOR_CONCEPT;
import static sh.isaac.model.observable.ObservableFields.DESCRIPTION_TYPE_FOR_DESCRIPTION;
import static sh.isaac.model.observable.ObservableFields.LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH;
import static sh.isaac.model.observable.ObservableFields.PREMISE_TYPE_FOR_TAXONOMY_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.STAMP_POSITION_FOR_STAMP_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.STAMP_PRECEDENCE_FOR_STAMP_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.TEXT_FOR_DESCRIPTION;
import static sh.isaac.model.observable.ObservableFields.TIME_FOR_STAMP_POSITION;
import static sh.isaac.model.observable.ObservableFields.UUID_FOR_TAXONOMY_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.VERSION_LIST_FOR_CHRONICLE;
import static sh.isaac.model.observable.ObservableFields.AUTHOR_NID_FOR_EDIT_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.MODULE_NID_FOR_EDIT_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.PATH_NID_FOR_EDIT_CORDINATE;
import static sh.isaac.model.observable.ObservableFields.LANGUAGE_NID_FOR_LANGUAGE_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.DIALECT_ASSEMBLAGE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.CLASSIFIER_NID_FOR_LOGIC_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.MODULE_NID_ARRAY_FOR_STAMP_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.PATH_NID_FOR_STAMP_PATH;
import static sh.isaac.model.observable.ObservableFields.PATH_NID_FOR_STAMP_POSITION;
import static sh.isaac.model.observable.ObservableFields.CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION;
import static sh.isaac.model.observable.ObservableFields.CONCEPT_ASSEMBLAGE_FOR_LOGIC_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION;
import static sh.isaac.model.observable.ObservableFields.SEMANTIC_LIST_FOR_CHRONICLE;
import static sh.isaac.model.observable.ObservableFields.PRIMORDIAL_UUID_FOR_COMPONENT;
import static sh.isaac.model.observable.ObservableFields.ENTRY_SEQUENCE_FOR_COMPONENT;
import static sh.isaac.model.observable.ObservableFields.CONCEPT_VERSION;
import static sh.isaac.model.observable.ObservableFields.CORELATION_COMPARISON_EXPRESSION;
import static sh.isaac.model.observable.ObservableFields.CORELATION_EXPRESSION;
import static sh.isaac.model.observable.ObservableFields.CORELATION_REFERENCE_EXPRESSION;
import static sh.isaac.model.observable.ObservableFields.DESCRIPTION_DIALECT;
import static sh.isaac.model.observable.ObservableFields.DESCRIPTION_DIALECT_DESCRIPTION;
import static sh.isaac.model.observable.ObservableFields.DESCRIPTION_DIALECT_DIALECT;
import static sh.isaac.model.observable.ObservableFields.DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.GIT_PASSWORD;
import static sh.isaac.model.observable.ObservableFields.GIT_URL;
import static sh.isaac.model.observable.ObservableFields.GIT_USER_NAME;
import static sh.isaac.model.observable.ObservableFields.LANGUAGE_FOR_LANGUAGE_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.MANIFOLD_COORDINATE_REFERENCE;
import static sh.isaac.model.observable.ObservableFields.MODULE_NID_PREFERENCE_LIST_FOR_STAMP_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.MODULE_OPTIONS_FOR_EDIT_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.MODULE_SPECIFICATION_PREFERENCE_LIST_FOR_STAMP_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.NATIVE_ID_FOR_COMPONENT;
import static sh.isaac.model.observable.ObservableFields.PATH_OPTIONS_FOR_EDIT_COORDINATE;
import static sh.isaac.model.observable.ObservableFields.UUID_LIST_FOR_COMPONENT;

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
            createConcept(TermAux.SEMANTIC_TYPE);
            pushParent(current());
                createConcept(TermAux.MEMBERSHIP_SEMANTIC);
                createConcept(TermAux.DYNAMIC_SEMANTIC);
                createConcept(TermAux.CONCEPT_SEMANTIC).addComponentIntSemantic(TermAux.CONCEPT_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.COMPONENT_SEMANTIC).addComponentIntSemantic(TermAux.COMPONENT_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.LOGICAL_EXPRESSION_SEMANTIC).addComponentIntSemantic(TermAux.LOGICAL_EXPRESSION_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.INTEGER_SEMANTIC).addComponentIntSemantic(TermAux.INTEGER_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.STRING_SEMANTIC).addComponentIntSemantic(TermAux.STRING_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
                createConcept(TermAux.DESCRIPTION_SEMANTIC)
                        .addComponentIntSemantic(TermAux.CONCEPT_FIELD, 0, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE)
                        .addComponentIntSemantic(TermAux.CONCEPT_FIELD, 1, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE)
                        .addComponentIntSemantic(TermAux.CONCEPT_FIELD, 2, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE)
                        .addComponentIntSemantic(TermAux.STRING_FIELD, 3, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE);
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
                  createConcept("SOLOR quality assurance rule module");
                  createConcept("SOLOR temporary concept module")
                          .addDescription("The temporary module is used for concepts such as those that represent feature paths, that are never part of a release.", TermAux.DEFINITION_DESCRIPTION_TYPE);
                  createConcept("SOLOR automation rule module");
                  createConcept("SOLOR genomic module").addDescription("Collection of Variant and Gene", TermAux.DEFINITION_DESCRIPTION_TYPE);
                  // The second UUID here was the old value from the TermAux - but this was an orphan. to best fix the bug that resulted,
                  // the type5 UUID from here was moved to TermAux, and the old UUID was added here as an additional.
                  createConcept(TermAux.SOLOR_OVERLAY_MODULE).addDescription("SOLOR overlay", TermAux.REGULAR_NAME_DESCRIPTION_TYPE)
                     .addUuids(UUID.fromString("1f2016a6-960e-11e5-8994-feff819cdc9f"));
               
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
               createConcept("Retro ID", "SOLOR Retro Identifier").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("CLINVAR Variant Name").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("NCIB Gene ID").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("CLINVAR Allele ID").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);
               createConcept("MIM Number").addAssemblageMembership(TermAux.IDENTIFIER_SOURCE);

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
               createConcept(TermAux.ASSEMBLAGE_SEMANTIC_FIELDS);
               createConcept("Issue managment assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
               pushParent(current());
                  createConcept("Content issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("KOMET issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Quality assurance rule issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Automation issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Clinical statement issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("SNOMED® issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("LOINC® issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("RxNorm issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("SOLOR issue assemblage").addComponentSemantic(STRING_SEMANTIC, SEMANTIC_TYPE);
                  popParent();
               createConcept(TermAux.DESCRIPTION_ASSEMBLAGE);
               createConcept("Dialect assemblage");
               pushParent(current());
                  createConcept(TermAux.ENGLISH_DIALECT_ASSEMBLAGE).addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  pushParent(current());
                     createConcept("GB English dialect").mergeFromSpec(TermAux.GB_DIALECT_ASSEMBLAGE).addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     createConcept("US English dialect").mergeFromSpec(TermAux.US_DIALECT_ASSEMBLAGE).addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     pushParent(current());
                        createConcept("US Nursing dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE).setPrimordialUuid("6e447636-1085-32ff-bc36-6748a45255de");
                        popParent();
                     popParent();
                  createConcept(TermAux.SPANISH_DIALECT_ASSEMBLAGE).addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  pushParent(current());
                     createConcept(TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE).addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     popParent();
                  createConcept("French dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Korean dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  pushParent(current());
                     createConcept("Standard Korean dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                     popParent();
                  createConcept("Polish dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Irish dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Czech dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  createConcept("Russian dialect").addComponentSemantic(CONCEPT_SEMANTIC, SEMANTIC_TYPE);
                  popParent();
               createConcept("Logic assemblage");
               pushParent(current());
                  createConcept(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE);
                  createConcept(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE);
                  createConcept(TermAux.SRF_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE);
                  ConceptBuilder builder = createConcept(TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE);
                  builder.getPreferredDescriptionBuilder().setDescriptionText("SNOMED legacy implication");
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
                  popParent();
               createConcept("Rule assemblage");
               pushParent(current());
                  createConcept("Module assemblage");
                  createConcept("Quality assurance rule assemblage");
                  createConcept("Automation rule assemblage");
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
                       .addComponentSemantic(DESCRIPTION_SEMANTIC, SEMANTIC_TYPE)
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021920-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.SPANISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021c36-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.FRENCH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021dbc-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.DANISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45021f10-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.POLISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022140-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.DUTCH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022280-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.LITHUANIAN_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022410-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.CHINESE_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022532-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.JAPANESE_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("450226cc-9567-11e5-8994-feff819cdc9f"));
               createConcept(TermAux.SWEDISH_LANGUAGE, 
                     TermAux.DESCRIPTION_ASSEMBLAGE.getNid())
                       .addComponentIntSemantic(TEXT_FOR_DESCRIPTION, 0, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION, 1, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(DESCRIPTION_TYPE_FOR_DESCRIPTION, 2, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addComponentIntSemantic(CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION, 3, TermAux.ASSEMBLAGE_SEMANTIC_FIELDS)
                       .addUuids(UUID.fromString("45022848-9567-11e5-8994-feff819cdc9f"));
               createConcept("Korean language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               createConcept("Russian language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               createConcept("Irish language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               createConcept("Czech language", null, TermAux.DESCRIPTION_ASSEMBLAGE.getNid(), null);
               popParent();
            createConcept("Measurement semantic");
            pushParent(current());
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
            createConcept("Feature").setPrimordialUuid(NodeSemantic.FEATURE.getSemanticUuid());
            pushParent(current());
               createConcept("Ingredient strength");
            popParent();
            createConcept("Role").setPrimordialUuid("6155818b-09ed-388e-82ce-caa143423e99");
            pushParent(current());
               createConcept("Intrinsic role");
               pushParent(current());
                  createConcept(TermAux.ROLE_GROUP);
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
