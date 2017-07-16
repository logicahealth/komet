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

import org.codehaus.plexus.util.FileUtils;

import sh.isaac.MetaData;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.UuidIntMapMap;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeBuilder;
import sh.isaac.api.component.sememe.SememeBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.ComponentNidSememe;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.StringSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import sh.isaac.api.constants.Constants;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.MultipleDataWriterService;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.identity.StampedVersion;
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
import sh.isaac.model.sememe.dataTypes.DynamicSememeStringImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeUUIDImpl;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

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
   /** The Constant METADATA_SEMANTIC_TAG. */
   public final static String METADATA_SEMANTIC_TAG = " (ISAAC)";

   /** The read back stamp. */
   protected static StampCoordinate readBackStamp;

   //~--- fields --------------------------------------------------------------

   /** The module. */
   private ComponentReference module = null;

   /** The refset allowed column types. */
   private final HashMap<UUID, DynamicSememeColumnInfo[]> refexAllowedColumnTypes = new HashMap<>();

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
   private final SememeBuilderService<?> sememeBuilderService;

   /** The writer. */
   private final DataWriterService writer;

   //~--- constructors --------------------------------------------------------

   /**
    * Creates and stores the path concept - sets up the various namespace details.
    * If creating a module per version, you should specify both module parameters - for the version specific module to create, and the parent grouping module.
    * The namespace will be specified based on the parent grouping module.
    *
    * @param moduleToCreate - if present, a new concept will be created, using this value as the FSN / preferred term for use as the module
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
      this.authorSeq          = MetaData.USER____ISAAC.getConceptSequence();
      this.terminologyPathSeq = MetaData.DEVELOPMENT_PATH____ISAAC.getConceptSequence();

      // TODO automate this somehow....
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE
                                .getDynamicSememeColumns());
      registerDynamicSememeColumnInfo(
          DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_RELATIONSHIP_TYPE
                                .getUUID(),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_RELATIONSHIP_TYPE
                                .getDynamicSememeColumns());

      // TODO figure out how to get rid of this copy/paste mess too
      registerDynamicSememeColumnInfo(
          MetaData.LOINC_NUM____ISAAC.getPrimordialUuid(),
          new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(
              0,
              DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_VALUE.getPrimordialUuid(),
              DynamicSememeDataType.STRING,
              null,
              true,
              true) });
      this.conceptBuilderService = Get.conceptBuilderService();
      this.conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____ISAAC);
      this.conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____ISAAC);
      this.conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());
      this.expressionBuilderService = Get.logicalExpressionBuilderService();
      this.sememeBuilderService     = Get.sememeBuilderService();
      this.defaultTime              = defaultTime;

      final StampPosition stampPosition = new StampPositionImpl(
                                              Long.MAX_VALUE,
                                                    MetaData.DEVELOPMENT_PATH____ISAAC.getConceptSequence());

      readBackStamp = new StampCoordinateImpl(
          StampPrecedence.PATH,
          stampPosition,
          ConceptSequenceSet.EMPTY,
          State.ANY_STATE_SET);

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
                                           : MetaData.MODULE____ISAAC.getPrimordialUuid());
      } else {
         this.module = ComponentReference.fromConcept(
             preExistingModule.get()
                              .getPrimordialUuid(),
             preExistingModule.get()
                              .getConceptSequence());
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
      /** Fully specified name. */
      FSN,

      /** Synonym. */
      SYNONYM,

      /** Definition. */
      DEFINITION;

      /**
       * Convert a UUID to a description type.
       *
       * @param typeUuid the type id
       * @return the description type
       */
      public static DescriptionType convert(UUID typeUuid) {
         if (MetaData.FULLY_SPECIFIED_NAME____ISAAC.getPrimordialUuid()
                                          .equals(typeUuid)) {
            return FSN;
         } else if (MetaData.SYNONYM____ISAAC.getPrimordialUuid()
                                    .equals(typeUuid)) {
            return SYNONYM;
         }

         if (MetaData.DEFINITION_DESCRIPTION_TYPE____ISAAC.getPrimordialUuid()
               .equals(typeUuid)) {
            return DEFINITION;
         }

         throw new RuntimeException("Unknown description type for UUID: " + typeUuid);
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Gets the concept spec.
       *
       * @return the concept spec
       */
      public ConceptSpecification getConceptSpec() {
         switch (this) {
         case FSN:
            return MetaData.FULLY_SPECIFIED_NAME____ISAAC;

         case SYNONYM:
            return MetaData.SYNONYM____ISAAC;

         case DEFINITION:
            return MetaData.DEFINITION_DESCRIPTION_TYPE____ISAAC;

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
    * @param refexDynamicTypeUuid - the uuid of the dynamic sememe type -
    * @param state -  state or null (for active)
    * @param time - if null, uses the component time
    * @return the sememe chronology
    */
   public SememeChronology<DynamicSememe<?>> addAnnotation(ComponentReference referencedComponent,
         UUID uuidForCreatedAnnotation,
         DynamicSememeData value,
         UUID refexDynamicTypeUuid,
         State state,
         Long time) {
      return addAnnotation(referencedComponent, uuidForCreatedAnnotation, ((value == null) ? new DynamicSememeData[] {}
            : new DynamicSememeData[] { value }), refexDynamicTypeUuid, state, time, null);
   }

   /**
    * Adds the annotation.
    *
    * @param referencedComponent The component to attach this annotation to
    * @param uuidForCreatedAnnotation  - the UUID to use for the created annotation.  If null, generated from uuidForCreatedAnnotation, value, refexDynamicTypeUuid
    * @param values - the values to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
    * of the sememe represented by refexDynamicTypeUuid
    * @param refexDynamicTypeUuid - the uuid of the dynamic sememe type -
    * @param state -  state or null (for active)
    * @param time - if null, uses the component time
    * @param module the module
    * @return the sememe chronology
    */
   @SuppressWarnings("unchecked")
   public SememeChronology<DynamicSememe<?>> addAnnotation(ComponentReference referencedComponent,
         UUID uuidForCreatedAnnotation,
         DynamicSememeData[] values,
         UUID refexDynamicTypeUuid,
         State state,
         Long time,
         UUID module) {
      validateDataTypes(refexDynamicTypeUuid, values);

      @SuppressWarnings("rawtypes")
      final SememeBuilder sb = this.sememeBuilderService.getDynamicSememeBuilder(
                                   referencedComponent.getNid(),
                                   Get.identifierService()
                                      .getConceptSequenceForUuids(refexDynamicTypeUuid),
                                   values);

      if (uuidForCreatedAnnotation == null) {
         final StringBuilder temp = new StringBuilder();

         temp.append(refexDynamicTypeUuid.toString());
         temp.append(referencedComponent.getPrimordialUuid()
                                        .toString());

         if (values != null) {
            for (final DynamicSememeData d: values) {
               if (d == null) {
                  temp.append("null");
               } else {
                  temp.append(d.getDynamicSememeDataType()
                               .getDisplayName());
                  temp.append(ChecksumGenerator.calculateChecksum("SHA1", d.getData()));
               }
            }
         }

         uuidForCreatedAnnotation = ConverterUUID.createNamespaceUUIDFromString(temp.toString());
      }

      sb.setPrimordialUuid(uuidForCreatedAnnotation);

      final ArrayList<OchreExternalizable> builtObjects = new ArrayList<>();
      final SememeChronology<DynamicSememe<?>> sc = (SememeChronology<DynamicSememe<?>>) sb.build(
                                                        createStamp(
                                                              state,
                                                                    selectTime(time, referencedComponent),
                                                                    module),
                                                              builtObjects);

      for (final OchreExternalizable ochreObject: builtObjects) {
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
   public SememeChronology<DynamicSememe<?>> addAssociation(ComponentReference concept,
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

      return addAnnotation(
          concept,
          associationPrimordialUuid,
          new DynamicSememeData[] { new DynamicSememeUUIDImpl(targetUuid) },
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
   public SememeChronology<DescriptionSememe<?>> addDescription(ComponentReference concept,
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
   public SememeChronology<DescriptionSememe<?>> addDescription(ComponentReference concept,
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
   public SememeChronology<DescriptionSememe<?>> addDescription(ComponentReference concept,
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
         dialect = MetaData.US_ENGLISH_DIALECT____ISAAC.getPrimordialUuid();
      }

      if (languageCode == null) {
         languageCode = MetaData.ENGLISH_LANGUAGE____ISAAC.getPrimordialUuid();
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
      final SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder =
         this.sememeBuilderService.getDescriptionSememeBuilder(
             Get.identifierService()
                .getConceptSequenceForUuids(
                    (caseSignificant == null) ? MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____ISAAC.getPrimordialUuid()
            : caseSignificant),
             Get.identifierService()
                .getConceptSequenceForUuids(
                    languageCode),
             wbDescriptionType.getConceptSpec()
                              .getConceptSequence(),
             descriptionValue,
             concept.getNid());

      descBuilder.setPrimordialUuid(descriptionPrimordialUUID);

      final List<ObjectChronology<? extends StampedVersion>> builtObjects = new ArrayList<>();
      final SememeChronology<DescriptionSememe<?>> newDescription =
         (SememeChronology<DescriptionSememe<?>>) descBuilder.build(
             createStamp(
                 state,
                 selectTime(time, concept),
                 module),
             builtObjects);

      if (preferred == null) {
         // noop
      } else {
         final SememeBuilder<?> acceptabilityTypeBuilder = this.sememeBuilderService.getComponentSememeBuilder(
                                                               preferred ? TermAux.PREFERRED.getNid()
               : TermAux.ACCEPTABLE.getNid(),
                                                                     newDescription.getNid(),
                                                                     Get.identifierService()
                                                                           .getConceptSequenceForUuids(dialect));
         final UUID acceptabilityTypePrimordialUUID = ConverterUUID.createNamespaceUUIDFromStrings(
                                                          descriptionPrimordialUUID.toString(),
                                                                dialect.toString());

         acceptabilityTypeBuilder.setPrimordialUuid(acceptabilityTypePrimordialUUID);
         acceptabilityTypeBuilder.build(createStamp(state, selectTime(time, concept), module), builtObjects);
         this.ls.addAnnotation("Description", getOriginStringForUuid(dialect));
      }

      for (final OchreExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      this.ls.addDescription(wbDescriptionType.name() + ((sourceDescriptionTypeUUID == null) ? ""
            : ":" + getOriginStringForUuid(sourceDescriptionTypeUUID)));

      if (sourceDescriptionTypeUUID != null) {
         addAnnotation(
             ComponentReference.fromChronology(newDescription, () -> "Description"),
             null,
             ((sourceDescriptionTypeUUID == null) ? null
               : new DynamicSememeUUIDImpl(sourceDescriptionTypeUUID)),
             DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE
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
   public SememeChronology<ComponentNidSememe<?>> addDescriptionAcceptibility(ComponentReference description,
         UUID acceptabilityPrimordialUUID,
         UUID dialectRefset,
         boolean preferred,
         State state,
         Long time,
         UUID module) {
      final SememeBuilder sb = this.sememeBuilderService.getComponentSememeBuilder(
                                   preferred ? TermAux.PREFERRED.getNid()
            : TermAux.ACCEPTABLE.getNid(),
                                   description.getNid(),
                                   Get.identifierService()
                                      .getConceptSequenceForUuids(dialectRefset));

      if (acceptabilityPrimordialUUID == null) {
         // TODO not sure if preferred should be part of UUID
         acceptabilityPrimordialUUID = ConverterUUID.createNamespaceUUIDFromStrings(
             description.getPrimordialUuid()
                        .toString(),
             dialectRefset.toString(),
             preferred + "");
      }

      sb.setPrimordialUuid(acceptabilityPrimordialUUID);

      final ArrayList<OchreExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SememeChronology<ComponentNidSememe<?>> sc = (SememeChronology<ComponentNidSememe<?>>) sb.build(
                                                             createStamp(state, selectTime(time, description), module),
                                                                   builtObjects);

      for (final OchreExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      this.ls.addAnnotation("Description", getOriginStringForUuid(dialectRefset));
      return sc;
   }

   /**
    * Add a batch of WB descriptions, following WB rules in always generating a FSN (picking the value based on the propertySubType order).
    * And then adding other types as specified by the propertySubType value, setting preferred / acceptable according to their ranking.
    *
    * @param concept the concept
    * @param descriptions the descriptions
    * @return the list
    */
   public List<SememeChronology<DescriptionSememe<?>>> addDescriptions(ComponentReference concept,
         List<? extends ValuePropertyPair> descriptions) {
      final ArrayList<SememeChronology<DescriptionSememe<?>>> result = new ArrayList<>(descriptions.size());

      Collections.sort(descriptions);

      boolean haveFSN                 = false;
      boolean havePreferredSynonym    = false;
      boolean havePreferredDefinition = false;

      for (final ValuePropertyPair vpp: descriptions) {
         DescriptionType descriptionType = null;
         boolean         preferred;

         if (!haveFSN) {
            descriptionType = DescriptionType.FSN;
            preferred       = true;
            haveFSN         = true;
         } else {
            if (vpp.getProperty()
                   .getPropertySubType() < BPT_Descriptions.SYNONYM) {
               descriptionType = DescriptionType.FSN;
               preferred       = false;  // true case is handled above
            } else if ((vpp.getProperty().getPropertySubType() >= BPT_Descriptions.SYNONYM) &&
                       ((vpp.getProperty().getPropertySubType() < BPT_Descriptions.DEFINITION) ||
                        (vpp.getProperty().getPropertySubType() == Integer.MAX_VALUE))) {
               descriptionType = DescriptionType.SYNONYM;

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
    * Add a workbench official "Fully Specified Name".  Convenience method for adding a description of type FSN
    *
    * @param concept the concept
    * @param fullySpecifiedName the fully specified name
    * @return the sememe chronology
    */
   public SememeChronology<DescriptionSememe<?>> addFullySpecifiedName(ComponentReference concept,
         String fullySpecifiedName) {
      return addDescription(concept, fullySpecifiedName, DescriptionType.FSN, true, null, State.ACTIVE);
   }

   /**
    * Add an IS_A_REL relationship, with the time set to now.
    * Can only be called once per concept.
    *
    * @param concept the concept
    * @param targetUuid the target uuid
    * @return the sememe chronology
    */
   public SememeChronology<LogicGraphSememe<?>> addParent(ComponentReference concept, UUID targetUuid) {
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
   public SememeChronology<LogicGraphSememe<?>> addParent(ComponentReference concept,
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
   public SememeChronology<LogicGraphSememe<?>> addParent(ComponentReference concept,
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
                                      .getConceptSequenceForUuids(targetUuid[i]), leb);
      }

      NecessarySet(And(cas));

      final LogicalExpression logicalExpression = leb.build();
      @SuppressWarnings("rawtypes")
      final SememeBuilder sb = this.sememeBuilderService.getLogicalExpressionSememeBuilder(
                                   logicalExpression,
                                   concept.getNid(),
                                   this.conceptBuilderService.getDefaultLogicCoordinate()
                                         .getStatedAssemblageSequence());

      sb.setPrimordialUuid(
          (relPrimordialUuid != null) ? relPrimordialUuid
                                      : ConverterUUID.createNamespaceUUIDFromStrings(
                                            concept.getPrimordialUuid()
                                                  .toString(),
                                                  Arrays.toString(targetUuid),
                                                  MetaData.IS_A____ISAAC.getPrimordialUuid()
                                                        .toString()));

      final ArrayList<OchreExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SememeChronology<LogicGraphSememe<?>> sci = (SememeChronology<LogicGraphSememe<?>>) sb.build(
                                                            createStamp(State.ACTIVE, selectTime(time, concept)),
                                                                  builtObjects);

      for (final OchreExternalizable ochreObject: builtObjects) {
         this.writer.put(ochreObject);
      }

      if (sourceRelTypeUUID != null) {
         addUUIDAnnotation(
             ComponentReference.fromChronology(sci, () -> "Graph"),
             sourceRelTypeUUID,
             DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_RELATIONSHIP_TYPE
                                   .getPrimordialUuid());
         this.ls.addRelationship(
             getOriginStringForUuid(
                 MetaData.IS_A____ISAAC.getPrimordialUuid()) + ":" + getOriginStringForUuid(sourceRelTypeUUID));
      } else {
         this.ls.addRelationship(getOriginStringForUuid(MetaData.IS_A____ISAAC.getPrimordialUuid()));
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
   public SememeChronology<DynamicSememe<?>> addRefsetMembership(ComponentReference referencedComponent,
         UUID refexDynamicTypeUuid,
         State state,
         Long time) {
      return addAnnotation(
          referencedComponent,
          null,
          (DynamicSememeData[]) null,
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
   public SememeChronology<LogicGraphSememe<?>> addRelationshipGraph(ComponentReference concept,
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
   public SememeChronology<LogicGraphSememe<?>> addRelationshipGraph(ComponentReference concept,
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
      final SememeBuilder sb = this.sememeBuilderService.getLogicalExpressionSememeBuilder(
                                   logicalExpression,
                                   concept.getNid(),
                                   stated ? this.conceptBuilderService.getDefaultLogicCoordinate()
                                         .getStatedAssemblageSequence()
            : this.conceptBuilderService.getDefaultLogicCoordinate()
                                        .getInferredAssemblageSequence());

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

      final ArrayList<OchreExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SememeChronology<LogicGraphSememe<?>> sci = (SememeChronology<LogicGraphSememe<?>>) sb.build(
                                                            createStamp(
                                                                  State.ACTIVE,
                                                                        selectTime(time, concept),
                                                                        module),
                                                                  builtObjects);

      for (final OchreExternalizable ochreObject: builtObjects) {
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
   public SememeChronology<StringSememe<?>> addStaticStringAnnotation(ComponentReference referencedComponent,
         String annotationValue,
         UUID refsetUuid,
         State state) {
      @SuppressWarnings("rawtypes")
      final SememeBuilder sb = this.sememeBuilderService.getStringSememeBuilder(
                                   annotationValue,
                                   referencedComponent.getNid(),
                                   Get.identifierService()
                                      .getConceptSequenceForUuids(refsetUuid));
      final StringBuilder temp = new StringBuilder();

      temp.append(annotationValue);
      temp.append(refsetUuid.toString());
      temp.append(referencedComponent.getPrimordialUuid()
                                     .toString());
      sb.setPrimordialUuid(ConverterUUID.createNamespaceUUIDFromString(temp.toString()));

      final ArrayList<OchreExternalizable> builtObjects = new ArrayList<>();
      @SuppressWarnings("unchecked")
      final SememeChronology<StringSememe<?>> sc = (SememeChronology<StringSememe<?>>) sb.build(
                                                       createStamp(state, selectTime(null, referencedComponent)),
                                                             builtObjects);

      for (final OchreExternalizable ochreObject: builtObjects) {
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
   public SememeChronology<DynamicSememe<?>> addStringAnnotation(ComponentReference referencedComponent,
         String annotationValue,
         UUID refsetUuid,
         State status) {
      return addAnnotation(
          referencedComponent,
          null,
          new DynamicSememeData[] { new DynamicSememeStringImpl(annotationValue) },
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
   public SememeChronology<DynamicSememe<?>> addStringAnnotation(ComponentReference referencedComponent,
         UUID uuidForCreatedAnnotation,
         String annotationValue,
         UUID refsetUuid,
         State status) {
      return addAnnotation(
          referencedComponent,
          uuidForCreatedAnnotation,
          new DynamicSememeData[] { new DynamicSememeStringImpl(annotationValue) },
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
                                                                                 .getConcept(existingUUID);

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
   public SememeChronology<DynamicSememe<?>> addUUIDAnnotation(ComponentReference object, UUID value, UUID refsetUuid) {
      return addAnnotation(
          object,
          null,
          new DynamicSememeData[] { new DynamicSememeUUIDImpl(value) },
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
      final DynamicSememeColumnInfo[] colInfo = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(
                                                    0,
                                                          DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(),
                                                          DynamicSememeDataType.UUID,
                                                          null,
                                                          true,
                                                          true) };

      configureConceptAsDynamicRefex(
          ComponentReference.fromConcept(associationTypeConcept),
          "Defines an Association Type",
          colInfo,
          null,
          null);
      addRefsetMembership(
          ComponentReference.fromConcept(associationTypeConcept),
          DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME
                                .getUUID(),
          State.ACTIVE,
          null);

      if (!StringUtils.isBlank(inverseName)) {
         final SememeChronology<DescriptionSememe<?>> inverseDesc = addDescription(
                                                                        ComponentReference.fromConcept(
                                                                              associationTypeConcept),
                                                                              inverseName,
                                                                              DescriptionType.SYNONYM,
                                                                              false,
                                                                              null,
                                                                              State.ACTIVE);

         addRefsetMembership(
             ComponentReference.fromChronology(inverseDesc),
             DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
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
         DynamicSememeColumnInfo[] columns,
         ObjectChronologyType referencedComponentTypeRestriction,
         SememeType referencedComponentTypeSubRestriction) {
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
                            MetaData.US_ENGLISH_DIALECT____ISAAC.getPrimordialUuid()
                                  .toString(),
                            MetaData.ENGLISH_LANGUAGE____ISAAC.getPrimordialUuid()
                                  .toString(),
                            new Boolean("true").toString(),
                            "DynamicSememeMarker");
      final SememeChronology<DescriptionSememe<?>> desc = addDescription(
                                                              concept,
                                                                    temp,
                                                                    refexDescription,
                                                                    DescriptionType.DEFINITION,
                                                                    true,
                                                                    null,
                                                                    State.ACTIVE);

      // Annotate the description as the 'special' type that means this concept is suitable for use as an assemblage concept
      addAnnotation(
          ComponentReference.fromChronology(desc),
          null,
          (DynamicSememeData) null,
          DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION
                                .getUUID(),
          State.ACTIVE,
          null);

      // define the data columns (if any)
      if ((columns != null) && (columns.length > 0)) {
         for (final DynamicSememeColumnInfo col: columns) {
            final DynamicSememeData[] data = LookupService.getService(DynamicSememeUtility.class)
                                                          .configureDynamicSememeDefinitionDataForColumn(col);

            addAnnotation(
                concept,
                null,
                data,
                DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION
                                      .getUUID(),
                State.ACTIVE,
                null,
                null);
         }

         final DynamicSememeArray<DynamicSememeData> indexInfo = LookupService.getService(DynamicSememeUtility.class)
                                                                              .configureColumnIndexInfo(columns);

         if (indexInfo != null) {
            addAnnotation(
                concept,
                null,
                new DynamicSememeData[] { indexInfo },
                DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION
                                      .getPrimordialUuid(),
                State.ACTIVE,
                null,
                null);
         }
      }

      registerDynamicSememeColumnInfo(concept.getPrimordialUuid(), columns);

      // Add the restriction information (if any)
      final DynamicSememeData[] data = LookupService.getService(DynamicSememeUtility.class)
                                                    .configureDynamicSememeRestrictionData(
                                                          referencedComponentTypeRestriction,
                                                                referencedComponentTypeSubRestriction);

      if (data != null) {
         addAnnotation(
             concept,
             null,
             data,
             DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION
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
   public ConceptChronology<? extends ConceptVersion<?>> createConcept(UUID conceptPrimordialUuid) {
      return createConcept(conceptPrimordialUuid, (Long) null, State.ACTIVE, null);
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description, calculates
    * the UUID, status current, etc).
    *
    * @param fsn the fsn
    * @param createSynonymFromFSN the create synonym from FSN
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology<? extends ConceptVersion<?>> createConcept(String fsn, boolean createSynonymFromFSN) {
      return createConcept(ConverterUUID.createNamespaceUUIDFromString(fsn), fsn, createSynonymFromFSN);
   }

   /**
    * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
    *
    * @param fsn the fsn
    * @param createSynonymFromFSN the create synonym from FSN
    * @param parentConceptPrimordial the parent concept primordial
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology<? extends ConceptVersion<?>> createConcept(String fsn,
         boolean createSynonymFromFSN,
         UUID parentConceptPrimordial) {
      final ConceptChronology<? extends ConceptVersion<?>> concept = createConcept(fsn, createSynonymFromFSN);

      addParent(ComponentReference.fromConcept(concept), parentConceptPrimordial);
      return concept;
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description (en US)
    * status current, etc.
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @param fsn the fsn
    * @param createSynonymFromFSN the create synonym from FSN
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology<? extends ConceptVersion<?>> createConcept(UUID conceptPrimordialUuid,
         String fsn,
         boolean createSynonymFromFSN) {
      return createConcept(conceptPrimordialUuid, fsn, createSynonymFromFSN, null, State.ACTIVE);
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
   public ConceptChronology<? extends ConceptVersion<?>> createConcept(UUID conceptPrimordialUuid,
         Long time,
         State status,
         UUID module) {
      final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) Get.conceptService()
                                                                                 .getConcept(conceptPrimordialUuid);

      conceptChronology.createMutableVersion(createStamp(status, time, module));
      this.writer.put(conceptChronology);
      this.ls.addConcept();
      return conceptChronology;
   }

   /**
    * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @param fsn the fsn
    * @param createSynonymFromFSN the create synonym from FSN
    * @param relParentPrimordial the rel parent primordial
    * @return the concept chronology<? extends concept version<?>>
    */
   public final ConceptChronology<? extends ConceptVersion<?>> createConcept(UUID conceptPrimordialUuid,
         String fsn,
         boolean createSynonymFromFSN,
         UUID relParentPrimordial) {
      final ConceptChronology<? extends ConceptVersion<?>> concept = createConcept(
                                                                         conceptPrimordialUuid,
                                                                               fsn,
                                                                               createSynonymFromFSN);

      addParent(ComponentReference.fromConcept(concept), relParentPrimordial);
      return concept;
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description (en US)).
    *
    * @param conceptPrimordialUuid the concept primordial uuid
    * @param fsn the fsn
    * @param createSynonymFromFSN the create synonym from FSN
    * @param time - set to now if null
    * @param status the status
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology<? extends ConceptVersion<?>> createConcept(UUID conceptPrimordialUuid,
         String fsn,
         boolean createSynonymFromFSN,
         Long time,
         State status) {
      final ConceptChronology<? extends ConceptVersion<?>> cc = createConcept(
                                                                    conceptPrimordialUuid,
                                                                          time,
                                                                          status,
                                                                          null);
      final ComponentReference concept = ComponentReference.fromConcept(cc);

      addFullySpecifiedName(concept, fsn);

      if (createSynonymFromFSN) {
         addDescription(
             concept,
             fsn.endsWith(METADATA_SEMANTIC_TAG) ? fsn.substring(0, fsn.lastIndexOf(METADATA_SEMANTIC_TAG))
               : fsn,
             DescriptionType.SYNONYM,
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
    * @param fsnName the fsn name
    * @param preferredName - optional
    * @param altName - optional
    * @param definition - optional
    * @param relParentPrimordial the rel parent primordial
    * @param secondParent - optional
    * @return the concept chronology<? extends concept version<?>>
    */
   public ConceptChronology<? extends ConceptVersion<?>> createConcept(UUID primordial,
         String fsnName,
         String preferredName,
         String altName,
         String definition,
         UUID relParentPrimordial,
         UUID secondParent) {
      final ConceptChronology<? extends ConceptVersion<?>> concept = createConcept(
                                                                         (primordial == null)
                                                                         ? ConverterUUID.createNamespaceUUIDFromString(
                                                                               fsnName)
            : primordial,
                                                                               fsnName,
                                                                               StringUtils.isEmpty(preferredName) ? true
            : false);
      final LogicalExpressionBuilder leb = this.expressionBuilderService.getLogicalExpressionBuilder();

      if (secondParent == null) {
         NecessarySet(
             And(ConceptAssertion(Get.identifierService()
                                     .getConceptSequenceForUuids(relParentPrimordial), leb)));
      } else {
         NecessarySet(
             And(
                 ConceptAssertion(Get.identifierService()
                                     .getConceptSequenceForUuids(relParentPrimordial), leb),
                 ConceptAssertion(Get.identifierService()
                                     .getConceptSequenceForUuids(secondParent), leb)));
      }

      final LogicalExpression logicalExpression = leb.build();

      addRelationshipGraph(ComponentReference.fromConcept(concept), null, logicalExpression, true, null, null);

      if (StringUtils.isNotEmpty(preferredName)) {
         addDescription(
             ComponentReference.fromConcept(concept),
             preferredName,
             DescriptionType.SYNONYM,
             true,
             null,
             State.ACTIVE);
      }

      if (StringUtils.isNotEmpty(altName)) {
         addDescription(
             ComponentReference.fromConcept(concept),
             altName,
             DescriptionType.SYNONYM,
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
    * first two characters of a string in this array are '[]' - it will create a dynamic sememe array type for strings, rather than a single string.
    * @param columnTypes - optional - if not provided, makes all columns strings.  If provided, must match size of columnNames
    * @return the property
    */
   public Property createMultiColumnDynamicStringSememe(String sememeName,
         String[] columnNames,
         DynamicSememeDataType[] columnTypes) {
      final DynamicSememeColumnInfo[] cols = new DynamicSememeColumnInfo[columnNames.length];

      for (int i = 0; i < cols.length; i++) {
         String                colName;
         DynamicSememeDataType type;

         if (columnNames[i].startsWith("[]")) {
            colName = columnNames[i].substring(2, columnNames[i].length());
            type    = DynamicSememeDataType.ARRAY;
         } else {
            colName = columnNames[i];
            type    = (columnTypes == null) ? DynamicSememeDataType.STRING
                                            : columnTypes[i];
         }

         final UUID descriptionConcept = createConcept(
                                             colName,
                                                   true,
                                                   DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMNS
                                                         .getPrimordialUuid()).getPrimordialUuid();

         cols[i] = new DynamicSememeColumnInfo(i, descriptionConcept, type, null, false, true);
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
                                            .getConceptSequenceForUuids(module)),
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
                MetaData.SOLOR_REFSETS____ISAAC.getPrimordialUuid(),
                (BPT_DualParentPropertyType) pt);
         } else if (pt instanceof BPT_Descriptions) {
            // should only do this once, in case we see a BPT_Descriptions more than once
            secondParent = setupWbPropertyMetadata(
                MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____ISAAC.getPrimordialUuid(),
                (BPT_DualParentPropertyType) pt);
         } else if (pt instanceof BPT_Relations) {
            // should only do this once, in case we see a BPT_Relations more than once
            secondParent = setupWbPropertyMetadata(
                MetaData.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY____ISAAC.getPrimordialUuid(),
                (BPT_DualParentPropertyType) pt);
         }

         for (final Property p: pt.getProperties()) {
            if (p.isFromConceptSpec()) {
               // This came from a conceptSpecification (metadata in ISAAC), and we don't need to create it.
               // Just need to add one relationship to the existing concept.
               addParent(ComponentReference.fromConcept(p.getUUID()), pt.getPropertyTypeUUID());
            } else {
               // don't feed in the 'definition' if it is an association, because that will be done by the configureConceptAsDynamicRefex method
               final ConceptChronology<? extends ConceptVersion<?>> concept = createConcept(
                                                                                  p.getUUID(),
                                                                                        p.getSourcePropertyNameFSN() +
                                                                                        METADATA_SEMANTIC_TAG,
                                                                                        p.getSourcePropertyNameFSN(),
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
                          p.getSourcePropertyNameFSN()),
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
                  addRefsetMembership(
                      ComponentReference.fromConcept(concept),
                      DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME
                                            .getUUID(),
                      State.ACTIVE,
                      null);

                  // add the inverse name, if it has one
                  if (!StringUtils.isBlank(item.getAssociationInverseName())) {
                     final SememeChronology<DescriptionSememe<?>> inverseDesc = addDescription(
                                                                                    ComponentReference.fromConcept(
                                                                                          concept),
                                                                                          item.getAssociationInverseName(),
                                                                                          DescriptionType.SYNONYM,
                                                                                          false,
                                                                                          null,
                                                                                          State.ACTIVE);

                     addRefsetMembership(
                         ComponentReference.fromChronology(inverseDesc),
                         DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME
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
          MetaData.SOURCE_ARTIFACT_VERSION____ISAAC.getPrimordialUuid(),
          State.ACTIVE);
      addStaticStringAnnotation(
          terminologyMetadataRootConcept,
          converterOutputArtifactVersion,
          MetaData.CONVERTED_IBDF_ARTIFACT_VERSION____ISAAC.getPrimordialUuid(),
          State.ACTIVE);
      addStaticStringAnnotation(
          terminologyMetadataRootConcept,
          converterVersion,
          MetaData.CONVERTER_VERSION____ISAAC.getPrimordialUuid(),
          State.ACTIVE);

      if (converterOutputArtifactClassifier.isPresent() &&
            StringUtils.isNotBlank(converterOutputArtifactClassifier.get())) {
         addStaticStringAnnotation(
             terminologyMetadataRootConcept,
             converterOutputArtifactClassifier.get(),
             MetaData.CONVERTED_IBDF_ARTIFACT_CLASSIFIER____ISAAC.getPrimordialUuid(),
             State.ACTIVE);
      }

      if (converterSourceReleaseDate.isPresent() && StringUtils.isNotBlank(converterSourceReleaseDate.get())) {
         addStaticStringAnnotation(
             terminologyMetadataRootConcept,
             converterSourceReleaseDate.get(),
             MetaData.SOURCE_RELEASE_DATE____ISAAC.getPrimordialUuid(),
             State.ACTIVE);
      }
   }

   /**
    * Register dynamic sememe column info.
    *
    * @param sememeUUID the sememe UUID
    * @param columnInfo the column info
    */
   public final void registerDynamicSememeColumnInfo(UUID sememeUUID, DynamicSememeColumnInfo[] columnInfo) {
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
   private void validateDataTypes(UUID refexDynamicTypeUuid, DynamicSememeData[] values) {
      // TODO this should be a much better validator - checking all of the various things in RefexDynamicCAB.validateData - or in
      // generateMetadataEConcepts - need to enforce the restrictions defined in the columns in the validators
      if (!this.refexAllowedColumnTypes.containsKey(refexDynamicTypeUuid)) {
         throw new RuntimeException("Attempted to store data on a concept not configured as a dynamic sememe");
      }

      final DynamicSememeColumnInfo[] colInfo = this.refexAllowedColumnTypes.get(refexDynamicTypeUuid);

      if ((values != null) && (values.length > 0)) {
         if (colInfo != null) {
            for (int i = 0; i < values.length; i++) {
               DynamicSememeColumnInfo column = null;

               for (final DynamicSememeColumnInfo x: colInfo) {
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
                             (column.getColumnDataType() != values[i].getDynamicSememeDataType()) &&
                             (column.getColumnDataType() != DynamicSememeDataType.POLYMORPHIC)) {
                     throw new RuntimeException(
                         "Datatype mismatch - " + column.getColumnDataType() + " - " +
                         values[i].getDynamicSememeDataType());
                  }
               }
            }
         } else if (values.length > 0) {
            throw new RuntimeException("Column count mismatch - this dynamic sememe doesn't allow columns!");
         }
      } else if (colInfo != null) {
         for (final DynamicSememeColumnInfo ci: colInfo) {
            if (ci.isColumnRequired()) {
               throw new RuntimeException("Missing column data for column " + ci.getColumnName());
            }
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if configured as dynamic sememe.
    *
    * @param refexDynamicTypeUuid the refex dynamic type uuid
    * @return true, if configured as dynamic sememe
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

