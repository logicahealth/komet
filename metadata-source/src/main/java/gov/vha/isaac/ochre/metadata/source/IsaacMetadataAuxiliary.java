/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.metadata.source;

import gov.vha.isaac.ochre.api.IsaacTaxonomy;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

import static gov.vha.isaac.ochre.model.observable.ObservableFields.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kec
 */
public class IsaacMetadataAuxiliary extends IsaacTaxonomy {
     
 
   public IsaacMetadataAuxiliary() throws NoSuchAlgorithmException, UnsupportedEncodingException {
      super(TermAux.DEVELOPMENT_PATH, TermAux.USER, TermAux.ISAAC_MODULE, TermAux.IS_A, "ISAAC");

      try {
         createConcept("ISAAC root");
         pushParent(current());
            createConcept("module");
            pushParent(current());
                createConcept(TermAux.ISAAC_MODULE);
                createConcept(TermAux.SNOMED_CT_CORE_MODULE);
                createConcept("US Extension module");
                createConcept("LOINC module");
                createConcept("RxNorm module");
                createConcept("AMT module");
                createConcept("VHA module");
                createConcept("DOD module");
                createConcept("IPO module");
                createConcept("SOLOR overlay module");
            popParent();
            createConcept("user").setPrimordialUuid(TermAux.USER.getPrimordialUuid());
            createConcept("path").setPrimordialUuid("4459d8cf-5a6f-3952-9458-6d64324b27b7");
            pushParent(current());
                ConceptBuilder developmentPath = createConcept("development path");
                developmentPath.setPrimordialUuid("1f200ca6-960e-11e5-8994-feff819cdc9f");
                ConceptBuilder masterPath = createConcept("master path");
                masterPath.setPrimordialUuid("1f20134a-960e-11e5-8994-feff819cdc9f");
                masterPath.addUuids(UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")); // UUID from WB_AUX_PATH
            popParent();
            createConcept("set operator");
            pushParent(current());
                createConcept("sufficient set").setPrimordialUuid(NodeSemantic.SUFFICIENT_SET.getSemanticUuid());
                createConcept("necessary set").setPrimordialUuid(NodeSemantic.NECESSARY_SET.getSemanticUuid());
            popParent();
            createConcept("identifier source");
            pushParent(current());
                createConcept("SNOMED integer id").setPrimordialUuid("0418a591-f75b-39ad-be2c-3ab849326da9");
                createConcept("generated UUID").setPrimordialUuid(TermAux.GENERATED_UUID.getPrimordialUuid());
                createConcept("LOINC Num");
                createConcept("RXCUI").setPrimordialUuid("617761d2-80ef-5585-83a0-60851dd44158");  //comes from the algorithm in the rxnorm econ loader
                createConcept("VUID");
            popParent();
            createConcept("language");
            pushParent(current());
                createConcept("English language").setPrimordialUuid("06d905ea-c647-3af9-bfe5-2514e135b558");
                createConcept("Spanish language").setPrimordialUuid("0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e");
                createConcept("French language").setPrimordialUuid("8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e");
                createConcept("Danish language").setPrimordialUuid("7e462e33-6d94-38ae-a044-492a857a6853");
                createConcept("Polish language").setPrimordialUuid("c924b887-da88-3a72-b8ea-fa86990467c9");
                createConcept("Dutch language").setPrimordialUuid("674ad858-0224-3f90-bcf0-bc4cab753d2d");
                createConcept("Lithuanian language").setPrimordialUuid("e9645d95-8a1f-3825-8feb-0bc2ee825694");
                createConcept("Chinese language").setPrimordialUuid("ba2efe6b-fe56-3d91-ae0f-3b389628f74c");
                createConcept("Japanese language").setPrimordialUuid("b90a1097-29e3-42bc-8576-8e8eb6715c44");
                createConcept("Swedish language").setPrimordialUuid("9784a791-8fdb-32f7-88da-74ab135fe4e3");
            popParent();
            createConcept("assemblage membership type");
            pushParent(current());
                createConcept("normal member").setPrimordialUuid("cc624429-b17d-4ac5-a69e-0b32448aaf3c");
                createConcept("marked parent").setPrimordialUuid("125f3d04-de17-490e-afec-1431c2a39e29");
            popParent();
            createConcept("assemblage").setPrimordialUuid(TermAux.ASSEMBLAGE.getPrimordialUuid());
            pushParent(current());
                createConcept("description assemblage");
                pushParent(current());
                    createConcept("English description assemblage").setPrimordialUuid("45021920-9567-11e5-8994-feff819cdc9f");
                    createConcept("Spanish description assemblage").setPrimordialUuid("45021c36-9567-11e5-8994-feff819cdc9f");
                    createConcept("French description assemblage").setPrimordialUuid("45021dbc-9567-11e5-8994-feff819cdc9f");
                    createConcept("Danish description assemblage").setPrimordialUuid("45021f10-9567-11e5-8994-feff819cdc9f");
                    createConcept("Polish description assemblage").setPrimordialUuid("45022140-9567-11e5-8994-feff819cdc9f");
                    createConcept("Dutch description assemblage").setPrimordialUuid("45022280-9567-11e5-8994-feff819cdc9f");
                    createConcept("Lithuanian description assemblage").setPrimordialUuid("45022410-9567-11e5-8994-feff819cdc9f");
                    createConcept("Chinese description assemblage").setPrimordialUuid("45022532-9567-11e5-8994-feff819cdc9f");
                    createConcept("Japanese description assemblage").setPrimordialUuid("450226cc-9567-11e5-8994-feff819cdc9f");
                    createConcept("Swedish description assemblage").setPrimordialUuid("45022848-9567-11e5-8994-feff819cdc9f");
                popParent();
                createConcept("dialect assemblage");
                pushParent(current());
                    createConcept("US English dialect").setPrimordialUuid(TermAux.US_DIALECT_ASSEMBLAGE.getPrimordialUuid());
                    createConcept("GB English dialect").setPrimordialUuid(TermAux.GB_DIALECT_ASSEMBLAGE.getPrimordialUuid());
                popParent();
                createConcept("logic assemblage");
                    pushParent(current());
                    createConcept("EL++ stated form assemblage").setPrimordialUuid("1f201994-960e-11e5-8994-feff819cdc9f");
                    createConcept("EL++ inferred form").setPrimordialUuid("1f20182c-960e-11e5-8994-feff819cdc9f");
                popParent();
                createConcept("assemblage related to path management");
                pushParent(current());
                    ConceptBuilder paths = createConcept("paths assemblage");
                    paths.setPrimordialUuid(TermAux.PATH_ASSEMBLAGE.getPrimordialUuid());
                    addPath(paths, masterPath);
                    addPath(paths, developmentPath);
                    
                    ConceptBuilder pathOrigins = createConcept("path origins assemblage");
                    pathOrigins.setPrimordialUuid(TermAux.PATH_ORIGIN_ASSEMBLAGE.getPrimordialUuid());
                    //addPathOrigin(pathOrigins, developmentPath, masterPath);
                popParent();
                createConcept("SOLOR Refsets").setPrimordialUuid("7a9b495e-69c1-53e5-a2d5-41be2429c146");
          popParent();
          //
            createConcept("axiom origin");
                pushParent(current());
                ConceptBuilder stated = createConcept("stated");
                stated.setPrimordialUuid(TermAux.STATED_RELATIONSHIP.getPrimordialUuid());
                stated.addUuids(TermAux.REL_STATED_CHAR.getPrimordialUuid());
                ConceptBuilder inferred = createConcept("inferred");
                inferred.setPrimordialUuid(TermAux.INFERRED_RELATIONSHIP.getPrimordialUuid());
                inferred.addUuids(TermAux.REL_INFERED_CHAR.getPrimordialUuid());
            popParent();
          //
            createConcept("description type");
            pushParent(current());
                ConceptBuilder fsn = createConcept("fully specified name");
                fsn.setPrimordialUuid(TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getPrimordialUuid());
                fsn.addUuids(UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66")); // RF1 FSN
                ConceptBuilder syn = createConcept("synonym");
                syn.setPrimordialUuid(TermAux.SYNONYM_DESCRIPTION_TYPE.getPrimordialUuid());
                syn.addUuids(UUID.fromString("d6fad981-7df6-3388-94d8-238cc0465a79"));
                createConcept("definition description type").setPrimordialUuid(TermAux.DEFINITION_DESCRIPTION_TYPE.getPrimordialUuid());
                createConcept("description type in source terminology");  //LOINC and RxNorm description types are created under this node
                pushParent(current());
                    createConcept("description source type reference sets");  //Dynamic Sememes are created under this node for LOINC and RxNorm description types
                popParent();
            popParent();
            createConcept("description case significance");  
            pushParent(current());
                createConcept("description case sensitive").setPrimordialUuid("0def37bc-7e1b-384b-a6a3-3e3ceee9c52e");
                createConcept("description not case sensitive").setPrimordialUuid("ecea41a2-f596-3d98-99d1-771b667e55b8");
                createConcept("description initial character sensitive").setPrimordialUuid("17915e0d-ed38-3488-a35c-cda966db306a");
            popParent();
            createConcept("description acceptability");
            pushParent(current());
                createConcept("acceptable").setPrimordialUuid(TermAux.ACCEPTABLE.getPrimordialUuid());
                createConcept("preferred").setPrimordialUuid(TermAux.PREFERRED.getPrimordialUuid());
            popParent();
            
            createConcept("taxonomy operator");
            pushParent(current());
                ConceptBuilder isa = createConcept("is-a");
                isa.setPrimordialUuid(TermAux.IS_A.getPrimordialUuid());
                isa.addUuids(TermAux.IS_A_ATTRIBUTE.getUuids());
                createConcept("relationship type in source terminology");  //RxNorm relationship types are created under this node
                pushParent(current());
                    createConcept("relationship source type reference sets"); //Dynamic Sememes are created under this node for LOINC and RxNorm relationship types
                popParent();
            popParent();
            createConcept("connective operator");
            pushParent(current());
                createConcept("and").setPrimordialUuid(NodeSemantic.AND.getSemanticUuid());
                createConcept("or").setPrimordialUuid(NodeSemantic.OR.getSemanticUuid());
                createConcept("disjoint with").setPrimordialUuid(NodeSemantic.DISJOINT_WITH.getSemanticUuid());
                createConcept("definition root").setPrimordialUuid(NodeSemantic.DEFINITION_ROOT.getSemanticUuid());
            popParent();
            createConcept("node operator");
            pushParent(current());
                createConcept("template merge");
                createConcept("field substitution");
                pushParent(current());
                    createConcept("concept substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_CONCEPT.getSemanticUuid());
                    createConcept("boolean substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_BOOLEAN.getSemanticUuid());
                    createConcept("float substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_FLOAT.getSemanticUuid());
                    createConcept("instant substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_INSTANT.getSemanticUuid());
                    createConcept("integer substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_INTEGER.getSemanticUuid());
                    createConcept("string substitution").setPrimordialUuid(NodeSemantic.SUBSTITUTION_STRING.getSemanticUuid());
                popParent();
                createConcept("concept reference").setPrimordialUuid(NodeSemantic.CONCEPT.getSemanticUuid());
            popParent();
            createConcept("template concept").setPrimordialUuid(NodeSemantic.TEMPLATE.getSemanticUuid());
            pushParent(current());
                createConcept("skin of region template");
                // add annotations for order and labels
                // create template
            popParent();
            createConcept("role operator");
            pushParent(current());
                createConcept("universal restriction").setPrimordialUuid(NodeSemantic.ROLE_ALL.getSemanticUuid());
                createConcept("existential restriction").setPrimordialUuid(NodeSemantic.ROLE_SOME.getSemanticUuid());
            popParent();
            createConcept("feature").setPrimordialUuid(NodeSemantic.FEATURE.getSemanticUuid());
            createConcept("literal value");
            pushParent(current());
                createConcept("boolean literal").setPrimordialUuid(NodeSemantic.LITERAL_BOOLEAN.getSemanticUuid());
                createConcept("float literal").setPrimordialUuid(NodeSemantic.LITERAL_FLOAT.getSemanticUuid());
                createConcept("instant literal").setPrimordialUuid(NodeSemantic.LITERAL_INSTANT.getSemanticUuid());
                createConcept("integer literal").setPrimordialUuid(NodeSemantic.LITERAL_INTEGER.getSemanticUuid());
                createConcept("string literal").setPrimordialUuid(NodeSemantic.LITERAL_STRING.getSemanticUuid());
            popParent();
            createConcept("concrete domain operator");
            pushParent(current());
                createConcept("greater than");
                createConcept("greater than or equal to");
                createConcept("equal to");
                createConcept("less than or equal to");
                createConcept("less than");
            popParent();
            createConcept("description-logic profile");
            pushParent(current());
                createConcept("EL++ profile").setPrimordialUuid("1f201e12-960e-11e5-8994-feff819cdc9f");
                createConcept("SH profile");
            popParent();
            createConcept("description-logic classifier");
            pushParent(current());
                createConcept("IHTSDO classifier").setPrimordialUuid("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9");
                createConcept("SnoRocket classifier").setPrimordialUuid("1f201fac-960e-11e5-8994-feff819cdc9f");
                createConcept("ConDOR classifier");
            popParent();
            createConcept("role").setPrimordialUuid("6155818b-09ed-388e-82ce-caa143423e99");
                pushParent(current());
                createConcept("Has strength");
                popParent();
            pushParent(current());
                createConcept("intrinsic role");
                pushParent(current());
                    createConcept("role group").setPrimordialUuid("a63f4bf2-a040-11e5-8994-feff819cdc9f");
                popParent();
            popParent();
            createConcept("unmodeled concept");
            pushParent(current());
                createConcept("anonymous concept");
                createConcept("unmodeled role concept");
                createConcept("unmodeled feature concept");
                createConcept("unmodeled taxonomic concept");
            popParent();
            createConcept("health concept").setPrimordialUuid("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
            createConcept("object properties");
            pushParent(current());
                createConcept("coordinate properties");
                pushParent(current());
                    createConcept(AUTHOR_SEQUENCE_FOR_EDIT_COORDINATE);
                    createConcept(MODULE_SEQUENCE_FOR_EDIT_COORDINATE);
                    createConcept(PATH_SEQUENCE_FOR_EDIT_CORDINATE);
                    createConcept(LANGUAGE_SEQUENCE_FOR_LANGUAGE_COORDINATE);
                    createConcept(DIALECT_ASSEMBLAGE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE);
                    createConcept(DESCRIPTION_TYPE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE);
                    createConcept(STATED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE);
                    createConcept(INFERRED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE);
                    createConcept(DESCRIPTION_LOGIC_PROFILE_SEQUENCE_FOR_LOGIC_COORDINATE);
                    createConcept(CLASSIFIER_SEQUENCE_FOR_LOGIC_COORDINATE);
                    createConcept(STAMP_PRECEDENCE_FOR_STAMP_COORDINATE);
                    createConcept(STAMP_POSITION_FOR_STAMP_COORDINATE);
                    createConcept(ALLOWED_STATES_FOR_STAMP_COORDINATE);
                    createConcept(MODULE_SEQUENCE_ARRAY_FOR_STAMP_COORDINATE);
                    createConcept(PATH_SEQUENCE_FOR_STAMP_PATH);
                    createConcept(PATH_ORIGIN_LIST_FOR_STAMP_PATH);
                    createConcept(TIME_FOR_STAMP_POSITION);
                    createConcept(PATH_SEQUENCE_FOR_STAMP_POSITION);
                    createConcept(PREMISE_TYPE_FOR_TAXONOMY_COORDINATE);
                    createConcept(UUID_FOR_TAXONOMY_COORDINATE);
                    createConcept(STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE);
                    createConcept(LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE);
                    createConcept(LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE);
                popParent();
                createConcept("version properties");
                pushParent(current());
                    createConcept(STATUS_FOR_VERSION);
                    createConcept(TIME_FOR_VERSION);
                    createConcept(AUTHOR_SEQUENCE_FOR_VERSION);
                    createConcept(MODULE_SEQUENCE_FOR_VERSION);
                    createConcept(PATH_SEQUENCE_FOR_VERSION);
                    createConcept(COMMITTED_STATE_FOR_VERSION);
                    createConcept(STAMP_SEQUENCE_FOR_VERSION);
                    createConcept("description version properties");
                    pushParent(current());
                        createConcept(CASE_SIGNIFICANCE_CONCEPT_SEQUENCE_FOR_DESCRIPTION);
                        createConcept(LANGUAGE_CONCEPT_SEQUENCE_FOR_DESCRIPTION);
                        createConcept(TEXT_FOR_DESCRIPTION);
                        createConcept(DESCRIPTION_TYPE_FOR_DESCRIPTION);
                    popParent();
                popParent();
                createConcept("chronicle properties");
                pushParent(current());
                    createConcept(VERSION_LIST_FOR_CHRONICLE);
                    createConcept(NATIVE_ID_FOR_CHRONICLE);
                    createConcept(CONCEPT_SEQUENCE_FOR_CHRONICLE);
                    createConcept(SEMEME_SEQUENCE_FOR_CHRONICLE);
                    createConcept(PRIMORDIAL_UUID_FOR_CHRONICLE);
                    createConcept(UUID_LIST_FOR_CHRONICLE);
                    createConcept(COMMITTED_STATE_FOR_CHRONICLE);
                    createConcept(SEMEME_LIST_FOR_CHRONICLE);
                    createConcept(ASSEMBLAGE_SEQUENCE_FOR_SEMEME_CHRONICLE);
                popParent();
                createConcept("concept properties");
                pushParent(current());
                    createConcept(DESCRIPTION_LIST_FOR_CONCEPT);
                popParent();
            popParent();
            createConcept("solor metadata");
            pushParent(current());
                createConcept("Content Source Artifact Version");
                createConcept("Content Converter Version");
                createConcept("Content Converted IBDF Artifact Version");
                createConcept("Content Converted IBDF Artifact Classifier");
            popParent();
                
      } catch (Exception ex) {
         Logger.getLogger(IsaacMetadataAuxiliary.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

    public static void main(String[] args) {
        try {
            IsaacMetadataAuxiliary aux = new IsaacMetadataAuxiliary();
            aux.export(new DataOutputStream(new ByteArrayOutputStream(10240)));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(IsaacMetadataAuxiliary.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
