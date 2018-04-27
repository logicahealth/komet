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

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
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
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.FileUtils;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.UuidIntMapMap;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.externalizable.MultipleDataWriterService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Associations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Descriptions;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_HasAltMetaDataParent;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Skip;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyAssociation;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.stats.LoadStats;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptVersionImpl;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.mojo.IndexTermstore;
import sh.isaac.mojo.LoadTermstore;

/**
 * 
 * {@link IBDFCreationUtility}
 * 
 * Various constants and methods for building ISAAC terminology content, and writing it directly
 * to an IBDF file rather than a database.  
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IBDFCreationUtility
{
   public static enum DescriptionType 
   {
      FULLY_QUALIFIED_NAME, REGULAR_NAME, DEFINITION;
      
      public ConceptSpecification getConceptSpec()
      {
         if (DescriptionType.FULLY_QUALIFIED_NAME == this)
         {
            return MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR;
         }
         else if (DescriptionType.REGULAR_NAME == this)
         {
            return MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR;
         }
         else if (DescriptionType.DEFINITION == this)
         {
            return MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR;
         }
         else
         {
            throw new RuntimeException("Unsupported descriptiontype '" + this + "'");
         }
      }

      public static DescriptionType parse(UUID typeId)
      {
         if (MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getUuidList().contains(typeId))
         {
            return FULLY_QUALIFIED_NAME;
         }
         else if (MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getUuidList().contains(typeId))
         {
            return REGULAR_NAME;
         }
         if (MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getUuidList().contains(typeId))
         {
            return DEFINITION;
         }
         throw new RuntimeException("Unknown description type UUID " + typeId);
      }
      
      public static DescriptionType parse(int typeId)
      {
         if (MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid() == typeId)
         {
            return FULLY_QUALIFIED_NAME;
         }
         else if ( MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid() == typeId)
         {
            return REGULAR_NAME;
         }
         if (MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getNid() == typeId)
         {
            return DEFINITION;
         }
         throw new RuntimeException("Unknown description type " + typeId);
      }
   };
   
   private final int authorNid;
   private final int terminologyPathNid;
   private long defaultTime;
   
   private final static UUID isARelUuid_ = MetaData.IS_A____SOLOR.getPrimordialUuid();
   public final static String METADATA_SEMANTIC_TAG = " (ISAAC)";
   
   private ComponentReference module = null;
   private HashMap<UUID, DynamicColumnInfo[]> refexAllowedColumnTypes = new HashMap<>();
   private HashSet<UUID> identifierTypes = new HashSet<>();
   
   private HashSet<UUID> conceptHasStatedGraph = new HashSet<>();
   private HashSet<UUID> conceptHasInferredGraph = new HashSet<>();
   
   private ConceptBuilderService conceptBuilderService;
   private LogicalExpressionBuilderService expressionBuilderService;
   private SemanticBuilderService<?> semanticBuilder;
   protected static StampCoordinate readBackStamp;
   
   private DataWriterService writer;
   private boolean writeToDB = false;
   
   private ConverterUUID converterUUID;

   private LoadStats ls = new LoadStats();
   
   /**
    * Creates and stores the path concept - sets up the various namespace details.
    * If creating a module per version, you should specify both module parameters - for the version specific module to create, and the parent grouping module.
    * The namespace will be specified based on the parent grouping module.
    * @param moduleToCreate - if present, a new concept will be created, using this value as the FSN / preferred term for use as the module
    * @param preExistingModule - if moduleToCreate is not present, lookup the concept with this UUID to use as the module.  if moduleToCreate is present
    *   use preExistingModule as the parent concept for the moduleToCreate, rather than the default of MODULE.
    * @param outputDirectory - The path to write the output files to
    * @param outputArtifactId - Combined with outputArtifactClassifier and outputArtifactVersion to name the final ibdf file
    * @param outputArtifactVersion - Combined with outputArtifactClassifier and outputArtifactId to name the final ibdf file
    * @param outputArtifactClassifier - optional - Combined with outputArtifactId and outputArtifactVersion to name the final ibdf file   
    * @param outputGson - true to dump out the data in gson format for debug
    * @param defaultTime - the timestamp to place on created elements, when no other timestamp is specified on the element itself.
    * @throws Exception
    */
   public IBDFCreationUtility(Optional<String> moduleToCreate, Optional<ConceptSpecification> preExistingModule, File outputDirectory, 
         String outputArtifactId,  String outputArtifactVersion, String outputArtifactClassifier, boolean outputGson, long defaultTime) throws Exception
   {
      this(moduleToCreate, preExistingModule, outputDirectory, outputArtifactId, outputArtifactVersion, outputArtifactClassifier, outputGson, defaultTime, null, null);
   }

   /**
    * Creates and stores the path concept - sets up the various namespace details.
    * If creating a module per version, you should specify both module parameters - for the version specific module to create, and the parent grouping module.
    * The namespace will be specified based on the parent grouping module.
    * @param moduleToCreate - if present, a new concept will be created, using this value as the FSN / preferred term for use as the module
    * @param preExistingModule - if moduleToCreate is not present, lookup the concept with this UUID to use as the module.  if moduleToCreate is present
    *   use preExistingModule as the parent concept for the moduleToCreate, rather than the default of MODULE.
    * @param outputDirectory - The path to write the output files to
    * @param outputArtifactId - Combined with outputArtifactClassifier and outputArtifactVersion to name the final ibdf file
    * @param outputArtifactVersion - Combined with outputArtifactClassifier and outputArtifactId to name the final ibdf file
    * @param outputArtifactClassifier - optional - Combined with outputArtifactId and outputArtifactVersion to name the final ibdf file    
    * @param outputGson - true to dump out the data in gson format for debug
    * @param defaultTime - the timestamp to place on created elements, when no other timestamp is specified on the element itself.
    * @param versionTypesToSkip - if ibdfPreLoadFiles are provided, this list of types can be specified as the types to ignore in the preload files
    * @param preloadActiveOnly - only load active elements from preload files when true
    * @param ibdfPreLoadFiles (optional) load these ibdf files into the isaac DB after starting (required for some conversions like LOINC)
    * @throws Exception
    */
   public IBDFCreationUtility(Optional<String> moduleToCreate, Optional<ConceptSpecification> preExistingModule, File outputDirectory, 
         String outputArtifactId, String outputArtifactVersion, String outputArtifactClassifier, boolean outputGson, long defaultTime, 
         Collection<VersionType> versionTypesToSkip, Boolean preloadActiveOnly, File ... ibdfPreLoadFiles) throws Exception
   {
      converterUUID = Get.service(ConverterUUID.class);
      converterUUID.clearCache();
      UuidIntMapMap.NID_TO_UUID_CACHE_ENABLED = true;
      File file = new File(outputDirectory, "isaac-db");
      //make sure this is empty
      FileUtils.deleteDirectory(file);
      
      Get.configurationService().setDataStoreFolderPath(file.toPath());

      LookupService.startupIsaac();
      
      if (ibdfPreLoadFiles != null && ibdfPreLoadFiles.length > 0)
      {
         ConsoleUtil.println("Loading ibdf files");
         LoadTermstore lt = new LoadTermstore();
         lt.dontSetDBMode();
         lt.setLog(new SystemStreamLog());
         lt.setibdfFiles(ibdfPreLoadFiles);
         lt.setActiveOnly(preloadActiveOnly != null ? preloadActiveOnly : true);
         //skip descriptions, acceptabilities
         if (versionTypesToSkip != null)
         {
            lt.skipVersionTypes(versionTypesToSkip);
         }
         lt.execute();
         
         new IndexTermstore().execute();
      }
      
      //While I don't need/want the metadata to be loaded into the datastore/ibdf output file - I do need UUID to nid mappings for the metadata.
      for (ConceptSpecification cs : MetaData.META_DATA_CONCEPTS)
      {
         Get.identifierService().assignNid(cs.getUuids());
      }
      
      this.authorNid = MetaData.USER____SOLOR.getNid();
      this.terminologyPathNid = MetaData.DEVELOPMENT_PATH____SOLOR.getNid();
      
      //TODO automate this somehow....
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_ASSOCIATION.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_ASSOCIATION.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getDynamicColumns());
      registerDynamicColumnInfo(DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE.getPrimordialUuid(), 
            DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE.getDynamicColumns());

      //TODO figure out how to get rid of this copy/paste mess too      
      
      this.conceptBuilderService = Get.conceptBuilderService();
      this.conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____SOLOR);
      this.conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____SOLOR);
      this.conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

      this.expressionBuilderService = Get.logicalExpressionBuilderService();
      
      this.semanticBuilder = Get.semanticBuilderService();
      
      this.defaultTime = defaultTime;
      
      StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, terminologyPathNid);
      IBDFCreationUtility.readBackStamp = new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, NidSet.EMPTY, Status.ANY_STATUS_SET);
      
      UUID moduleUUID = moduleToCreate.isPresent() ? UuidT5Generator.get(UuidT5Generator.PATH_ID_FROM_FS_DESC, moduleToCreate.get()) : 
         preExistingModule.get().getPrimordialUuid();
      
      //If both modules are specified, use the parent grouping module.  If not, use the module as determined above.
      converterUUID.configureNamespace(((moduleToCreate.isPresent() && preExistingModule.isPresent()) ? preExistingModule.get().getPrimordialUuid() : 
         moduleUUID));
      
      //tack the version onto the end of the ibdf file, so that when multiple ibdf files for a single type of content, such as 
      //loinc 2.52, loinc 2.54 - we don't have a file name collision during the ibdf build.
      String outputName = outputArtifactId + (StringUtils.isBlank(outputArtifactClassifier) ? "" : "-" + outputArtifactClassifier) + "-" + outputArtifactVersion;
      
      this.writer = new MultipleDataWriterService(
            outputGson ? Optional.of(new File(outputDirectory, outputName + ".json").toPath()) : Optional.empty(),
                  Optional.of(new File(outputDirectory, outputName + ".ibdf").toPath()));
      
      if (moduleToCreate.isPresent())
      {
         this.module = ComponentReference.fromConcept(moduleUUID);
         createConcept(moduleUUID, moduleToCreate.get(), true, 
               preExistingModule.isPresent() ? preExistingModule.get().getPrimordialUuid() : MetaData.MODULE____SOLOR.getPrimordialUuid());
         ConsoleUtil.println("Creating module " + this.module.getPrimordialUuid() + " with description '" + moduleToCreate.get() + "'");
      }
      else
      {
         this.module = ComponentReference.fromConcept(preExistingModule.get().getPrimordialUuid(), preExistingModule.get().getNid());
      }
      
      ConsoleUtil.println("Loading with module '" + this.module.getPrimordialUuid() + "' (" + this.module.getNid() + ") on DEVELOPMENT path");
   }
   
   
   /**
    * This constructor is for use when we are using this utility code to process changes directly into a (already) running DB, rather than writing an IBDF file.
    * 
    * DANGER WILL ROBINSON!
    * Using this class at runtime is tricky, and not the designed use case.
    * If you do so make sure that you)
    * A) - only use a time of Long.MAX_VALUE.
    * B) - you may need to record some changes yourself, using {@link #storeManualUpdate(IsaacExternalizable)}
    * C) - the underlying ConverterUUID instance is not thread safe - only one thread at a time should use the IBDF Creation Utility this way.
    * 
    *  @param author - which author to use for these changes
    *  @param module - which module to use while loading
    *  @param path - which path to use for these changes
    *  @param debugOutputDirectory - optional - if provided, json debug files will be written here
    */
   public IBDFCreationUtility(UUID author, UUID module, UUID path, File debugOutputDirectory) throws Exception
   {
      this.converterUUID = Get.service(ConverterUUID.class);
      this.converterUUID.clearCache();
      this.authorNid = Get.identifierService().getNidForUuids(author);
      this.terminologyPathNid = Get.identifierService().getNidForUuids(path);
      this.module = ComponentReference.fromConcept(module);
      this.writeToDB = true;
      
      this.refexAllowedColumnTypes = null;

      this.conceptBuilderService = Get.conceptBuilderService();
      this.conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____SOLOR);
      this.conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____SOLOR);
      this.conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

      this.expressionBuilderService = Get.logicalExpressionBuilderService();
      
      this.semanticBuilder = Get.semanticBuilderService();
      
      this.defaultTime = Long.MAX_VALUE;
      
      StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, terminologyPathNid);
      IBDFCreationUtility.readBackStamp = new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, NidSet.EMPTY, Status.ANY_STATUS_SET);
      
      if (converterUUID.getNamespace() == null)
      {
         throw new RuntimeException("Namespace not configured!");
      }
      
      this.writer = new MultipleDataWriterService(debugOutputDirectory == null ? Optional.empty() : 
         Optional.of(new File(debugOutputDirectory, "xmlBatchLoad-" + defaultTime + ".json").toPath()), Optional.empty());
      
      ConsoleUtil.println("Loading with module '" + this.module.getPrimordialUuid() + "' (" + this.module.getNid() + ") on DEVELOPMENT path");
   }
   
   /**
    * This constructor is for use when we are using this utility code to wrap a loader like the RF2 direct loader, 
    * which uses a completely different paradigm.  Just need a constructor that lets me utilize some utility methods, 
    * without doing most of the work....
    * 
    * DANGER WILL ROBINSON!
    * Using this class at runtime is tricky, and not the designed use case.
    * If you do so make sure that you)
    * A) - only use a time of Long.MAX_VALUE.
    * B) - the underlying ConverterUUID instance is not thread safe - only one thread at a time should use the IBDF Creation Utility this way.
    * 
    *  @param author - which author to use for these changes
    *  @param module - which module to use while loading
    *  @param path - which path to use for these changes
    *  @param writer - the writer to write resulting IBDF into
    */
   public IBDFCreationUtility(UUID author, UUID module, UUID path, DataWriterService writer) throws Exception
   {
      converterUUID = Get.service(ConverterUUID.class);
      converterUUID.clearCache();
      this.authorNid = Get.identifierService().getNidForUuids(author);
      this.terminologyPathNid = Get.identifierService().getNidForUuids(path);
      this.module = ComponentReference.fromConcept(module);
      this.writeToDB = true;
      
      this.refexAllowedColumnTypes = null;

      this.conceptBuilderService = Get.conceptBuilderService();
      this.conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE____SOLOR);
      this.conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT____SOLOR);
      this.conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

      this.expressionBuilderService = Get.logicalExpressionBuilderService();
      
      this.semanticBuilder = Get.semanticBuilderService();
      
      this.defaultTime = Long.MAX_VALUE;
      
      StampPosition stampPosition = new StampPositionImpl(Long.MAX_VALUE, terminologyPathNid);
      IBDFCreationUtility.readBackStamp = new StampCoordinateImpl(StampPrecedence.PATH, stampPosition, NidSet.EMPTY, Status.ANY_STATUS_SET);
      
      if (converterUUID.getNamespace() == null)
      {
         throw new RuntimeException("Namespace not configured!");
      }
      
      this.writer = writer;
      
      ConsoleUtil.println("Loading with module '" + this.module.getPrimordialUuid() + "' (" + this.module.getNid() + ") on DEVELOPMENT path");
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description, calculates
    * the UUID, status current, etc)
    * @param fsn the fully specified name
    * @param createSynonymFromFSN true, to also create a preferred synonym
    * @return the created concept
    */
   public ConceptVersion createConcept(String fsn, boolean createSynonymFromFSN)
   {
      return createConcept(converterUUID.createNamespaceUUIDFromString(fsn), fsn, createSynonymFromFSN);
   }

   /**
    * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
    * @param fsn the fully specified name
    * @param createSynonymFromFSN true, to also create a preferred synonym
    * @param parentConceptPrimordial create an isA relationship to this concept
    * @return the created concept
    */
   public ConceptVersion createConcept(String fsn, boolean createSynonymFromFSN, UUID parentConceptPrimordial)
   {
      ConceptVersion concept = createConcept(fsn, createSynonymFromFSN);
      addParent(ComponentReference.fromConcept(concept), parentConceptPrimordial);
      return concept;
   }

   /**
    * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
    * @param conceptPrimordial the UUID to use for the concept
    * @param fsn the fully specified name
    * @param createSynonymFromFSN true, to also create a preferred synonym
    * @param parentConceptPrimordial create an isA relationship to this concept
    * @return the created concept
    */
   public ConceptVersion createConcept(UUID conceptPrimordial, String fsn, boolean createSynonymFromFSN, UUID parentConceptPrimordial)
   {
      ConceptVersion concept = createConcept(conceptPrimordial, fsn, createSynonymFromFSN);
      addParent(ComponentReference.fromConcept(concept), parentConceptPrimordial);
      return concept;
   }
   
   public ConceptVersion createConcept(UUID conceptPrimordialUuid, UUID ...additionalUUIDs)
   {
      return createConcept(conceptPrimordialUuid, (Long)null, Status.ACTIVE, null, additionalUUIDs);
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description (en US)
    * status current, etc
    * @param conceptPrimordial the UUID to use for the concept
    * @param fsn the fully specified name
    * @param createSynonymFromFSN true, to also create a preferred synonym
    * @return the created concept
    */
   public ConceptVersion createConcept(UUID conceptPrimordial, String fsn, boolean createSynonymFromFSN)
   {
      return createConcept(conceptPrimordial, fsn, createSynonymFromFSN, null, Status.ACTIVE);
   }

   /**
    * Create a concept, automatically setting as many fields as possible (adds a description (en US))
    * @param conceptPrimordial the UUID to use for the concept
    * @param fsn the fully specified name
    * @param createSynonymFromFSN true, to also create a preferred synonym
    * @param time - set to now if null
    * @return the created concept
    */
   public ConceptVersion createConcept(UUID conceptPrimordial, String fsn, boolean createSynonymFromFSN, Long time, Status status)
   {
      ConceptVersion cc = createConcept(conceptPrimordial, time, status, null);
      ComponentReference concept = ComponentReference.fromConcept(cc);
      addFullySpecifiedName(concept, fsn);
      if (createSynonymFromFSN)
      {
         addDescription(concept, fsn.endsWith(METADATA_SEMANTIC_TAG) ? fsn.substring(0, fsn.lastIndexOf(METADATA_SEMANTIC_TAG)) : fsn, 
               DescriptionType.REGULAR_NAME, true, null, Status.ACTIVE);
      }
      return cc;
   }

   /**
    * Just create a concept.
    * 
    * @param conceptPrimordialUuid
    * @param time - if null, set to default
    * @param status - if null, set to ACTIVE
    * @param module - if null, uses the default
    * @param additionalUUIDs additional UUIDs for this concept 
    * @return the created concept
    */
   public ConceptVersion createConcept(UUID conceptPrimordialUuid, Long time, Status status, UUID module, UUID ... additionalUUIDs) 
   {
      //If any of the provided UUIDs already have a nid assigned, use that one as the primordial.
      //If more than one of the provided UUIDs have a nid assigned, and they don't match, we are SOL.... but it will error out in the create later.
      UUID uuidWithNid = null;
      if (additionalUUIDs != null && additionalUUIDs.length > 0)
      {
         if (Get.identifierService().hasUuid(conceptPrimordialUuid))
         {
            uuidWithNid = conceptPrimordialUuid;
         }
         else
         {
            for (UUID uuid : additionalUUIDs)
            {
               if (Get.identifierService().hasUuid(uuid))
               {
                  uuidWithNid = uuid;
                  break;
               }
            }
         }
      }
      
      ConceptChronologyImpl conceptChronology = new ConceptChronologyImpl(uuidWithNid == null ? conceptPrimordialUuid : uuidWithNid, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
      ConceptVersionImpl conceptVersion = conceptChronology.createMutableVersion(createStamp(status, time, module));
      if (additionalUUIDs != null)
      {
         conceptVersion.addAdditionalUuids(additionalUUIDs);
         conceptVersion.addAdditionalUuids(conceptPrimordialUuid);  //We may not have used the primoridial in the constructor above, make sure its added here.
         //The addAdditionalUUIDs already remove duplicates / don't duplicate the primordial.
      }
      this.writer.put(conceptChronology);
      dbWrite(conceptChronology);
      this.ls.addConcept();
      return conceptVersion;
   }
   
   /**
    * Utility method to build and store a concept.
    * @param primordial - optional
    * @param fsnName
    * @param preferredName - optional
    * @param altName - optional
    * @param definition - optional
    * @param parentConceptPrimordial
    * @param secondParent - optional
    * @return the created concept
    */
   public ConceptVersion createConcept(UUID primordial, String fsnName, String preferredName, String altName, 
         String definition, UUID parentConceptPrimordial, UUID secondParent)
   {
      ConceptVersion concept = createConcept(primordial == null ? converterUUID.createNamespaceUUIDFromString(fsnName) : primordial,
            fsnName, StringUtils.isEmpty(preferredName) ? true : false);
      
      LogicalExpressionBuilder leb = this.expressionBuilderService.getLogicalExpressionBuilder();

      if (secondParent == null)
      {
         NecessarySet(And(ConceptAssertion(Get.identifierService().getNidForUuids(parentConceptPrimordial), leb)));
      }
      else
      {
         NecessarySet(And(ConceptAssertion(Get.identifierService().getNidForUuids(parentConceptPrimordial), leb), 
            ConceptAssertion(Get.identifierService().getNidForUuids(secondParent), leb)));
         
      }
      
      LogicalExpression logicalExpression = leb.build();
      
      addRelationshipGraph(ComponentReference.fromConcept(concept), null, logicalExpression, true, null, null);
      
      if (StringUtils.isNotEmpty(preferredName))
      {
         addDescription(ComponentReference.fromConcept(concept), preferredName, DescriptionType.REGULAR_NAME, true, null, Status.ACTIVE);
      }
      if (StringUtils.isNotEmpty(altName) && !altName.equals(preferredName))
      {
         addDescription(ComponentReference.fromConcept(concept), altName, DescriptionType.REGULAR_NAME, false, null, Status.ACTIVE);
      }
      if (StringUtils.isNotEmpty(definition))
      {
         addDescription(ComponentReference.fromConcept(concept), definition, DescriptionType.DEFINITION, true, null, Status.ACTIVE);
      }
      
      return concept;
   }

   /**
    * Add a workbench official "Fully Specified Name".  Convenience method for adding a description of type FSN
    * @param concept the concept to add the description to
    * @param fullySpecifiedName the description to add
    */
   public SemanticChronology addFullySpecifiedName(ComponentReference concept, String fullySpecifiedName)
   {
      return addDescription(concept, fullySpecifiedName, DescriptionType.FULLY_QUALIFIED_NAME, true, null, Status.ACTIVE);
   }
   
   
   /**
    * Add a batch of WB descriptions, following WB rules in always generating a FSN (picking the value based on the propertySubType order). 
    * And then adding other types as specified by the propertySubType value, setting preferred / acceptable according to their ranking. 
    * @param concept the concept to add the description to
    * @param descriptions the description to add
    */
   public List<SemanticChronology> addDescriptions(ComponentReference concept, List<? extends ValuePropertyPair> descriptions)
   {
      ArrayList<SemanticChronology> result = new ArrayList<>(descriptions.size());
      Collections.sort(descriptions);
      
      boolean haveFSN = false;
      boolean havePreferredSynonym = false;
      boolean havePreferredDefinition = false;
      for (ValuePropertyPair vpp : descriptions)
      {
         DescriptionType descriptionType = null;
         boolean preferred;
         
         if (!haveFSN)
         {
            descriptionType = DescriptionType.FULLY_QUALIFIED_NAME;
            preferred = true;
            haveFSN = true;
         }
         else
         {
            if (vpp.getProperty().getPropertySubType() < BPT_Descriptions.SYNONYM)
            {
               descriptionType = DescriptionType.FULLY_QUALIFIED_NAME;
               preferred = false;  //true case is handled above
            }
            else if (vpp.getProperty().getPropertySubType() >= BPT_Descriptions.SYNONYM && 
                  (vpp.getProperty().getPropertySubType() < BPT_Descriptions.DEFINITION || vpp.getProperty().getPropertySubType() == Integer.MAX_VALUE))
            {
               descriptionType = DescriptionType.REGULAR_NAME;
               if (!havePreferredSynonym)
               {
                  preferred = true;
                  havePreferredSynonym = true;
               }
               else
               {
                  preferred = false;
               }
            }
            else if (vpp.getProperty().getPropertySubType() >= BPT_Descriptions.DEFINITION)
            {
               descriptionType = DescriptionType.DEFINITION;
               if (!havePreferredDefinition)
               {
                  preferred = true;
                  havePreferredDefinition = true;
               }
               else
               {
                  preferred = false;
               }
            }
            else
            {
               throw new RuntimeException("Unexpected error");
            }
         }
         
         if (!(vpp.getProperty().getPropertyType() instanceof BPT_Descriptions))
         {
            throw new RuntimeException("This method requires properties that have a parent that are an instance of BPT_Descriptions");
         }
         result.add(addDescription(concept, vpp.getUUID(), vpp.getValue(), descriptionType, preferred, null, null, null, null, vpp.getProperty().getUUID(), 
               (vpp.isDisabled() ? Status.INACTIVE : Status.ACTIVE), vpp.getTime()));
      }
      
      return result;
   }
   
   /**
    * Add a description to the concept.  UUID for the description is calculated from the target concept, description value, type, and preferred flag.
    * @param concept the concept to add a description to
    * @param descriptionValue the text for the description
    * @param wbDescriptionType the type of the description
    * @param preferred true for preferred
    * @param sourceDescriptionType - this optional value is attached as the extended description type
    * @param status
    * @return
    */
   public SemanticChronology addDescription(ComponentReference concept, String descriptionValue, DescriptionType wbDescriptionType, 
         boolean preferred, UUID sourceDescriptionType, Status status)
   {
      return addDescription(concept, null, descriptionValue, wbDescriptionType, preferred, null, null, null, null, sourceDescriptionType, 
            status, null);
   }
   
   /**
    * Add a description to the concept.
    * @param concept the concept to add the description to
    * @param descriptionPrimordial the UUID to use for the description
    * @param descriptionValue the text value of the description
    * @param wbDescriptionType the description type
    * @param preferred true for preferred
    * @param sourceDescriptionType - this optional value is attached as the extended description type
    * @param status status of the description
    * @return
    */
   public SemanticChronology addDescription(ComponentReference concept, UUID descriptionPrimordial, String descriptionValue, 
      DescriptionType wbDescriptionType, boolean preferred, UUID sourceDescriptionType, Status status)
   {
      return addDescription(concept, descriptionPrimordial, descriptionValue, wbDescriptionType, preferred, null, null, null, null, sourceDescriptionType, 
            status, null);
   }
   

   /**
    * Add a description to the concept.
    * 
    * @param concept - the concept to add this description to
    * @param descriptionPrimordial - if not supplied, created from the concept UUID and the description value and description type
    * @param descriptionValue - the text value
    * @param wbDescriptionType - the type of the description
    * @param preferred - true, false, or null to not create any acceptability entry see {@link #addDescriptionacceptability()}
    * @param dialect - ignored if @param preferred is set to null.  if null, defaults to {@link MetaData#US_ENGLISH_DIALECT}
    * @param caseSignificant - if null, defaults to {@link MetaData#DESCRIPTION_NOT_CASE_SENSITIVE}
    * @param languageCode - if null, uses {@link MetaData#ENGLISH_LANGUAGE}
    * @param module - if null, uses the default from the EConceptUtility instance
    * @param sourceDescriptionType - this optional value is attached as the extended description type
    * @param status active / inactive
    * @param time - defaults to concept time
    */
   public SemanticChronology addDescription(ComponentReference concept, UUID descriptionPrimordial, String descriptionValue, 
         DescriptionType wbDescriptionType, Boolean preferred, UUID dialect, UUID caseSignificant, UUID languageCode, UUID module, 
         UUID sourceDescriptionType, Status status, Long time)
   {
      if (descriptionValue == null)
      {
         throw new RuntimeException("Description value is required");
      }
      if (dialect == null)
      {
         dialect =  MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid();
      }
      if (languageCode == null)
      {
         languageCode = MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid();
      }
      
      SemanticBuilder<? extends SemanticChronology> descBuilder = semanticBuilder.getDescriptionBuilder(
                  Get.identifierService().getNidForUuids(caseSignificant == null ? MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid() : caseSignificant),
                  Get.identifierService().getNidForUuids(languageCode),
                  wbDescriptionType.getConceptSpec().getNid(), 
                  descriptionValue, 
                  concept.getNid());
      if (descriptionPrimordial == null) {
         descBuilder.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));
      } else {
         descBuilder.setPrimordialUuid(descriptionPrimordial);
      }
      
      List<Chronology> builtObjects = new ArrayList<>();
      
      SemanticChronology newDescription = (SemanticChronology)
            descBuilder.build(
                  createStamp(status, selectTime(concept, time), module), 
                  builtObjects);

      if (preferred == null)
      {
         //noop
      }
      else
      {
         SemanticBuilder<? extends SemanticChronology> acceptabilityTypeBuilder = semanticBuilder.getComponentSemanticBuilder(
               preferred ? TermAux.PREFERRED.getNid() : TermAux.ACCEPTABLE.getNid(), newDescription.getNid(),
               Get.identifierService().getNidForUuids(dialect));

         acceptabilityTypeBuilder.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));
         acceptabilityTypeBuilder.build(createStamp(status, selectTime(concept, time), module), builtObjects);

         this.ls.addAnnotation("Description", getOriginStringForUuid(dialect));
      }
      
      for (IsaacExternalizable ochreObject : builtObjects)
      {
         this.writer.put(ochreObject);
         dbWrite(ochreObject);
      }
      
      this.ls.addDescription(wbDescriptionType.name() + (sourceDescriptionType == null ? "" : ":" + getOriginStringForUuid(sourceDescriptionType)));
      
      if (sourceDescriptionType != null)
      {
         addAnnotation(ComponentReference.fromChronology(newDescription, () -> "Description"), null, 
               (sourceDescriptionType == null ? null : new DynamicUUIDImpl(sourceDescriptionType)),
               DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getPrimordialUuid(), null, null);
      }
      
      return newDescription;
   }
   
   /**
    * Add an acceptability to a description
    * 
    * @param description - the description to add put the acceptability on
    * @param acceptabilityPrimordial - if not supplied, created from the description UUID, dialectRefsetg and preferred flag
    * @param dialectRefset - A UUID for a refset like MetaData.US_ENGLISH_DIALECT
    * @param preferred - true for preferred, false for acceptable
    * @param status - 
    * @param time - if null, uses the description time
    * @param module - optional
    */
   public SemanticChronology addDescriptionAcceptability(ComponentReference description, UUID acceptabilityPrimordial, 
         UUID dialectRefset, boolean preferred, Status status, Long time, UUID module)
   {
      SemanticBuilder<? extends SemanticChronology> sb = Get.semanticBuilderService().getComponentSemanticBuilder(preferred ? TermAux.PREFERRED.getNid() : TermAux.ACCEPTABLE.getNid(),
            description.getNid(), Get.identifierService().getNidForUuids(dialectRefset));
      
      if (acceptabilityPrimordial == null) {
         sb.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));
      } else {
         sb.setPrimordialUuid(acceptabilityPrimordial);
      }
      
      ArrayList<Chronology> builtObjects = new ArrayList<>();
      SemanticChronology sc = sb.build(createStamp(status, selectTime(description, time), module), builtObjects);
      for (IsaacExternalizable ochreObject : builtObjects)
      {
         this.writer.put(ochreObject);
         dbWrite(ochreObject);
      }

      this.ls.addAnnotation("Description", getOriginStringForUuid(dialectRefset));
      return sc;
   }

   /**
    * uses the concept time, UUID is created from the component UUID, the annotation value and type.  Creates a dynamic string semantic
    * @param referencedComponent the component to put an annotation on
    * @param annotationValue the value of the annotation
    * @param annotationTypeAssemblage the type of the annotation
    * @param status the state of the new annotation
    * @return
    */
   public SemanticChronology addStringAnnotation(ComponentReference referencedComponent, String annotationValue, UUID annotationTypeAssemblage, Status status)
   {
      return addAnnotation(referencedComponent, null, new DynamicData[] {new DynamicStringImpl(annotationValue)}, annotationTypeAssemblage, status, null, null);
   }
   
   /**
    * uses the concept time.
    * Note - this convenience method automatically chooses between creating a static string semantic, and a dynamic string semantic, depending on if the passed in 
    * refsetUuid is marked as an identifier type.

    * @param referencedComponent the component to put the annotation on
    * @param uuidForCreatedAnnotation the optional uuid for the created annotation
    * @param annotationValue the value of the annotation
    * @param annotationTypeAssemblage the type of the annotation 
    * @param status the optional status of the newly created assemblage
    * @return
    */
   public SemanticChronology addStringAnnotation(ComponentReference referencedComponent, UUID uuidForCreatedAnnotation, String annotationValue, 
      UUID annotationTypeAssemblage, Status status)
   {
      if (this.identifierTypes.contains(annotationTypeAssemblage))
      {
         return addStaticStringAnnotation(referencedComponent, uuidForCreatedAnnotation, annotationValue, annotationTypeAssemblage, status);
      }
      else
      {
         return addAnnotation(referencedComponent, uuidForCreatedAnnotation, new DynamicData[] {new DynamicStringImpl(annotationValue)}, 
               annotationTypeAssemblage, status, null, null);
      }
   }
   
   /**
    * Mark an item as a member of the specified dynamic assemblage (make a refset member)
    * @param referencedComponent the  item to be added to the assemblage membership
    * @param membershipAssemblageType the assemblage to be listed as a member of
    * @param status the status of the assemblage membership
    * @param time the optional time to use for the newly created assemblage
    * @return
    */
   public SemanticChronology addAssemblageMembership(ComponentReference referencedComponent, UUID membershipAssemblageType, Status status, Long time)
   {
      return addAnnotation(referencedComponent, null, (DynamicData[])null, membershipAssemblageType, status, time, null);
   }
   
   /**
    * @param referencedComponent The component to attach this annotation to
    * @param uuidForCreatedAnnotation  - the UUID to use for the created annotation.  If null, generated from uuidForCreatedAnnotation, value, refexDynamicTypeUuid
    * @param value - the value to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
    * of the semantic represented by annotationTypeAssemblage
    * @param annotationTypeAssemblage - the uuid of the dynamic semantic type - 
    * @param status -  Status or null (for active)
    * @param time - if null, uses the component time
    * @return
    */
   public SemanticChronology addAnnotation(ComponentReference referencedComponent, UUID uuidForCreatedAnnotation, DynamicData value, 
         UUID annotationTypeAssemblage, Status status, Long time)
   {
      return addAnnotation(referencedComponent, uuidForCreatedAnnotation, 
            (value == null ? new DynamicData[] {} : new DynamicData[] {value}), annotationTypeAssemblage, status, time, null);
   }
   
   /**
    * @param referencedComponent The component to attach this annotation to
    * @param uuidForCreatedAnnotation  - the UUID to use for the created annotation.  If null, generated from uuidForCreatedAnnotation, value, refexDynamicTypeUuid
    * @param values - the values to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
    * of the semantic represented by annotationTypeAssemblage
    * @param annotationTypeAssemblage - the uuid of the dynamic semantic type - 
    * @param status -  Status or null (for active)
    * @param time - if null, uses the component time
    * @param module - optional
    * @return
    */
   public SemanticChronology addAnnotation(ComponentReference referencedComponent, UUID uuidForCreatedAnnotation, DynamicData[] values, 
         UUID annotationTypeAssemblage, Status status, Long time, UUID module)
   {
      validateDataTypes(annotationTypeAssemblage, values);
      SemanticBuilder<? extends SemanticChronology>  sb = semanticBuilder.getDynamicBuilder(referencedComponent.getNid(), 
            Get.identifierService().getNidForUuids(annotationTypeAssemblage), values);
      
      if (uuidForCreatedAnnotation == null) {
         sb.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));
      } else {
         sb.setPrimordialUuid(uuidForCreatedAnnotation);
      }
      
      ArrayList<Chronology> builtObjects = new ArrayList<>();
      SemanticChronology sc = (SemanticChronology)sb.build(createStamp(status, selectTime(referencedComponent, time), module), 
            builtObjects);
      
      for (IsaacExternalizable ochreObject : builtObjects)
      {
         this.writer.put(ochreObject);
         dbWrite(ochreObject);
      }
      if (values == null || values.length == 0)
      {
         this.ls.addRefsetMember(getOriginStringForUuid(annotationTypeAssemblage));
      }
      else
      {
         if (BPT_Associations.isAssociation(annotationTypeAssemblage))
         {
            this.ls.addAssociation(getOriginStringForUuid(annotationTypeAssemblage));
         }
         else
         {
            this.ls.addAnnotation((referencedComponent.getTypeString().length() == 0 ? 
                  getOriginStringForUuid(referencedComponent.getPrimordialUuid())
                        : referencedComponent.getTypeString()), getOriginStringForUuid(annotationTypeAssemblage));
         }
      }
      return sc;
   }
   
   /**
    * @param ochreObject
    */
   private void dbWrite(IsaacExternalizable ochreObject)
   {
      if (this.writeToDB)
      {
         try
         {
            if (ochreObject instanceof ConceptChronology)
            {
               Get.commitService().addUncommitted((ConceptChronology)ochreObject).get();
            }
            else if (ochreObject instanceof SemanticChronology)
            {
               Get.commitService().addUncommitted((SemanticChronology)ochreObject).get();
            }
            else
            {
               throw new RuntimeException("Unexpected type! " + ochreObject);
            }
         }
         catch (Exception e)
         {
            if (e instanceof RuntimeException)
            {
               throw (RuntimeException)e;
            }
            else
            {
               throw new RuntimeException("Unexpected error doing add Uncommitted", e);
            }
         }
      }
   }

   /**
    * 
    * @param assemblageType
    * @return
    */
   private boolean isConfiguredAsDynamicSemantic(UUID assemblageType)
   {
      if (this.refexAllowedColumnTypes == null)
      {
         return DynamicUsageDescriptionImpl.isDynamicSemantic(Get.identifierService().getNidForUuids(assemblageType));
      }
      else
      {
         return this.refexAllowedColumnTypes.containsKey(assemblageType);
      }
   }

   /**
    * 
    * @param referencedComponent
    * @param assemblage
    * @return
    */
   private SemanticChronology addMembership(ComponentReference referencedComponent, ConceptSpecification assemblage) {
      SemanticBuilder<? extends SemanticChronology> sb = Get.semanticBuilderService().getMembershipSemanticBuilder(referencedComponent.getNid(), assemblage.getNid());
      sb.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));

      ArrayList<Chronology> builtObjects = new ArrayList<>();
      SemanticChronology sc = (SemanticChronology)sb.build(createStamp(Status.ACTIVE, selectTime(referencedComponent, (Long)null)), builtObjects);

      for (IsaacExternalizable ochreObject : builtObjects)
      {
         this.writer.put(ochreObject);
         dbWrite(ochreObject);
      }

      this.ls.addRefsetMember(getOriginStringForUuid(assemblage.getPrimordialUuid()));
   
      return sc;
   }
   /**
    * @param assemblageType
    * @param values
    */
   private void validateDataTypes(UUID assemblageType, DynamicData[] values)
   {
      //TODO [DAN 3] this should be a much better validator - checking all of the various things in DynamicUtility.validate - 
      //need to enforce the restrictions defined in the columns in the validators
      
      DynamicColumnInfo[] colInfo;
      
      if (this.refexAllowedColumnTypes == null)
      {
         colInfo = DynamicUsageDescriptionImpl.read(Get.identifierService().getNidForUuids(assemblageType)).getColumnInfo();
      }
      else
      {
         if (!this.refexAllowedColumnTypes.containsKey(assemblageType))
         {
            throw new RuntimeException("Attempted to store data on a concept not configured as a dynamic semantic: " + assemblageType + " values: " +
                  (values == null ? "" : Arrays.toString(values)));
         }
         
         colInfo = this.refexAllowedColumnTypes.get(assemblageType);
      }
      
      if (values != null && values.length > 0)
      {
         if (colInfo != null)
         {
            for (int i = 0; i < values.length; i++)
            {
               DynamicColumnInfo column = null;
               for (DynamicColumnInfo x : colInfo)
               {
                  if(x.getColumnOrder() == i)
                  {
                     column = x;
                     break;
                  }
               }
               if (column == null)
               {
                  throw new RuntimeException("Column count mismatch");
               }
               else
               {
                  if (values[i] == null && column.isColumnRequired())
                  {
                     throw new RuntimeException("Missing column data for column " + column.getColumnName());
                  }
                  else if (values[i] != null && column.getColumnDataType() != values[i].getDynamicDataType() 
                        && column.getColumnDataType() != DynamicDataType.POLYMORPHIC)
                  {
                     throw new RuntimeException("Datatype mismatch - " + column.getColumnDataType() + " - " + values[i].getDynamicDataType());
                  }
               }
            }
         }
         else if (values.length > 0)
         {
            throw new RuntimeException("Column count mismatch - this dynamic semantic doesn't allow columns!");
         }
      }
      else if (colInfo != null)
      {
         for (DynamicColumnInfo ci : colInfo)
         {
            if (ci.isColumnRequired())
            {
               throw new RuntimeException("Missing column data for column " + ci.getColumnName());
            }
         }
      }
   }
   
   /**
    * uses the concept time, UUID is created from the component UUID, the annotation value and type.
    * @param referencedComponent
    * @param annotationValue
    * @param annotationAssemblageType
    * @param status
    * @return
    */
   public SemanticChronology addStaticStringAnnotation(ComponentReference referencedComponent, String annotationValue, UUID annotationAssemblageType, 
         Status status)
   {
      return addStaticStringAnnotation(referencedComponent, null, annotationValue, annotationAssemblageType, status);
   }
   
   /**
    * uses the concept time, UUID is created from the component UUID, the annotation value and type if the uuidForCreationAnnotation is null.
    * @param referencedComponent
    * @param uuidForCreatedAnnotation
    * @param annotationValue
    * @param annotationTypeAssemblage
    * @param status
    * @return
    */
   public SemanticChronology addStaticStringAnnotation(ComponentReference referencedComponent, UUID uuidForCreatedAnnotation, String annotationValue, 
         UUID annotationTypeAssemblage, Status status)
   {
      SemanticBuilder<? extends SemanticChronology>  sb = semanticBuilder.getStringSemanticBuilder(annotationValue, referencedComponent.getNid(), 
            Get.identifierService().getNidForUuids(annotationTypeAssemblage));
      
      if (uuidForCreatedAnnotation == null)
      {
         sb.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));
      }
      else
      {
         sb.setPrimordialUuid(uuidForCreatedAnnotation);
      }

      ArrayList<Chronology> builtObjects = new ArrayList<>();
      SemanticChronology sc = (SemanticChronology)sb.build(createStamp(status, selectTime(referencedComponent, null)), builtObjects);
      
      for (IsaacExternalizable ochreObject : builtObjects)
      {
         this.writer.put(ochreObject);
         dbWrite(ochreObject);
      }
      this.ls.addAnnotation(
            referencedComponent.getTypeString().length() > 0 ? referencedComponent.getTypeString() : getOriginStringForUuid(referencedComponent.getPrimordialUuid()),
            getOriginStringForUuid(annotationTypeAssemblage));

      return sc;
   }

   /**
    * Generates the UUID, uses the component time
    * @param object
    * @param value
    * @param annotationTypeAssemblage
    * @return
    */
   public SemanticChronology addUUIDAnnotation(ComponentReference object, UUID value, UUID annotationTypeAssemblage)
   {
      return addAnnotation(object, null, new DynamicData[] {new DynamicUUIDImpl(value)}, annotationTypeAssemblage, null, null, null);
   }
   
   /**
    * Add an association. The source of the association is assumed to be the specified concept.
    * 
    * @param concept
    * @param associationPrimordialUuid - optional - if not provided, created from the source, target and type.
    * @param target
    * @param associationTypeAssemblage required
    * @param status
    * @param time - if null, default is used
    * @param module - optional
    */
   public SemanticChronology addAssociation(ComponentReference concept, UUID associationPrimordialUuid, UUID target, 
         UUID associationTypeAssemblage, Status status, Long time, UUID module)
   {
      if (!isConfiguredAsDynamicSemantic(associationTypeAssemblage))
      {
         ConsoleUtil.printErrorln("Asked to create an association with an unregistered association type.  This is deprecated, and should be fixed...");
         configureConceptAsAssociation(associationTypeAssemblage, null);
      }
      return addAnnotation(concept, associationPrimordialUuid, 
            new DynamicData[]{new DynamicUUIDImpl(target)}, 
            associationTypeAssemblage, status, time, module);
   }

   /**
    * Add Stated IS_A_REL relationships, with the time set to now.
    * Can only be called once per concept.
    * 
    * @param concept - ComponentReference for [child] concept to which to add parents
    * @param parentUuids - UUIDs of concepts to add as parents
    */
   public void addParents(ComponentReference concept, UUID...parentUuids) {
      addParents(concept, true, parentUuids);
   }

   /**
    * Add Statusd or inferred IS_A_REL relationships, with the time set to now.
    * Can only be called once per concept.
    * 
    * @param concept - ComponentReference for [child] concept to which to add parents
    * @param stated - boolean indicating that new graph should be Statusd
    * @param parents - UUIDs of concepts to add as parents
    */
   public void addParents(ComponentReference concept, boolean stated, UUID...parents) {
      // Ignore calls with empty arg lists
      if (parents != null && parents.length > 0) {
         // Check appropriate cache of concepts to which parents have been added
         // to prevent adding parents twice
         if ((stated ? conceptHasStatedGraph : conceptHasInferredGraph).contains(concept.getPrimordialUuid()))
         {
            throw new RuntimeException("Can only call an addParent() or addParents() method with Statusd=" + stated + " once!  Must utilize addRelationshipGraph for more complex objects." 
                  + " Parents: " + Arrays.toString(parents)
                  + " Child: " + concept.getPrimordialUuid()); 
         }

         // Don't add child concept to appropriate cache of concepts to which parents have been added
         // to prevent adding parents twice, because addRelationshipGraph() will do that,
         // and will fail if it has already been done

         final LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();

         // Eliminate duplicates
         final Set<UUID> uuids = new HashSet<>();
         for (UUID parentUuid : parents) {
            uuids.add(parentUuid);
         }
         
         final Assertion[] assertions = new Assertion[uuids.size()];
         
         int i = 0;
         for (UUID parentUuid : uuids) {
            assertions[i++] = ConceptAssertion(Get.identifierService().getNidForUuids(parentUuid), leb);
         }
         NecessarySet(And(assertions));
      
         addRelationshipGraph(concept, null, leb.build(), stated, null, null);
      }
   }

   /**
    * Add an IS_A_REL relationship, with the time set to now.
    * Can only be called once per concept.
    * @param concept
    * @param parent
    * @return
    */
   public SemanticChronology addParent(ComponentReference concept, UUID parent)
   {
      return addParent(concept, null, new UUID[] {parent}, null, null);
   }

   /**
    * This rel add method handles the advanced cases where a rel type 'foo' is actually being loaded as "is_a" (or some other arbitrary type)
    * it makes the swap, and adds the second value as a UUID annotation on the created relationship. 
    * Can only be called once per concept
    * @param concept
    * @param parent
    * @param p the property type, to read the WBType info from to determine if a native relationship annotation needs to be added.
    * @param time
    * @return
    */
   public SemanticChronology addParent(ComponentReference concept, UUID parent, Property p, Long time)
   {
      if (p.getWBTypeUUID() == null)
      {
         return addParent(concept, null, new UUID[] {parent}, null, time);
      }
      else
      {
         return addParent(concept, null, new UUID[] {parent}, p.getUUID(), time);
      }
   }
   
   /**
    * Add a parent (is a ) relationship. The source of the relationship is assumed to be the specified concept.
    * Can only be called once per concept
    * 
    * @param concept
    * @param relPrimordial - optional - if not provided, created from the source, target and type.
    * @param parents
    * @param sourceRelType - optional - native relationship type being mapped to isa
    * @param time - if null, default is used
    */
   public SemanticChronology addParent(ComponentReference concept, UUID relPrimordial, UUID[] parents, UUID sourceRelType, Long time)
   {
      if (this.conceptHasStatedGraph.contains(concept.getPrimordialUuid()))
      {
         throw new RuntimeException("Can only call addParent once!  Must utilize addRelationshipGraph for more complex objects.  " 
               + "Parents: " + Arrays.toString(parents) + " Child: " + concept.getPrimordialUuid()); 
      }
      
      this.conceptHasStatedGraph.add(concept.getPrimordialUuid());
      LogicalExpressionBuilder leb = this.expressionBuilderService.getLogicalExpressionBuilder();

      //We are only building isA here, choose necessary set over sufficient.
      
      ConceptAssertion[] cas = new ConceptAssertion[parents.length];
      for (int i = 0; i < parents.length; i++)
      {
         cas[i] = ConceptAssertion(Get.identifierService().getNidForUuids(parents[i]), leb);
      }
      
      NecessarySet(And(cas));

      LogicalExpression logicalExpression = leb.build();

      SemanticBuilder<? extends SemanticChronology> sb = semanticBuilder.getLogicalExpressionBuilder(logicalExpression, concept.getNid(),
           this.conceptBuilderService.getDefaultLogicCoordinate().getStatedAssemblageNid());

      
      if (relPrimordial != null) {
         sb.setPrimordialUuid(relPrimordial);
      } else {
         sb.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));
      }

      ArrayList<Chronology> builtObjects = new ArrayList<>();

      SemanticChronology sci = (SemanticChronology) sb.build(createStamp(Status.ACTIVE, selectTime(concept, time)), builtObjects);

      for (IsaacExternalizable ochreObject : builtObjects)
      {
         this.writer.put(ochreObject);
         dbWrite(ochreObject);
      }

      if (sourceRelType != null)
      {
         addUUIDAnnotation(ComponentReference.fromChronology(sci, () -> "Graph"), sourceRelType,
               DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE.getPrimordialUuid());
         ls.addRelationship(getOriginStringForUuid(isARelUuid_) + ":" + getOriginStringForUuid(sourceRelType));
      }
      else
      {
         ls.addRelationship(getOriginStringForUuid(isARelUuid_));
      }
      return sci;
   }
   
   /**
    * 
    * @param concept
    * @param graphPrimordial optional UUID of the created graph
    * @param logicalExpression
    * @param status
    * @param time
    * @param module
    * @return
    */
   public SemanticChronology addRelationshipGraph(ComponentReference concept, UUID graphPrimordial, 
         LogicalExpression logicalExpression, boolean status, Long time, UUID module)
   {
      return addRelationshipGraph(concept, graphPrimordial, logicalExpression, status, time, module, null);
   }
   
   /**
    * @param concept
    * @param graphPrimordial
    * @param logicalExpression
    * @param stated
    * @param time
    * @param module
    * @param sourceRelType - optional - native relationship type being mapped to isa
    * @return
    */
   public SemanticChronology addRelationshipGraph(ComponentReference concept, UUID graphPrimordial, 
         LogicalExpression logicalExpression, boolean stated, Long time, UUID module, UUID sourceRelType)
   {
      HashSet<UUID> temp = stated ? this.conceptHasStatedGraph : this.conceptHasInferredGraph;
      if (temp.contains(concept.getPrimordialUuid()))
      {
         throw new RuntimeException("Already have a " + (stated ? "stated" : "inferred") + " graph for concept " + concept.getPrimordialUuid());
      }
      temp.add(concept.getPrimordialUuid());
      
      SemanticBuilder<? extends SemanticChronology> sb = this.semanticBuilder.getLogicalExpressionBuilder(logicalExpression, concept.getNid(),
            stated ? this.conceptBuilderService.getDefaultLogicCoordinate().getStatedAssemblageNid() : 
               this.conceptBuilderService.getDefaultLogicCoordinate().getInferredAssemblageNid());

      if (graphPrimordial == null)
      {
         sb.setT5Uuid(converterUUID.getNamespace(), (name, uuid) -> converterUUID.addMapping(name, uuid));
      }
      else
      {
         sb.setPrimordialUuid(graphPrimordial);
      }      

      ArrayList<Chronology> builtObjects = new ArrayList<>();

      SemanticChronology sci = (SemanticChronology) sb.build(
            createStamp(Status.ACTIVE, selectTime(concept, time), module), builtObjects);
      
      if (sourceRelType != null)
      {
         builtObjects.add(addUUIDAnnotation(ComponentReference.fromChronology(sci, () -> "Graph"), sourceRelType,
               DynamicConstants.get().DYNAMIC_EXTENDED_RELATIONSHIP_TYPE.getPrimordialUuid()));
         ls.addRelationship(getOriginStringForUuid(isARelUuid_) + ":" + getOriginStringForUuid(sourceRelType));
      }

      for (IsaacExternalizable ochreObject : builtObjects)
      {
         this.writer.put(ochreObject);
         dbWrite(ochreObject);
      }

      ls.addGraph();
      return sci;
   }
   
   /**
    * uses providedTime first, if present, followed by readTimeFrom.
    * 
    * Note, this still may return null.
    * 
    * @param readTimeFrom
    * @param providedTime
    * @return
    */
   public Long selectTime(ComponentReference readTimeFrom, Long providedTime)
   {
      if (providedTime != null)
      {
         return providedTime;
      }
      else if (readTimeFrom != null)
      {
         return readTimeFrom.getTime();
      }
      else {
         return null;
      }
   }

   /**
    * Set up all the boilerplate stuff.
    * 
    * @param status - Status or null (for current)
    * @param time - time or null (for default)
    */
   public int createStamp(Status status, Long time) 
   {
      return createStamp(status, time, null);
   }
   
   /**
    * Set up all the boilerplate stuff.
    * 
    * @param status - Status or null (for active)
    * @param module - module or null for default module
    * @param time - time or null (for default)
    * @return the stamp identifier
    */
   public int createStamp(Status status, Long time, UUID module) 
   {
      return Get.stampService().getStampSequence(
           status == null ? Status.ACTIVE : status,
            time == null ? this.defaultTime : time.longValue(), 
                 this.authorNid, (module == null ? this.module.getNid() : Get.identifierService().getNidForUuids(module)), this.terminologyPathNid);
   }

   /**
    * 
    * @param uuid
    * @return
    */
   private String getOriginStringForUuid(UUID uuid)
   {
      String temp = converterUUID.getUUIDCreationString(uuid);
      if (temp != null)
      {
         String[] parts = temp.split(":");
         if (parts != null && parts.length > 1)
         {
            return parts[parts.length - 1];
         }
         return temp;
      }
      return "Unknown";
   }

   /**
    * 
    * @return
    */
   public ComponentReference getModule()
   {
      return this.module;
   }

   /**
    * 
    * @return
    */
   public LoadStats getLoadStats()
   {
      return this.ls;
   }

   /**
    * 
    */
   public void clearLoadStats()
   {
      this.ls = new LoadStats();
   }

   /**
    * 
    * @param propertyType
    * @param parentPrimordial
    * @throws Exception
    */
   public void loadMetaDataItems(PropertyType propertyType, UUID parentPrimordial) throws Exception
   {
      ArrayList<PropertyType> propertyTypes = new ArrayList<PropertyType>();
      propertyTypes.add(propertyType);
      loadMetaDataItems(propertyTypes, parentPrimordial);
   }

   /**
    * Create metadata concepts from the PropertyType structure
    * @param propertyTypes
    * @param parentPrimordial
    * @throws Exception
    */
   public void loadMetaDataItems(Collection<PropertyType> propertyTypes, UUID parentPrimordial) throws Exception
   {
      for (PropertyType pt : propertyTypes)
      {
         if (pt instanceof BPT_Skip)
         {
            continue;
         }
         
         ConceptChronology groupingConcept;
         
         if (!this.writeToDB || !Get.conceptService().hasConcept(Get.identifierService().getNidForUuids(pt.getPropertyTypeUUID())))
         {
            groupingConcept = createConcept(pt.getPropertyTypeUUID(), pt.getPropertyTypeDescription() + IBDFCreationUtility.METADATA_SEMANTIC_TAG, true).getChronology();
            if (pt instanceof BPT_HasAltMetaDataParent && ((BPT_HasAltMetaDataParent)pt).getAltMetaDataParentUUID() != null) {
               addParents(ComponentReference.fromChronology(groupingConcept), parentPrimordial, ((BPT_HasAltMetaDataParent)pt).getAltMetaDataParentUUID());
            } else {
               addParents(ComponentReference.fromChronology(groupingConcept), parentPrimordial);
            }
         }
         else
         {
            groupingConcept = Get.conceptService().getConceptChronology(pt.getPropertyTypeUUID());
         }

         
         for (Property p : pt.getProperties())
         {
            if (this.writeToDB && Get.conceptService().hasConcept(Get.identifierService().getNidForUuids(p.getUUID())))
            {
               continue;
            }
            if (p.isFromConceptSpec())
            {
               //This came from a conceptSpecification (metadata in ISAAC), and we don't need to create it.
               //Just need to add one relationship to the existing concept.
               addParent(ComponentReference.fromConcept(p.getUUID()), pt.getPropertyTypeUUID());

               if (p.isIdentifier()) 
               {
                  this.identifierTypes.add(p.getUUID());
               }
            }
            else
            {
               //don't feed in the 'definition' if it is an association, because that will be done by the configureConceptAsDynamicRefex method
               UUID secondParentToUse = p.getSecondParent();
               ConceptVersion concept = createConcept(p.getUUID(), p.getSourcePropertyNameFQN() + IBDFCreationUtility.METADATA_SEMANTIC_TAG, 
                     p.getSourcePropertyNameFQN(), 
                     p.getSourcePropertyAltName(), (p instanceof PropertyAssociation ? null : p.getSourcePropertyDefinition()), 
                     pt.getPropertyTypeUUID(),
                     secondParentToUse);

               if (p.isIdentifier()) {
                  // Add IDENTIFIER_ASSEMBLAGE membership
                  addMembership(ComponentReference.fromConcept(concept), MetaData.IDENTIFIER_SOURCE____SOLOR);
                  this.identifierTypes.add(p.getUUID());
               } else if (pt.createAsDynamicRefex()) {
                  configureConceptAsDynamicRefex(ComponentReference.fromConcept(concept), 
                        findFirstNotEmptyString(p.getSourcePropertyDefinition(), p.getSourcePropertyAltName(), p.getSourcePropertyNameFQN()),
                        p.getDataColumnsForDynamicRefex(), null, null);
               }

               else if (p instanceof PropertyAssociation)
               {
                  //TODO [DAN] need to migrate code from api-util (AssociationType, etc) down into the ISAAC packages... integrate here, at least at doc level
                  //associations return false for "createAsDynamicRefex"
                  PropertyAssociation item = (PropertyAssociation)p;
                  
                  //Make this a dynamic refex - with the association column info
                  configureConceptAsDynamicRefex(ComponentReference.fromConcept(concept), item.getSourcePropertyDefinition(),
                        item.getDataColumnsForDynamicRefex(), item.getAssociationComponentTypeRestriction(), item.getAssociationComponentTypeSubRestriction());
                  
                  //Add this concept to the association semantic
                  addAssemblageMembership(ComponentReference.fromConcept(concept), DynamicConstants.get().DYNAMIC_ASSOCIATION.getPrimordialUuid(), 
                        Status.ACTIVE, null);
                  
                  //add the inverse name, if it has one
                  if (!StringUtils.isBlank(item.getAssociationInverseName()))
                  {
                     //Need to make our own UUID here, cause there are cases where the inverse name is identical to the forward name.
                     SemanticChronology inverseDesc = addDescription(ComponentReference.fromConcept(concept), 
                           converterUUID.createNamespaceUUIDFromStrings(concept.getPrimordialUuid().toString(), item.getAssociationInverseName(), 
                                 "inverse", DescriptionType.REGULAR_NAME.name()),
                           item.getAssociationInverseName(), 
                           DescriptionType.REGULAR_NAME, false, null, Status.ACTIVE);
                     
                     addAssemblageMembership(ComponentReference.fromChronology(inverseDesc), DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getPrimordialUuid(), 
                           Status.ACTIVE, selectTime(ComponentReference.fromChronology(inverseDesc), null));
                  }
               }
            }
         }
      }
   }
   
   /**
    * @param converterSourceArtifactVersion
    * @param converterSourceReleaseDate
    * @param converterOutputArtifactVersion
    * @param converterOutputArtifactClassifier
    * @param converterVersion
    */
   public void loadTerminologyMetadataAttributes(String converterSourceArtifactVersion,  Optional<String> converterSourceReleaseDate, String converterOutputArtifactVersion,
       Optional<String> converterOutputArtifactClassifier, String converterVersion) 
   {
     loadTerminologyMetadataAttributes(getModule(), converterSourceArtifactVersion, converterSourceReleaseDate,
         converterOutputArtifactVersion, converterOutputArtifactClassifier, converterVersion);
   }
   
   /**

    * @param terminologyMetadataRootConcept
    * @param converterSourceArtifactVersion
    * @param converterSourceReleaseDate
    * @param converterOutputArtifactVersion
    * @param converterOutputArtifactClassifier
    * @param converterVersion
    */
   public void loadTerminologyMetadataAttributes(ComponentReference terminologyMetadataRootConcept,  String converterSourceArtifactVersion, Optional<String> converterSourceReleaseDate,
       String converterOutputArtifactVersion, Optional<String> converterOutputArtifactClassifier, String converterVersion) {
      addStaticStringAnnotation(terminologyMetadataRootConcept, converterSourceArtifactVersion, 
            MetaData.SOURCE_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(), Status.ACTIVE);
      addStaticStringAnnotation(terminologyMetadataRootConcept, converterOutputArtifactVersion, 
            MetaData.CONVERTED_IBDF_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(), Status.ACTIVE);
      addStaticStringAnnotation(terminologyMetadataRootConcept, converterVersion, 
            MetaData.CONVERTER_VERSION____SOLOR.getPrimordialUuid(), Status.ACTIVE);
      if (converterOutputArtifactClassifier.isPresent() && StringUtils.isNotBlank(converterOutputArtifactClassifier.get()))
      {
         addStaticStringAnnotation(terminologyMetadataRootConcept, converterOutputArtifactClassifier.get(), 
               MetaData.CONVERTED_IBDF_ARTIFACT_CLASSIFIER____SOLOR.getPrimordialUuid(), Status.ACTIVE);
      }
      if (converterSourceReleaseDate.isPresent() && StringUtils.isNotBlank(converterSourceReleaseDate.get()))
      {
         addStaticStringAnnotation(terminologyMetadataRootConcept, converterSourceReleaseDate.get(), 
               MetaData.SOURCE_RELEASE_DATE____SOLOR.getPrimordialUuid(), Status.ACTIVE);
      }
   }
   
   /**
    * 
    * @param strings
    * @return
    */
   private String findFirstNotEmptyString(String ... strings)
   {
      for (String s : strings)
      {
         if (StringUtils.isNotEmpty(s))
         {
            return s;
         }
      }
      return "";
   }
   
   /**
    * 
    * @param semanticAssemblage
    * @param columnInfo
    */
   public void registerDynamicColumnInfo(UUID semanticAssemblage, DynamicColumnInfo[] columnInfo)
   {
      if (refexAllowedColumnTypes != null)
      {
         refexAllowedColumnTypes.put(semanticAssemblage, columnInfo);
      }
   }
   
   /**
    * This method probably shouldn't be used - better to use the PropertyAssotion type
    * @param associationTypeConcept
    * @param inverseName
    * @deprecated - Better to set things up as {@link BPT_Associations}
    */
   public void configureConceptAsAssociation(UUID associationTypeConcept, String inverseName)
   {
      DynamicColumnInfo[] colInfo = new DynamicColumnInfo[] {new DynamicColumnInfo(
            0, DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid(), DynamicDataType.UUID, null, true, true)};
      configureConceptAsDynamicRefex(ComponentReference.fromConcept(associationTypeConcept), 
            "Defines an Association Type", colInfo, null, null);
      
      addAssemblageMembership(ComponentReference.fromConcept(associationTypeConcept), DynamicConstants.get().DYNAMIC_ASSOCIATION.getPrimordialUuid(), 
            Status.ACTIVE, null);
      
      if (!StringUtils.isBlank(inverseName))
      {
         SemanticChronology inverseDesc = addDescription(ComponentReference.fromConcept(associationTypeConcept), inverseName, 
               DescriptionType.REGULAR_NAME, false, null, Status.ACTIVE);
         
         addAssemblageMembership(ComponentReference.fromChronology(inverseDesc), DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getPrimordialUuid(), 
               Status.ACTIVE, selectTime(ComponentReference.fromChronology(inverseDesc), null));
      }
      BPT_Associations.registerAsAssociation(associationTypeConcept);
   }
   
   /**
    * 
    * @param concept
    * @param semanticDescription
    * @param columns
    * @param referencedComponentTypeRestriction
    * @param referencedComponentTypeSubRestriction
    */
   public void configureConceptAsDynamicRefex(ComponentReference concept, String semanticDescription,
         DynamicColumnInfo[] columns, IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction)
   {
      if (semanticDescription == null)
      {
         throw new RuntimeException("Refex description is required");
      }
      // See {@link DynamicUsageDescription} class for more details on this format.
      //Add the special synonym to establish this as an assemblage concept
      
      //Need a custom UUID, otherwise duplicates are likely
      UUID temp = converterUUID.createNamespaceUUIDFromStrings(concept.getPrimordialUuid().toString(), semanticDescription, 
         DescriptionType.DEFINITION.name(),  MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid().toString(), MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid().toString(), 
         new Boolean("true").toString(), "DynamicSemanticMarker");
      
      SemanticChronology desc = addDescription(concept, temp, semanticDescription, DescriptionType.DEFINITION, true, null, Status.ACTIVE);
      
      //Annotate the description as the 'special' type that means this concept is suitable for use as an assemblage concept
      addAnnotation(ComponentReference.fromChronology(desc), null, (DynamicData)null, 
            DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getPrimordialUuid(), Status.ACTIVE, null);
      
      //define the data columns (if any)
      if (columns != null && columns.length > 0)
      {
         for (DynamicColumnInfo col : columns)
         {
            DynamicData[] data = LookupService.getService(DynamicUtility.class).configureDynamicDefinitionDataForColumn(col);
            addAnnotation(concept, null, data, DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION.getPrimordialUuid(), Status.ACTIVE, null, null);
         }

         DynamicArray<DynamicData> indexInfo = LookupService.getService(DynamicUtility.class).configureColumnIndexInfo(columns);
         
         if (indexInfo != null)
         {
            addAnnotation(concept, null, new DynamicData[] {indexInfo},
               DynamicConstants.get().DYNAMIC_INDEX_CONFIGURATION.getPrimordialUuid(), Status.ACTIVE, null, null);
         }
      }
      registerDynamicColumnInfo(concept.getPrimordialUuid(), columns);
      
      //Add the restriction information (if any)
      DynamicData[] data = LookupService.getService(DynamicUtility.class).
            configureDynamicRestrictionData(referencedComponentTypeRestriction, referencedComponentTypeSubRestriction);
      if (data != null)
      {
         addAnnotation(concept, null, data, DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getPrimordialUuid(), Status.ACTIVE, null, null);
      }
   }
   
   /**
    * Creates column concepts (for the column labels) for each provided columnName, then creates a property with a multi-column data set
    * each column being of type string, and optional.
    * @param semanticName 
    * @param columnNames - Create concepts to represent column names for each item here.  Supports a stupid hack, where if the 
    * first two characters of a string in this array are '[]' - it will create a dynamic semantic array type for strings, rather than a single string.
    * @param columnTypes - optional - if not provided, makes all columns strings.  If provided, must match size of columnNames
    * @return
    */
   public Property createMultiColumnDynamicStringSemantic(String semanticName, String[] columnNames, DynamicDataType[] columnTypes)
   {
      DynamicColumnInfo[] cols = new DynamicColumnInfo[columnNames.length];
      for (int i = 0; i < cols.length; i++)
      {
         String colName;
         DynamicDataType type;
         if (columnNames[i].startsWith("[]"))
         {
            colName = columnNames[i].substring(2, columnNames[i].length());
            type = DynamicDataType.ARRAY;
         }
         else
         {
            colName = columnNames[i];
            type = columnTypes == null ? DynamicDataType.STRING : columnTypes[i];
         }
         UUID descriptionConcept = createConcept(colName, true, DynamicConstants.get().DYNAMIC_COLUMNS.getPrimordialUuid()).getPrimordialUuid();
         cols[i] = new DynamicColumnInfo(i, descriptionConcept, type, null, false, true);
      }
      
      //return new Property(null, semanticName, null, null, false, Integer.MAX_VALUE, cols);
      return new Property(semanticName, false, Integer.MAX_VALUE, cols);
   }
   
   /**
    * In some use cases, like the vhat delta import, I have to process some of my own mutations, and its useful to route the addUncommitted and db adds
    * back here, for logging / consistency.
    * @param ochreObject
    */
   public void storeManualUpdate(IsaacExternalizable ochreObject)
   {
      this.writer.put(ochreObject);
      dbWrite(ochreObject);
   }

   /**
    * 
    * @throws IOException
    */
   public void shutdown() throws IOException   
   {
      this.writer.close();
      if (!writeToDB)
      {
         LookupService.shutdownSystem();
      }
      converterUUID.clearCache();
      clearLoadStats();
   }
   
   /**
    * Allows to set the module and associated time, helpful for versioning.
    * 
    * @param module The module UUID, setting as the default module
    * @param time The module time, if applicable, setting as the defaul time
    */
   public void setModule(UUID module, Long time)
   {
      if (module != null)
      {
         this.module = ComponentReference.fromConcept(module);
      }

      if (time != null && time.longValue() > 0)
      {
         this.defaultTime = time.longValue();
      }
   }
}
