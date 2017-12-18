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



package sh.isaac.model.observable;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.UuidT5Generator;

//~--- enums ------------------------------------------------------------------

/**
 * The Enum ObservableFields.
 *
 * @author kec
 */
public enum ObservableFields
         implements ConceptSpecification {
   /** The author nid for edit coordinate. */
   AUTHOR_NID_FOR_EDIT_COORDINATE("author nid for edit coordinate"),

   /** The module nid for edit coordinate. */
   MODULE_NID_FOR_EDIT_COORDINATE("module nid for edit coordinate"),

   /** The path nid for edit coordinate. */
   PATH_NID_FOR_EDIT_CORDINATE("path nid for edit cordinate"),

   /** The language nid for language coordinate. */
   LANGUAGE_NID_FOR_LANGUAGE_COORDINATE("language nid for language coordinate"),

   /** The dialect assemblage nid preference list for language coordinate. */
   DIALECT_ASSEMBLAGE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
      "dialect assemblage nid preference list for language coordinate"),

   /** The description type nid preference list for language coordinate. */
   DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
      "description type nid preference list for language coordinate"),

   /** The stated assemblage nid for logic coordinate. */
   STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE("stated assemblage nid for logic coordinate"),

   /** The inferred assemblage nid for logic coordinate. */
   INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE("inferred assemblage nid for logic coordinate"),

   /** The description logic profile nid for logic coordinate. */
   DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE("description logic profile nid for logic coordinate"),

   /** The classifier nid for logic coordinate. */
   CLASSIFIER_NID_FOR_LOGIC_COORDINATE("classifier nid for logic coordinate"),

   /** The stamp precedence for stamp coordinate. */
   STAMP_PRECEDENCE_FOR_STAMP_COORDINATE("stamp precedence for stamp coordinate"),

   /** The stamp position for stamp coordinate. */
   STAMP_POSITION_FOR_STAMP_COORDINATE("stamp position for stamp coordinate"),

   /** The module nid array for stamp coordinate. */
   MODULE_NID_ARRAY_FOR_STAMP_COORDINATE("module nid array for stamp coordinate"),

   /** The allowed states for stamp coordinate. */
   ALLOWED_STATES_FOR_STAMP_COORDINATE("allowed states for stamp coordinate"),

   /** The path nid for stamp path. */
   PATH_NID_FOR_STAMP_PATH("path nid for stamp path"),

   /** The path origin list for stamp path. */
   PATH_ORIGIN_LIST_FOR_STAMP_PATH("path origin list for stamp path"),

   /** The time for stamp position. */
   TIME_FOR_STAMP_POSITION("time for stamp position"),

   /** The path nid for stamp position. */
   PATH_NID_FOR_STAMP_POSITION("path nid for stamp position"),

   /** The premise type for taxonomy coordinate. */
   PREMISE_TYPE_FOR_TAXONOMY_COORDINATE("premise type for taxonomy coordinate"),

   /** The stamp coordinate for taxonomy coordinate. */
   STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE("stamp coordinate for taxonomy coordinate"),

   /** The language coordinate for taxonomy coordinate. */
   LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE("language coordinate for taxonomy coordinate"),

   /** The logic coordinate for taxonomy coordinate. */
   LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE("logic coordinate for taxonomy coordinate"),

   /** The uuid for taxonomy coordinate. */
   UUID_FOR_TAXONOMY_COORDINATE("uuid for taxonomy coordinate"),

   /** The status for version. */
   STATUS_FOR_VERSION("status for version"),

   /** The time for version. */
   TIME_FOR_VERSION("time for version"),

   /** The author nid for version. */
   AUTHOR_NID_FOR_VERSION("author nid for version"),

   /** The module nid for version. */
   MODULE_NID_FOR_VERSION("module nid for version"),

   /** The path nid for version. */
   PATH_NID_FOR_VERSION("path nid for version"),

   /** The committed state for version. */
   COMMITTED_STATE_FOR_VERSION("committed state for version"),

   /** The stamp nid for version. */
   STAMP_SEQUENCE_FOR_VERSION("stamp sequence for version"),

   /** The case significance concept nid for description. */
   CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION("case significance concept nid for description"),

   /** The language concept nid for description. */
   LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION("language concept nid for description"),

   /** The text for description. */
   TEXT_FOR_DESCRIPTION("text for description"),

   /** The description type for description. */
   DESCRIPTION_TYPE_FOR_DESCRIPTION("description type for description"),

   /** The version list for chronicle. */
   VERSION_LIST_FOR_CHRONICLE("version list for chronicle"),

   /** The native id for chronicle. */
   NATIVE_ID_FOR_CHRONICLE("native id for chronicle"),

   /** The entry sequence for chronicle. */
   ENTRY_SEQUENCE_FOR_CHRONICLE("entry sequence for chronicle"),

   /** The assemblage nid for chronicle. */
   ASSEMBLAGE_NID_FOR_CHRONICLE("assemblage nid for chronicle"),

   /** The referenced component nid for semantic chronicle. */
   REFERENCED_COMPONENT_NID_FOR_SEMANTIC_CHRONICLE("referenced component nid for semantic chronicle"),

   /** The primordial uuid for chronicle. */
   PRIMORDIAL_UUID_FOR_CHRONICLE("primordial UUID for chronicle"),

   /** The uuid list for chronicle. */
   UUID_LIST_FOR_CHRONICLE("UUID list for chronicle"),

   /** The committed state for chronicle. */
   COMMITTED_STATE_FOR_CHRONICLE("committed state for chronicle"),

   /** The semantic list for chronicle. */
   SEMANTIC_LIST_FOR_CHRONICLE("semantic list for chronicle"),

   /** The description list for concept. */
   DESCRIPTION_LIST_FOR_CONCEPT("description list for concept"),
   
   STRING_VALUE_FOR_SEMANTIC("string value for semantic"),
   
   COMPONENT_NID_FOR_SEMANTIC("component nid for semantic"),
   
   LOGIC_GRAPH_FOR_SEMANTIC("logic graph for semantic"),
   
   LONG_VALUE_FOR_SEMANTIC("long value for semantic"),
   
   TYPE_NID_FOR_RF2_REL("type nid for rf2 relationship"),
   
   DESTINATION_NID_FOR_RF2_REL("destination nid for rf2 relationship"),
   
   REL_GROUP_FOR_RF2_REL("relationship group for rf2 relationship"),
   
   CHARACTERISTIC_NID_FOR_RF2_REL("characteristic nid for rf2 relationship"),
   
   MODIFIER_NID_FOR_RF2_REL("modifier nid for rf2 relationship"),
   
   NID1("Refset nid1"),
   NID2("Refset nid2"),
   NID3("Refset nid3"),
   NID4("Refset nid4"),
   NID5("Refset nid5"),
   NID6("Refset nid6"),
   NID7("Refset nid7"),
   STR1("Refset str1"),
   STR2("Refset str2"),
   STR3("Refset str3"),
   STR4("Refset str4"),
   STR5("Refset str5"),
   STR6("Refset str6"),
   STR7("Refset str7"),
   INT1("Refset int1"),
   INT2("Refset int2"),
   INT3("Refset int3"),
   INT4("Refset int4"),
   INT5("Refset int5"),
   INT6("Refset int6"),
   INT7("Refset int7")
           
         
;
   /** The Constant namespace. */
   private static final UUID namespace = UUID.fromString("cbbd1e22-0cac-11e5-a6c0-1697f925ec7b");

   //~--- fields --------------------------------------------------------------

   /** The description. */
   String description;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable fields.
    *
    * @param description the description
    */
   ObservableFields(String description) {
      this.description = description;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To external string.
    *
    * @return the string
    */
   @Override
   public String toExternalString() {
      final StringBuilder sb = new StringBuilder();

      sb.append(this.description);
      sb.append(FIELD_SEPARATOR)
        .append(getUuid().toString());
      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept description text.
    *
    * @return the concept description text
    */
   @Override
   public String getFullySpecifiedConceptDescriptionText() {
      return getDescription();
   }

   /**
    * Gets the description.
    *
    * @return the description
    */
   public String getDescription() {
      return this.description;
   }

   /**
    * Gets the uuid.
    *
    * @return the uuid
    */
   public UUID getUuid() {
      return UuidT5Generator.get(namespace, name());
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      return Arrays.asList(new UUID[] { getUuid() });
   }

   @Override
   public Optional<String> getPreferedConceptDescriptionText() {
      return Optional.empty();
   }

}

