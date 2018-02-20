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
   AUTHOR_NID_FOR_EDIT_COORDINATE("Author nid for edit coordinate", "Author"),

   /** The module nid for edit coordinate. */
   MODULE_NID_FOR_EDIT_COORDINATE("Module nid for edit coordinate", "Module"),

   /** The path nid for edit coordinate. */
   PATH_NID_FOR_EDIT_CORDINATE("Path nid for edit cordinate", "Path"),

   /** The language nid for language coordinate. */
   LANGUAGE_NID_FOR_LANGUAGE_COORDINATE("Language nid for language coordinate", "Language"),

   /** The dialect assemblage nid preference list for language coordinate. */
   DIALECT_ASSEMBLAGE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
      "Dialect assemblage nid preference list for language coordinate", "Dialect preferences"),
   
   NEXT_PRIORITY_LANGUAGE_COORDINATE(
      "Next priority language coordinate", "Next coordinate"),

   /** The description type nid preference list for language coordinate. */
   DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE(
      "Description type nid preference list for language coordinate", "Type preferences"),

   /** The stated assemblage nid for logic coordinate. */
   STATED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE("Stated assemblage nid for logic coordinate", "Stated assemblage"),

   /** The inferred assemblage nid for logic coordinate. */
   INFERRED_ASSEMBLAGE_NID_FOR_LOGIC_COORDINATE("Inferred assemblage nid for logic coordinate", "Inferred assemblage"),

   /** The description logic profile nid for logic coordinate. */
   DESCRIPTION_LOGIC_PROFILE_NID_FOR_LOGIC_COORDINATE("Description logic profile nid for logic coordinate", "Logic profile"),

   /** The classifier nid for logic coordinate. */
   CLASSIFIER_NID_FOR_LOGIC_COORDINATE("Classifier nid for logic coordinate", "Classifier"),

   /** The stamp precedence for stamp coordinate. */
   STAMP_PRECEDENCE_FOR_STAMP_COORDINATE("Stamp precedence for stamp coordinate", "Precedence"),

   /** The stamp position for stamp coordinate. */
   STAMP_POSITION_FOR_STAMP_COORDINATE("Stamp position for stamp coordinate", "Stamp position"),

   /** The module nid array for stamp coordinate. */
   MODULE_NID_ARRAY_FOR_STAMP_COORDINATE("Module nid array for stamp coordinate", "Modules"),

   /** The allowed states for stamp coordinate. */
   ALLOWED_STATES_FOR_STAMP_COORDINATE("Allowed states for stamp coordinate", "Allowed states"),

   /** The path nid for stamp path. */
   PATH_NID_FOR_STAMP_PATH("Path nid for stamp path", "Path"),

   /** The path origin list for stamp path. */
   PATH_ORIGIN_LIST_FOR_STAMP_PATH("Path origin list for stamp path", "Path origin"),

   /** The time for stamp position. */
   TIME_FOR_STAMP_POSITION("Time for stamp position", "Time"),

   /** The path nid for stamp position. */
   PATH_NID_FOR_STAMP_POSITION("Path nid for stamp position", "Path"),

   /** The premise type for taxonomy coordinate. */
   PREMISE_TYPE_FOR_TAXONOMY_COORDINATE("Premise type for taxonomy coordinate", "Premise"),

   /** The stamp coordinate for taxonomy coordinate. */
   STAMP_COORDINATE_FOR_TAXONOMY_COORDINATE("Stamp coordinate for taxonomy coordinate", "Stamp coordinate"),

   /** The language coordinate for taxonomy coordinate. */
   LANGUAGE_COORDINATE_FOR_TAXONOMY_COORDINATE("Language coordinate for taxonomy coordinate", "Language coordinate"),

   /** The logic coordinate for taxonomy coordinate. */
   LOGIC_COORDINATE_FOR_TAXONOMY_COORDINATE("Logic coordinate for taxonomy coordinate", "Logic coordinate"),

   /** The uuid for taxonomy coordinate. */
   UUID_FOR_TAXONOMY_COORDINATE("UUID for taxonomy coordinate", "Coordinate UUID"),

   /** The status for version. */
   STATUS_FOR_VERSION("Status for version", "Status"),

   /** The time for version. */
   TIME_FOR_VERSION("Time for version", "Time"),

   /** The author nid for version. */
   AUTHOR_NID_FOR_VERSION("Author nid for version", "Author"),

   /** The module nid for version. */
   MODULE_NID_FOR_VERSION("Module nid for version", "Module"),

   /** The path nid for version. */
   PATH_NID_FOR_VERSION("Path nid for version", "Path"),

   /** The committed state for version. */
   COMMITTED_STATE_FOR_VERSION("Committed state for version", "Committed state"),

   /** The stamp nid for version. */
   STAMP_SEQUENCE_FOR_VERSION("Stamp sequence for version", "Stamp"),

   /** The case significance concept nid for description. */
   CASE_SIGNIFICANCE_CONCEPT_NID_FOR_DESCRIPTION("Case significance concept nid for description", "Case significance"),

   /** The language concept nid for description. */
   LANGUAGE_CONCEPT_NID_FOR_DESCRIPTION("Language concept nid for description", "Language"),

   /** The text for description. */
   TEXT_FOR_DESCRIPTION("Text for description", "Text"),

   /** The description type for description. */
   DESCRIPTION_TYPE_FOR_DESCRIPTION("Description type for description", "Description type"),

   /** The version list for chronicle. */
   VERSION_LIST_FOR_CHRONICLE("Version list for chronicle", "Versions"),

   /** The native id for chronicle. */
   NATIVE_ID_FOR_CHRONICLE("Native id for chronicle", "Nid"),

   /** The entry sequence for chronicle. */
   ENTRY_SEQUENCE_FOR_CHRONICLE("Entry sequence for chronicle", "Entry ID"),

   /** The assemblage nid for chronicle. */
   ASSEMBLAGE_NID_FOR_CHRONICLE("Assemblage nid for chronicle", "Assemblage"),

   /** The referenced component nid for semantic chronicle. */
   REFERENCED_COMPONENT_NID_FOR_SEMANTIC_CHRONICLE("Referenced component nid for semantic chronicle", "Referenced component"),

   /** The primordial uuid for chronicle. */
   PRIMORDIAL_UUID_FOR_CHRONICLE("Primordial UUID for chronicle", "UUID"),

   /** The uuid list for chronicle. */
   UUID_LIST_FOR_CHRONICLE("UUID list for chronicle", "UUIDs"),

   /** The committed state for chronicle. */
   COMMITTED_STATE_FOR_CHRONICLE("Committed state for chronicle", "Committed state"),

   /** The semantic list for chronicle. */
   SEMANTIC_LIST_FOR_CHRONICLE("semantic list for chronicle"),

   /** The description list for concept. */
   DESCRIPTION_LIST_FOR_CONCEPT("description list for concept"),
   
   STRING_VALUE_FOR_SEMANTIC("String value for semantic", "String"),
   
   COMPONENT_NID_FOR_SEMANTIC("Component nid for semantic", "Component"),
   
   LOGIC_GRAPH_FOR_SEMANTIC("logic graph for semantic", "Logic graph"),
   
   LONG_VALUE_FOR_SEMANTIC("long value for semantic", "Long value"),
   
   TYPE_NID_FOR_RF2_REL("Type nid for rf2 relationship", "Rel type"),
   
   DESTINATION_NID_FOR_RF2_REL("Destination nid for rf2 relationship", "Rel destination"),
   
   REL_GROUP_FOR_RF2_REL("Relationship group for rf2 relationship", "Rel group"),
   
   CHARACTERISTIC_NID_FOR_RF2_REL("Characteristic nid for rf2 relationship", "Rel characteristic"),
   
   MODIFIER_NID_FOR_RF2_REL("modifier nid for rf2 relationship", "Rel modifier"),
   
   NID1("Component id 1", "Component 1"),
   NID2("Component id 2", "Component 2"),
   NID3("Component id 3", "Component 3"),
   NID4("Component id 4", "Component 4"),
   NID5("Component id 5", "Component 5"),
   NID6("Component id 6", "Component 6"),
   NID7("Component id 7", "Component 7"),
   STR1("String 1"),
   STR2("String 2"),
   STR3("String 3"),
   STR4("String 4"),
   STR5("String 5"),
   STR6("String 6"),
   STR7("String 7"),
   INT1("Integer 1"),
   INT2("Integer 2"),
   INT3("Integer 3"),
   INT4("Integer 4"),
   INT5("Integer 5"),
   INT6("Integer 6"),
   INT7("Integer 7"),
   
   CIRCUMSTANCE_PURPOSE_LIST("Action purpose"),
   CIRCUMSTANCE_TIMING("Timing"),
   
   STATEMENT_NARRATIVE("Statement narrative", "Narrative"),
   STATEMENT_TIME("Statement time", "Statement time"),
   STATEMENT_ID("Statement identifier"),
   STATEMENT_SOR("Statement subject of record", "Subject of record"),
   STATEMENT_AUTHORS("Statement authors", "Authors"),
   STATEMENT_SOI("Statement subject of information", "Subject"),
   STATEMENT_TYPE("Statement type", "Type"),
   STATEMENT_TOPIC("Statement topic", "Topic"),
   STATEMENT_CIRCUMSTANCE("Statement circumstance", "Circumstance"),
   STATEMENT_ASSOCIATIONS("Statement associations", "Associations"),
   
   INTERVAL_LOWER_BOUND("Lower bound"),
   INTERVAL_UPPER_BOUND("Upper bound"),
   INTERVAL_INCLUDE_UPPER_BOUND("Include upper bound"),
   INTERVAL_INCLUDE_LOWER_BOUND("Include lower bound"),
   
   INTERVENTION_RESULT_STATUS("Status"),
   
   MEASURE_RESOLUTION("Resolution"),
   MEASURE_SEMANTIC("Measurement semantic"),
   OBSERVATION_RESULT_HEALTH_RISK("Health risk"),
   MEASURE_NORMAL_RANGE("Normal range"),
   
   PARTICIPANT_ID("Participant id"),   
   PARTICIPANT_ROLE("Participant role"),
   
   PERFORMANCE_CIRCUMSTANCE_RESULT("Result"),
   PERFORMANCE_CIRCUMSTANCE_PARTICIPANTS("Participants"),
   
   REPETITION_PERIOD_START("Period start"),
   REPETITION_PERIOD_DURATION("Period duration"),
   REPETITION_EVENT_FREQUENCY("Event frequency"),
   REPETITION_EVENT_SEPARATION("Event separation"),
   REPETITION_EVENT_DURATION("Event duration"),
   
   REQUEST_CIRCUMSTANCE_CONDITIONAL_TRIGGERS("Conditional triggers"),
   REQUEST_CIRCUMSTANCE_REQUESTED_PARTICIPANTS("Requested participants"),
   REQUEST_CIRCUMSTANCE_PRIORITY("Priority"),
   REQUEST_CIRCUMSTANCE_REPETITIONS("Repetitions"),
   REQUEST_CIRCUMSTANCE_REQUESTED_RESULT("Requested result"),
   
   STATEMENT_ASSOCIATION_SEMANTIC("Association semantic"),
   STATEMENT_ASSOCIATION_ID("Association id"),
   
   UNSTRUCTURED_CIRCUMSTANCE_TEXT("Unstructured circumstance text", "Text"),
   
   STATEMENT_STAMP_COORDINATE("Stamp coordinate"),
   
   STATEMENT_MODE("Statement mode", "Mode"),
   
   LOINC_NUMBER("LOINC number"),
   LOINC_COMPONENT("LOINC component"),
   LOINC_PROPERTY("LOINC property"),
   LOINC_TIME_ASPECT("LOINC time aspect"),
   LOINC_SYSTEM("LOINC system"),
   LOINC_SCALE_TYPE("LOINC scale type"),
   LOINC_METHOD_TYPE("LOINC method type"),
   LOINC_STATUS("LOINC status"),
   LOINC_SHORT_NAME("LOINC short name"),
   LOINC_LONG_COMMON_NAME("LOINC long common name"),
;
   // this, ObservableFields..toExternalString()
   /** The Constant namespace. */
   private static final UUID namespace = UUID.fromString("cbbd1e22-0cac-11e5-a6c0-1697f925ec7b");

   //~--- fields --------------------------------------------------------------

   /** The description. */
   String fullyQualifiedDescription;
   String regularDescription;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable fields.
    *
    * @param description the description
    */
   ObservableFields(String fullyQualifiedDescription) {
      this.fullyQualifiedDescription = fullyQualifiedDescription;
      this.regularDescription = null;
   }
   ObservableFields(String fullyQualifiedDescription, String regularDescription) {
      this.fullyQualifiedDescription = fullyQualifiedDescription;
      this.regularDescription = regularDescription;
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

      sb.append(this.fullyQualifiedDescription);
      sb.append(FIELD_SEPARATOR)
        .append(getUuid().toString());
      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Actually calls {@link #getDescription()}
    * 
    * {@inheritDoc}
    */
   @Override
   public String getFullyQualifiedName() {
      return getDescription();
   }

   /**
    * Gets the description.
    *
    * @return the description
    */
   public String getDescription() {
      return this.fullyQualifiedDescription;
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
   public Optional<String> getRegularName() {
      return Optional.ofNullable(regularDescription);
   }

}

