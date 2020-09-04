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

import jakarta.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.jvnet.hk2.annotations.Service;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;

//~--- classes ----------------------------------------------------------------
/**
 * This class only contains manually created, hard coded constants which need to
 * exist earlier in the classpath than {@link MetaData} is available. If you are
 * down the classpath far enough to have MetaData, use that class.
 *
 * Data created here does NOT automatically get created as actual concepts. All
 * concepts here must be referenced by {@link IsaacMetadataAuxiliary}, or they
 * will not get created at runtime, and you will have broken references in your
 * DB. {@link TermAux}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class TermAux implements StaticIsaacCache {
   // J-
   public static UUID MASTER_PATH_SEMANTIC_UUID = UUID.fromString("79a92f9e-cd93-5537-984c-c9aa4532e59d");
   public static UUID DEVELOPMENT_PATH_SEMANTIC_UUID = UUID.fromString("f02874c5-186b-53c4-9054-f819975a9814");
   public static UUID PRIMORDIAL_PATH_SEMANTIC_UUID = UUID.fromString("786afd5d-9f6b-52cb-8d71-f2e0564b621d");
   public static UUID SANDBOX_PATH_SEMANTIC_UUID = UUID.fromString("3aee7ff3-8718-5342-b831-2974a8cf1734");
   public static UUID MASTER_PATH_ORIGIN_SEMANTIC_UUID = UUID.fromString("1063412d-207c-57bd-abd9-60b31691507d");
    public static UUID DEVELOPMENT_PATH_ORIGIN_SEMANTIC_UUID = UUID.fromString("126e5d77-bd9a-53e8-9260-76aba9613288");
    public static UUID SANDBOX_PATH_ORIGIN_SEMANTIC_UUID = UUID.fromString("a0781163-f0b8-5e25-9090-d4648bb227d6");


   //SNOMED CT universally unique identifier (core metadata concept)
   // SCTID: 900000000000002006
   public static ConceptProxy ISAAC_UUID = new ConceptProxy("UUID", UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66"),
    UuidT3Generator.fromSNOMED("900000000000002006"));
   
   /** ConceptProxy for: "Part of concept". */
   public static ConceptProxy PART_OF = new ConceptProxy("Part of", UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73"));

   /** ConceptProxy for: "Laterality concept". */
   public static ConceptProxy LATERALITY = new ConceptProxy("Laterality", UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b"));

   /** ConceptProxy for: "Has active ingredient concept". */
   public static ConceptProxy HAS_ACTIVE_INGREDIENT = new ConceptProxy("Has active ingredient", UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8"));

   /** ConceptProxy for: "Has dose form concept". */
   public static ConceptProxy HAS_DOSE_FORM = new ConceptProxy("Has dose form", UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63"));

   /** ConceptProxy for: "ISAAC root concept". */
   public static ConceptProxy SOLOR_ROOT = new ConceptProxy("SOLOR concept", UUID.fromString("7c21b6c5-cf11-5af9-893b-743f004c97f5"));

   /** ConceptProxy for: "database uuid". */
   public static ConceptProxy DATABASE_UUID = new ConceptProxy("Database UUID", UUID.fromString("49b882a1-05e4-52cf-96d8-5de024b24632"));

   /** ConceptProxy for: "user". */
   public static ConceptProxy USER = new ConceptProxy("User", UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));

   /** ConceptProxy for: "is a". */
   public static ConceptProxy IS_A = new ConceptProxy("Is a (relationship type)", UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
   /** ConceptProxy for: "Child of". */
   public static ConceptProxy CHILD_OF = new ConceptProxy("Child of (internal use relationship type)", UUID.fromString("91947d30-7555-5400-bbe2-4415472cff1b"));

   /** ConceptProxy for: "IHTSDO classifier. */
   public static ConceptProxy IHTSDO_CLASSIFIER = new ConceptProxy("IHTSDO Classifier", UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));

   /** ConceptProxy for: "Path". */
   public static ConceptProxy PATH = new ConceptProxy("Path", UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));

   /**
    * ConceptProxy for: "Path origin assemblage". IsaacMetadataAuxiliary has "path origins assemblage", TermAux has "Path origin reference set"
    */
   public static ConceptProxy PATH_ORIGIN_ASSEMBLAGE = new ConceptProxy("Path origin assemblage", UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b"));

   /** ConceptProxy for: "path assemblage". IsaacMetadataAuxiliary has "paths assemblage", TermAux has "paths" */
   public static ConceptProxy PATH_ASSEMBLAGE = new ConceptProxy("Paths", UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f"));

   /** ConceptProxy for: "SNOMED integer identifier. 'SNOMED CT integer identifier' - 900000000000294009; 'SNOMED integer id' */
   public static ConceptProxy SNOMED_IDENTIFIER = new ConceptProxy("SNOMED integer id", UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"),
         UUID.fromString("87360947-e603-3397-804b-efd0fcc509b9"));

   /** ConceptProxy for: "Assemblage". Formerly known as REFSET_IDENTITY */
   public static ConceptProxy ASSEMBLAGE = new ConceptProxy("Assemblage", UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da"));

   /** ConceptProxy for: "Unspecified module. */
   public static ConceptProxy UNSPECIFIED_MODULE = new ConceptProxy("Module", // simply labeled 'module' in IsaacMetadataAuxiliary
         UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));

   /** ConceptProxy for: "SOLOR module". */
   public static ConceptProxy SOLOR_MODULE = new ConceptProxy("SOLOR module", UUID.fromString("f680c868-f7e5-5d0e-91f2-615eca8f8fd2"));
   
   // 900000000000207008
   public static ConceptProxy SCT_CORE_MODULE = new ConceptProxy("SNOMED CT® core modules", "SNOMED CT core", UUID.fromString("1b4f1ba5-b725-390f-8c3b-33ec7096bdca"));
   /** ConceptProxy for: "Synonym description type". SCT ID: 900000000000013009 */
   public static ConceptProxy REGULAR_NAME_DESCRIPTION_TYPE = new ConceptProxy("Regular name description type", "Regular name", UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"));

   /** ConceptProxy for: "Fully qualified description type. */
   public static ConceptProxy FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE = new ConceptProxy("Fully qualified name description type", "Fully qualified name",
         UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));

   /** ConceptProxy for: "Definition description type". IsaacMetadataAuxiliary has "definition description type", TermAux has "Definition" */
   public static ConceptProxy DEFINITION_DESCRIPTION_TYPE = new ConceptProxy("Definition description type", "Definition", UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09"));

   /**
    * ConceptProxy for: "Description type in source terminology". Needed within DynamicMetadata constants, which can't reference
    * IsaacMetadataAuxiliary
    */
   public static ConceptProxy DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY = new ConceptProxy("Description type in source terminology",
         UUID.fromString("ef7d9808-a839-5119-a604-b777268eb719"));

   /** ConceptProxy for: "Relationship type in source terminology". */
   public static ConceptProxy RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY = new ConceptProxy("Relationship type in source terminology",
         UUID.fromString("46bc0e6b-0e64-5aa6-af27-a823e9156dfc"));

   public static ConceptProxy ENGLISH_DIALECT_ASSEMBLAGE =
      new ConceptProxy("English dialect assemblage",
                       UUID.fromString("c0836284-f631-3c86-8cfc-56a67814efab"));

   /**
    * ConceptProxy for: "US dialect assemblage". IsaacMetadataAuxiliary has "US English dialect", TermAux has "United States of America English
    * language reference set"
    */
   public static ConceptProxy US_DIALECT_ASSEMBLAGE = new ConceptProxy("United States of America English dialect assemblage", "US English",
         UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));

   /**
    * ConceptProxy for: "Great Britain dialect assemblage". IsaacMetadataAuxiliary has "GB English dialect", TermAux has "Great Britain English
    * language reference set"
    */
   public static ConceptProxy GB_DIALECT_ASSEMBLAGE = new ConceptProxy("Great Britain English dialect assemblage", "GB English",
         UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));
   
   public static ConceptProxy SPANISH_DIALECT_ASSEMBLAGE = new ConceptProxy("Spanish dialect assemblage",
         UUID.fromString("03615ef2-aa56-336d-89c5-a1b5c4cee8f6"));

   public static ConceptProxy SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE = new ConceptProxy("Latin american spanish dialect assemblage", "Latin American Spanish",
         UUID.fromString("835d3fe2-7bd7-3aa8-a52a-25e203b0afbe"));

   public static ConceptProxy LANGUAGE = new ConceptProxy("Language", UUID.fromString("f56fa231-10f9-5e7f-a86d-a1d61b5b56e3"));
   
   /** ConceptProxy for: "English language". */
   public static ConceptProxy ENGLISH_LANGUAGE = new ConceptProxy("English language", UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558"));

   /** ConceptProxy for: "Spanish language". */
   public static ConceptProxy SPANISH_LANGUAGE = new ConceptProxy("Spanish language", UUID.fromString("0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e"));

   /** ConceptProxy for: "French language". */
   public static ConceptProxy FRENCH_LANGUAGE = new ConceptProxy("French language", UUID.fromString("8b23e636-a0bd-30fb-b8e2-1f77eaa3a87e"));

   /** ConceptProxy for: "Danish language". */
   public static ConceptProxy DANISH_LANGUAGE = new ConceptProxy("Danish language", UUID.fromString("7e462e33-6d94-38ae-a044-492a857a6853"));

   /** ConceptProxy for: "Polish language". */
   public static ConceptProxy POLISH_LANGUAGE = new ConceptProxy("Polish language", UUID.fromString("c924b887-da88-3a72-b8ea-fa86990467c9"));

   /** ConceptProxy for: "Dutch language". */
   public static ConceptProxy DUTCH_LANGUAGE = new ConceptProxy("Dutch language", UUID.fromString("674ad858-0224-3f90-bcf0-bc4cab753d2d"));

   /** ConceptProxy for: "Lithuanian language". */
   public static ConceptProxy LITHUANIAN_LANGUAGE = new ConceptProxy("Lithuanian language", UUID.fromString("e9645d95-8a1f-3825-8feb-0bc2ee825694"));

   /** ConceptProxy for: "Chinese language". */
   public static ConceptProxy CHINESE_LANGUAGE = new ConceptProxy("Chinese language", UUID.fromString("ba2efe6b-fe56-3d91-ae0f-3b389628f74c"));

   /** ConceptProxy for: "Japanese language". */
   public static ConceptProxy JAPANESE_LANGUAGE = new ConceptProxy("Japanese language", UUID.fromString("aa789d52-2278-54cb-9a13-f41c36249f77"));

   /** ConceptProxy for: "Swedish language". */
   public static ConceptProxy SWEDISH_LANGUAGE = new ConceptProxy("Swedish language", UUID.fromString("9784a791-8fdb-32f7-88da-74ab135fe4e3"));

   /** ConceptProxy for: "English description assemblage". */
   public static ConceptProxy DESCRIPTION_ASSEMBLAGE = new ConceptProxy("Description assemblage", UUID.fromString("c9b9a4ac-3a1c-516c-bbef-3a13e30df27d"));

   /** ConceptProxy for: "preferred". SCT ID: 900000000000548007 */
   public static ConceptProxy PREFERRED = new ConceptProxy("Preferred", UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));

   /** ConceptProxy for: "acceptable". SCT ID: 900000000000549004 */
   public static ConceptProxy ACCEPTABLE = new ConceptProxy("Acceptable", UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));

   /** ConceptProxy for: "description case sensitive". */

   // SCT ID: 900000000000017005
   public static ConceptProxy DESCRIPTION_CASE_SENSITIVE = new ConceptProxy("Description case sensitive", "Case sensitive",
         UUID.fromString("0def37bc-7e1b-384b-a6a3-3e3ceee9c52e"));

   /** ConceptProxy for: "description not case sensitive". SCT ID: 900000000000448009 */
   public static ConceptProxy DESCRIPTION_NOT_CASE_SENSITIVE = new ConceptProxy("Description not case sensitive", "Not case sensitive", 
         UUID.fromString("ecea41a2-f596-3d98-99d1-771b667e55b8"));

   /** ConceptProxy for: "description initial character sensitive". SCT ID: 900000000000020002 */
   public static ConceptProxy DESCRIPTION_INITIAL_CHARACTER_SENSITIVE = new ConceptProxy("Description initial character case sensitive", 
           "First char case sensitive",
         UUID.fromString("17915e0d-ed38-3488-a35c-cda966db306a"));

   /** ConceptProxy for: "development path". */
   public static ConceptProxy DEVELOPMENT_PATH = new ConceptProxy("Development path", UUID.fromString("1f200ca6-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "master path". */
   public static ConceptProxy MASTER_PATH = new ConceptProxy("Master path", UUID.fromString("1f20134a-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "VHAT modules". */
   public static ConceptProxy VHAT_MODULES = new ConceptProxy("VHAT modules", UUID.fromString("8aa5fda8-33e9-5eaf-88e8-dd8a024d2489"));

   /** ConceptProxy for: "SOLOR overlay module". */
   public static ConceptProxy SOLOR_OVERLAY_MODULE = new ConceptProxy("SOLOR overlay module", UUID.fromString("9ecc154c-e490-5cf8-805d-d2865d62aef3"));

   /** ConceptProxy for: "EL++ inferred assemblage". */
   public static ConceptProxy EL_PLUS_PLUS_INFERRED_ASSEMBLAGE = new ConceptProxy("EL++ inferred form assemblage",
         UUID.fromString("1f20182c-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "EL++ stated assemblage". */
   public static ConceptProxy EL_PLUS_PLUS_STATED_ASSEMBLAGE = new ConceptProxy("EL++ stated form assemblage",
         UUID.fromString("1f201994-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "EL++ logic profile". IsaacMetadataAuxiliary has "EL++ profile", TermAux has "EL++ logic profile" */
   public static ConceptProxy EL_PLUS_PLUS_LOGIC_PROFILE = new ConceptProxy("EL++ logic profile", UUID.fromString("1f201e12-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "Snorocket classifier". IsaacMetadataAuxiliary has "SnoRocket classifier", TermAux has "Snorocket classifier" */
   public static ConceptProxy SNOROCKET_CLASSIFIER = new ConceptProxy("Snorocket classifier", UUID.fromString("1f201fac-960e-11e5-8994-feff819cdc9f"));

   /** ConceptProxy for: "role group". */
   public static ConceptProxy ROLE_GROUP = new ConceptProxy("Role group", UUID.fromString("a63f4bf2-a040-11e5-8994-feff819cdc9f"));

   public static ConceptProxy IDENTIFIER_SOURCE = new ConceptProxy("Identifier source", UUID.fromString("5a87935c-d654-548f-82a2-0c06e3801162"));

   /** ConceptProxy for: "sufficient concept definition". SCTID: 900000000000073002 */
   public static ConceptProxy SUFFICIENT_CONCEPT_DEFINITION = new ConceptProxy("Sufficient concept definition",
         UUID.fromString("6d9cd46e-8a8f-310a-a298-3e55dcf7a986"));

   /** ConceptProxy for: "necessary but not sufficient concept definition". SCTID: 900000000000074008 */
   public static ConceptProxy NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION = new ConceptProxy("Necessary but not sufficient concept definition",
         UUID.fromString("e1a12059-3b01-3296-9532-d10e49d0afc3"));

   /** ConceptProxy for: "ISAAC metadata" now called "Model concept*/
   public static ConceptProxy SOLOR_METADATA = new ConceptProxy( "Model concept", UUID.fromString("7bbd4210-381c-11e7-9598-0800200c9a66"));

   public static ConceptProxy ACTIVE_QUERY_CLAUSE = new ConceptProxy("Component is active (query clause)", "active ",
         UUID.fromString("45df0b38-67ec-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy INACTIVE_QUERY_CLAUSE = new ConceptProxy("Component is inactive (query clause)", "inactive",
         UUID.fromString("50e719a8-67ec-11e7-907b-a6006ad3dba0"));
   
   public static ConceptProxy REFERENCED_COMPONENT_IS_ACTIVE = new ConceptProxy("Referenced component is active (query clause)", "Referenced component is active", UUID.fromString("d6f0f08f-d2db-5143-9466-cb60073879f3"));
   public static ConceptProxy REFERENCED_COMPONENT_IS_INACTIVE = new ConceptProxy("Referenced component is NOT active (query clause)", "Referenced component is NOT active", UUID.fromString("1fbdd3c7-efd1-59ec-a648-79582d0f8f1e"));
   
   
   public static ConceptProxy AND_QUERY_CLAUSE = new ConceptProxy("All child criterion are satisfied for component (query clause)", "and query",
         UUID.fromString("d9c1e360-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy NOT_QUERY_CLAUSE = new ConceptProxy("Not (query clause)", "not", UUID.fromString("d9c1ea9a-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy AND_NOT_QUERY_CLAUSE = new ConceptProxy("and not (query clause)", "and not query",
         UUID.fromString("d9c1ec02-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy OR_QUERY_CLAUSE = new ConceptProxy("Any child criterion is satisfied for component (query clause)", "or query",
         UUID.fromString("d9c1f24c-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy XOR_QUERY_CLAUSE = new ConceptProxy("XOR (query clause)", "xor", UUID.fromString("d9c1f42c-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy CHANGED_FROM_PREVIOUS_VERSION_QUERY_CLAUSE = new ConceptProxy("changed between STAMPs (query clause)",
         "changed between STAMPs", UUID.fromString("d9c1f530-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy CONCEPT_IS_QUERY_CLAUSE = new ConceptProxy("Concept is (query clause)", "concept is",
         UUID.fromString("d9c1f602-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy CONCEPT_IS_KIND_OF_QUERY_CLAUSE = new ConceptProxy("Concept is kind of (query clause)", "kind of",
         UUID.fromString("d9c1f6d4-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy DESCRIPTION_LUCENE_MATCH_QUERY_CLAUSE = new ConceptProxy("Description Lucene match (query clause)", "Lucene match",
         UUID.fromString("d9c1f7a6-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy DESCRIPTION_LUCENE_ACTIVE_ONLY_MATCH_QUERY_CLAUSE = new ConceptProxy("Active only description Lucene match (query clause)",
         "Lucene active descriptions match", UUID.fromString("e047b6ea-c00f-11e7-abc4-cec278b6b50a"));
   public static ConceptProxy PREFERRED_NAME_FOR_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Preferred name for concept (query clause)",
         "preferred name for concept", UUID.fromString("d9c1f882-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy RELATIONSHIP_IS_CIRCULAR_QUERY_CLAUSE = new ConceptProxy("Relationship is circular (query clause)", "relationship is circular",
         UUID.fromString("d9c1fcec-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy CONCEPT_IS_CHILD_OF_QUERY_CLAUSE = new ConceptProxy("Concept is child of (query clause)", "child of query",
         UUID.fromString("d9c1fddc-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy DESCRIPTION_REGEX_MATCH_QUERY_CLAUSE = new ConceptProxy("Description regex match (query clause)", "regex match",
         UUID.fromString("d9c1ff9e-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy DESCRIPTION_REGEX_ACTIVE_ONLY_MATCH_QUERY_CLAUSE = new ConceptProxy("Active only description regex match (query clause)",
         "Regex active description match", UUID.fromString("48bafde1-02a4-5d74-b1e4-8909e7e5b5fc"));
   public static ConceptProxy CONCEPT_FOR_COMPONENT_QUERY_CLAUSE = new ConceptProxy("Concept for component (query clause)", "concept for component",
         UUID.fromString("d9c20070-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy CONCEPT_IS_DESCENDENT_OF_QUERY_CLAUSE = new ConceptProxy("Concept is descendent of (query clause)", "descendent of",
         UUID.fromString("d9c20142-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy FULLY_QUALIFIED_NAME_FOR_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Fully qualified name for concept (query clause)",
         "fully qualified name for concept", UUID.fromString("f8eb8a8c-57aa-11e7-907b-a6006ad3dba0"));

   public static ConceptProxy ASSEMBLAGE_CONTAINS_STRING_QUERY_CLAUSE = new ConceptProxy("Assemblage contains string (query clause)",
         "assemblage contains string", UUID.fromString("d9c207c8-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy ASSEMBLAGE_CONTAINS_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Assemblage contains concept (query clause)",
         "assemblage contains concept", UUID.fromString("d9c208a4-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy ASSEMBLAGE_CONTAINS_COMPONENT_QUERY_CLAUSE = new ConceptProxy("Assemblage contains component (query clause)",
         "assemblage contains component", UUID.fromString("d9c20976-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy ASSEMBLAGE_LUCENE_MATCH_QUERY_CLAUSE = new ConceptProxy("Assemblage Lucene match (query clause)", "assemblage Lucene match",
         UUID.fromString("d9c20a5c-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy ASSEMBLAGE_CONTAINS_KIND_OF_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Assemblage contains kind-of concept (query clause)",
         "assemblage contains kind-of concept", UUID.fromString("d9c20b38-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy REL_RESTRICTION_QUERY_CLAUSE = new ConceptProxy("Relationship restriction (query clause)", "relationship restriction",
         UUID.fromString("d9c20c0a-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy REL_TYPE_QUERY_CLAUSE = new ConceptProxy("Relationship type (query clause)", "relationship type",
         UUID.fromString("d9c211be-579e-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy ENCLOSING_CONCEPT_QUERY_CLAUSE = new ConceptProxy("Enclosing concept (query clause)", "enclosing concept",
         UUID.fromString("f5111e6c-681d-11e7-907b-a6006ad3dba0"));
   public static ConceptProxy ASSOCIATED_PARAMETER_QUERY_CLAUSE = new ConceptProxy("Associated parameter (query clause)", " ↳ ",
         UUID.fromString("a44e673b-14c4-525b-99be-0b5dfa0280c8"));
    public static ConceptProxy JOIN_QUERY_CLAUSE = new ConceptProxy("Join query clause", "join", UUID.fromString("18fa4f5c-6691-597d-897a-93c6f709ec66"));

    public static ConceptProxy REFERENCED_COMPONENT_IS = new ConceptProxy("Referenced component is (query clause)", "RC is", UUID.fromString("9d4f0198-4f84-5172-b78c-f47fd048c851"));
    public static ConceptProxy REFERENCED_COMPONENT_IS_MEMBER_OF = new ConceptProxy("Referenced component is member of (query clause)", "RC is member of", UUID.fromString("d39a3b5f-a862-5b52-93ed-0a7a0bbe329f"));
    public static ConceptProxy REFERENCED_COMPONENT_IS_NOT_MEMBER_OF = new ConceptProxy("Referenced component is NOT member of (query clause)", "RC is NOT member of", UUID.fromString("c92e5540-0d38-5f08-94e2-6ae673ee9c6b"));
    
    public static ConceptProxy COMPONENT_IS_MEMBER_OF = new ConceptProxy("Component is member of (query clause)", "Component is member of", UUID.fromString("380e0514-906b-5675-9ac4-6e788b1269cd"));
    public static ConceptProxy COMPONENT_IS_NOT_MEMBER_OF = new ConceptProxy("Component is NOT member of (query clause)", "Component is NOT member of", UUID.fromString("117cf5cd-80aa-58b0-b216-fd60c953af22"));
    public static ConceptProxy REFERENCED_COMPONENT_IS_KIND_OF = new ConceptProxy("Referenced component is kind-of (query clause)", "RC is kind-of", UUID.fromString("74ba168f-7932-5f3d-a8c5-28dc6d9fe647"));
    public static ConceptProxy REFERENCED_COMPONENT_IS_NOT_KIND_OF = new ConceptProxy("Referenced component is NOT kind-of (query clause)", "RC is NOT kind-of", UUID.fromString("82ae0a41-8a1c-5cb5-8087-5562608b3373"));
    
   public static ConceptProxy STATED_PREMISE_TYPE = new ConceptProxy("Stated premise type", "Stated", UUID.fromString("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"));

   public static ConceptProxy INFERRED_PREMISE_TYPE = new ConceptProxy("Inferred premise type", "Inferred", UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"));
  
   public static ConceptProxy EXTERNAL_DATA_ASSEMBLAGE = new ConceptProxy("External data assemblage (SOLOR)", UUID.fromString("0fba52dd-0727-5e87-a41d-e30ec88b7f87"));
   public static ConceptProxy CONCEPT_ASSEMBLAGE = new ConceptProxy("Concept assemblage", "Concept assemblage",
         UUID.fromString("e8060eec-b9b9-11e7-abc4-cec278b6b50a"));
   public static ConceptProxy SOLOR_CONCEPT_ASSEMBLAGE = new ConceptProxy("SOLOR concept assemblage", "SOLOR concepts",
         UUID.fromString("d39b3ecd-9a80-5009-a8ac-0b947f95ca7c"));
   public static ConceptProxy LOINC_CONCEPT_ASSEMBLAGE = new ConceptProxy("LOINC concept assemblage", "LOINC concepts",
         UUID.fromString("d4d1bb43-bf2f-5c4e-b8b8-f0be8a5cca83"));
   public static ConceptProxy RXNORM_CONCEPT_ASSEMBLAGE = new ConceptProxy("RxNorm concept assemblage", "RxNorm concepts",
         UUID.fromString("914cd34d-c97a-5fc5-abac-53bfb161eca0"));
   public static ConceptProxy RF2_STATED_RELATIONSHIP_ASSEMBLAGE = new ConceptProxy("RF2 stated relationship assemblage", "RF2 stated relationships",
         UUID.fromString("c5c57241-e1c3-5c8b-85c6-0edffb28cfd0"));
   public static ConceptProxy RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE = new ConceptProxy("RF2 inferred relationship assemblage", "RF2 inferred relationships",
         UUID.fromString("e3436c74-2491-50fa-b43c-13d83238648c"));
   public static ConceptProxy RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE = new ConceptProxy("RF2 legacy relationship implication assemblage",
         "RF2 legacy relationship implication", UUID.fromString("b7c0f2ee-5481-5122-8910-6d89543ff278"));
   public static ConceptProxy LOINC_RECORD_ASSEMBLAGE = new ConceptProxy("LOINC record assemblage", "LOINC record assemblage",
         UUID.fromString("282b86b7-8ae4-5e6a-9dbd-849aabc67922"));

   public static ConceptProxy CONTENT_LICENSE = new ConceptProxy("Content license", "Content license", UUID.fromString("b3305461-6954-574e-9124-285a02d4ecae"));
   public static ConceptProxy SCT_AFFILIATES_LICENSE = new ConceptProxy("SNOMED® affiliates license", "SNOMED® affiliates license",
         UUID.fromString("4e7d8a63-1f36-56bb-9217-daa6da1b47e7"));
   public static ConceptProxy LOINC_LICENSE = new ConceptProxy("LOINC® license", "LOINC® license", UUID.fromString("2c6f846b-a61d-5358-afdd-5e2309157408"));
   public static ConceptProxy RXNORM_LICENSE = new ConceptProxy("RxNorm license", "RxNorm license", UUID.fromString("9ca299bb-61b8-5aaa-b1c1-131600067947"));
   public static ConceptProxy APACHE_2_LICENSE = new ConceptProxy("Apache 2 license", "Apache 2 license",
         UUID.fromString("a4516185-deb8-5db1-8db8-10dbe021ffa5"));
   public static ConceptProxy CC_BY_LICENSE = new ConceptProxy("Creative Commons BY license", "Creative Commons BY license",
         UUID.fromString("3415a972-7850-57cd-aa86-a572ca1c2ceb"));
   public static ConceptProxy US_GOVERNMENT_WORK = new ConceptProxy("US Government Work", "US Government Work",
         UUID.fromString("9f031ac8-4737-508b-8874-a6c1a6e134e2"));

   public static ConceptProxy COPYRIGHT = new ConceptProxy("Copyright", "Copyright", UUID.fromString("57b405d5-20b5-5aa3-923c-ead3af1e692e"));
   public static ConceptProxy SNOMED_COPYRIGHT = new ConceptProxy("© SNOMED International", "© SNOMED International",
         UUID.fromString("d03b0cc5-dfdf-5580-b162-f2fb0e15eb94"));
   public static ConceptProxy COPYRIGHT_FREE_WORK = new ConceptProxy("Copyright free work", "Copyright free work",
         UUID.fromString("4d268bfc-026d-53a4-b7d0-cbe3ee109337"));
   public static ConceptProxy REGENSTRIEF_AND_LOINC_COPYRIGHT = new ConceptProxy(
         "© Regenstrief Institute, Inc. and © The Logical Observation Identifiers Names and Codes LOINC Committee", "© Regenstrief Institute and the LOINC Committee",
         UUID.fromString("005d1366-7865-5055-9cd7-2b40a0396326"));
   public static ConceptProxy INFORMATICS_INC_COPYRIGHT = new ConceptProxy("© Informatics, Incorporated", "© Informatics, Inc.",
         UUID.fromString("f892783f-4aa4-5ba8-a0bf-8a99c4149155"));

   // SNOMED CT High Level Taxonomy Classes
   public static ConceptProxy BODY_STRUCTURE = new ConceptProxy("Body structure", "Body structure", UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790"));
   public static ConceptProxy EVENT = new ConceptProxy("Event", "Event", UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698"));
   public static ConceptProxy FORCE = new ConceptProxy("Force", "Force", UUID.fromString("32213bf6-c073-3ce1-b0c7-9463e43af2f1"));
   public static ConceptProxy MEDICATION = new ConceptProxy("Medication", "Medication", UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449"));
   public static ConceptProxy PHENOMENON = new ConceptProxy("Phenomenon", "Phenomenon", UUID.fromString("c2e8bc47-3353-5e02-b0d1-2a5916efed4d"));
   public static ConceptProxy ORGANISM = new ConceptProxy("Organism", "Organism", UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072"));
   public static ConceptProxy OBJECT = new ConceptProxy("Object", "Object", UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));
   public static ConceptProxy PROCEDURE = new ConceptProxy("Procedure", "Procedure", UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b"));
   public static ConceptProxy SPECIMEN = new ConceptProxy("Specimen", "Specimen", UUID.fromString("3680e12d-c14c-39cb-ac89-2ae1fa125d41"));
   public static ConceptProxy SUBSTANCE = new ConceptProxy("Substance", "Substance", UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));

   
    public static ConceptProxy MODE = new ConceptProxy("Mode", "Mode", UUID.fromString("ea584999-1ddd-583d-af7d-c337c1b4c1b8"));
    public static ConceptProxy INSTANCE = new ConceptProxy("Instance mode", "Instance", UUID.fromString("fa0b8eeb-374c-5a31-a5b4-b6334abe31f5"));
    public static ConceptProxy TEMPLATE = new ConceptProxy("Template mode", "Template", UUID.fromString("727dd2af-4cca-5cdb-b776-75dc7f4c1733"));

    public static ConceptProxy STATEMENT_TYPE = new ConceptProxy("Type of statement", "Statement type", UUID.fromString("eefc0031-43b0-5eed-aec4-9fe9ed66c624"));
    public static ConceptProxy REQUEST_STATEMENT = new ConceptProxy("Request statement", "Request", UUID.fromString("2e2a53ac-a1bc-5eca-bef5-56fa31ee7ea7"));
    public static ConceptProxy PERFORMANCE_STATEMENT = new ConceptProxy("Performance statement", "Performance", UUID.fromString("1c0d30a0-b7aa-5b2a-b295-4cd5c68ab4ec"));

    public static ConceptProxy SUBJECT_OF_INFORMATION = new ConceptProxy("Subject of information", "Subject of information", UUID.fromString("4aebb90a-e361-5d07-b5e2-2250b7d8b60d"));
    public static ConceptProxy SUBJECT_OF_RECORD = new ConceptProxy("Subject of record", "Subject of record", UUID.fromString("8fbaaefc-e7df-5cd3-8f91-788cbb4997c9"));
    public static ConceptProxy MATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD = new ConceptProxy("Maternal ancestor of subject of record", "Maternal ancestor", UUID.fromString("bb34ccb6-857d-5cbd-8114-8c34a14b1e42"));
    public static ConceptProxy MOTHER_OF_SUBJECT_OF_RECORD = new ConceptProxy("Mother of subject of record", "Mother", UUID.fromString("270737bc-2454-5bb5-b9eb-c142f2cc6004"));
    public static ConceptProxy FATHER_OF_SUBJECT_OF_RECORD = new ConceptProxy("Father of subject of record", "Father", UUID.fromString("eada0aea-a14c-549d-89c8-9aa48aa6d184"));
    public static ConceptProxy PATERNAL_ANCESTOR_OF_SUBJECT_OF_RECORD = new ConceptProxy("Paternal ancestor of subject of record", "Paternal ancestor", UUID.fromString("e6ec4948-9167-5c16-9698-ab2747e5cdf3"));

    public static ConceptProxy ISO_8601 = new ConceptProxy("ISO 8601 representation of dates and times", "ISO 8601", UUID.fromString("38baca53-e626-5196-91a5-76e05cb3e115"));
    public static ConceptProxy ISO_8601_AFTER = new ConceptProxy("ISO 8601 interval after statement time", "ISO 8601 after statement", UUID.fromString("53fb2295-49f9-58ec-8d71-167974e70eae"));
    public static ConceptProxy ISO_8601_PRIOR = new ConceptProxy("ISO 8601 interval prior to statement time", "ISO 8601 before statement", UUID.fromString("0b7d4e60-7ef5-57fa-ad0e-ee13433b7ee1"));

    public static ConceptProxy UNINITIALIZED_COMPONENT_ID = new ConceptProxy("Uninitialized component", "Uninitialized", UUID.fromString("55f74246-0a25-57ac-9473-a788d08fb656"));
    
    public static ConceptProxy TEXT_COMPARISON_SEMANTIC = new ConceptProxy("Text comparison measure semantic", "Text comparison", UUID.fromString("b1531e68-4e7a-5194-b1f9-9aaace269372"));
    public static ConceptProxy CASE_SENSITIVE_EVAL = new ConceptProxy("Case sensitive evaluation", "Compare case", UUID.fromString("a95e5dbc-a179-57f9-9cdd-6de8c026396d"));
    public static ConceptProxy CASE_INSENSITIVE_EVAL = new ConceptProxy("Case insensitive evaluation", "Ignore case", UUID.fromString("74bbdaff-f061-5807-b334-3c88ac3e9421"));
    public static ConceptProxy UNICODE_OPERATION_EVAL = new ConceptProxy("Unicode evaluation", "Unicode", UUID.fromString("977e682d-9611-5316-9791-f349b1d10fed"));

    public static ConceptProxy DISCRETE_MEASURE_SEMANTICS = new ConceptProxy("Discrete measure semantic", "Item measurement", UUID.fromString("2bd0936f-62dd-5425-bc38-1eca8abb8242"));
    public static ConceptProxy ITEM_COUNT = new ConceptProxy("Item count", "Items", UUID.fromString("ea3c3d2a-4a76-5441-90f8-a8caa4903a3f"));
    public static ConceptProxy PRESSURE_MEASURE_SEMANTICS = new ConceptProxy("Pressure measure semantic", "Pressure measurement", UUID.fromString("62728a7c-7546-5f70-83c4-2859e08dd9be"));
    public static ConceptProxy MM_HG = new ConceptProxy("Milimeters of mercury", "mm Hg", UUID.fromString("713876c4-a832-5302-8baa-41cd7e7bcd2d"));
    public static ConceptProxy TIME_MEASUREMENT_SEMANTIC = new ConceptProxy("Time measurement semantic", "Time measurement", UUID.fromString("6bd7c916-2bf1-5ae4-996a-6390074bf27f"));
    public static ConceptProxy EXISTENTIAL_MEASUREMENT_SEMANTIC = new ConceptProxy("Existential measurement semantic", "Existential measurement", UUID.fromString("57e1643b-da06-5684-a2ef-044727c25b81"));

    public static ConceptProxy MILLIGRAM = new ConceptProxy("milligram", "mg", UUID.fromString("64b14d8e-5893-5927-a165-28d7ea0a1357"));
    public static ConceptProxy MASS_MEASUREMENT_SEMANTIC = new ConceptProxy("Mass measurement semantic", "Mass measurement", UUID.fromString("cda282b6-5c4f-539e-ad88-64f88a61263e"));

    public static ConceptProxy RXNORM_CUI = new ConceptProxy("RxNorm CUI", "RxNorm CUI", UUID.fromString("492b1a88-dbce-56a0-a405-6c7742f3be86"));

    public static ConceptProxy SRF_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE = new ConceptProxy("SRF legacy relationship implication assemblage",
            "SRF legacy relationship implication", UUID.fromString("d16114d4-5df9-58d9-bafd-216bf336cf18"));
    public static ConceptProxy SRF_INFERRED_RELATIONSHIP_ASSEMBLAGE = new ConceptProxy("SRF inferred relationship assemblage", "SRF inferred relationships",
            UUID.fromString("b218d92d-df8e-52d2-9214-c84a64862a23"));
    public static ConceptProxy SRF_STATED_RELATIONSHIP_ASSEMBLAGE = new ConceptProxy("SRF stated relationship assemblage", "SRF stated relationships",
            UUID.fromString("c2b97bad-d5e0-5eb5-ba13-b9e6824411e0"));

    public static ConceptProxy SEMANTIC_TYPE = new ConceptProxy("Semantic type", UUID.fromString("3daac6c4-78c5-5271-9c63-6e28f80e0c52"));
    public static ConceptProxy MEMBERSHIP_SEMANTIC = new ConceptProxy("Membership semantic", UUID.fromString("4fa29287-a80e-5f83-abab-4b587973e7b7"));
    public static ConceptProxy DYNAMIC_SEMANTIC = new ConceptProxy("Dynamic semantic", UUID.fromString("8ed01f85-4ecc-5a40-8061-d537106d9c9e"));
    public static ConceptProxy CONCEPT_SEMANTIC = new ConceptProxy("Concept semantic", UUID.fromString("fbf054fb-ceaf-5ab8-b946-bbcc4835ce07"));
    public static ConceptProxy COMPONENT_SEMANTIC = new ConceptProxy("Component semantic", UUID.fromString("127e7274-0b88-5519-a9db-85d4b9ce6a4a"));
    public static ConceptProxy LOGICAL_EXPRESSION_SEMANTIC = new ConceptProxy("Logical expression semantic", UUID.fromString("d19306b1-4744-5028-a715-17ca4a4d657f"));
    public static ConceptProxy INTEGER_SEMANTIC = new ConceptProxy("Integer semantic", UUID.fromString("a248fe51-70db-5a3f-9d23-9cf20afa2b4d"));
    public static ConceptProxy STRING_SEMANTIC = new ConceptProxy("String semantic", UUID.fromString("e196e48a-760b-5cd3-b5f0-8e5b3bb49627"));
    public static ConceptProxy DESCRIPTION_SEMANTIC = new ConceptProxy("Description semantic", UUID.fromString("81487d5f-6115-51e2-a3b3-93d783888eb8"));
    public static ConceptProxy IMAGE_SEMANTIC = new ConceptProxy("Image semantic", UUID.fromString("5c31cb70-a042-59b8-a21c-6aca1c03f907"));
    public static ConceptProxy SEMANTIC_FIELD_TYPE = new ConceptProxy("Semantic field type", UUID.fromString("9c3dfc88-51e4-5e51-a59a-88dd580162b7"));
    public static ConceptProxy ARRAY_FIELD = new ConceptProxy("Array field", UUID.fromString("b168ad04-f814-5036-b886-fd4913de88c8"));
    public static ConceptProxy BOOLEAN_FIELD = new ConceptProxy("Boolean field", UUID.fromString("d6b9e2cc-31c6-5e80-91b7-7537690aae32"));
    public static ConceptProxy BYTE_ARRAY_FIELD = new ConceptProxy("Byte array field", UUID.fromString("dbdd8df2-aec3-596b-88fc-7b83b5594a45"));
    public static ConceptProxy DOUBLE_FIELD = new ConceptProxy("Double field", UUID.fromString("85ff6e8f-9151-5428-a5f0-e07844b69260"));
    public static ConceptProxy FLOAT_FIELD = new ConceptProxy("Float field", UUID.fromString("6efe7087-3e3c-5b45-8109-90d7652b1506"));
    public static ConceptProxy INTEGER_FIELD = new ConceptProxy("Integer field", UUID.fromString("ff59c300-9c4e-5e77-a35d-6a133eb3440f"));
    public static ConceptProxy LOGICAL_EXPRESSION_FIELD = new ConceptProxy("Logical expression field", UUID.fromString("c16eb414-8840-54f8-9bd2-e2f1ab37e19d"));
    public static ConceptProxy LONG_FIELD = new ConceptProxy("Long field", UUID.fromString("9574952e-6507-589f-b789-9e9c5d81e50b"));
    public static ConceptProxy COMPONENT_FIELD = new ConceptProxy("Component field", UUID.fromString("fb00d132-fcc3-5cbf-881d-4bcc4b4c91b3"));
    public static ConceptProxy CONCEPT_FIELD = new ConceptProxy("Concept field", UUID.fromString("ac8f1f54-c7c6-5fc7-b1a8-ebb04b918557"));
    public static ConceptProxy STRING_FIELD = new ConceptProxy("String field", UUID.fromString("8d0fdf86-9c18-50b4-b59f-fb83db9cbcaf"));
    public static ConceptProxy IMAGE_FIELD = new ConceptProxy("Image field", UUID.fromString("cd9ea037-0af9-586b-9369-7bc044cdb8f7"));
    public static ConceptProxy POLYMORPHIC_FIELD = new ConceptProxy("Polymorphic field", UUID.fromString("9c3e4a52-bfa8-5f42-8fb1-3681f5a58ecb"));
    public static ConceptProxy UUID_FIELD = new ConceptProxy("UUID field", UUID.fromString("dea8cb0f-9bb5-56bb-af27-a14943cb24ba"));
    public static ConceptProxy ASSEMBLAGE_SEMANTIC_FIELDS = new ConceptProxy("Semantic fields assemblage", "Semantic fields", UUID.fromString("ac6d947d-384e-5293-a3b8-5f0c318ee0f7"));
    public static ConceptProxy SEMANTIC_FIELD_CONCEPTS = new ConceptProxy("Semantic field concepts", "Semantic field concepts", UUID.fromString("b4316cb8-14fe-5b32-b03b-f5f966c87819"));
    public static ConceptProxy SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE = new ConceptProxy("Semantic field data types assemblage", "Semantic data type", UUID.fromString("2fc4663f-c389-590e-9445-df02e277ddb1"));

    public static ConceptProxy METADATA_MODULES = new ConceptProxy("Metadata Modules", "Metadata Modules", UUID.fromString("04769bab-9ec6-5f79-aa0f-888a3ca8379c"));
    public static ConceptProxy PRIMORDIAL_MODULE = new ConceptProxy("Primordial module", "Primordial module", UUID.fromString("c2012321-3903-532e-8a5f-b13e4ca46e86"));
    public static ConceptProxy KOMET_MODULE = new ConceptProxy("KOMET module", "KOMET module", UUID.fromString("34a6dae3-e5e9-50db-a9ee-69c1067911d8"));


   public static ConceptProxy STATUS_FOR_VERSION = new ConceptProxy("Status for version", "Status for version", UUID.fromString("0608e233-d79d-5076-985b-9b1ea4e14b4c"));
   public static ConceptProxy TIME_FOR_VERSION = new ConceptProxy("Time for version", "Time for version", UUID.fromString("a9b0dfb2-f463-5dae-8ba8-7f2e8385571b"));
   public static ConceptProxy AUTHOR_FOR_VERSION = new ConceptProxy("Author for version", "Author", UUID.fromString("4eb9de0d-7486-5f18-a9b4-82e3432f4103"));
   public static ConceptProxy MODULE_FOR_VERSION = new ConceptProxy("Module for version", "Module", UUID.fromString("67cd64f1-96d7-5110-b847-556c055ac063"));
   public static ConceptProxy PATH_FOR_VERSION = new ConceptProxy("Path for version", "Path", UUID.fromString("ad3dd2dd-ddb0-584c-bea4-c6d9b91d461f"));
   public static ConceptProxy COMMITTED_STATE_FOR_VERSION = new ConceptProxy("Committed state for version", UUID.fromString("cc1e1cd7-0627-513d-aba6-f2a6a1326906"));
   public static ConceptProxy STAMP_SEQUENCE_FOR_VERSION = new ConceptProxy("Filter sequence for version", "Filter sequence for version", UUID.fromString("0b026997-e0c6-59b1-871f-8486710f7ac1"));
   public static ConceptProxy ASSEMBLAGE_NID_FOR_COMPONENT = new ConceptProxy("Assemblage nid for component", "Assemblage for component", UUID.fromString("3de7b91a-d384-5651-95eb-161f13cde990"));
   public static ConceptProxy REFERENCED_COMPONENT_NID_FOR_SEMANTIC = new ConceptProxy("Referenced component nid for semantic", "Referenced component id", UUID.fromString("a9ba4749-c11f-5f35-a991-21796fb89ddc"));
   public static ConceptProxy INACTIVE_STATUS  = new ConceptProxy("Inactive status", "Inactive", UUID.fromString("03004053-c23e-5206-8514-fb551dd328f4"));
   public static ConceptProxy ACTIVE_STATUS  = new ConceptProxy("Active status", "Active", UUID.fromString("09f12001-0e4f-51e2-9852-44862a4a0db4"));
   public static ConceptProxy PRIMORDIAL_STATUS = new ConceptProxy("Primordial status", "Primordial", UUID.fromString("b17bde5d-98ed-5416-97cf-2d837d75159d"));
   public static ConceptProxy CANCELED_STATUS = new ConceptProxy("Canceled status", "Canceled", UUID.fromString("b42c1948-7645-5da8-a888-de6ec020ab98"));
   public static ConceptProxy WITHDRAWN_STATUS = new ConceptProxy("Withdrawn status", "Withdrawn", UUID.fromString("35fd4750-6e43-5fa3-ba7f-f2ad376052bc"));

    public static ConceptProxy TIME_PRECEDENCE = new ConceptProxy("Time precedence", "Time precedence", UUID.fromString("cd5ccb52-9a7c-5e35-9d82-4d936bc3b086"));
    public static ConceptProxy PATH_PRECEDENCE = new ConceptProxy("Path precedence", "Path precedence", UUID.fromString("fba850b7-de84-5df2-ab0b-d1caa6a358ff"));

    public static ConceptProxy ORIGIN_STAMP_COORDINATE_KEY_FOR_MANIFOLD = new ConceptProxy("Origin Filter coordinate key for manifold", "Filter coordinate key", UUID.fromString("bb3bfeb0-5090-5512-9281-c2ce60927820"));
    public static ConceptProxy LANGUAGE_COORDINATE_KEY_FOR_MANIFOLD = new ConceptProxy("Language coordinate key for manifold", "Language coordiante key", UUID.fromString("2621ca26-8efb-56e2-9b1c-61313c40c27f"));
    public static ConceptProxy LOGIC_COORDINATE_KEY_FOR_MANIFOLD = new ConceptProxy("Logic coordinate key for manifold", "Logic coordinate key", UUID.fromString("300bf628-b40d-534f-9072-7b38c4f6f1b5"));
    public static ConceptProxy PREMISE_TYPE_FOR_MANIFOLD = new ConceptProxy("Premise type for manifold", "Premise type", UUID.fromString("7df6c6cb-9f91-5515-a05b-6b2886938363"));
    public static ConceptProxy REFLECTION_CLASS_ASSEMBLAGE = new ConceptProxy("Reflection class assemblage", "Reflection class", UUID.fromString("43f5bdcb-c902-5ea2-9ed7-2572fa468bae"));
    public static ConceptProxy PROVIDER_CLASS_ASSEMBLAGE = new ConceptProxy("Provider class assemblage", "Provider classs", UUID.fromString("8551edb2-6e34-52fe-9113-d25b742b303a"));
    public static ConceptProxy ANY_ASSEMBLAGE = new ConceptProxy("Any assemblage", "Any assemblage", UUID.fromString("6b8b1f01-9da2-585e-828c-eb1c7b93d250"));
    public static ConceptProxy MANIFOLD_COORDINATE_KEY = new ConceptProxy("Manifold coordinate key", "Manifold key", UUID.fromString("64d9dea8-aafd-5c8a-bce6-a208f91eb82e"));
    public static ConceptProxy VIEW_COORDINATE_KEY = new ConceptProxy("View coordinate key", "View key", UUID.fromString("4211cf36-bd75-586a-805c-51f059e2eaaa"));


    public static ConceptProxy CONCEPT_HAS_TAXONOMY_DISTANCE_FROM = new ConceptProxy("Concept has taxonomy distance from", "Within taxonomy distance", UUID.fromString("9533dce4-efde-51a3-94f8-a4fb06b9d08c"));
    public static ConceptProxy INTEGER_REFERENCE = new ConceptProxy("Integer reference", "Integer reference", UUID.fromString("11d47ca2-4207-5aaa-a068-196038aeee4c"));
    public static ConceptProxy BOOLEAN_REFERENCE = new ConceptProxy("Boolean reference", "Boolean reference", UUID.fromString("de49d207-a26e-5f8a-b905-953a4dd13c21"));

    public static ConceptProxy EL_PLUS_PLUS_DIGRAPH = new ConceptProxy("EL++ digraph", "EL++ digraph", UUID.fromString("ee04d7db-3407-568f-9b93-7b1f9f5bb0fc"));

    public static ConceptProxy SANDBOX_COMPONENT = new ConceptProxy("Sandbox component", "Sandbox component", UUID.fromString("c93829b2-aa78-5a84-ac9a-c34307844166"));
    public static ConceptProxy SANDBOX_PATH = new ConceptProxy("Sandbox path", "Sandbox path", UUID.fromString("80710ea6-983c-5fa0-8908-e479f1f03ea9"));
    public static ConceptProxy SANDBOX_MODULE = new ConceptProxy("Sandbox module", "Sandbox module", UUID.fromString("c5daf0e9-30dc-5b3e-a521-d6e6e72c8a95"));
    public static ConceptProxy SANDBOX_PATH_MODULE = new ConceptProxy("Sandbox path module", "Sandbox path module", UUID.fromString("715bd36d-6090-5b37-8ae7-88c9e532010e"));

    public static ConceptProxy PRIMORDIAL_PATH = new ConceptProxy("Primordial path", "Primordial path", UUID.fromString("e95b6718-f824-5540-817b-8e79544eb97a"));
    public static ConceptProxy FOUNDATION_MODULE = new ConceptProxy("Solor foundation module", "Foundation module", UUID.fromString("676a8e10-f75a-5574-a493-3a95aef6ec35"));
    public static ConceptProxy DEVELOPMENT_MODULE = new ConceptProxy("Development module", "Development module", UUID.fromString("529a7069-bd33-59e6-b2ce-537fa874360a"));

    public static ConceptProxy DEPENDENCY_MANAGEMENT = new ConceptProxy("Dependency management assemblage", "Dependency managment", UUID.fromString("b1dbb86b-e283-549e-ba94-5cb7dc3190c1"));

    /*
public static ConceptProxy SPEC95 = new ConceptProxy("", "", UUID.fromString("4f473e84-5f44-5ece-9fdd-cf6cec19b488"));
    */
   // J+

   // ~--- methods -------------------------------------------------------------

   /**
    * Case significance to concept nid.
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
     * Gets the concept specification for language nid.
     *
     * @param languageConceptNid the language concept nid
     * @return the concept specification for language nid
     */
    public static ConceptProxy getConceptSpecificationForLanguageNid(int languageConceptNid) {
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

    public static ConceptProxy[] getAllSpecs() {
        ArrayList<ConceptProxy> items = new ArrayList<>();

        try {
            for (Field f : TermAux.class.getFields()) {
                if (f.getType().equals(ConceptProxy.class)) {
                    items.add((ConceptProxy) f.get(null));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error");
        }
        return items.toArray(new ConceptProxy[items.size()]);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {

            StringBuilder builder = new StringBuilder();
            builder.append("public static ConceptProxy SPEC");
            builder.append(i);
            builder.append(" = new ConceptProxy(\"\", \"\", UUID.fromString(\"");
            builder.append(UuidT5Generator.get(UUID.randomUUID().toString()));
            builder.append("\"));");

            System.out.println(builder.toString());
        }
    }

    @Override
    public void reset() {
        LogManager.getLogger().info("Clearing cached nids in static metadata from TermAux");
        for (ConceptProxy cs : getAllSpecs()) {
            cs.clearCache();
        }
    }
}
