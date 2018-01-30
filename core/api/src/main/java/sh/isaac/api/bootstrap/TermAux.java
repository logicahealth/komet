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

package sh.isaac.api.bootstrap;

import java.lang.reflect.Field;
import java.util.ArrayList;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.jvnet.hk2.annotations.Service;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IsaacCache;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.UuidT5Generator;

//~--- classes ----------------------------------------------------------------

/**
 * This class only contains manually created, hard coded constants which need to exist earlier in the classpath than {@link MetaData} is
 * available. If you are down the classpath far enough to have MetaData, use that class.
 *
 * Data created here does NOT automatically get created as actual concepts. All concepts here must be referenced by
 * {@link IsaacMetadataAuxiliary}, or they will not get created at runtime, and you will have broken references in your DB. {@link TermAux}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service
@Singleton
public class TermAux implements IsaacCache {
   // J-
   public static UUID MASTER_PATH_SEMANTIC_UUID = UUID.fromString("79a92f9e-cd93-5537-984c-c9aa4532e59d");
   public static UUID DEVELOPMENT_PATH_SEMANTIC_UUID = UUID.fromString("f02874c5-186b-53c4-9054-f819975a9814");

   /** ConceptProxy for: "Part of concept". */
   public static ConceptSpecification PART_OF = new ConceptProxy("Part of", UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73"));

   /** ConceptProxy for: "Laterality concept". */
   public static ConceptSpecification LATERALITY = new ConceptProxy("Laterality", UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b"));

   /** ConceptProxy for: "Has active ingredient concept". */
   public static ConceptSpecification HAS_ACTIVE_INGREDIENT = new ConceptProxy("Has active ingredient", UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8"));

   /** ConceptProxy for: "Has dose form concept". */
   public static ConceptSpecification HAS_DOSE_FORM = new ConceptProxy("Has dose form", UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63"));

   /** ConceptProxy for: "ISAAC root concept". */
   public static ConceptSpecification SOLOR_ROOT = new ConceptProxy("SOLOR concept", UUID.fromString("7c21b6c5-cf11-5af9-893b-743f004c97f5"));

   /** ConceptProxy for: "database uuid". */
   public static ConceptSpecification DATABASE_UUID = new ConceptProxy("Database UUID", UUID.fromString("49b882a1-05e4-52cf-96d8-5de024b24632"));

   /** ConceptProxy for: "user". */
   public static ConceptSpecification USER = new ConceptProxy("User", UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));

   /** ConceptProxy for: "is a". */
   public static ConceptSpecification IS_A = new ConceptProxy("Is a (relationship type)", UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
   /** ConceptProxy for: "Child of". */
   public static ConceptSpecification CHILD_OF = new ConceptProxy("Child of (internal use relationship type)", UUID.fromString("91947d30-7555-5400-bbe2-4415472cff1b"));

   /** ConceptProxy for: "IHTSDO classifier. */
   public static ConceptSpecification IHTSDO_CLASSIFIER = new ConceptProxy("IHTSDO Classifier", UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));

   /** ConceptProxy for: "Path". */
   public static ConceptSpecification PATH = new ConceptProxy("Path", UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));

   /**
    * ConceptProxy for: "Path origin assemblage". IsaacMetadataAuxiliary has "path origins assemblage", TermAux has "Path origin reference set"
    */
   public static ConceptSpecification PATH_ORIGIN_ASSEMBLAGE = new ConceptProxy("Path origin assemblage", UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b"));

   /** ConceptProxy for: "path assemblage". IsaacMetadataAuxiliary has "paths assemblage", TermAux has "paths" */
   public static ConceptSpecification PATH_ASSEMBLAGE = new ConceptProxy("Paths", UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f"));

   /** ConceptProxy for: "SNOMED integer identifier. 'SNOMED CT integer identifier' - 900000000000294009; 'SNOMED integer id' */
   public static ConceptSpecification SNOMED_IDENTIFIER = new ConceptProxy("SNOMED integer id", UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"),
         UUID.fromString("87360947-e603-3397-804b-efd0fcc509b9"));

   /** ConceptProxy for: "Assemblage". Formerly known as REFSET_IDENTITY */
   public static ConceptSpecification ASSEMBLAGE = new ConceptProxy("Assemblage", UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da"));

   /** ConceptProxy for: "Unspecified module. */
   public static ConceptSpecification UNSPECIFIED_MODULE = new ConceptProxy("Module", // simply labeled 'module' in IsaacMetadataAuxiliary
         UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));

   /** ConceptProxy for: "ISAAC module". */
   public static ConceptSpecification SOLOR_MODULE = new ConceptProxy("SOLOR module", UUID.fromString("f680c868-f7e5-5d0e-91f2-615eca8f8fd2"));

   /** ConceptProxy for: "Synonym description type". SCT ID: 900000000000013009 */
   public static ConceptSpecification REGULAR_NAME_DESCRIPTION_TYPE = new ConceptProxy("Regular name", UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"));

   /** ConceptProxy for: "Fully qualified description type. */
   public static ConceptSpecification FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE = new ConceptProxy("Fully qualified name",
         UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));

   /** ConceptProxy for: "Definition description type". IsaacMetadataAuxiliary has "definition description type", TermAux has "Definition" */
   public static ConceptSpecification DEFINITION_DESCRIPTION_TYPE = new ConceptProxy("Definition", UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09"));

   /**
    * ConceptProxy for: "Description type in source terminology". Needed within DynamicMetadata constants, which can't reference
    * IsaacMetadataAuxiliary
    */
   public static ConceptSpecification DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY = new ConceptProxy("Description type in source terminology",
         UUID.fromString("ef7d9808-a839-5119-a604-b777268eb719"));

   /** ConceptProxy for: "Relationship type in source terminology". */
   public static ConceptSpecification RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY = new ConceptProxy("Relationship type in source terminology",
         UUID.fromString("46bc0e6b-0e64-5aa6-af27-a823e9156dfc"));

   public static ConceptSpecification ENGLISH_DIALECT_ASSEMBLAGE =
      new ConceptProxy("English dialect assemblage",
                       UUID.fromString("c0836284-f631-3c86-8cfc-56a67814efab"));

   /**
    * ConceptProxy for: "US dialect assemblage". IsaacMetadataAuxiliary has "US English dialect", TermAux has "United States of America English
    * language reference set"
    */
   public static ConceptSpecification US_DIALECT_ASSEMBLAGE = new ConceptProxy("United States of America English dialect assemblage",
         UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));

   /**
    * ConceptProxy for: "Great Britain dialect assemblage". IsaacMetadataAuxiliary has "GB English dialect", TermAux has "Great Britain English
    * language reference set"
    */
   public static ConceptSpecification GB_DIALECT_ASSEMBLAGE = new ConceptProxy("Great Britain English dialect assemblage",
         UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));
   
   public static ConceptSpecification SPANISH_DIALECT_ASSEMBLAGE = new ConceptProxy("Spanish dialect assemblage",
         UUID.fromString("03615ef2-aa56-336d-89c5-a1b5c4cee8f6"));

   public static ConceptSpecification SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE = new ConceptProxy("Latin american spanish dialect assemblage",
         UUID.fromString("835d3fe2-7bd7-3aa8-a52a-25e203b0afbe"));

   /** ConceptProxy for: "English language". */
   public static ConceptSpecification ENGLISH_LANGUAGE = new ConceptProxy("English language", UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558"));

   /** ConceptProxy for: "Spanish language". */
   public static ConceptSpecification SPANISH_LANGUAGE = new ConceptProxy("Spanish language", UUID.fromString("0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e"));

   /** ConceptProxy for: "French language". */
   public static ConceptSpecification FRENCH_LANGUAGE = new ConceptProxy("French language", UUID.fromString("8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e"));

   /** ConceptProxy for: "Danish language". */
   public static ConceptSpecification DANISH_LANGUAGE = new ConceptProxy("Danish language", UUID.fromString("7e462e33-6d94-38ae-a044-492a857a6853"));

   /** ConceptProxy for: "Polish language". */
   public static ConceptSpecification POLISH_LANGUAGE = new ConceptProxy("Polish language", UUID.fromString("c924b887-da88-3a72-b8ea-fa86990467c9"));

   /** ConceptProxy for: "Dutch language". */
   public static ConceptSpecification DUTCH_LANGUAGE = new ConceptProxy("Dutch language", UUID.fromString("674ad858-0224-3f90-bcf0-bc4cab753d2d"));

   /** ConceptProxy for: "Lithuanian language". */
   public static ConceptSpecification LITHUANIAN_LANGUAGE = new ConceptProxy("Lithuanian language", UUID.fromString("e9645d95-8a1f-3825-8feb-0bc2ee825694"));

   /** ConceptProxy for: "Chinese language". */
   public static ConceptSpecification CHINESE_LANGUAGE = new ConceptProxy("Chinese language", UUID.fromString("ba2efe6b-fe56-3d91-ae0f-3b389628f74c"));

   /** ConceptProxy for: "Japanese language". */
   public static ConceptSpecification JAPANESE_LANGUAGE = new ConceptProxy("Japanese language", UUID.fromString("aa789d52-2278-54cb-9a13-f41c36249f77"));

   /** ConceptProxy for: "Swedish language". */
   public static ConceptSpecification SWEDISH_LANGUAGE = new ConceptProxy("Swedish language", UUID.fromString("9784a791-8fdb-32f7-88da-74ab135fe4e3"));

   /** ConceptProxy for: "English description assemblage". */
   public static ConceptSpecification DESCRIPTION_ASSEMBLAGE = new ConceptProxy("Description assemblage", UUID.fromString("c9b9a4ac-3a1c-516c-bbef-3a13e30df27d"));

   /** ConceptProxy for: "preferred". SCT ID: 900000000000548007 */
   public static ConceptSpecification PREFERRED = new ConceptProxy("preferred", UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));

   /** ConceptProxy for: "acceptable". SCT ID: 900000000000549004 */
   public static ConceptSpecification ACCEPTABLE = new ConceptProxy("acceptable", UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));

   /** ConceptProxy for: "description case sensitive". */

   // SCT ID: 900000000000017005
   public static ConceptSpecification DESCRIPTION_CASE_SENSITIVE = new ConceptProxy("description case sensitive",
         UUID.fromString("0def37bc-7e1b-384b-a6a3-3e3ceee9c52e"));

   /** ConceptProxy for: "description not case sensitive". SCT ID: 900000000000448009 */
   public static ConceptSpecification DESCRIPTION_NOT_CASE_SENSITIVE = new ConceptProxy("Description not case sensitive",
         UUID.fromString("ecea41a2-f596-3d98-99d1-771b667e55b8"));

   /** ConceptProxy for: "description initial character sensitive". SCT ID: 900000000000020002 */
   public static ConceptSpecification DESCRIPTION_INITIAL_CHARACTER_SENSITIVE = new ConceptProxy("Description initial character sensitive",
         UUID.fromString("17915e0d-ed38-3488-a35c-cda966db306a"));

   /** ConceptProxy for: "development path". */
   public static ConceptSpecification DEVELOPMENT_PATH = new ConceptProxy("Development path", UUID.fromString("1f200ca6-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "master path". */
   public static ConceptSpecification MASTER_PATH = new ConceptProxy("Master path", UUID.fromString("1f20134a-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "VHAT modules". */
   public static ConceptSpecification VHAT_MODULES = new ConceptProxy("VHAT modules", UUID.fromString("8aa5fda8-33e9-5eaf-88e8-dd8a024d2489"));

   public static ConceptSpecification VHAT_EDIT = new ConceptProxy("VHAT Edit", UUID.fromString("afd3be8f-4a49-5e12-bc51-efed33aacd87"));

   /** ConceptProxy for: "SOLOR overlay module". */
   public static ConceptSpecification SOLOR_OVERLAY_MODULE = new ConceptProxy("SOLOR overlay module", UUID.fromString("9ecc154c-e490-5cf8-805d-d2865d62aef3"));

   /** ConceptProxy for: "EL++ inferred assemblage". */
   public static ConceptSpecification EL_PLUS_PLUS_INFERRED_ASSEMBLAGE = new ConceptProxy("EL++ inferred form assemblage",
         UUID.fromString("1f20182c-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "EL++ stated assemblage". */
   public static ConceptSpecification EL_PLUS_PLUS_STATED_ASSEMBLAGE = new ConceptProxy("EL++ stated form assemblage",
         UUID.fromString("1f201994-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "EL++ logic profile". IsaacMetadataAuxiliary has "EL++ profile", TermAux has "EL++ logic profile" */
   public static ConceptSpecification EL_PLUS_PLUS_LOGIC_PROFILE = new ConceptProxy("EL++ logic profile", UUID.fromString("1f201e12-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "Snorocket classifier". IsaacMetadataAuxiliary has "SnoRocket classifier", TermAux has "Snorocket classifier" */
   public static ConceptSpecification SNOROCKET_CLASSIFIER = new ConceptProxy("Snorocket classifier", UUID.fromString("1f201fac-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "role group". */
   public static ConceptSpecification ROLE_GROUP = new ConceptProxy("Role group", UUID.fromString("a63f4bf2-a040-11e5-8994-feff819cdc9f"));

   public static ConceptSpecification IDENTIFIER_SOURCE = new ConceptProxy("identifier source", UUID.fromString("5a87935c-d654-548f-82a2-0c06e3801162"));

   /** ConceptProxy for: "sufficient concept definition". SCTID: 900000000000073002 */
   public static ConceptSpecification SUFFICIENT_CONCEPT_DEFINITION = new ConceptProxy("Sufficient concept definition",
         UUID.fromString("6d9cd46e-8a8f-310a-a298-3e55dcf7a986"));

   /** ConceptProxy for: "necessary but not sufficient concept definition". SCTID: 900000000000074008 */
   public static ConceptSpecification NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION = new ConceptProxy("Necessary but not sufficient concept definition",
         UUID.fromString("e1a12059-3b01-3296-9532-d10e49d0afc3"));

   /** ConceptProxy for: "ISAAC metadata" */
   public static ConceptSpecification SOLOR_METADATA = new ConceptProxy("Metadata", UUID.fromString("7bbd4210-381c-11e7-9598-0800200c9a66"));

   public static ConceptSpecification ACTIVE_QUERY_CLAUSE = new ConceptProxy("Component is active (query clause)", "active ",
         UUID.fromString("45df0b38-67ec-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification INACTIVE_QUERY_CLAUSE = new ConceptProxy("Component is inactive (query clause)", "inactive",
         UUID.fromString("50e719a8-67ec-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification AND_QUERY_CLAUSE = new ConceptProxy("All child criterion are satisfied for component (query clause)", "and",
         UUID.fromString("d9c1e360-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification NOT_QUERY_CLAUSE = new ConceptProxy("Not (query clause)", "not", UUID.fromString("d9c1ea9a-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification AND_NOT_QUERY_CLAUSE = new ConceptProxy("and not (query clause)", "and not",
         UUID.fromString("d9c1ec02-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification OR_QUERY_CLAUSE = new ConceptProxy("Any child criterion is satisfied for component (query clause)", "or",
         UUID.fromString("d9c1f24c-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification XOR_QUERY_CLAUSE = new ConceptProxy("XOR (query clause)", "xor", UUID.fromString("d9c1f42c-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification CHANGED_FROM_PREVIOUS_VERSION_QUERY_CLAUSE = new ConceptProxy("changed from previous version (query clause)",
         "changed from previous version", UUID.fromString("d9c1f530-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification CONCEPT_IS_QUERY_CLAUSE = new ConceptProxy("Concept is (query clause)", "concept is",
         UUID.fromString("d9c1f602-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification CONCEPT_IS_KIND_OF_QUERY_CLAUSE = new ConceptProxy("Concept is kind of (query clause)", "kind of",
         UUID.fromString("d9c1f6d4-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification DESCRIPTION_LUCENE_MATCH_QUERY_CLAUSE = new ConceptProxy("Description Lucene match (query clause)", "Lucene match",
         UUID.fromString("d9c1f7a6-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification DESCRIPTION_LUCENE_ACTIVE_ONLY_MATCH_QUERY_CLAUSE = new ConceptProxy("Active only description Lucene match (query clause)",
         "Lucene active descriptions match", UUID.fromString("e047b6ea-c00f-11e7-abc4-cec278b6b50a"));
   public static ConceptSpecification PREFERRED_NAME_FOR_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Preferred name for concept (query clause)",
         "preferred name for concept", UUID.fromString("d9c1f882-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification RELATIONSHIP_IS_CIRCULAR_QUERY_CLAUSE = new ConceptProxy("Relationship is circular (query clause)", "relationship is circular",
         UUID.fromString("d9c1fcec-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification CONCEPT_IS_CHILD_OF_QUERY_CLAUSE = new ConceptProxy("Concept is child of (query clause)", "child of",
         UUID.fromString("d9c1fddc-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification DESCRIPTION_REGEX_MATCH_QUERY_CLAUSE = new ConceptProxy("Description regex match (query clause)", "regex match",
         UUID.fromString("d9c1ff9e-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification DESCRIPTION_REGEX_ACTIVE_ONLY_MATCH_QUERY_CLAUSE = new ConceptProxy("Active only description regex match (query clause)",
         "Regex active description match", UUID.fromString("48bafde1-02a4-5d74-b1e4-8909e7e5b5fc"));
   public static ConceptSpecification CONCEPT_FOR_COMPONENT_QUERY_CLAUSE = new ConceptProxy("Concept for component (query clause)", "concept for component",
         UUID.fromString("d9c20070-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification CONCEPT_IS_DESCENDENT_OF_QUERY_CLAUSE = new ConceptProxy("Concept is descendent of (query clause)", "descendent of",
         UUID.fromString("d9c20142-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification FULLY_QUALIFIED_NAME_FOR_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Fully qualified name for concept (query clause)",
         "fully qualified name for concept", UUID.fromString("f8eb8a8c-57aa-11e7-907b-a6006ad3dba0"));

   public static ConceptSpecification ASSEMBLAGE_CONTAINS_STRING_QUERY_CLAUSE = new ConceptProxy("Assemblage contains string (query clause)",
         "assemblage contains string", UUID.fromString("d9c207c8-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification ASSEMBLAGE_CONTAINS_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Assemblage contains concept (query clause)",
         "assemblage contains concept", UUID.fromString("d9c208a4-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE = new ConceptProxy("Assemblage contains component (query clause)",
         "assemblage contains component", UUID.fromString("d9c20976-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification ASSEMBLAGE_LUCENE_MATCH_QUERY_CLAUSE = new ConceptProxy("Assemblage Lucene match (query clause)", "assemblage Lucene match",
         UUID.fromString("d9c20a5c-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Assemblage contains kind-of concept (query clause)",
         "assemblage contains kind-of concept", UUID.fromString("d9c20b38-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification REL_RESTRICTION_QUERY_CLAUSE = new ConceptProxy("Relationship restriction (query clause)", "relationship restriction",
         UUID.fromString("d9c20c0a-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification REL_TYPE_QUERY_CLAUSE = new ConceptProxy("Relationship type (query clause)", "relationship type",
         UUID.fromString("d9c211be-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification ENCLOSING_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Enclosing concept (query clause)", "enclosing concept",
         UUID.fromString("f5111e6c-681d-11e7-907b-a6006ad3dba0"));
   public static ConceptSpecification ASSOCIATED_PARAMETER_QUERY_CLAUSE = new ConceptProxy("Associated parameter (query clause)", " ↳ ",
         UUID.fromString("a44e673b-14c4-525b-99be-0b5dfa0280c8"));

   public static ConceptSpecification STATED_PREMISE_TYPE = new ConceptProxy("Stated premise type", "Stated", UUID.fromString("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"));

   public static ConceptSpecification INFERRED_PREMISE_TYPE = new ConceptProxy("Inferred premise type", "Inferred", UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"));
  
   public static ConceptSpecification CONCEPT_ASSEMBLAGE = new ConceptProxy("Concept assemblage", "Concept assemblage",
         UUID.fromString("e8060eec-b9b9-11e7-abc4-cec278b6b50a"));
   public static ConceptSpecification SOLOR_CONCEPT_ASSEMBLAGE = new ConceptProxy("SOLOR concept assemblage", "SOLOR concepts",
         UUID.fromString("d39b3ecd-9a80-5009-a8ac-0b947f95ca7c"));
   public static ConceptSpecification LOINC_CONCEPT_ASSEMBLAGE = new ConceptProxy("LOINC concept assemblage", "LOINC concepts",
         UUID.fromString("d4d1bb43-bf2f-5c4e-b8b8-f0be8a5cca83"));
   public static ConceptSpecification RXNORM_CONCEPT_ASSEMBLAGE = new ConceptProxy("RxNorm concept assemblage", "RxNorm concepts",
         UUID.fromString("914cd34d-c97a-5fc5-abac-53bfb161eca0"));
   public static ConceptSpecification RF2_STATED_RELATIONSHIP_ASSEMBLAGE = new ConceptProxy("RF2 stated relationship assemblage", "RF2 stated relationships",
         UUID.fromString("c5c57241-e1c3-5c8b-85c6-0edffb28cfd0"));
   public static ConceptSpecification RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE = new ConceptProxy("RF2 inferred relationship assemblage", "RF2 inferred relationships",
         UUID.fromString("e3436c74-2491-50fa-b43c-13d83238648c"));
   public static ConceptSpecification RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE = new ConceptProxy("RF2 legacy relationship implication assemblage",
         "RF2 legacy relationship implication", UUID.fromString("b7c0f2ee-5481-5122-8910-6d89543ff278"));
   public static ConceptSpecification LOINC_RECORD_ASSEMBLAGE = new ConceptProxy("LOINC record assemblage", "LOINC record assemblage", 
         UUID.fromString("282b86b7-8ae4-5e6a-9dbd-849aabc67922"));

   public static ConceptSpecification CONTENT_LICENSE = new ConceptProxy("Content license", "Content license", UUID.fromString("b3305461-6954-574e-9124-285a02d4ecae"));
   public static ConceptSpecification SCT_AFFILIATES_LICENSE = new ConceptProxy("SNOMED® affiliates license", "SNOMED® affiliates license",
         UUID.fromString("4e7d8a63-1f36-56bb-9217-daa6da1b47e7"));
   public static ConceptSpecification LOINC_LICENSE = new ConceptProxy("LOINC® license", "LOINC® license", UUID.fromString("2c6f846b-a61d-5358-afdd-5e2309157408"));
   public static ConceptSpecification RXNORM_LICENSE = new ConceptProxy("RxNorm license", "RxNorm license", UUID.fromString("9ca299bb-61b8-5aaa-b1c1-131600067947"));
   public static ConceptSpecification APACHE_2_LICENSE = new ConceptProxy("Apache 2 license", "Apache 2 license",
         UUID.fromString("a4516185-deb8-5db1-8db8-10dbe021ffa5"));
   public static ConceptSpecification CC_BY_LICENSE = new ConceptProxy("Creative Commons BY license", "Creative Commons BY license",
         UUID.fromString("3415a972-7850-57cd-aa86-a572ca1c2ceb"));
   public static ConceptSpecification US_GOVERNMENT_WORK = new ConceptProxy("US Government Work", "US Government Work",
         UUID.fromString("9f031ac8-4737-508b-8874-a6c1a6e134e2"));

   public static ConceptSpecification COPYRIGHT = new ConceptProxy("Copyright", "Copyright", UUID.fromString("57b405d5-20b5-5aa3-923c-ead3af1e692e"));
   public static ConceptSpecification SNOMED_COPYRIGHT = new ConceptProxy("© SNOMED International", "© SNOMED International",
         UUID.fromString("d03b0cc5-dfdf-5580-b162-f2fb0e15eb94"));
   public static ConceptSpecification COPYRIGHT_FREE_WORK = new ConceptProxy("Copyright free work", "Copyright free work",
         UUID.fromString("4d268bfc-026d-53a4-b7d0-cbe3ee109337"));
   public static ConceptSpecification REGENSTRIEF_AND_LOINC_COPYRIGHT = new ConceptProxy(
         "© Regenstrief Institute, Inc. and © The Logical Observation Identifiers Names and Codes LOINC Committee", "© Regenstrief Institute and the LOINC Committee",
         UUID.fromString("005d1366-7865-5055-9cd7-2b40a0396326"));
   public static ConceptSpecification INFORMATICS_INC_COPYRIGHT = new ConceptProxy("© Informatics, Incorporated", "© Informatics, Inc.",
         UUID.fromString("f892783f-4aa4-5ba8-a0bf-8a99c4149155"));
   public static ConceptSpecification KEITH_CAMPBELL = new ConceptProxy("Keith Eugene Campbell", "kec", UUID.fromString("5e47881c-9e27-5578-a1c1-5535a9400bc6"));

   // SNOMED CT High Level Taxonomy Classes
   public static ConceptSpecification BODY_STRUCTURE = new ConceptProxy("Body structure", "Body structure", UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790"));
   public static ConceptSpecification EVENT = new ConceptProxy("Event", "Event", UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698"));
   public static ConceptSpecification FORCE = new ConceptProxy("Force", "Force", UUID.fromString("32213bf6-c073-3ce1-b0c7-9463e43af2f1"));
   public static ConceptSpecification MEDICATION = new ConceptProxy("Medication", "Medication", UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449"));
   public static ConceptSpecification PHENOMENON = new ConceptProxy("Phenomenon", "Phenomenon", UUID.fromString("c2e8bc47-3353-5e02-b0d1-2a5916efed4d"));
   public static ConceptSpecification ORGANISM = new ConceptProxy("Organism", "Organism", UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072"));
   public static ConceptSpecification OBJECT = new ConceptProxy("Object", "Object", UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));
   public static ConceptSpecification PROCEDURE = new ConceptProxy("Procedure", "Procedure", UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b"));
   public static ConceptSpecification SPECIMEN = new ConceptProxy("Specimen", "Specimen", UUID.fromString("3680e12d-c14c-39cb-ac89-2ae1fa125d41"));
   public static ConceptSpecification SUBSTANCE = new ConceptProxy("Substance", "Substance", UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));

   // J+
   // ~--- methods -------------------------------------------------------------

   /**
    * Case significance to concept sequence.
    *
    * @param initialCaseSignificant the initial case significant
    * @return the int
    */
   public static int caseSignificanceToConceptNid(boolean initialCaseSignificant) {
      if (initialCaseSignificant) {
         return Get.identifierService().getNidForUuids(TermAux.DESCRIPTION_CASE_SENSITIVE.getUuids());
      }

      return Get.identifierService().getNidForUuids(TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getUuids());
   }

   /**
    * Concept id to case significance.
    *
    * @param nid the id
    * @return true, if successful
    */
   public static boolean conceptIdToCaseSignificance(int nid) {

      return TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getNid() == nid;
   }

   // ~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept specification for language sequence.
    *
    * @param languageConceptNid the language concept sequence
    * @return the concept specification for language sequence
    */
   public static ConceptSpecification getConceptSpecificationForLanguageSequence(int languageConceptNid) {
      if (languageConceptNid == ENGLISH_LANGUAGE.getNid() 
            || languageConceptNid == ENGLISH_DIALECT_ASSEMBLAGE.getNid() || languageConceptNid == GB_DIALECT_ASSEMBLAGE.getNid()) {
         return ENGLISH_LANGUAGE;
      }

      if (languageConceptNid == SPANISH_LANGUAGE.getNid() || languageConceptNid == SPANISH_DIALECT_ASSEMBLAGE.getNid()) {
         return SPANISH_LANGUAGE;
      }

      if (languageConceptNid == FRENCH_LANGUAGE.getNid()) {
         return FRENCH_LANGUAGE;
      }

      if (languageConceptNid == DANISH_LANGUAGE.getNid()) {
         return DANISH_LANGUAGE;
      }

      if (languageConceptNid == POLISH_LANGUAGE.getNid()) {
         return POLISH_LANGUAGE;
      }

      if (languageConceptNid == DUTCH_LANGUAGE.getNid()) {
         return DUTCH_LANGUAGE;
      }

      if (languageConceptNid == LITHUANIAN_LANGUAGE.getNid()) {
         return LITHUANIAN_LANGUAGE;
      }

      if (languageConceptNid == CHINESE_LANGUAGE.getNid()) {
         return CHINESE_LANGUAGE;
      }

      if (languageConceptNid == JAPANESE_LANGUAGE.getNid()) {
         return JAPANESE_LANGUAGE;
      }

      if (languageConceptNid == SWEDISH_LANGUAGE.getNid()) {
         return SWEDISH_LANGUAGE;
      }

      throw new RuntimeException("Unsupported language: " + Get.conceptDescriptionText(languageConceptNid));
   }
   
   public static ConceptSpecification[] getAllSpecs() {
      ArrayList<ConceptSpecification> items = new ArrayList<>();
      
      try {
         for (Field f : TermAux.class.getFields()) {
            if (f.getType().equals(ConceptSpecification.class)) {
               items.add((ConceptSpecification)f.get(null));
            }
         }
      } catch (Exception e) {
         throw new RuntimeException("Unexpected error");
      } 
      return items.toArray(new ConceptSpecification[items.size()]);
   }

   public static void main(String[] args) {
      for (int i = 0; i < 19; i++) {
         System.out.println(UuidT5Generator.get(UUID.randomUUID().toString()));
      }
   }

   @Override
   public void reset() {
      LogManager.getLogger().info("Clearing cached nids in static metadata from TermAux");
      for (ConceptSpecification cs : getAllSpecs()) {
         cs.clearCache();
      }
   }
}
