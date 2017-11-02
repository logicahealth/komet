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



package sh.isaac.converters.sharedUtils;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.codehaus.plexus.util.FileUtils;

import sh.isaac.MetaData;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.UuidIntMapMap;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.Constants;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.MultipleDataWriterService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.util.ChecksumGenerator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Associations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Descriptions;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_DualParentPropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Relations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Skip;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyAssociation;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.stats.LoadStats;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link IBDFCreationUtility}
 *
 * Various constants and methods for building ISAAC terminology content, and writing it directly
 * to an IBDF file rather than a database.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IBDFCreationUtility {
   
   /** The LOG. */
   private static final Logger LOG = LogManager.getLogger();
   
   
   /** The Constant METADATA_SEMANTIC_TAG. */
   public final static String METADATA_SEMANTIC_TAG = " (ISAAC)";

   /** The read back stamp. */
   protected static StampCoordinate readBackStamp;

   //~--- fields --------------------------------------------------------------

   /** The module. */
   private ComponentReference module = null;

   /** The refset allowed column types. */
   private final HashMap<UUID, DynamicColumnInfo[]> refexAllowedColumnTypes = new HashMap<>();

   /** The concept has stated graph. */
   private final HashSet<UUID> conceptHasStatedGraph = new HashSet<>();

   /** The concept has inferred graph. */
   private final HashSet<UUID> conceptHasInferredGraph = new HashSet<>();

   /** The load statistics. */
   private LoadStats ls = new LoadStats();

   /** The author concept sequence. */
   private final int authorSeq;

   /** The terminology path sequence. */
   private final int terminologyPathSeq;

   /** The default time. */
   private final long defaultTime;

   /** The concept builder service. */
   private final ConceptBuilderService conceptBuilderService;

   /** The expression builder service. */
   private final LogicalExpressionBuilderService expressionBuilderService;

   /** The sememe builder service. */
   private final SemanticBuilderService<?> sememeBuilderService;

   /** The writer. */
   private final DataWriterService writer;

   //~--- constructors --------------------------------------------------------

   /**
    * Creates and stores the path concept - sets up the various namespace details.
    * If creating a module per version, you should specify both module parameters - for the version specific module to create, and the parent grouping module.
    * The namespace will be specified based on the parent grouping module.
    *
    * @param moduleToCreate - if present, a new concept will be created, using this value as the FULLY_QUALIFIED_NAME / preferred term for use as the module
    * @param preExistingModule - if moduleToCreate is not present, lookup the concept with this UUID to use as the module.  if moduleToCreate is present
    *   use preExistingModule as the parent concept for the moduleToCreate, rather than the default of MODULE.
    * @param outputDirectory - The path to write the output files to
    * @param outputArtifactId - Combined with outputArtifactClassifier and outputArtifactVersion to name the final ibdf file
    * @param outputArtifactVersion - Combined with outputArtifactClassifier and outputArtifactId to name the final ibdf file
    * @param outputArtifactClassifier - optional - Combined with outputArtifactId and outputArtifactVersion to name the final ibdf file
    * @param outputJson - true to dump out the data in gson format for debug
    * @param defaultTime - the timestamp to place on created elements, when no other timestamp is specified on the element itself.
    * @throws Exception the exception
    */
   public IBDFCreationUtility(Optional<String> moduleToCreate,
                              Optional<ConceptSpecification> preExistingModule,
                              File outputDirectory,
                              String outputArtifactId,
                              String outputArtifactVersion,
                              String outputArtifactClassifier,
                              boolean outputJson,
                              long defaultTime)
            throws Exception {
      UuidIntMapMap.NID_TO_UUID_CACHE_SIZE = 5000000;

      final File file = new File(outputDirectory, "isaac-db");

      // make sure this is empty
      FileUtils.deleteDirectory(file);
      System.setProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY, file.getCanonicalPath());
      LookupService.startupIsaac();

      // Initialize after starting up isaac...
      this.authorSeq          = MetaData.USER____SOLOR.getNid();
      this.terminologyPathSeq = MetaData.DEVELOPMENT_PATH____SOLOR.getNid();

      // TODO automate this somehow....
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_ASSOCIATION
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_ASSOCIATION
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE
                                .getDynamicColumns());
      registerDynamicSememeColumnInfo(DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE
                                .getUUID(),
          DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE
                                .getDynamicColumns());

      // TODO figure out how to get rid of this copy/paste mess too
      registerDynamicSememeColumnInfo(MetaData.LOINC_NUM____SOLOR.getPrimordialUuid(),
          new DynamicColumnInfo[] { new DynamicColumnInfo(
              0,
              DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(),
              DynamicDataType.STRING,
              null,
              true,
              true) });
      this.conceptBuilderService = Get.conceptBuilderService();
      this.conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____SOLOR);
      this.conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____SOLOR);
      this.conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());
      this.expressionBuilderService = Get.logicalExpressionBuilderService();
      this.sememeBuilderService     = Get.semanticBuilderService();
      this.defaultTime              = defaultTime;

      final StampPosition stampPosition = new StampPositionImpl(
                                              Long.MAX_VALUE,
                                                    MetaData.DEVELOPMENT_PATH____SOLOR.getNid());

      readBackStamp = new StampCoordinateImpl(
          StampPrecedence.PATH,
          stampPosition,
          new NidSet(),
          State.makeAnyStateSet());

      final UUID moduleUUID = moduleToCreate.isPresent() ? UuidT5Generator.get(
                                  UuidT5Generator.PATH_ID_FROM_FS_DESC,
                                  moduleToCreate.get())
            : preExistingModule.get()
                               .getPrimordialUuid();

      // tack the version onto the end of the ibdf file, so that when multiple ibdf files for a single type of content, such as
      // loinc 2.52, loinc 2.54 - we don't have a file name collision during the ibdf build.
      final String outputName = outputArtifactId + "-" + outputArtifactVersion + (StringUtils.isBlank(outputArtifactClassifier) ? ""
            : "-" + outputArtifactClassifier);

      this.writer = new MultipleDataWriterService(
          outputJson ? Optional.of(new File(outputDirectory, outputName + ".json").toPath())
                     : Optional.empty(),
          Optional.of(new File(outputDirectory, outputName + ".ibdf").toPath()));

      if (moduleToCreate.isPresent()) {
         this.module = ComponentReference.fromConcept(moduleUUID);
         createConcept(
             moduleUUID,
             moduleToCreate.get(),
             true,
             preExistingModule.isPresent() ? preExistingModule.get()
                   .getPrimordialUuid()
                                           : MetaData.MODULE____SOLOR.getPrimordialUuid());
      } else {
         this.module = ComponentReference.fromConcept(
             preExistingModule.get()
                              .getPrimordialUuid(),
             preExistingModule.get()
                              .getNid());
      }

      ConsoleUtil.println(
          "Loading with module '" + this.module.getPrimordialUuid() + "' (" + this.module.getNid() +
          ") on DEVELOPMENT path");
   }

   //~--- enums ---------------------------------------------------------------

   /**
    * The Enum DescriptionType.
    */
   public static enum DescriptionType {
      /** Fully qualified name. */
      FULLY_QUALIFIED_NAME,

      /** Synonym. */
      REGULAR_NAME,

      /** Definition. */
      DEFINITION,
      
      /** Should not every be used...*/
      UNKNOWN;

      /**
       * Convert a UUID to a description type.
       *
       * @param typeUuid the type id
       * @return the description type
       */
      public static DescriptionType convert(UUID typeUuid) {
         if (MetaData.FULLY_QUALIFIED_NAME____SOLOR.getUuidList().contains(typeUuid)) {
            return FULLY_QUALIFIED_NAME;
         } else if (MetaData.REGULAR_NAME____SOLOR.getUuidList().contains(typeUuid)) {
            return REGULAR_NAME;
         }

         if (MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getUuidList().contains(typeUuid)) {
            return DEFINITION;
         }
         LOG.error(typeUuid + " is not a known description type. ");

         return UNKNOWN;
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Gets the concept spec.
       *
       * @return the concept spec
       */
      public ConceptSpecification getConceptSpec() {
         switch (this) {
         case FULLY_QUALIFIED_NAME:
            return MetaData.FULLY_QUALIFIED_NAME____SOLOR;

         case REGULAR_NAME:
            return MetaData.REGULAR_NAME____SOLOR; 

         case DEFINITION:
            return MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR;

         case UNKNOWN:
             return MetaData.UNKNOWN_DESCRIPTION_TYPE____SOLOR;
                    
         default:
            throw new RuntimeException("Unsupported descriptiontype '" + this + "'");
         }
      }
   }

   ;

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the annotation.
    *
    * @param referencedComponent The component to attach this annotation to
    * @param uuidForCreatedAnnotation  - the UUID to use for the created annotation.  If null, generated from uuidForCreatedAnnotation, value, refexDynamicTypeUuid
    * @param value - the value to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
    * of the sememe represented by refexDynamicTypeUuid
    * @param refexDynamicTypeUuid - the uuid of the dynamic element type -
    * @param state -  state or null (for active)
    * @param time - if null, uses the component time
    * @return the sememe chronology
    */
   public SemanticChronology addAnnotation(ComponentReference referencedComponent,
         UUID uuidForCreatedAnnotation,
         DynamicData value,
         UUID refexDynamicTypeUuid,
         State state,
         Long time) {
      return addAnnotation(referencedComponent, uuidForCreatedAnnotation, ((value == null) ? new DynamicData[] {}
            : new DynamicData[] { value }), refexDynamicTypeUuid, state, time, null);
   }

   /**
    * Adds the annotation.
    *
    * @param referencedComponent The component to attach this annotation to
    * @param uuidForCreatedAnnotation  - the UUID to use for the created annotation.  If null, generated from uuidForCreatedAnnotation, value, refexDynamicTypeUuid
    * @param values - the values to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
    * of the sememe represented by refexDynamicTypeUuid
    * @param refexDynamicTypeUuid - the uuid of the dynamic element type -
    * @param state -  state or null (for active)
    * @param time - if null, uses the component time
    * @param module the module
    * @return the sememe chronology
    */
   @SuppressWarnings("unchecked")
   public SemanticChronology addAnnotation(ComponentReference referencedComponent,
         UUID uuidForCreatedAnnotation,
         DynamicData[] values,
         UUID refexDynamicTypeUuid,
         State state,
         Long time,
         UUID module) {
      validateDataTypes(refexDynamicTypeUuid, values);

      @SuppressWarnings("rawtypes")
      final SemanticBuilder sb = this.sememeBuilderService.getDynamicBuilder(
                                   referencedComponent.getNid(),
                                   Get.identifierService()
                                      .getNidForUuids(refexDynamicTypeUuid),
                                   values);

      if (uuidForCreatedAnnotation == null) {
         final StringBuilder temp = new StringBuilder();

         temp.append(refexDynamicTypeUuid.toString());
         temp.append(referencedComponent.getPrimordialUuid()
                                        .toString());

         if (values != null) {
            for (final DynamicData d: values) {
               if (d == null) {
                  temp.append("null");
               } else {
                  temp.append(d.getDynamicDataType()
                               .getDisplayName());
                  temp.append(ChecksumGenerator.calculateChecksum("SHA1", d.getData()));
               }
            }
         }

         uuidForCreatedAnnotation = ConverterUUID.createNamespaceUUIDFromString(temp.toString());
      }

      sb.setPrimordialUuid(uuidForCreatedAnnotation);

      final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();
      final SemanticChronology sc = (SemanticChronology) sb.build(
                                                        createStamp(
                                                              state,
                                                                    selectTime(time, referencedComponent),
                                                                    module),
                                                              builtObjects);

      for (final IsaacExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      if ((values == null) || (values.length == 0)) {
         this.ls.addRefsetMember(getOriginStringForUuid(refexDynamicTypeUuid));
      } else {
         if (BPT_Associations.isAssociation(refexDynamicTypeUuid)) {
            this.ls.addAssociation(getOriginStringForUuid(refexDynamicTypeUuid));
         } else {
            this.ls.addAnnotation(
                ((referencedComponent.getTypeString()
                                     .length() == 0) ? getOriginStringForUuid(referencedComponent.getPrimordialUuid())
                  : referencedComponent.getTypeString()),
                getOriginStringForUuid(refexDynamicTypeUuid));
         }
      }

      return sc;
   }

   /**
    * Add an association. The source of the association is assumed to be the specified concept.
    *
    * @param concept the concept
    * @param associationPrimordialUuid - optional - if not provided, created from the source, target and type.
    * @param targetUuid the target uuid
    * @param associationTypeUuid required
    * @param state the state
    * @param time - if null, default is used
    * @param module the module
    * @return the sememe chronology
    */
   public SemanticChronology addAssociation(ComponentReference concept,
         UUID associationPrimordialUuid,
         UUID targetUuid,
         UUID associationTypeUuid,
         State state,
         Long time,
         UUID module) {
      if (!isConfiguredAsDynamicSememe(associationTypeUuid)) {
         ConsoleUtil.printErrorln(
             "Asked to create an association with an unregistered association type.  This is deprecated, and should be fixed...");
         configureConceptAsAssociation(associationTypeUuid, null);
      }

      return addAnnotation(concept,
          associationPrimordialUuid,
          new DynamicData[] { new DynamicUUIDImpl(targetUuid) },
          associationTypeUuid,
          state,
          time,
          module);
   }

   /**
    * Add a description to the concept.  UUID for the description is calculated from the target concept, description value, type, and preferred flag.
    *
    * @param concept the concept
    * @param descriptionValue the description value
    * @param wbDescriptionType the wb description type
    * @param preferred the preferred
    * @param sourceDescriptionTypeUUID the source description type UUID
    * @param state the state
    * @return the sememe chronology
    */
   public SemanticChronology addDescription(ComponentReference concept,
         String descriptionValue,
         DescriptionType wbDescriptionType,
         boolean preferred,
         UUID sourceDescriptionTypeUUID,
         State state) {
      return addDescription(
          concept,
          null,
          descriptionValue,
          wbDescriptionType,
          preferred,
          null,
          null,
          null,
          null,
          sourceDescriptionTypeUUID,
          state,
          null);
   }

   /**
    * Add a description to the concept.
    *
    * @param concept the concept
    * @param descriptionPrimordialUUID the description primordial UUID
    * @param descriptionValue the description value
    * @param wbDescriptionType the wb description type
    * @param preferred the preferred
    * @param sourceDescriptionTypeUUID the source description type UUID
    * @param status the status
    * @return the sememe chronology
    */
   public SemanticChronology addDescription(ComponentReference concept,
         UUID descriptionPrimordialUUID,
         String descriptionValue,
         DescriptionType wbDescriptionType,
         boolean preferred,
         UUID sourceDescriptionTypeUUID,
         State status) {
      return addDescription(
          concept,
          descriptionPrimordialUUID,
          descriptionValue,
          wbDescriptionType,
          preferred,
          null,
          null,
          null,
          null,
          sourceDescriptionTypeUUID,
          status,
          null);
   }

   /**
    * Add a description to the concept.
    *
    * @param concept - the concept to add this description to
    * @param descriptionPrimordialUUID - if not supplied, created from the concept UUID and the description value and description type
    * @param descriptionValue - the text value
    * @param wbDescriptionType - the type of the description
    * @param preferred - true, false, or null to not create any acceptability entry see {@link #addDescriptionAcceptibility()}
    * @param dialect - ignored if @param preferred is set to null.  if null, defaults to {@link MetaData#US_ENGLISH_DIALECT}
    * @param caseSignificant - if null, defaults to {@link MetaData#DESCRIPTION_NOT_CASE_SENSITIVE}
    * @param languageCode - if null, uses {@link MetaData#ENGLISH_LANGUAGE}
    * @param module - if null, uses the default from the EConceptUtility instance
    * @param sourceDescriptionTypeUUID - this optional value is attached as the extended description type
    * @param state active / inactive
    * @param time - defaults to concept time
    * @return the sememe chronology
    */
   @SuppressWarnings("unchecked")
   public SemanticChronology addDescription(ComponentReference concept,
         UUID descriptionPrimordialUUID,
         String descriptionValue,
         DescriptionType wbDescriptionType,
         Boolean preferred,
         UUID dialect,
         UUID caseSignificant,
         UUID languageCode,
         UUID module,
         UUID sourceDescriptionTypeUUID,
         State state,
         Long time) {
      if (descriptionValue == null) {
         throw new RuntimeException("Description value is required");
      }

      if (dialect == null) {
         dialect = MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid();
      }

      if (languageCode == null) {
         languageCode = MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid();
      }

      if (descriptionPrimordialUUID == null) {
         descriptionPrimordialUUID = ConverterUUID.createNamespaceUUIDFromStrings(
             concept.getPrimordialUuid()
                    .toString(),
             descriptionValue,
             wbDescriptionType.name(),
             dialect.toString(),
             languageCode.toString(),
             (preferred == null) ? "null"
                                 : preferred.toString());
      }

      @SuppressWarnings({ "rawtypes" })
      final SemanticBuilder<? extends SemanticChronology> descBuilder =
         this.sememeBuilderService.getDescriptionBuilder(
             Get.identifierService()
                .getNidForUuids(
                    (caseSignificant == null) ? MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid()
            : caseSignificant),
             Get.identifierService()
                .getNidForUuids(
                    languageCode),
             wbDescriptionType.getConceptSpec()
                              .getNid(),
             descriptionValue,
             concept.getNid());

      descBuilder.setPrimordialUuid(descriptionPrimordialUUID);

      final List<Chronology> builtObjects = new ArrayList<>();
      final SemanticChronology newDescription =
         (SemanticChronology) descBuilder.build(
             createStamp(
                 state,
                 selectTime(time, concept),
                 module),
             builtObjects);

      if (preferred == null) {
         // noop
      } else {
         final SemanticBuilder<?> acceptabilityTypeBuilder = this.sememeBuilderService.getComponentSemanticBuilder(
                                                               preferred ? TermAux.PREFERRED.getNid()
               : TermAux.ACCEPTABLE.getNid(),
                                                                     newDescription.getNid(),
                                                                     Get.identifierService()
                                                                           .getNidForUuids(dialect));
         final UUID acceptabilityTypePrimordialUUID = ConverterUUID.createNamespaceUUIDFromStrings(
                                                          descriptionPrimordialUUID.toString(),
                                                                dialect.toString());

         acceptabilityTypeBuilder.setPrimordialUuid(acceptabilityTypePrimordialUUID);
         acceptabilityTypeBuilder.build(createStamp(state, selectTime(time, concept), module), builtObjects);
         this.ls.addAnnotation("Description", getOriginStringForUuid(dialect));
      }

      for (final IsaacExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      this.ls.addDescription(wbDescriptionType.name() + ((sourceDescriptionTypeUUID == null) ? ""
            : ":" + getOriginStringForUuid(sourceDescriptionTypeUUID)));

      if (sourceDescriptionTypeUUID != null) {
         addAnnotation(ComponentReference.fromChronology(newDescription, () -> "Description"),
             null,
             ((sourceDescriptionTypeUUID == null) ? null
               : new DynamicUUIDImpl(sourceDescriptionTypeUUID)),
             DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE
                                   .getPrimordialUuid(),
             null,
             null);
      }

      return newDescription;
   }

   /**
    * Add a description to the concept.
    *
    * @param description the description
    * @param acceptabilityPrimordialUUID - if not supplied, created from the description UUID, dialectRefsetg and preferred flag
    * @param dialectRefset - A UUID for a refset like MetaData.US_ENGLISH_DIALECT
    * @param preferred - true for preferred, false for acceptable
    * @param state -
    * @param time - if null, uses the description time
    * @param module - optional
    * @return the sememe chronology
    */
   public SemanticChronology addDescriptionAcceptibility(ComponentReference description,
         UUID acceptabilityPrimordialUUID,
         UUID dialectRefset,
         boolean preferred,
         State state,
         Long time,
         UUID module) {
      final SemanticBuilder sb = this.sememeBuilderService.getComponentSemanticBuilder(
                                   preferred ? TermAux.PREFERRED.getNid()
            : TermAux.ACCEPTABLE.getNid(),
                                   description.getNid(),
                                   Get.identifierService()
                                      .getNidForUuids(dialectRefset));

      if (acceptabilityPrimordialUUID == null) {
         // TODO not sure if preferred should be part of UUID
         acceptabilityPrimordialUUID = ConverterUUID.createNamespaceUUIDFromStrings(
             description.getPrimordialUuid()
                        .toString(),
             dialectRefset.toString(),
             preferred + "");
      }

      sb.setPrimordialUuid(acceptabilityPrimordialUUID);

      final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SemanticChronology sc = (SemanticChronology) sb.build(
                                                             createStamp(state, selectTime(time, description), module),
                                                                   builtObjects);

      for (final IsaacExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      this.ls.addAnnotation("Description", getOriginStringForUuid(dialectRefset));
      return sc;
   }

   /**
    * Add a batch of WB descriptions, following WB rules in always generating a FULLY_QUALIFIED_NAME (picking the value based on the propertySubType order).
    * And then adding other types as specified by the propertySubType value, setting preferred / acceptable according to their ranking.
    *
    * @param concept the concept
    * @param descriptions the descriptions
    * @return the list
    */
   public List<SemanticChronology> addDescriptions(ComponentReference concept,
         List<? extends ValuePropertyPair> descriptions) {
      final ArrayList<SemanticChronology> result = new ArrayList<>(descriptions.size());

      Collections.sort(descriptions);

      boolean haveFQN                 = false;
      boolean havePreferredSynonym    = false;
      boolean havePreferredDefinition = false;

      for (final ValuePropertyPair vpp: descriptions) {
         DescriptionType descriptionType = null;
         boolean         preferred;

         if (!haveFQN) {
            descriptionType = DescriptionType.FULLY_QUALIFIED_NAME;
            preferred       = true;
            haveFQN         = true;
         } else {
            if (vpp.getProperty()
                   .getPropertySubType() < BPT_Descriptions.SYNONYM) {
               descriptionType = DescriptionType.FULLY_QUALIFIED_NAME;
               preferred       = false;  // true case is handled above
            } else if ((vpp.getProperty().getPropertySubType() >= BPT_Descriptions.SYNONYM) &&
                       ((vpp.getProperty().getPropertySubType() < BPT_Descriptions.DEFINITION) ||
                        (vpp.getProperty().getPropertySubType() == Integer.MAX_VALUE))) {
               descriptionType = DescriptionType.REGULAR_NAME;

               if (!havePreferredSynonym) {
                  preferred            = true;
                  havePreferredSynonym = true;
               } else {
                  preferred = false;
               }
            } else if (vpp.getProperty()
                          .getPropertySubType() >= BPT_Descriptions.DEFINITION) {
               descriptionType = DescriptionType.DEFINITION;

               if (!havePreferredDefinition) {
                  preferred               = true;
                  havePreferredDefinition = true;
               } else {
                  preferred = false;
               }
            } else {
               throw new RuntimeException("Unexpected error");
            }
         }

         if (!(vpp.getProperty()
                  .getPropertyType() instanceof BPT_Descriptions)) {
            throw new RuntimeException(
                "This method requires properties that have a parent that are an instance of BPT_Descriptions");
         }

         result.add(
             addDescription(
                 concept,
                 vpp.getUUID(),
                 vpp.getValue(),
                 descriptionType,
                 preferred,
                 null,
                 null,
                 null,
                 null,
                 vpp.getProperty()
                    .getUUID(),
                 (vpp.isDisabled() ? State.INACTIVE
                                   : State.ACTIVE),
                 vpp.getTime()));
      }

      return result;
   }

   /**
    * Add a workbench official "Fully Specified Name".  Convenience method for adding a description of type FULLY_QUALIFIED_NAME
    *
    * @param concept the concept
    * @param fullySpecifiedName the fully specified name
    * @return the sememe chronology
    */
   public SemanticChronology addFullySpecifiedName(ComponentReference concept,
         String fullySpecifiedName) {
      return addDescription(concept, fullySpecifiedName, DescriptionType.FULLY_QUALIFIED_NAME, true, null, State.ACTIVE);
   }

   /**
    * Add an IS_A_REL relationship, with the time set to now.
    * Can only be called once per concept.
    *
    * @param concept the concept
    * @param targetUuid the target uuid
    * @return the sememe chronology
    */
   public SemanticChronology addParent(ComponentReference concept, UUID targetUuid) {
      return addParent(concept, null, new UUID[] { targetUuid }, null, null);
   }

   /**
    * This rel add method handles the advanced cases where a rel type 'foo' is actually being loaded as "is_a" (or some other arbitrary type)
    * it makes the swap, and adds the second value as a UUID annotation on the created relationship.
    * Can only be called once per concept
    *
    * @param concept the concept
    * @param targetUuid the target uuid
    * @param p the p
    * @param time the time
    * @return the sememe chronology
    */
   public SemanticChronology addParent(ComponentReference concept,
         UUID targetUuid,
         Property p,
         Long time) {
      if (p.getWBTypeUUID() == null) {
         return addParent(concept, null, new UUID[] { targetUuid }, null, time);
      } else {
         return addParent(concept, null, new UUID[] { targetUuid }, p.getUUID(), time);
      }
   }

   /**
    * Add a parent (is a ) relationship. The source of the relationship is assumed to be the specified concept.
    * Can only be called once per concept
    *
    * @param concept the concept
    * @param relPrimordialUuid - optional - if not provided, created from the source, target and type.
    * @param targetUuid the target uuid
    * @param sourceRelTypeUUID the source rel type UUID
    * @param time - if null, default is used
    * @return the sememe chronology
    */
   public SemanticChronology addParent(ComponentReference concept,
         UUID relPrimordialUuid,
         UUID[] targetUuid,
         UUID sourceRelTypeUUID,
         Long time) {
      if (this.conceptHasStatedGraph.contains(concept.getPrimordialUuid())) {
         throw new RuntimeException(
             "Can only call addParent once!  Must utilize addRelationshipGraph for more complex objects.  " +
             "Parents: " + Arrays.toString(
                 targetUuid) + " Child: " + concept.getPrimordialUuid());
      }

      this.conceptHasStatedGraph.add(concept.getPrimordialUuid());

      final LogicalExpressionBuilder leb = this.expressionBuilderService.getLogicalExpressionBuilder();

      // We are only building isA here, choose necessary set over sufficient.
      final ConceptAssertion[] cas = new ConceptAssertion[targetUuid.length];

      for (int i = 0; i < targetUuid.length; i++) {
         cas[i] = ConceptAssertion(Get.identifierService()
                                      .getNidForUuids(targetUuid[i]), leb);
      }

      NecessarySet(And(cas));

      final LogicalExpression logicalExpression = leb.build();
      @SuppressWarnings("rawtypes")
      final SemanticBuilder sb = this.sememeBuilderService.getLogicalExpressionBuilder(
                                   logicalExpression,
                                   concept.getNid(),
                                   this.conceptBuilderService.getDefaultLogicCoordinate()
                                         .getStatedAssemblageNid());

      sb.setPrimordialUuid(
          (relPrimordialUuid != null) ? relPrimordialUuid
                                      : ConverterUUID.createNamespaceUUIDFromStrings(
                                            concept.getPrimordialUuid()
                                                  .toString(),
                                                  Arrays.toString(targetUuid),
                                                  MetaData.IS_A____SOLOR.getPrimordialUuid()
                                                        .toString()));

      final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SemanticChronology sci = (SemanticChronology) sb.build(
                                                            createStamp(State.ACTIVE, selectTime(time, concept)),
                                                                  builtObjects);

      for (final IsaacExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      if (sourceRelTypeUUID != null) {
         addUUIDAnnotation(ComponentReference.fromChronology(sci, () -> "Graph"),
             sourceRelTypeUUID,
             DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE
                                   .getPrimordialUuid());
         this.ls.addRelationship(
             getOriginStringForUuid(
                 MetaData.IS_A____SOLOR.getPrimordialUuid()) + ":" + getOriginStringForUuid(sourceRelTypeUUID));
      } else {
         this.ls.addRelationship(getOriginStringForUuid(MetaData.IS_A____SOLOR.getPrimordialUuid()));
      }

      return sci;
   }

   /**
    * Adds the refset membership.
    *
    * @param referencedComponent the referenced component
    * @param refexDynamicTypeUuid the refex dynamic type uuid
    * @param state the state
    * @param time the time
    * @return the sememe chronology
    */
   public SemanticChronology addRefsetMembership(ComponentReference referencedComponent,
         UUID refexDynamicTypeUuid,
         State state,
         Long time) {
      return addAnnotation(referencedComponent,
          null,
          (DynamicData[]) null,
          refexDynamicTypeUuid,
          state,
          time,
          null);
   }

   /**
    * Adds the relationship graph.
    *
    * @param concept the concept
    * @param logicalExpression the logical expression
    * @param stated the stated
    * @param time the time
    * @param module the module
    * @return the sememe chronology
    */
   public SemanticChronology addRelationshipGraph(ComponentReference concept,
         LogicalExpression logicalExpression,
         boolean stated,
         Long time,
         UUID module) {
      return this.addRelationshipGraph(concept, null, logicalExpression, stated, time, module);
   }

   /**
    * Adds the relationship graph.
    *
    * @param concept the concept
    * @param graphPrimordialUuid the graph primordial uuid
    * @param logicalExpression the logical expression
    * @param stated the stated
    * @param time the time
    * @param module the module
    * @return the sememe chronology
    */
   public SemanticChronology addRelationshipGraph(ComponentReference concept,
         UUID graphPrimordialUuid,
         LogicalExpression logicalExpression,
         boolean stated,
         Long time,
         UUID module) {
      final HashSet<UUID> temp = stated ? this.conceptHasStatedGraph
                                        : this.conceptHasInferredGraph;

      if (temp.contains(concept.getPrimordialUuid())) {
         throw new RuntimeException("Already have a " + (stated ? "stated"
               : "inferred") + " graph for concept " + concept.getPrimordialUuid());
      }

      temp.add(concept.getPrimordialUuid());

      @SuppressWarnings("rawtypes")
      final SemanticBuilder sb = this.sememeBuilderService.getLogicalExpressionBuilder(
                                   logicalExpression,
                                   concept.getNid(),
                                   stated ? this.conceptBuilderService.getDefaultLogicCoordinate()
                                         .getStatedAssemblageNid()
            : this.conceptBuilderService.getDefaultLogicCoordinate()
                                        .getInferredAssemblageNid());

      // Build a LogicGraph UUID seed based on concept & logicExpression.getData(EXTERNAL)
      final StringBuilder byteString = new StringBuilder();
      final byte[][]      byteArray  = logicalExpression.getData(DataTarget.EXTERNAL);

      for (byte[] byteArray1: byteArray) {
         byteString.append(Arrays.toString(byteArray1));
      }

      // Create UUID from seed and assign SesemeBuilder the value
      final UUID generatedGraphPrimordialUuid = ConverterUUID.createNamespaceUUIDFromStrings(
                                                    concept.getPrimordialUuid()
                                                          .toString(),
                                                          "" + stated,
                                                          byteString.toString());

      sb.setPrimordialUuid((graphPrimordialUuid != null) ? graphPrimordialUuid
            : generatedGraphPrimordialUuid);

      final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SemanticChronology sci = (SemanticChronology) sb.build(
                                                            createStamp(
                                                                  State.ACTIVE,
                                                                        selectTime(time, concept),
                                                                        module),
                                                                  builtObjects);

      for (final IsaacExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      this.ls.addGraph();
      return sci;
   }

   /**
    * uses the concept time, UUID is created from the component UUID, the annotation value and type.
    *
    * @param referencedComponent the referenced component
    * @param annotationValue the annotation value
    * @param refsetUuid the refset uuid
    * @param state the state
    * @return the sememe chronology
    */
   public SemanticChronology addStaticStringAnnotation(ComponentReference referencedComponent,
         String annotationValue,
         UUID refsetUuid,
         State state) {
      @SuppressWarnings("rawtypes")
      final SemanticBuilder sb = this.sememeBuilderService.getStringSemanticBuilder(
                                   annotationValue,
                                   referencedComponent.getNid(),
                                   Get.identifierService()
                                      .getNidForUuids(refsetUuid));
      final StringBuilder temp = new StringBuilder();

      temp.append(annotationValue);
      temp.append(refsetUuid.toString());
      temp.append(referencedComponent.getPrimordialUuid()
                                     .toString());
      sb.setPrimordialUuid(ConverterUUID.createNamespaceUUIDFromString(temp.toString()));

      final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SemanticChronology sc = (SemanticChronology) sb.build(
                                                       createStamp(state, selectTime(null, referencedComponent)),
                                                             builtObjects);

      for (final IsaacExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      this.ls.addAnnotation((referencedComponent.getTypeString()
            .length() > 0) ? referencedComponent.getTypeString()
                           : getOriginStringForUuid(
                               referencedComponent.getPrimordialUuid()), getOriginStringForUuid(refsetUuid));
      return sc;
   }

   /**
    * uses the concept time, UUID is created from the component UUID, the annotation value and type.
    *
    * @param referencedComponent the referenced component
    * @param annotationValue the annotation value
    * @param refsetUuid the refset uuid
    * @param status the status
    * @return the sememe chronology
    */
   public SemanticChronology addStringAnnotation(ComponentReference referencedComponent,
         String annotationValue,
         UUID refsetUuid,
         State status) {
      return addAnnotation(referencedComponent,
          null,
          new DynamicData[] { new DynamicStringImpl(annotationValue) },
          refsetUuid,
          status,
          null,
          null);
   }

   /**
    * uses the concept time.
    *
    * @param referencedComponent the referenced component
    * @param uuidForCreatedAnnotation the uuid for created annotation
    * @param annotationValue the annotation value
    * @param refsetUuid the refset uuid
    * @param status the status
    * @return the sememe chronology
    */
   public SemanticChronology addStringAnnotation(ComponentReference referencedComponent,
         UUID uuidForCreatedAnnotation,
         String annotationValue,
         UUID refsetUuid,
         State status) {
      return addAnnotation(referencedComponent,
          uuidForCreatedAnnotation,
          new DynamicData[] { new DynamicStringImpl(annotationValue) },
          refsetUuid,
          status,
          null,
          null);
   }

   /**
    * Add an alternate ID to the concept.
    *
    * @param existingUUID the existing UUID
    * @param newUUID the new UUID
    */
   public void addUUID(UUID existingUUID, UUID newUUID) {
      final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) Get.conceptService()
                                                                                 .getConceptChronology(existingUUID);

      conceptChronology.addAdditionalUuids(newUUID);
      this.writer.put(conceptChronology);
   }

   /**
    * Generates the UUID, uses the component time.
    *
    * @param object the object
    * @param value the value
    * @param refsetUuid the refset uuid
    * @return the sememe chronology
    */
   public SemanticChronology addUUIDAnnotation(ComponentReference object, UUID value, UUID refsetUuid) {
      return addAnnotation(object,
          null,
          new DynamicData[] { new DynamicUUIDImpl(value) },
          refsetUuid,
          null,
          null,
          null);
   }

   /**
    * Clear load stats.
    */
   public void clearLoadStats() {
      this.ls = new LoadStats();
   }

   /**
    * This method probably shouldn't be used - better to use the PropertyAssotion type.
    *
    * @param associationTypeConcept the association type concept
    * @param inverseName the inverse name
    * @deprecated - Better to set things up as {@link BPT_Associations}
    */
   @Deprecated
   public void configureConceptAsAssociation(UUID associationTypeConcept, String inverseName) {
      final DynamicColumnInfo[] colInfo = new DynamicColumnInfo[] { new DynamicColumnInfo(
                                                    0,
                                                          DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(),
                                                          DynamicDataType.UUID,
                                                          null,
                                                          true,
                                                          true) };

      configureConceptAsDynamicRefex(
          ComponentReference.fromConcept(associationTypeConcept),
          "Defines an Association Type",
          colInfo,
          null,
          null);
      addRefsetMembership(ComponentReference.fromConcept(associationTypeConcept),
          DynamicConstants.get().DYNAMIC_ASSOCIATION
                                .getUUID(),
          State.ACTIVE,
          null);

      if (!StringUtils.isBlank(inverseName)) {
         final SemanticChronology inverseDesc = addDescription(ComponentReference.fromConcept(
                                                                              associationTypeConcept),
                                                                              inverseName,
                                                                              DescriptionType.REGULAR_NAME,
                                                                              false,
                                                                              null,
                                                                              State.ACTIVE);

         addRefsetMembership(ComponentReference.fromChronology(inverseDesc),
             DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME
                                   .getUUID(),
             State.ACTIVE,
             selectTime(null, ComponentReference.fromChronology(inverseDesc)));
      }

      BPT_Associations.registerAsAssociation(associationTypeConcept);
   }

   /**
    * Configure concept as dynamic refex.
    *
    * @param concept the concept
    * @param refexDescription the refex description
    * @param columns the columns
    * @param referencedComponentTypeRestriction the referenced component type restriction
    * @param referencedComponentTypeSubRestriction the referenced component type sub restriction
    */
   public void configureConceptAsDynamicRefex(ComponentReference concept,
         String refexDescription,
         DynamicColumnInfo[] columns,
         ObjectChronologyType referencedComponentTypeRestriction,
         VersionType referencedComponentTypeSubRestriction) {
      if (refexDescription == null) {
         throw new RuntimeException("Refex description is required");
      }

      // See {@link DynamicSememeUsageDescription} class for more details on this format.
      // Add the special synonym to establish this as an assemblage concept
      // Need a custom UUID, otherwise duplicates are likely
      final UUID temp = ConverterUUID.createNamespaceUUIDFromStrings(
                            concept.getPrimordialUuid()
                                   .toString(),
                            refexDescription,
                            DescriptionType.DEFINITION.name(),
                            MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid()
                                  .toString(),
                            MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid()
                                  .toString(),
                            new Boolean("true").toString(),
                            "DynamicSememeMarker");
      final SemanticChronology desc = addDescription(
                                                              concept,
                                                                    temp,
                                                                    refexDescription,
                                                                    DescriptionType.DEFINITION,
                                                                    true,
                                                                    null,
                                                                    State.ACTIVE);

      // Annotate the description as the 'special' type that means this concept is suitable for use as an assemblage concept
      addAnnotation(ComponentReference.fromChronology(desc),
          null,
          (DynamicData) null,
          DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION
                                .getUUID(),
          State.ACTIVE,
          null);

      // define the data columns (if any)
      if ((columns != null) && (columns.length > 0)) {
         for (final DynamicColumnInfo col: columns) {
            final DynamicData[] data = LookupService.getService(DynamicUtility.class)
                                                          .configureDynamicDefinitionDataForColumn(col);

            addAnnotation(concept,
                null,
                data,
                DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION
                                      .getUUID(),
                State.ACTIVE,
                null,
                null);
         }

         final DynamicArray<DynamicData> indexInfo = LookupService.getService(DynamicUtility.class)
                                                                              .configureColumnIndexInfo(columns);

         if (indexInfo != null) {
            addAnnotation(concept,
                null,
                new DynamicData[] { indexInfo },
                DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION
                                      .getPrimordialUuid(),
                State.ACTIVE,
                null,
                null);
         }
      }

      registerDynamicSememeColumnInfo(concept.getPrimordialUuid(), columns);

      // Add the restriction information (if any)
      final DynamicData[] data = LookupService.getService(DynamicUtility.class)
                                                    .configureDynamicRestrictionData(
                                                          referencedComponentTypeRestriction,
                                                                referencedComponentTypeSubRestriction);

      if (data != null) {
         addAnnotation(concept,
             null,
             data,
             DynamicConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION
                                   .getUUID(),
             State.ACTIVE,
             null,
             null);
      }
   }

   /**
    * Creates the concept.
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology createConcept(UUID conceptPrimordialUuid) {
      return createConcept(conceptPrimordialUuid, (Long) null, State.ACTIVE, null);
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description, calculates
    * the UUID, status current, etc).
    *
    * @param fqn the fqn
    * @param createSynonymFromFQN the create synonym from FULLY_QUALIFIED_NAME
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology createConcept(String fqn, boolean createSynonymFromFQN) {
      return createConcept(ConverterUUID.createNamespaceUUIDFromString(fqn), fqn, createSynonymFromFQN);
   }

   /**
    * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
    *
    * @param fqn the fqn
    * @param createSynonymFromFQN the create synonym from FULLY_QUALIFIED_NAME
    * @param parentConceptPrimordial the parent concept primordial
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology createConcept(String fqn,
         boolean createSynonymFromFQN,
         UUID parentConceptPrimordial) {
      final ConceptChronology concept = createConcept(fqn, createSynonymFromFQN);

      addParent(ComponentReference.fromConcept(concept), parentConceptPrimordial);
      return concept;
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description (en US)
    * status current, etc.
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @param fqn the fqn
    * @param createSynonymFromFQN the create synonym from FULLY_QUALIFIED_NAME
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology createConcept(UUID conceptPrimordialUuid,
         String fqn,
         boolean createSynonymFromFQN) {
      return createConcept(conceptPrimordialUuid, fqn, createSynonymFromFQN, null, State.ACTIVE);
   }

   /**
    * Just create a concept.
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @param time - if null, set to default
    * @param status - if null, set to current
    * @param module - if null, uses the default
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology createConcept(UUID conceptPrimordialUuid,
         Long time,
         State status,
         UUID module) {
      final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) Get.conceptService()
                                                                                 .getConceptChronology(conceptPrimordialUuid);

      conceptChronology.createMutableVersion(createStamp(status, time, module));
      this.writer.put(conceptChronology);
      this.ls.addConcept();
      return conceptChronology;
   }

   /**
    * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @param fqn the fqn
    * @param createSynonymFromFQN the create synonym from FULLY_QUALIFIED_NAME
    * @param relParentPrimordial the rel parent primordial
    * @return the concept chronology<? extends concept version<?>>
    */
   public final ConceptChronology createConcept(UUID conceptPrimordialUuid,
         String fqn,
         boolean createSynonymFromFQN,
         UUID relParentPrimordial) {
      final ConceptChronology concept = createConcept(
                                                                         conceptPrimordialUuid,
                                                                               fqn,
                                                                               createSynonymFromFQN);

      addParent(ComponentReference.fromConcept(concept), relParentPrimordial);
      return concept;
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description (en US)).
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @param fqn the fqn
    * @param createSynonymFromFQN the create synonym from FULLY_QUALIFIED_NAME
    * @param time - set to now if null
    * @param status the status
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology createConcept(UUID conceptPrimordialUuid,
         String fqn,
         boolean createSynonymFromFQN,
         Long time,
         State status) {
      final ConceptChronology cc = createConcept(
                                                                    conceptPrimordialUuid,
                                                                          time,
                                                                          status,
                                                                          null);
      final ComponentReference concept = ComponentReference.fromConcept(cc);

      addFullySpecifiedName(concept, fqn);

      if (createSynonymFromFQN) {
         addDescription(concept,
             fqn.endsWith(METADATA_SEMANTIC_TAG) ? fqn.substring(0, fqn.lastIndexOf(METADATA_SEMANTIC_TAG))
               : fqn,
             DescriptionType.REGULAR_NAME,
             true,
             null,
             State.ACTIVE);
      }

      return cc;
   }

   /**
    * Utility method to build and store a concept.
    *
    * @param primordial - optional
    * @param fqn the fqn 
    * @param preferredName - optional
    * @param altName - optional
    * @param definition - optional
    * @param relParentPrimordial the rel parent primordial
    * @param secondParent - optional
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology createConcept(UUID primordial,
         String fqn,
         String preferredName,
         String altName,
         String definition,
         UUID relParentPrimordial,
         UUID secondParent) {
      final ConceptChronology concept = createConcept(
                                                                         (primordial == null)
                                                                         ? ConverterUUID.createNamespaceUUIDFromString(
                                                                               fqn)
            : primordial,
                                                                               fqn,
                                                                               StringUtils.isEmpty(preferredName) ? true
            : false);
      final LogicalExpressionBuilder leb = this.expressionBuilderService.getLogicalExpressionBuilder();

      if (secondParent == null) {
         NecessarySet(
             And(ConceptAssertion(Get.identifierService()
                                     .getNidForUuids(relParentPrimordial), leb)));
      } else {
         NecessarySet(
             And(
                 ConceptAssertion(Get.identifierService()
                                     .getNidForUuids(relParentPrimordial), leb),
                 ConceptAssertion(Get.identifierService()
                                     .getNidForUuids(secondParent), leb)));
      }

      final LogicalExpression logicalExpression = leb.build();

      addRelationshipGraph(ComponentReference.fromConcept(concept), null, logicalExpression, true, null, null);

      if (StringUtils.isNotEmpty(preferredName)) {
         addDescription(ComponentReference.fromConcept(concept),
             preferredName,
             DescriptionType.REGULAR_NAME,
             true,
             null,
             State.ACTIVE);
      }

      if (StringUtils.isNotEmpty(altName)) {
         addDescription(ComponentReference.fromConcept(concept),
             altName,
             DescriptionType.REGULAR_NAME,
             false,
             null,
             State.ACTIVE);
      }

      if (StringUtils.isNotEmpty(definition)) {
         addDescription(
             ComponentReference.fromConcept(concept),
             definition,
             DescriptionType.DEFINITION,
             true,
             null,
             State.ACTIVE);
      }

      return concept;
   }

   /**
    * Creates column concepts (for the column labels) for each provided columnName, then creates a property with a multi-column data set
    * each column being of type string, and optional.
    *
    * @param sememeName the sememe name
    * @param columnNames - Create concepts to represent column names for each item here.  Supports a stupid hack, where if the
    * first two characters of a string in this array are '[]' - it will create a dynamic element array type for strings, rather than a single string.
    * @param columnTypes - optional - if not provided, makes all columns strings.  If provided, must match size of columnNames
    * @return the property
    */
   public Property createMultiColumnDynamicStringSememe(String sememeName,
         String[] columnNames,
         DynamicDataType[] columnTypes) {
      final DynamicColumnInfo[] cols = new DynamicColumnInfo[columnNames.length];

      for (int i = 0; i < cols.length; i++) {
         String                colName;
         DynamicDataType type;

         if (columnNames[i].startsWith("[]")) {
            colName = columnNames[i].substring(2, columnNames[i].length());
            type    = DynamicDataType.ARRAY;
         } else {
            colName = columnNames[i];
            type    = (columnTypes == null) ? DynamicDataType.STRING
                                            : columnTypes[i];
         }

         final UUID descriptionConcept = createConcept(colName,
                                                   true,
                                                   DynamicConstants.get().DYNAMIC_COLUMNS
                                                         .getPrimordialUuid()).getPrimordialUuid();

         cols[i] = new DynamicColumnInfo(i, descriptionConcept, type, null, false, true);
      }

      return new Property(null, sememeName, null, null, false, Integer.MAX_VALUE, cols);
   }

   /**
    * Set up all the boilerplate stuff.
    *
    * @param state - state or null (for current)
    * @param time - time or null (for default)
    * @return the int
    */
   public int createStamp(State state, Long time) {
      return createStamp(state, time, null);
   }

   /**
    * Set up all the boilerplate stuff.
    *
    * @param state - state or null (for active)
    * @param time - time or null (for default)
    * @param module the module
    * @return the int
    */
   public int createStamp(State state, Long time, UUID module) {
      return Get.stampService()
                .getStampSequence(
                    (state == null) ? State.ACTIVE
                                    : state,
                    (time == null) ? this.defaultTime
                                   : time,
                    this.authorSeq,
                    ((module == null) ? this.module.getSequence()
                                      : Get.identifierService()
                                            .getNidForUuids(module)),
                    this.terminologyPathSeq);
   }

   /**
    * Create metadata concepts from the PropertyType structure.
    *
    * @param propertyTypes the property types
    * @param parentPrimordial the parent primordial
    * @throws Exception the exception
    */
   public void loadMetaDataItems(Collection<PropertyType> propertyTypes, UUID parentPrimordial)
            throws Exception {
      for (final PropertyType pt: propertyTypes) {
         if (pt instanceof BPT_Skip) {
            continue;
         }

         createConcept(
             pt.getPropertyTypeUUID(),
             pt.getPropertyTypeDescription() + METADATA_SEMANTIC_TAG,
             true,
             parentPrimordial);

         UUID secondParent = null;

         if (pt instanceof BPT_Refsets) {
            secondParent = setupWbPropertyMetadata(
                MetaData.SOLOR_ASSEMBLAGE____SOLOR.getPrimordialUuid(),
                (BPT_DualParentPropertyType) pt);
         } else if (pt instanceof BPT_Descriptions) {
            // should only do this once, in case we see a BPT_Descriptions more than once
            secondParent = setupWbPropertyMetadata(
                MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid(),
                (BPT_DualParentPropertyType) pt);
         } else if (pt instanceof BPT_Relations) {
            // should only do this once, in case we see a BPT_Relations more than once
            secondParent = setupWbPropertyMetadata(
                MetaData.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid(),
                (BPT_DualParentPropertyType) pt);
         }

         for (final Property p: pt.getProperties()) {
            if (p.isFromConceptSpec()) {
               // This came from a conceptSpecification (metadata in ISAAC), and we don't need to create it.
               // Just need to add one relationship to the existing concept.
               addParent(ComponentReference.fromConcept(p.getUUID()), pt.getPropertyTypeUUID());
            } else {
               // don't feed in the 'definition' if it is an association, because that will be done by the configureConceptAsDynamicRefex method
               final ConceptChronology concept = createConcept(
                                                                                  p.getUUID(),
                                                                                        p.getSourcePropertyNameFQN() +
                                                                                        METADATA_SEMANTIC_TAG,
                                                                                        p.getSourcePropertyNameFQN(),
                                                                                        p.getSourcePropertyAltName(),
                                                                                        ((p instanceof
                                                                                          PropertyAssociation) ? null
                     : p.getSourcePropertyDefinition()),
                                                                                        pt.getPropertyTypeUUID(),
                                                                                        secondParent);

               if (pt.createAsDynamicRefex()) {
                  configureConceptAsDynamicRefex(
                      ComponentReference.fromConcept(concept),
                      findFirstNotEmptyString(
                          p.getSourcePropertyDefinition(),
                          p.getSourcePropertyAltName(),
                          p.getSourcePropertyNameFQN()),
                      p.getDataColumnsForDynamicRefex(),
                      null,
                      null);
               } else if (p instanceof PropertyAssociation) {
                  // TODO need to migrate code from api-util (AssociationType, etc) down into the ISAAC packages... integrate here, at least at doc level
                  // associations return false for "createAsDynamicRefex"
                  final PropertyAssociation item = (PropertyAssociation) p;

                  // Make this a dynamic refex - with the association column info
                  configureConceptAsDynamicRefex(
                      ComponentReference.fromConcept(concept),
                      item.getSourcePropertyDefinition(),
                      item.getDataColumnsForDynamicRefex(),
                      item.getAssociationComponentTypeRestriction(),
                      item.getAssociationComponentTypeSubRestriction());

                  // Add this concept to the association sememe
                  addRefsetMembership(ComponentReference.fromConcept(concept),
                      DynamicConstants.get().DYNAMIC_ASSOCIATION
                                            .getUUID(),
                      State.ACTIVE,
                      null);

                  // add the inverse name, if it has one
                  if (!StringUtils.isBlank(item.getAssociationInverseName())) {
                     final SemanticChronology inverseDesc = addDescription(ComponentReference.fromConcept(
                                                                                          concept),
                                                                                          item.getAssociationInverseName(),
                                                                                          DescriptionType.REGULAR_NAME,
                                                                                          false,
                                                                                          null,
                                                                                          State.ACTIVE);

                     addRefsetMembership(ComponentReference.fromChronology(inverseDesc),
                         DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME
                                               .getUUID(),
                         State.ACTIVE,
                         selectTime(null, ComponentReference.fromChronology(inverseDesc)));
                  }
               }
            }
         }
      }
   }

   /**
    * Create metadata TtkConceptChronicles from the PropertyType structure
    * NOTE - Refset types are not stored!.
    *
    * @param propertyType the property type
    * @param parentPrimordial the parent primordial
    * @throws Exception the exception
    */
   public void loadMetaDataItems(PropertyType propertyType, UUID parentPrimordial)
            throws Exception {
      final ArrayList<PropertyType> propertyTypes = new ArrayList<>();

      propertyTypes.add(propertyType);
      loadMetaDataItems(propertyTypes, parentPrimordial);
   }

   /**
    * Load terminology metadata attributes.
    *
    * @param terminologyMetadataRootConcept the terminology metadata root concept
    * @param converterSourceArtifactVersion the converter source artifact version
    * @param converterSourceReleaseDate the converter source release date
    * @param converterOutputArtifactVersion the converter output artifact version
    * @param converterOutputArtifactClassifier the converter output artifact classifier
    * @param converterVersion the converter version
    */
   public void loadTerminologyMetadataAttributes(ComponentReference terminologyMetadataRootConcept,
         String converterSourceArtifactVersion,
         Optional<String> converterSourceReleaseDate,
         String converterOutputArtifactVersion,
         Optional<String> converterOutputArtifactClassifier,
         String converterVersion) {
      addStaticStringAnnotation(
          terminologyMetadataRootConcept,
          converterSourceArtifactVersion,
          MetaData.SOURCE_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(),
          State.ACTIVE);
      addStaticStringAnnotation(
          terminologyMetadataRootConcept,
          converterOutputArtifactVersion,
          MetaData.CONVERTED_IBDF_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(),
          State.ACTIVE);
      addStaticStringAnnotation(
          terminologyMetadataRootConcept,
          converterVersion,
          MetaData.CONVERTER_VERSION____SOLOR.getPrimordialUuid(),
          State.ACTIVE);

      if (converterOutputArtifactClassifier.isPresent() &&
            StringUtils.isNotBlank(converterOutputArtifactClassifier.get())) {
         addStaticStringAnnotation(
             terminologyMetadataRootConcept,
             converterOutputArtifactClassifier.get(),
             MetaData.CONVERTED_IBDF_ARTIFACT_CLASSIFIER____SOLOR.getPrimordialUuid(),
             State.ACTIVE);
      }

      if (converterSourceReleaseDate.isPresent() && StringUtils.isNotBlank(converterSourceReleaseDate.get())) {
         addStaticStringAnnotation(
             terminologyMetadataRootConcept,
             converterSourceReleaseDate.get(),
             MetaData.SOURCE_RELEASE_DATE____SOLOR.getPrimordialUuid(),
             State.ACTIVE);
      }
   }

   /**
    * Register dynamic element column info.
    *
    * @param sememeUUID the sememe UUID
    * @param columnInfo the column info
    */
   public final void registerDynamicSememeColumnInfo(UUID sememeUUID, DynamicColumnInfo[] columnInfo) {
      this.refexAllowedColumnTypes.put(sememeUUID, columnInfo);
   }

   /**
    * uses providedTime first, if present, followed by readTimeFrom.
    *
    * Note, this still may return null.
    *
    * @param providedTime the provided time
    * @param readTimeFrom the read time from
    * @return the long
    */
   public Long selectTime(Long providedTime, ComponentReference readTimeFrom) {
      if (providedTime != null) {
         return providedTime;
      } else {
         return readTimeFrom.getTime();
      }
   }

   /**
    * Shutdown.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void shutdown()
            throws IOException {
      this.writer.close();
      LookupService.shutdownSystem();
      ConverterUUID.clearCache();
      clearLoadStats();
   }

   /**
    * Find first not empty string.
    *
    * @param strings the strings
    * @return the string
    */
   private String findFirstNotEmptyString(String... strings) {
      for (final String s: strings) {
         if (StringUtils.isNotEmpty(s)) {
            return s;
         }
      }

      return "";
   }

   /**
    * Setup wb property metadata.
    *
    * @param refsetValueParent the refset value parent
    * @param pt the pt
    * @return the uuid
    * @throws Exception the exception
    */
   private UUID setupWbPropertyMetadata(UUID refsetValueParent, BPT_DualParentPropertyType pt)
            throws Exception {
      if (pt.getSecondParentName() == null) {
         throw new RuntimeException("Unhandled case!");
      }

      // Create the terminology specific refset type as a child - this is just an organization concept
      // under description type in source terminology or relationship type in source terminology
      final UUID temp = createConcept(
                            ConverterUUID.createNamespaceUUIDFromString(pt.getSecondParentName(), true),
                            pt.getSecondParentName() + METADATA_SEMANTIC_TAG,
                            true,
                            refsetValueParent).getPrimordialUuid();

      pt.setSecondParentId(temp);
      return temp;
   }

   /**
    * Validate data types.
    *
    * @param refexDynamicTypeUuid the refex dynamic type uuid
    * @param values the values
    */
   private void validateDataTypes(UUID refexDynamicTypeUuid, DynamicData[] values) {
      // TODO this should be a much better validator - checking all of the various things in RefexDynamicCAB.validateData - or in
      // generateMetadataEConcepts - need to enforce the restrictions defined in the columns in the validators
      if (!this.refexAllowedColumnTypes.containsKey(refexDynamicTypeUuid)) {
         throw new RuntimeException("Attempted to store data on a concept not configured as a dynamic element");
      }

      final DynamicColumnInfo[] colInfo = this.refexAllowedColumnTypes.get(refexDynamicTypeUuid);

      if ((values != null) && (values.length > 0)) {
         if (colInfo != null) {
            for (int i = 0; i < values.length; i++) {
               DynamicColumnInfo column = null;

               for (final DynamicColumnInfo x: colInfo) {
                  if (x.getColumnOrder() == i) {
                     column = x;
                     break;
                  }
               }

               if (column == null) {
                  throw new RuntimeException("Column count mismatch");
               } else {
                  if ((values[i] == null) && column.isColumnRequired()) {
                     throw new RuntimeException("Missing column data for column " + column.getColumnName());
                  } else if ((values[i] != null) &&
                             (column.getColumnDataType() != values[i].getDynamicDataType()) &&
                             (column.getColumnDataType() != DynamicDataType.POLYMORPHIC)) {
                     throw new RuntimeException(
                         "Datatype mismatch - " + column.getColumnDataType() + " - " +
                         values[i].getDynamicDataType());
                  }
               }
            }
         } else if (values.length > 0) {
            throw new RuntimeException("Column count mismatch - this dynamic element doesn't allow columns!");
         }
      } else if (colInfo != null) {
         for (final DynamicColumnInfo ci: colInfo) {
            if (ci.isColumnRequired()) {
               throw new RuntimeException("Missing column data for column " + ci.getColumnName());
            }
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if configured as dynamic element.
    *
    * @param refexDynamicTypeUuid the refex dynamic type uuid
    * @return true, if configured as dynamic element
    */
   private boolean isConfiguredAsDynamicSememe(UUID refexDynamicTypeUuid) {
      return this.refexAllowedColumnTypes.containsKey(refexDynamicTypeUuid);
   }

   /**
    * Gets the load stats.
    *
    * @return the load stats
    */
   public LoadStats getLoadStats() {
      return this.ls;
   }

   /**
    * Gets the module.
    *
    * @return the module
    */
   public ComponentReference getModule() {
      return this.module;
   }

   /**
    * Gets the origin string for uuid.
    *
    * @param uuid the uuid
    * @return the origin string for uuid
    */
   private String getOriginStringForUuid(UUID uuid) {
      final String temp = ConverterUUID.getUUIDCreationString(uuid);

      if (temp != null) {
         final String[] parts = temp.split(":");

         if ((parts != null) && (parts.length > 1)) {
            return parts[parts.length - 1];
         }

         return temp;
      }

      return "Unknown";
   }
}

