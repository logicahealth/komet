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
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.UuidT5Generator;

import static sh.isaac.api.component.concept.ConceptSpecification.FIELD_SEPARATOR;

//~--- enums ------------------------------------------------------------------

/**
 * The Enum ObservableFields.
 *
 * @author kec
 */
public enum ObservableFields
         implements ConceptSpecification {
   /** The author sequence for edit coordinate. */
   AUTHOR_SEQUENCE_FOR_EDIT_COORDINATE("author sequence for edit coordinate"),

   /** The module sequence for edit coordinate. */
   MODULE_SEQUENCE_FOR_EDIT_COORDINATE("module sequence for edit coordinate"),

   /** The path sequence for edit cordinate. */
   PATH_SEQUENCE_FOR_EDIT_CORDINATE("path sequence for edit cordinate"),

   /** The language sequence for language coordinate. */
   LANGUAGE_SEQUENCE_FOR_LANGUAGE_COORDINATE("language sequence for language coordinate"),

   /** The dialect assemblage sequence preference list for language coordinate. */
   DIALECT_ASSEMBLAGE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
      "dialect assemblage sequence preference list for language coordinate"),

   /** The description type sequence preference list for language coordinate. */
   DESCRIPTION_TYPE_SEQUENCE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
      "description type sequence preference list for language coordinate"),

   /** The stated assemblage sequence for logic coordinate. */
   STATED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE("stated assemblage sequence for logic coordinate"),

   /** The inferred assemblage sequence for logic coordinate. */
   INFERRED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE("inferred assemblage sequence for logic coordinate"),

   /** The description logic profile sequence for logic coordinate. */
   DESCRIPTION_LOGIC_PROFILE_SEQUENCE_FOR_LOGIC_COORDINATE("description logic profile sequence for logic coordinate"),

   /** The classifier sequence for logic coordinate. */
   CLASSIFIER_SEQUENCE_FOR_LOGIC_COORDINATE("classifier sequence for logic coordinate"),

   /** The stamp precedence for stamp coordinate. */
   STAMP_PRECEDENCE_FOR_STAMP_COORDINATE("stamp precedence for stamp coordinate"),

   /** The stamp position for stamp coordinate. */
   STAMP_POSITION_FOR_STAMP_COORDINATE("stamp position for stamp coordinate"),

   /** The module sequence array for stamp coordinate. */
   MODULE_SEQUENCE_ARRAY_FOR_STAMP_COORDINATE("module sequence array for stamp coordinate"),

   /** The allowed states for stamp coordinate. */
   ALLOWED_STATES_FOR_STAMP_COORDINATE("allowed states for stamp coordinate"),

   /** The path sequence for stamp path. */
   PATH_SEQUENCE_FOR_STAMP_PATH("path sequence for stamp path"),

   /** The path origin list for stamp path. */
   PATH_ORIGIN_LIST_FOR_STAMP_PATH("path origin list for stamp path"),

   /** The time for stamp position. */
   TIME_FOR_STAMP_POSITION("time for stamp position"),

   /** The path sequence for stamp position. */
   PATH_SEQUENCE_FOR_STAMP_POSITION("path sequence for stamp position"),

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

   /** The author sequence for version. */
   AUTHOR_SEQUENCE_FOR_VERSION("author sequence for version"),

   /** The module sequence for version. */
   MODULE_SEQUENCE_FOR_VERSION("module sequence for version"),

   /** The path sequence for version. */
   PATH_SEQUENCE_FOR_VERSION("path sequence for version"),

   /** The committed state for version. */
   COMMITTED_STATE_FOR_VERSION("committed state for version"),

   /** The stamp sequence for version. */
   STAMP_SEQUENCE_FOR_VERSION("stamp sequence for version"),

   /** The case significance concept sequence for description. */
   CASE_SIGNIFICANCE_CONCEPT_SEQUENCE_FOR_DESCRIPTION("case significance concept sequence for description"),

   /** The language concept sequence for description. */
   LANGUAGE_CONCEPT_SEQUENCE_FOR_DESCRIPTION("language concept sequence for description"),

   /** The text for description. */
   TEXT_FOR_DESCRIPTION("text for description"),

   /** The description type for description. */
   DESCRIPTION_TYPE_FOR_DESCRIPTION("description type for description"),

   /** The version list for chronicle. */
   VERSION_LIST_FOR_CHRONICLE("version list for chronicle"),

   /** The native id for chronicle. */
   NATIVE_ID_FOR_CHRONICLE("native id for chronicle"),

   /** The concept sequence for chronicle. */
   CONCEPT_SEQUENCE_FOR_CHRONICLE("concept sequence for chronicle"),

   /** The sememe sequence for chronicle. */
   SEMEME_SEQUENCE_FOR_CHRONICLE("sememe sequence for chronicle"),

   /** The assemblage sequence for sememe chronicle. */
   ASSEMBLAGE_SEQUENCE_FOR_SEMEME_CHRONICLE("assemblage sequence for sememe chronicle"),

   /** The referenced component nid for sememe chronicle. */
   REFERENCED_COMPONENT_NID_FOR_SEMEME_CHRONICLE("referenced component nid for sememe chronicle"),

   /** The primordial uuid for chronicle. */
   PRIMORDIAL_UUID_FOR_CHRONICLE("primordial UUID for chronicle"),

   /** The uuid list for chronicle. */
   UUID_LIST_FOR_CHRONICLE("UUID list for chronicle"),

   /** The committed state for chronicle. */
   COMMITTED_STATE_FOR_CHRONICLE("committed state for chronicle"),

   /** The sememe list for chronicle. */
   SEMEME_LIST_FOR_CHRONICLE("sememe list for chronicle"),

   /** The description list for concept. */
   DESCRIPTION_LIST_FOR_CONCEPT("description list for concept");

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
   public String getConceptDescriptionText() {
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
}

