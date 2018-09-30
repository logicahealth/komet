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

package sh.isaac.api.constants;

//~--- JDK imports ------------------------------------------------------------

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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.UUID;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.IsaacCache;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- classes ----------------------------------------------------------------

/**
 * Constants relating to Dynamic Semantic Fields.
 *
 * @author darmbrust
 *
 *         Unfortunately, due to the use of the LookupService within this class - and the class itself being provided by a LookupService, we
 *         cannot create these constants as static - it leads to recursion in the LookupService init which breaks things.
 */
@Service
@Singleton
public class DynamicConstants implements ModuleProvidedConstants, IsaacCache {
   /** The cache. */
   private static DynamicConstants cache;

//J-

   /** The unknown concept. */
   public final UUID UNKNOWN_CONCEPT = UUID.fromString("00000000-0000-0000-C000-000000000046");

   /** The dynamic dt nid. */

   // Set up all of the data type columns
   public final MetadataConceptConstant DYNAMIC_DT_NID = new MetadataConceptConstant("nid", UUID.fromString("d1a17272-9785-51aa-8bde-cc556ab32ebb")) {
   };

   /** The dynamic dt boolean. */
   public final MetadataConceptConstant DYNAMIC_DT_BOOLEAN = new MetadataConceptConstant("boolean", UUID.fromString("08f2fb74-980d-5157-b92c-4ff1eac6a506")) {
   };

   /** The dynamic dt long. */
   public final MetadataConceptConstant DYNAMIC_DT_LONG = new MetadataConceptConstant("long", UUID.fromString("dea8cdf1-de75-5991-9791-79714e4a964d")) {
   };

   /** The dynamic dt byte array. */
   public final MetadataConceptConstant DYNAMIC_DT_BYTE_ARRAY = new MetadataConceptConstant("byte array", UUID.fromString("9a84fecf-708d-5de4-9c5f-e17973229e0f")) {
   };

   /** The dynamic dt float. */
   public final MetadataConceptConstant DYNAMIC_DT_FLOAT = new MetadataConceptConstant("float", UUID.fromString("fb591801-7b37-525d-980d-98a1c63ceee0")) {
   };

   /** The dynamic dt double. */
   public final MetadataConceptConstant DYNAMIC_DT_DOUBLE = new MetadataConceptConstant("double", UUID.fromString("7172e6ac-a05a-5a34-8275-aef430b18207")) {
   };

   /** The dynamic dt polymorphic. */
   public final MetadataConceptConstant DYNAMIC_DT_POLYMORPHIC = new MetadataConceptConstant("Polymorphic", UUID.fromString("3d634fd6-1498-5e8b-b914-e75b42018397")) {
   };

   /** The dynamic dt array. */
   public final MetadataConceptConstant DYNAMIC_DT_ARRAY = new MetadataConceptConstant("Array", UUID.fromString("318622e6-dd7a-5651-851d-2d5c2af85767")) {
   };

   /** The dynamic dt string. */

   // The following data types already exist, but I'm also adding them to our hierarchy for clarity
   public final MetadataConceptConstant DYNAMIC_DT_STRING = new MetadataConceptConstant("String", UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5")) {
   };

   /** The dynamic dt integer. */
   public final MetadataConceptConstant DYNAMIC_DT_INTEGER = new MetadataConceptConstant("Signed integer", UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c")) {
   };

   /** The dynamic dt uuid. */
   public final MetadataConceptConstant DYNAMIC_DT_UUID = new MetadataConceptConstant("Universally Unique Identifier", UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4")) {
   };

   /** The dynamic column data types. */
   public final MetadataConceptConstantGroup DYNAMIC_COLUMN_DATA_TYPES = new MetadataConceptConstantGroup("Dynamic column data types",
         UUID.fromString("61da7e50-f606-5ba0-a0df-83fd524951e7")) {
      {
         addChild(DynamicConstants.this.DYNAMIC_DT_NID);
         addChild(DynamicConstants.this.DYNAMIC_DT_BOOLEAN);
         addChild(DynamicConstants.this.DYNAMIC_DT_LONG);
         addChild(DynamicConstants.this.DYNAMIC_DT_BYTE_ARRAY);
         addChild(DynamicConstants.this.DYNAMIC_DT_FLOAT);
         addChild(DynamicConstants.this.DYNAMIC_DT_DOUBLE);
         addChild(DynamicConstants.this.DYNAMIC_DT_POLYMORPHIC);
         addChild(DynamicConstants.this.DYNAMIC_DT_ARRAY);
         addChild(DynamicConstants.this.DYNAMIC_DT_STRING);
         addChild(DynamicConstants.this.DYNAMIC_DT_INTEGER);
         addChild(DynamicConstants.this.DYNAMIC_DT_UUID);
      }
   };

   // Set up other metadata
   // used as salt for generating other UUIDs.
   public final MetadataConceptConstant DYNAMIC_NAMESPACE = new MetadataConceptConstant("Dynamic namespace", UUID.fromString("eb0c13ff-74fd-5987-88a0-6f5d75269e9d")) {
   };

   /** The dynamic column order. */

   // The seven column types we need for describing column types
   public final MetadataConceptConstant DYNAMIC_COLUMN_ORDER = new MetadataConceptConstant("Column order", UUID.fromString("8c501747-846a-5cea-8fd6-c9dd3dfc674f"),
         "Stores the column order of this column within a Dynamic Definition") {
   };

   /** The dynamic column name. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_NAME = new MetadataConceptConstant("Column name", UUID.fromString("89c0ded2-fd69-5654-a386-ded850d258a1"),
         "Stores the concept reference to the concept that defines the name of this column within a Dynamic Definition") {
   };

   /** The dynamic column type. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_TYPE = new MetadataConceptConstant("Column type", UUID.fromString("dbfd9bd2-b84f-574a-ab9e-64ba3bb94793"),
         "Stores the data type of this column within a Dynamic Definition") {
   };

   /** The dynamic column default value. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_DEFAULT_VALUE = new MetadataConceptConstant("Column default value", UUID.fromString("4d3e79aa-ab74-5858-beb3-15e0888986cb"),
         "Stores the (optional) default value of this column within a Dynamic Definition") {
   };

   /** The dynamic column required. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_REQUIRED = new MetadataConceptConstant("Column required", UUID.fromString("8a89ef19-bd5a-5e25-aa57-1172fbb437b6"),
         "Stores the (optional) flag to specify that this column is manditory within a Dynamic Definition") {
   };

   /** The dynamic column validator. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_VALIDATOR = new MetadataConceptConstant("Column validator", UUID.fromString("f295c3ba-d416-563d-8427-8b5d3e324192"),
         "Stores the (optional) validator type which will be applied to user supplied data of this column within a Dynamic Definition") {
   };

   /** The dynamic column validator data. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_VALIDATOR_DATA = new MetadataConceptConstant("Column validator data",
         UUID.fromString("50ea8378-8355-5a5d-bae2-ce7c10e92636"),
         "Stores the (optional) validator data which will be used by the validator to check the user input of this column within a Dynamic Definition") {
   };

   // used for index config
   public final MetadataConceptConstant DYNAMIC_COLUMN_COLUMNS_TO_INDEX = new MetadataConceptConstant("Columns to index", UUID.fromString("cede7677-3759-5dce-b28b-20a40fddf5d6"),
         "Contains an array of integers that denote the column positions within the referenced assemblage which should have their values indexed.") {
   };

   /** The dynamic column referenced component type. */

   // Used for referenced component type restrictions
   public final MetadataConceptConstant DYNAMIC_COLUMN_REFERENCED_COMPONENT_TYPE = new MetadataConceptConstant("Referenced component type restriction",
         UUID.fromString("902f97b6-2ef4-59d7-b6f9-01278a00061c"),
         "Stores the (optional) referenced component type restriction selection which will be used by the validator to check the user input for the "
               + "referenced component when creating an instance of a dynamic field") {
   };

   /** The dynamic column referenced component subtype. */

   // Used for referenced component sub-type restrictions
   public final MetadataConceptConstant DYNAMIC_COLUMN_REFERENCED_COMPONENT_SUBTYPE = new MetadataConceptConstant("Referenced component subtype restriction",
         UUID.fromString("8af1045e-1122-5072-9f29-ce7da9337915"),
         "Stores the (optional) referenced component type sub restriction selection which will be used by the validator to check the user input for the "
               + "referenced component when creating an instance of a dynamic field.") {
   };

   // Convenience column type for refex instances that just wish to attach a single column of data, and don't want to create another concept
   // to represent the column name. Typically only used when defining refexes where there is a single column of attached data (typically -
   // attaching an attribute,
   // the column represents the value, while the type of the attribute is represented by the refex type itself - so the column name isn't
   // really necessary)
   public final MetadataConceptConstant DYNAMIC_COLUMN_VALUE = new MetadataConceptConstant("Value", UUID.fromString("d94e271f-0e9b-5159-8691-6c29c7689ffb"),
         "The attached value of the semantic") {
   };

   /** The dynamic column editor comment. */

   // 2 columns for a comments semantic
   public final MetadataConceptConstant DYNAMIC_COLUMN_EDITOR_COMMENT = new MetadataConceptConstant("Editor comment", UUID.fromString("2b38b1a9-ce6e-5be2-8885-65cd76f40929"),
         "Stores the comment created by the editor") {
   };

   /** The dynamic column editor comment context. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_EDITOR_COMMENT_CONTEXT = new MetadataConceptConstant("Editor comment context",
         UUID.fromString("2e4187ca-ba45-5a87-8484-1f86801a331a"),
         "Stores an optional value that may be used to group comments, such as 'mapping comment' or 'assertion comment' which"
               + " then would allow programmatic filtering of comments to be context specific.") {
   };

   /** The dynamic column association target component. */

   // A column to store the target of an association within a semantic
   public final MetadataConceptConstant DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT = new MetadataConceptConstant("target", UUID.fromString("e598e12f-3d39-56ac-be68-4e9fca98fb7a"),
         "Stores the (optional) target concept or component of an association or mapping") {
   };

   /** The dynamic column business rules. */
   public final MetadataConceptConstant DYNAMIC_COLUMN_BUSINESS_RULES = new MetadataConceptConstant("Business rules", UUID.fromString("7ebc6742-8586-58c3-b49d-765fb5a93f35"),
         "Stores the business rules stored on a definition - specifically a map set definition, but can be used elsewhere") {
   };

   // parent concept for all of the column info
   // An organizational concept which serves as a parent concept for any column types that are defined
   // within the system.
   public final MetadataConceptConstantGroup DYNAMIC_COLUMNS = new MetadataConceptConstantGroup("Dynamic columns", UUID.fromString("46ddb9a2-0e10-586a-8b54-8e66333e9b77")) {
      {
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_ORDER);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_NAME);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_TYPE);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_DEFAULT_VALUE);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_REQUIRED);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_VALIDATOR);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_VALIDATOR_DATA);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_COLUMNS_TO_INDEX);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_REFERENCED_COMPONENT_TYPE);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_REFERENCED_COMPONENT_SUBTYPE);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_VALUE);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_EDITOR_COMMENT);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_EDITOR_COMMENT_CONTEXT);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_BUSINESS_RULES);
      }
   };

   // This is the assemblage type that is optionally attached to an assemblage itself, to declare type restrictions on the referenced component
   // of the semantic
   public final MetadataDynamicConstant DYNAMIC_REFERENCED_COMPONENT_RESTRICTION = new MetadataDynamicConstant("Dynamic referenced component restriction",
         UUID.fromString("0d94ceeb-e24f-5f1a-84b2-1ac35f671db5"),
         "This concept is used as an assemblage for defining new extensions.  It annotates other extensions to restrict the usage of a "
               + " semantic to a particular Component Type (Concept, Description, etc).  The attached data column specifies the allowed Component Type",
         new DynamicColumnInfo[] {
               new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_REFERENCED_COMPONENT_TYPE.getPrimordialUuid(), DynamicDataType.STRING, null, true,
                     new DynamicValidatorType[] { DynamicValidatorType.REGEXP },
                     new DynamicString[] { LookupService.getService(DynamicUtility.class)
                           .createDynamicStringData(IsaacObjectType.CONCEPT.name() + "|" + IsaacObjectType.SEMANTIC.name()) },
                     false),
               new DynamicColumnInfo(1, this.DYNAMIC_COLUMN_REFERENCED_COMPONENT_SUBTYPE.getPrimordialUuid(), DynamicDataType.STRING, null, false,
                     new DynamicValidatorType[] { DynamicValidatorType.REGEXP },
                     new DynamicString[] { LookupService.getService(DynamicUtility.class)
                           .createDynamicStringData(VersionType.COMPONENT_NID.name() + "|" + VersionType.DESCRIPTION.name() + "|" + VersionType.DYNAMIC.name() + "|"
                                 + VersionType.LOGIC_GRAPH.name() + "|" + VersionType.LONG.name() + "|" + VersionType.MEMBER.name() + "|" + VersionType.STRING.name()) },
                     false) }) {
   };

   // an organizational concept for all of the metadata concepts being added for dynamic field
   public final MetadataConceptConstantGroup DYNAMIC_METADATA = new MetadataConceptConstantGroup("Dynamic metadata", UUID.fromString("9769773c-7b70-523d-8fc5-b16621ffa57c")) {
      {
         addChild(DynamicConstants.this.DYNAMIC_NAMESPACE);
         addChild(DynamicConstants.this.DYNAMIC_COLUMNS);
         addChild(DynamicConstants.this.DYNAMIC_REFERENCED_COMPONENT_RESTRICTION);
         addChild(DynamicConstants.this.DYNAMIC_COLUMN_DATA_TYPES);

         setParent(TermAux.SOLOR_METADATA);

         // no parent, this can attach to root
      }
   };

   // Set up the dynamic fields that we require for dynamic fields themselves.
   // This is the assemblage type that is usually present on a concept when it is used as an assemblage itself to describe the attached data -
   // the attached
   // refex using this for an assemblage will describe a data column that is to be attached with the refex. This assemblage type wouldn't be
   // used if there was
   // no data to attach.
   public final MetadataDynamicConstant DYNAMIC_EXTENSION_DEFINITION = new MetadataDynamicConstant("Dynamic extension definition",
         UUID.fromString("406e872b-2e19-5f5e-a71d-e4e4b2c68fe5"),
         "This concept is used as an assemblage for defining new extensions.  " + "The attached data columns describe what columns are required to define a new Semantic.",
         new DynamicColumnInfo[] { new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_ORDER.getPrimordialUuid(), DynamicDataType.INTEGER, null, true, true),
               new DynamicColumnInfo(1, this.DYNAMIC_COLUMN_NAME.getPrimordialUuid(), DynamicDataType.UUID, null, true, true),
               new DynamicColumnInfo(2, this.DYNAMIC_COLUMN_TYPE.getPrimordialUuid(), DynamicDataType.STRING, null, true, true),
               new DynamicColumnInfo(3, this.DYNAMIC_COLUMN_DEFAULT_VALUE.getPrimordialUuid(), DynamicDataType.POLYMORPHIC, null, false, true),
               new DynamicColumnInfo(4, this.DYNAMIC_COLUMN_REQUIRED.getPrimordialUuid(), DynamicDataType.BOOLEAN, null, false, true),
               new DynamicColumnInfo(5, this.DYNAMIC_COLUMN_VALIDATOR.getPrimordialUuid(), DynamicDataType.ARRAY, null, false, DynamicValidatorType.REGEXP,
                     LookupService.getService(DynamicUtility.class)
                           .createDynamicStringData(DynamicValidatorType.COMPONENT_TYPE.name() + "|" + DynamicValidatorType.EXTERNAL.name() + "|"
                                 + DynamicValidatorType.GREATER_THAN.name() + "|" + DynamicValidatorType.GREATER_THAN_OR_EQUAL.name() + "|" + DynamicValidatorType.INTERVAL.name()
                                 + "|" + DynamicValidatorType.IS_CHILD_OF.name() + "|" + DynamicValidatorType.IS_KIND_OF.name() + "|" + DynamicValidatorType.LESS_THAN.name() + "|"
                                 + DynamicValidatorType.LESS_THAN_OR_EQUAL.name() + "|" + DynamicValidatorType.REGEXP.name()),
                     true),
               new DynamicColumnInfo(6, this.DYNAMIC_COLUMN_VALIDATOR_DATA.getPrimordialUuid(), DynamicDataType.ARRAY, null, false, true) },
         null) {
   };

   // This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an
   // assemblage concept for Dynamicrefexes. The description annotated with this type describes the intent of
   // using the concept containing the description as an assemblage concept.
   public final MetadataDynamicConstant DYNAMIC_DEFINITION_DESCRIPTION = new MetadataDynamicConstant("Dynamic definition description",
         UUID.fromString("b0372953-4f20-58b8-ad04-20c2239c7d4e"),
         "This is the extended description type that must be attached to a description within a concept to make the concept valid for use as an "
               + "assemblage concept for a dynamic field.  The description annotated with this type describes the intent of "
               + "using the concept containing the description as an assemblage concept.",
         new DynamicColumnInfo[0], null, null, IsaacObjectType.SEMANTIC, VersionType.DESCRIPTION) {
   };

   // This is the assemblage type that is used to record the current configuration of the Indexer for dynamic fields..
   // this is ALSO the concept used as the referenced component dynamic instances (of assemblage type itself) which define which other
   // dynamic fields should be indexed within the system.
   public final MetadataDynamicConstant DYNAMIC_INDEX_CONFIGURATION = new MetadataDynamicConstant("Dynamic index configuration",
         UUID.fromString("a5d187a7-3d95-5694-b2eb-a48d94cb0698"),
         "A Dynamic which contains the indexer configuration for dynamic fields within ISAAC.  "
               + "The referenced component ID will be the assemblage being configured for indexing.",
         new DynamicColumnInfo[] { new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_COLUMNS_TO_INDEX.getPrimordialUuid(), DynamicDataType.ARRAY, null, false, false) }, null) {
   };

   /** The dynamic comment attribute. */
   public final MetadataDynamicConstant DYNAMIC_COMMENT_ATTRIBUTE = new MetadataDynamicConstant("Comment", UUID.fromString("147832d4-b9b8-5062-8891-19f9c4e4760a"),
         "A component to store comments on arbitrary items (concepts, relationships, semantics, etc)",
         new DynamicColumnInfo[] { new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_EDITOR_COMMENT.getPrimordialUuid(), DynamicDataType.STRING, null, true, true),
               new DynamicColumnInfo(1, this.DYNAMIC_COLUMN_EDITOR_COMMENT_CONTEXT.getPrimordialUuid(), DynamicDataType.STRING, null, false, true) }) {
   }; // Index the comments, and the columns

   /**
    * see {@link #DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT}.
    */
   public final MetadataDynamicConstant DYNAMIC_ASSOCIATION = new MetadataDynamicConstant("Represents association", UUID.fromString("5252bafb-1ba7-5a35-b1a2-48d7a65fa477"),
         "A component used to annotate other components which define an association, which is defined as a which contains "
               + "a data column named 'target concept', among other criteria.",
         new DynamicColumnInfo[] {}, null) {
   };

   /** The dynamic association inverse name. */
   public final MetadataDynamicConstant DYNAMIC_ASSOCIATION_INVERSE_NAME = new MetadataDynamicConstant("Inverse name", UUID.fromString("c342d18a-ec1c-5583-bfe3-59e6324ae189"),
         "This is the extended description type that may be attached to a description within a concept that defines an Association Refex to signify that "
               + "the referenced description is the inverse of the association name.",
         new DynamicColumnInfo[0], new String[] {}, new String[] {}, IsaacObjectType.SEMANTIC, VersionType.DESCRIPTION) {
   };

   /** The dynamic extended description type. */
   public final MetadataDynamicConstant DYNAMIC_EXTENDED_DESCRIPTION_TYPE = new MetadataDynamicConstant("Extended description type",
         UUID.fromString("5a2e7786-3e41-11dc-8314-0800200c9a66"), "Used to store non-snomed description types when other terminologies are imported",
         new DynamicColumnInfo[] { new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.UUID, null, true, DynamicValidatorType.IS_KIND_OF,
               LookupService.getService(DynamicUtility.class).createDynamicUUIDData(TermAux.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY.getPrimordialUuid()), true) }) {
   };

   /** The dynamic extended relationship type. */
   public final MetadataDynamicConstant DYNAMIC_EXTENDED_RELATIONSHIP_TYPE = new MetadataDynamicConstant("Extended relationship type",
         UUID.fromString("d41d928f-8a97-55c1-aa6c-a289b413fbfd"),
         "Used to store non-snomed relationship types when other terminologies are imported - especially when a relationship is mapped onto a "
               + "snomed relationship type (such as isa)",
         new DynamicColumnInfo[] { new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.UUID, null, true, DynamicValidatorType.IS_KIND_OF,
               LookupService.getService(DynamicUtility.class).createDynamicUUIDData(TermAux.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY.getPrimordialUuid()), true) }) {
   };

   /** The dynamic external user id. */
   public final MetadataDynamicConstant DYNAMIC_EXTERNAL_USER_ID = new MetadataDynamicConstant("External user ID", UUID.fromString("00e6cca4-3c5b-5f2e-b2d8-2c4a6f8f6b46"),
         "Used to store an external user ID on a user/author concept",
         new DynamicColumnInfo[] { new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.STRING, null, true, true) }) {
   };
   
   /** The dynamic description core type is used to mark description types that come from non snomed terminologies
    * as the equivalent of one of the core types.  This information can then be utilized in constructing the language
    * ranking defaults - so when the user selects FQN or Regular Name in the GUI, we can pick the appropriate description
    * type to use as a substitute, when no FQN or Regular Name type is found. */
   public final MetadataDynamicConstant DYNAMIC_DESCRIPTION_CORE_TYPE = new MetadataDynamicConstant("Description core type", 
         UUID.fromString("351955ff-30f4-5806-a0a5-5dda79756377"),
         "Used to mark non-snomed descriptions as one of the core snomed types.",
         new DynamicColumnInfo[] { new DynamicColumnInfo(0, this.DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), DynamicDataType.UUID, 
               LookupService.getService(DynamicUtility.class).createDynamicUUIDData(TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getPrimordialUuid()), true,
               DynamicValidatorType.ONE_OF, 
               LookupService.getService(DynamicUtility.class).createDynamicStringArrayData(
                  TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getPrimordialUuid().toString(),
                  TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getPrimordialUuid().toString(),
                  TermAux.DEFINITION_DESCRIPTION_TYPE.getPrimordialUuid().toString()),
               true) }) {
   };

   // An organizational concept which serves as a parent concept for dynamic fields defined in the system
   // (unless they choose to put them some where else, this isn't required, is only for convenience)
   public final MetadataConceptConstantGroup DYNAMIC_ASSEMBLAGES = new MetadataConceptConstantGroup("Dynamic assemblages",
         UUID.fromString("e18265b7-5406-52b6-baf0-4cfb867829b4")) {
      {
         addChild(DynamicConstants.this.DYNAMIC_EXTENSION_DEFINITION);
         addChild(DynamicConstants.this.DYNAMIC_DEFINITION_DESCRIPTION);
         addChild(DynamicConstants.this.DYNAMIC_INDEX_CONFIGURATION);
         addChild(DynamicConstants.this.DYNAMIC_COMMENT_ATTRIBUTE);
         addChild(DynamicConstants.this.DYNAMIC_ASSOCIATION);
         addChild(DynamicConstants.this.DYNAMIC_ASSOCIATION_INVERSE_NAME);
         addChild(DynamicConstants.this.DYNAMIC_EXTENDED_DESCRIPTION_TYPE);
         addChild(DynamicConstants.this.DYNAMIC_EXTENDED_RELATIONSHIP_TYPE);
         addChild(DynamicConstants.this.DYNAMIC_DESCRIPTION_CORE_TYPE);
         addChild(DynamicConstants.this.DYNAMIC_EXTERNAL_USER_ID);
         setParent(TermAux.ASSEMBLAGE);
      }
   };

   // ~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic constants.
    */
   private DynamicConstants() {
      // making this class impossible to construct outside of HK2
   }
   
   @Override
   public void reset() {
      cache = null;
   }


   // ~--- get methods ---------------------------------------------------------

   /**
    * Gets the constants to create.
    *
    * @return the constants to create
    */
   @Override
   public MetadataConceptConstant[] getConstantsToCreate() {
      return new MetadataConceptConstant[] { this.DYNAMIC_ASSEMBLAGES, this.DYNAMIC_METADATA };
   }

   /**
    * Gets the.
    *
    * @return the dynamic constants
    */
   public static DynamicConstants get() {
      if (cache == null) {
         cache = LookupService.getService(DynamicConstants.class);
      }

      return cache;
   }
   
   @Override public int getModuleRank() {
      return Integer.MAX_VALUE;
   }
//J+
}
