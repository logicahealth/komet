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



package sh.isaac.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jakarta.inject.Singleton;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.jvnet.hk2.annotations.Service;
import com.lmax.disruptor.dsl.Disruptor;
import javafx.concurrent.Task;
import sh.isaac.api.alert.AlertEvent;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.commit.ChangeSetWriterService;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.PostCommitService;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.datastore.MasterDataStore;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.BinaryDataServiceFactory;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacExternalizableSpliterator;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.GenerateIndexes;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.index.IndexDescriptionQueryService;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicServiceSnoRocket;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.navigation.NavigationService;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.progress.CompletedTasks;
import sh.isaac.api.query.QueryHandler;
import sh.isaac.api.task.TaskCountManager;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.api.util.WorkExecutors;

//~--- classes ----------------------------------------------------------------

/**
 * Provides simple static access to common services, in a lookup service aware
 * way. Intended to be used in place of static fields placed in classes that
 * frequently use a common service. This class was added specifically to address
 * problems when a service is used in a mojo that spans more than one project,
 * by ensuring that static initialization of services does not provide a way to
 * retain stale services.
 *
 * @author kec
 */
@Service
@Singleton
public class Get
         implements StaticIsaacCache {
   /** The LOG. */
   private static final Logger LOG = LogManager.getLogger();
   private static final Disruptor<AlertEvent> ALERT_DISRUPTOR = new Disruptor<>(
                                                             AlertEvent::new,
                                                                   512,
                                                                   new NamedThreadFactory("alert-disruptor", true));
   /** The active task set. */
   private static ActiveTasks activeTaskSet;

   /** The active task set. */
   private static CompletedTasks completedTaskSet;

   /** The configuration service. */
   private static ConfigurationService configurationService;

   /** The commit service. */
   private static CommitService commitService;

   /** The concept active service. */
   private static ConceptActiveService conceptActiveService;

   /** The concept service. */
   private static ConceptService conceptService;

   /** The meta content service. */
   private static MetaContentService metaContentService;

   /** The concept snapshot. */
   private static ConceptSnapshotService conceptSnapshot;

   /** The identified object service. */
   private static IdentifiedObjectService identifiedObjectService;

   /** The identifier service. */
   private static IdentifierService identifierService;

   /** The language coordinate service. */
   private static LanguageCoordinateService languageCoordinateService;

   /** The logical expression builder service. */
   private static LogicalExpressionBuilderService logicalExpressionBuilderService;

   /** The logic service. */
   private static LogicServiceSnoRocket logicService;

   /** The path service. */
   private static VersionManagmentPathService versionManagementPathService;

   /** The semantic builder service. */
   private static SemanticBuilderService<?> semanticBuilderService;

   /** The assemblage service. */
   private static AssemblageService assemblageService;

   /** The coordinate factory. */
   private static CoordinateFactory coordinateFactory;

   /** The taxonomy service. */
   private static TaxonomyService taxonomyService;

   private static NavigationService navigationService;

   /** The work executors. */
   private static WorkExecutors workExecutors;

   /** The concept builder service. */
   private static ConceptBuilderService conceptBuilderService;

   /** The stamp service. */
   private static StampService stampService;

   /** The post commit service. */
   private static PostCommitService postCommitService;

   /** The change set writer service. */
   private static ChangeSetWriterService      changeSetWriterService;
   private static ObservableChronologyService observableChronologyService;
   private static SerializationService        serializationService;
   
   private static IndexDescriptionQueryService descriptionIndexer;
   private static IndexSemanticQueryService semanticIndexer;
   
   private static MasterDataStore dataStore;
   
   private static PreferencesService preferencesService;
   private static boolean useLuceneIndexes = true;

   private static MutableIntObjectMap<ConceptSpecification> TERM_AUX_CACHE = null;

   /**
    * Instantiates a new Get.
    */
   public Get() {}

   public static Transformer xsltTransformer(Source xsltSource, ManifoldCoordinateImmutable manifoldCoordinate) throws TransformerConfigurationException {
      return Get.service(XsltTransformer.class).getTransformer(xsltSource, manifoldCoordinate);
   }

   /**
    * Active tasks.
    *
    * @return the active tasks
    */
   public static ActiveTasks activeTasks() {
      if (activeTaskSet == null) {
         activeTaskSet = getService(ActiveTasks.class);
      }

      return activeTaskSet;
   }
   /**
    * Active tasks.
    *
    * @return the active tasks
    */
   public static CompletedTasks completedTasks() {
      if (completedTaskSet == null) {
         completedTaskSet = getService(CompletedTasks.class);
      }
      return completedTaskSet;
   }

   /**
    * Assemblage service.
    *
    * @return the assemblage service
    */
   public static AssemblageService assemblageService() {
      if (assemblageService == null) {
         assemblageService = getService(AssemblageService.class);
      }

      return assemblageService;
   }
   
   public static QueryHandler queryHandler() {
       return getService(QueryHandler.class);
   }
   
   /**
    * @return The {@link PreferencesService} provider, which is pref store based on the java preferences API, which provides access 
    * to the {@link IsaacPreferences} which allows storage of arbitrary data.  
    * 
    * This is primarily used by the Komet FX gui, and unless you know you specifically want this service, you will likely be much better
    * served by using the {@link #configurationService()} API to get access to the {@link UserConfiguration} for storage of non-terminology 
    * data, as it provides typed access to API specific parameters, and automatically handles passthru of different store types  - 
    * userConfigPerDB -> user Config per OS -> Global config per DB
    */
   public static PreferencesService preferencesService() {
      if (preferencesService == null) {
         preferencesService = getService(PreferencesService.class);
      }
      return preferencesService;
   }

   /**
    * Assemblage service available.
    *
    * @return true, if successful
    */
   public static boolean assemblageServiceAvailable() {
      if (assemblageService == null) {
         assemblageService = LookupService.getService(AssemblageService.class);
      }

      return assemblageService != null;
   }

   /**
    * Binary data reader.
    *
    * @param dataPath the data path
    * @return the binary data reader service
    * @throws FileNotFoundException the file not found exception
    */
   public static BinaryDataReaderService binaryDataReader(Path dataPath)
            throws FileNotFoundException {
      return getService(BinaryDataServiceFactory.class).getReader(dataPath);
   }

   public static BinaryDataReaderService binaryDataReader(InputStream inputStream)
            throws FileNotFoundException {
      return getService(BinaryDataServiceFactory.class).getReader(inputStream);
   }

   /**
    * Binary data writer.
    *
    * @param dataPath the data path
    * @return the data writer service
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static DataWriterService binaryDataWriter(Path dataPath)
            throws IOException {
      return getService(BinaryDataServiceFactory.class).getWriter(dataPath);
   }

   /**
    * Change set writer service.
    *
    * @return the change set writer service
    */
   public static ChangeSetWriterService changeSetWriterService() {
      if (changeSetWriterService == null) {
         changeSetWriterService = getService(ChangeSetWriterService.class);
      }

      return changeSetWriterService;
   }

   /**
    * Commit service.
    *
    * @return the commit service
    */
   public static CommitService commitService() {
      if (commitService == null) {
         commitService = getService(CommitService.class);
      }

      return commitService;
   }

   public static ConceptChronology concept(ConceptSpecification spec) {
      if (spec != null) {
         return conceptService().getConceptChronology(spec);
      }
      return null;
   }

   public static ConceptChronology concept(int id) {
      return conceptService().getConceptChronology(id);
   }

   public static ConceptChronology concept(UUID uuid) {
      return conceptService().getConceptChronology(uuid);
   }

   public static ConceptChronology concept(String uuidStr) {
      return concept(UUID.fromString(uuidStr));
   }

   /**
    * Concept active service.
    *
    * @return the concept active service
    */
   public static ConceptActiveService conceptActiveService() {
      if (conceptActiveService == null) {
         conceptActiveService = getService(ConceptActiveService.class);
      }

      return conceptActiveService;
   }

   /**
    * Concept builder service.
    *
    * @return the concept builder service
    */
   public static ConceptBuilderService conceptBuilderService() {
      if (conceptBuilderService == null) {
         conceptBuilderService = getService(ConceptBuilderService.class);
      }

      return conceptBuilderService;
   }

   /**
    * Simple method for getting text of the description of a concept. This
    * method will use the rules of the default {@code StampCoordinate} and the default {@code LanguageCoordinate}.
    * 
    * Note that this implementation does rely on the configuration of the 
    * {@link #defaultConceptSnapshotService()} - if that configuration is changed, the behavior of this method will follow.
    *
    * @param conceptNid nid of the concept to get the description for
    * @return a description for this concept. If no description can be found, {@code "No desc for: " + UUID;} will be returned.
    * @see ConceptSnapshotService#conceptDescriptionText(int)
    */
   public static String conceptDescriptionText(int conceptNid) {
      if (conceptNid == 0) {
         return "Uninitialized Component, nid == 0";
      }
      if (conceptNid > 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + conceptNid);
      }


      if (Get.identifierService().getObjectTypeForComponent(conceptNid) == IsaacObjectType.SEMANTIC) {
         SemanticChronology sc = Get.assemblageService().getSemanticChronology(conceptNid);
         if (sc.getVersionType() == VersionType.DESCRIPTION) {
            LatestVersion<DescriptionVersion> latestDescription = sc.getLatestVersion(defaultCoordinate().getViewStampFilter());
            if (latestDescription.isPresent()) {
               return "Desc: " + latestDescription.get().getText();
            }
         }
      }
      return defaultConceptSnapshotService().conceptDescriptionText(conceptNid);
   }
   
   public static String conceptDescriptionText(ConceptSpecification conceptSpec) {
       if (conceptSpec == null) {
           throw new NullPointerException("conceptSpec cannot be null.");
       }
       return conceptDescriptionText(conceptSpec.getNid());
   }

   /**
    * Concept description text list.
    *
    * @param conceptIds the concept ids
    * @return the string
    */
   public static String conceptDescriptionTextList(IntSet conceptIds) {
      return conceptDescriptionTextList(conceptIds.asArray());
   }

   /**
    * Concept description text list.
    *
    * @param conceptIds the concept ids
    * @return the string
    */
   public static String conceptDescriptionTextList(int[] conceptIds) {
      if ((conceptIds != null) && (conceptIds.length > 0)) {
         final StringBuilder builder = new StringBuilder();

         builder.append("[");
         Arrays.stream(conceptIds)
               .forEach(
                   (conceptId) -> {
                      builder.append(conceptDescriptionText(conceptId));
                      builder.append(", ");
                   });
         builder.delete(builder.length() - 2, builder.length());
         builder.append("]");
         return builder.toString();
      }

      return "[]";
   }
   public static String conceptDescriptionTextList(ConceptSpecification[] conceptSpecs) {
      if ((conceptSpecs != null) && (conceptSpecs.length > 0)) {
         final StringBuilder builder = new StringBuilder();

         builder.append("[");
         Arrays.stream(conceptSpecs)
               .forEach(
                   (conceptSpec) -> {
                      builder.append(conceptDescriptionText(conceptSpec));
                      builder.append(", ");
                   });
         builder.delete(builder.length() - 2, builder.length());
         builder.append("]");
         return builder.toString();
      }

      return "[]";
   }

   /**
    * Concept description text list.
    *
    * @param conceptIds the concept ids
    * @return the string
    */
   public static String conceptDescriptionTextList(Collection<Integer> conceptIds) {
      return conceptDescriptionTextList(conceptIds.stream()
            .mapToInt((boxedInt) -> (int) boxedInt)
            .toArray());
   }

   public static String conceptDescriptionTextListFromSpecList(Collection<ConceptSpecification> conceptSpecifications) {
      return conceptDescriptionTextList(conceptSpecifications.stream()
            .mapToInt((spec) -> (int) spec.getNid())
            .toArray());
   }

   /**
    * Concept service.
    *
    * @return the concept service
    */
   public static ConceptService conceptService() {
      if (conceptService == null) {
         conceptService = getService(ConceptService.class);
      }

      return conceptService;
   }

   /**
    * Note, this method may fail/lock during bootstrap, if concept being requested is not already loaded
    * into the concept service.
    * @param nid a concept nid
    * @return A concept specification for the corresponding identifier
    */
   public static ConceptSpecification conceptSpecification(int nid) {
       if (nid >= 0) {
           throw new IllegalStateException("Nids must be < 0: " + nid);
       }

      MutableIntObjectMap<ConceptSpecification> localRef = TERM_AUX_CACHE;
       if (localRef == null) {
          localRef = IntObjectMaps.mutable.empty();
          for (ConceptSpecification conceptSpecification: TermAux.getAllSpecs()) {
             localRef.put(conceptSpecification.getNid(), conceptSpecification);
          }
          TERM_AUX_CACHE = localRef;
       }
       if (localRef.containsKey(nid)) {
           return localRef.get(nid);
       }
       return new ConceptProxy(nid);
   } 

   /**
    * Note, this method may fail during bootstrap, if concept being requested is not already loaded
    * into the concept service.
    * @param uuid a concept uuid
    * @return A concept specification for the corresponding identifier
    */
   public static ConceptSpecification conceptSpecification(UUID uuid) {
       int nid = Get.identifierService().getNidForUuids(uuid);
      return conceptSpecification(nid);
   }

   public static ConceptSpecification conceptSpecification(String uuidString) {
      return conceptSpecification(UUID.fromString(uuidString));
   }

   
   /**
    * Get a reference to the {@link ConfigurationService} which also provides access to the {@link UserConfiguration}
    * 
    * These interfaces provide access to the global system configuration, and allow the reading and persisting of user and 
    * database specific options.
    *
    * @return the configuration service
    */
   public static ConfigurationService configurationService() {
      if (configurationService == null) {
         configurationService = getService(ConfigurationService.class);
      }

      return configurationService;
   }

   /**
    * ImmutableCoordinate factory.
    *
    * @return the coordinate factory
    */
   public static CoordinateFactory coordinateFactory() {
      if (coordinateFactory == null) {
         coordinateFactory = getService(CoordinateFactory.class);
      }

      return coordinateFactory;
   }

   /**
    * Concept snapshot.
    *
    * @return a {@code ConceptSnapshotService} configured using the default
    * {@code StampCoordinate} and {@code LanguageCoordinate} provided by the
    * configuration service.
    */
   public static ConceptSnapshotService defaultConceptSnapshotService() {
      if (conceptSnapshot == null) {
         conceptSnapshot = getService(
             ConceptService.class).getSnapshot(Get.configurationService()
                   .getGlobalDatastoreConfiguration().getDefaultManifoldCoordinate().getValue());
      }

      return conceptSnapshot;
   }

   /**
    *
    * @return the default manifold coordinate from the configuration service.
    */
   public static ManifoldCoordinate defaultCoordinate() {
      return configurationService().getGlobalDatastoreConfiguration().getDefaultManifoldCoordinate();
   }

   /**
    * @return The ISAAC common {@link ThreadPoolExecutor} 
    */
   public static ThreadPoolExecutor executor() {
      return workExecutors().getExecutor();
   }

   /**
    * Identified object service.
    *
    * @return the identified object service
    */
   public static IdentifiedObjectService identifiedObjectService() {
      if (identifiedObjectService == null) {
         identifiedObjectService = getService(IdentifiedObjectService.class);
      }

      return identifiedObjectService;
   }

   /**
    * Identifier service.
    *
    * @return the identifier service
    */
   public static IdentifierService identifierService() {
      if (identifierService == null) {
         identifierService = getService(IdentifierService.class);
      }

      return identifierService;
   }
   
   /**
    * Convenience method to get nids from the identifier service. 
    * @param uuids
    * @return a nid
    */
   public static int nidForUuids(UUID... uuids) {
       return identifierService().getNidForUuids(uuids);
   }
   
   /**
    * Assigns a nid for the UUIDs if a nid does not already exist.
    * @param uuids
    * @return 
    */
   public static int nidWithAssignment(UUID... uuids) {
       return identifierService().assignNid(uuids);
   }
   
   /**
    * 
    * @return a new random UUID that has been assigned a nid. 
    */
   public static UUID newUuidWithAssignment() {
       UUID uuid = UUID.randomUUID();
       identifierService().assignNid(uuid);
       return uuid;
   }

   /**
    * Inferred definition chronology.
    *
    * @param nid  a concept nid.
    * @return the inferred definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SemanticChronology> inferredDefinitionChronology(int nid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be < 0: " + nid);
      }
      return assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(
          nid,
          configurationService().getGlobalDatastoreConfiguration()
                                .getDefaultLogicCoordinate()
                                .getInferredAssemblageNid(), false)
                                .findAny();
   }

   /**
    * Language coordinate service.
    *
    * @return the language coordinate service
    */
   public static LanguageCoordinateService languageCoordinateService() {
      if (languageCoordinateService == null) {
         languageCoordinateService = getService(LanguageCoordinateService.class);
      }

      return languageCoordinateService;
   }

   /**
    * Logic service.
    *
    * @return the logic service
    */
   public static LogicService logicService() {
      if (logicService == null) {
         logicService = getService(LogicServiceSnoRocket.class);
      }

      return logicService;
   }

   /**
    * Logical expression builder service.
    *
    * @return the logical expression builder service
    */
   public static LogicalExpressionBuilderService logicalExpressionBuilderService() {
      if (logicalExpressionBuilderService == null) {
         logicalExpressionBuilderService = getService(LogicalExpressionBuilderService.class);
      }

      return logicalExpressionBuilderService;
   }

   /**
    * @return the implementation of the {@link MetaContentService} (if available) which allows for the storing of arbitrary data 
    * that resides along-side the terminology data.
    */
   public static MetaContentService metaContentService() {
      if (metaContentService == null) {
         metaContentService = getService(MetaContentService.class);
      }

      return metaContentService;
   }

   public static ObservableChronologyService observableChronologyService() {
      if (observableChronologyService == null) {
         observableChronologyService = getService(ObservableChronologyService.class);
      }

      return observableChronologyService;
   }

   public static ObservableSnapshotService observableSnapshotService(StampFilter stampFilter) {
      return observableChronologyService().getObservableSnapshotService(stampFilter);
   }
   

   public static IndexDescriptionQueryService indexDescriptionService() {
      if (descriptionIndexer == null) {
         descriptionIndexer = getService(IndexDescriptionQueryService.class);
      }

      return descriptionIndexer;
   }
   
   public static IndexSemanticQueryService indexSemanticService() {
      if (semanticIndexer == null) {
         semanticIndexer = getService(IndexSemanticQueryService.class);
      }

      return semanticIndexer;
   }
   
   public static DataStore dataStore() {
      if (dataStore == null) {
         dataStore = getService(MasterDataStore.class);
      }
      return dataStore;
   }

   /**
    * IsaacExternalizable stream.
    *
    * @return the stream
    */
   public static Stream<IsaacExternalizable> isaacExternalizableStream() {
      return StreamSupport.stream(new IsaacExternalizableSpliterator(), false);
   }

   public static <T> Optional<T> optionalService(Class<T> clazz) {
      return Optional.ofNullable(LookupService.getService(clazz));
   }

   /**
    * Path service.
    *
    * @return the path service
    */
   public static VersionManagmentPathService versionManagmentPathService() {
      if (versionManagementPathService == null) {
         versionManagementPathService = getService(VersionManagmentPathService.class);
      }

      return versionManagementPathService;
   }

   /**
    * Post commit service.
    *
    * @return the post commit service
    */
   public static PostCommitService postCommitService() {
      if (postCommitService == null) {
         postCommitService = getService(PostCommitService.class);
      }

      return postCommitService;
   }

   public static ConceptSnapshot conceptSnapshot(ConceptProxy concept, ManifoldCoordinate manifoldCoordinate) {
      return Get.conceptService().getConceptSnapshot(concept, manifoldCoordinate);
   }

   public static ConceptSnapshot conceptSnapshot(int conceptNid, ManifoldCoordinate manifoldCoordinate) {
      return Get.conceptService().getConceptSnapshot(conceptNid, manifoldCoordinate);
   }

    public static ConceptChronology[] conceptList(int[] conceptNidList) {
       ConceptChronology[] results = new ConceptChronology[conceptNidList.length];
       for (int i = 0; i < results.length; i++) {
          results[i] = concept(conceptNidList[i]);
       }
       return results;
    }

    /**
    * Reset.
    */
   @Override
   public void reset() {
      LOG.info("Resetting service cache.");
      activeTaskSet                   = null;
      configurationService            = null;
      commitService                   = null;
      conceptBuilderService           = null;
      conceptActiveService            = null;
      conceptService                  = null;
      metaContentService              = null;
      conceptSnapshot                 = null;
      coordinateFactory               = null;
      identifiedObjectService         = null;
      identifierService               = null;
      languageCoordinateService       = null;
      logicalExpressionBuilderService = null;
      logicService                    = null;
      versionManagementPathService    = null;
      semanticBuilderService          = null;
      assemblageService               = null;
      taxonomyService                 = null;
      navigationService               = null;
      workExecutors                   = null;
      stampService                    = null;
      postCommitService               = null;
      changeSetWriterService          = null;
      observableChronologyService     = null;
      serializationService            = null;
      descriptionIndexer              = null;
      semanticIndexer                 = null;
      dataStore                       = null;
      preferencesService              = null;
      TERM_AUX_CACHE                  = null;
   }

   public static ScheduledExecutorService scheduledExecutor() {
      return workExecutors().getScheduledThreadPoolExecutor();
   }

   /**
    * Semantic builder service.
    *
    * @return the semantic builder service
    */
   public static SemanticBuilderService<? extends SemanticChronology> semanticBuilderService() {
      if (semanticBuilderService == null) {
         semanticBuilderService = getService(SemanticBuilderService.class);
      }

      return semanticBuilderService;
   }

   public static SerializationService serializer() {
      if (serializationService == null) {
         serializationService = getService(SerializationService.class);
      }

      return serializationService;
   }

   public static <T> T service(Class<T> clazz) {
      return getService(clazz);
   }

   public static <T> List<T> services(Class<T> clazz) {
      return getServices(clazz);
   }
   
   /**
    * @return The service that manages the importation of terminology metadata content into the database
    */
   public static MetadataService metadataService() {
      return service(MetadataService.class);
   }

   /**
    * Filter service.
    *
    * @return the stamp service
    */
   public static StampService stampService() {
      if (stampService == null) {
         stampService = getService(StampService.class);
      }

      return stampService;
   }

   /**
    * Perform indexing according to all installed indexers.
    *
    * Cause all index generators implementing the {@link IndexBuilderService} to first
    * <code>clearIndex()</code> then iterate over all semanticChronologies in the database
    * and pass those chronicles to {@link IndexBuilderService#index(sh.isaac.api.chronicle.Chronology)}
    * and when complete, to call <code>commitWriter()</code>.
    * {@link IndexBuilderService} services will be discovered using the HK2 dependency injection framework.
    * @param indexersToReindex - if null or empty - all indexes found via HK2 will be cleared and
    * reindexed.  Otherwise, only clear and reindex the instances of {@link IndexBuilderService} which match the specified
    * class list.  Classes passed in should be an extension of {@link IndexBuilderService}
    *
    * @return Task that indicates progress.  The task will already be started, when it is returned.
    */
   public static Task<Void> startIndexTask(
         @SuppressWarnings("unchecked") Class<? extends IndexBuilderService>... indexersToReindex) {
      if (!Get.configurationService().getGlobalDatastoreConfiguration().enableLuceneIndexes()) {
         throw new UnsupportedOperationException();
      }
      final GenerateIndexes indexingTask = new GenerateIndexes(indexersToReindex);

      LookupService.getService(WorkExecutors.class)
                   .getExecutor()
                   .execute(indexingTask);
      return indexingTask;
   }

   /**
    * Stated definition chronology.
    *
    * @param nid a concept nid.
    * @return the stated definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SemanticChronology> statedDefinitionChronology(int nid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be < 0: " + nid);
      }
      return assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(
          nid,
          configurationService().getGlobalDatastoreConfiguration()
                                .getDefaultLogicCoordinate()
                                .getStatedAssemblageNid(), false)
                                .findAny();
   }

   /**
    * Taxonomy service.
    *
    * @return the taxonomy service
    */
   public static TaxonomyService taxonomyService() {
      if (taxonomyService == null) {
         taxonomyService = getService(TaxonomyService.class);
      }

      return taxonomyService;
   }
   public static NavigationService navigationService() {
      if (navigationService == null) {
         navigationService = getService(NavigationService.class);
      }

      return navigationService;
   }
   /**
    * Work executors.
    *
    * @return the work executors
    */
   public static WorkExecutors workExecutors() {
      if (workExecutors == null) {
         workExecutors = getService(WorkExecutors.class);
      }

      return workExecutors;
   }

   public static Disruptor<AlertEvent> alertDisruptor() {
      return ALERT_DISRUPTOR;
   }

   /**
    * Gets the service.
    *
    * @param <T> the generic type
    * @param clazz the clazz
    * @return the service
    */
   private static <T> T getService(Class<T> clazz) {
      final T service = LookupService.getService(clazz);

      if (service == null) {
         throw new RuntimeException(
             "No service for contract '" + clazz.getName() + "'... Is the service provider on the classpath?");
      }

      return service;
   }

   /**
    * Gets the service.
    *
    * @param <T> the generic type
    * @param clazz the clazz
    * @return the service
    */
   private static <T> List<T> getServices(Class<T> clazz) {
      final List<T> services = LookupService.getServices(clazz);

      return services;
   }
   
   private static final ConcurrentSkipListSet<ApplicationStates> APPLICATION_STATES = new ConcurrentSkipListSet<>();
   public static ConcurrentSkipListSet<ApplicationStates> applicationStates() {
       return APPLICATION_STATES;
   }

   public static ObservableChronology observableChronology(UUID... uuids) {
      return Get.observableChronologyService().getObservableChronology(Get.nidForUuids(uuids));
   }
   public static ObservableChronology observableChronology(int nid) {
      return Get.observableChronologyService().getObservableChronology(nid);
   }
   public static ObservableChronology observableChronology(ConceptSpecification spec) {
      return Get.observableChronologyService().getObservableChronology(spec);
   }

   public static boolean useLuceneIndexes() {
      return useLuceneIndexes;
   }

   public static void setUseLuceneIndexes(boolean useLuceneIndexes) {
      Get.useLuceneIndexes = useLuceneIndexes;
   }

   public static String conceptDescriptionText(UUID conceptUuid) {
      return conceptDescriptionText(Get.nidForUuids(conceptUuid));
   }

   /**
    * Provides a standard size for concurrent additions to queues for multi-threaded tasks. The size prevents
    * the queues from being overwhelmed, but also is large enough to keep the CPU occupied.
    * @return Runtime.getRuntime().availableProcessors() * 2
    */
   public static int permitCount() {
      return Runtime.getRuntime().availableProcessors() * 2;
   }

   /**
    * Use when multi threading a task, to ensure that queue resources don't get overwhelmed.
    * Search for usages for examples. Semaphore count is from the permitCount() method on this class.
    * @return a Semaphore for governing task execution.
    */
   public static TaskCountManager taskCountManager() {
      return new TaskCountManager(permitCount());
   }

   public static String getTextForComponent(int componentNid) {
      return Get.getTextForComponent(componentNid, Get.defaultCoordinate());
   }

   public static String getTextForComponent(Chronology component) {
      return Get.getTextForComponent(component, Get.defaultCoordinate().getViewStampFilter(),
              Get.defaultCoordinate().getLanguageCoordinate());
   }


   public static String getTextForComponent(Chronology component, ManifoldCoordinate manifoldCoordinate) {
      return Get.getTextForComponent(component, manifoldCoordinate.getViewStampFilter(),
              manifoldCoordinate.getLanguageCoordinate());
   }

   public static String getTextForComponent(int componentNid, ManifoldCoordinate manifoldCoordinate) {
      Optional<? extends Chronology> optionalComponent = Get.identifiedObjectService().getChronology(componentNid);
      if (optionalComponent.isPresent()) {
         return Get.getTextForComponent(optionalComponent.get(), manifoldCoordinate.getViewStampFilter(),
                 manifoldCoordinate.getLanguageCoordinate());
      }
      return "No component for: " + componentNid + " uuids: " + Get.identifierService().getUuidsForNid(componentNid);
   }

   public static String getTextForComponent(int componentNid, StampFilter stampFilter,
                                            LanguageCoordinate languageCoordinate) {
      Optional<? extends Chronology> optionalComponent = Get.identifiedObjectService().getChronology(componentNid);
      if (optionalComponent.isPresent()) {
         return Get.getTextForComponent(optionalComponent.get(), stampFilter, languageCoordinate);
      }
      return "No component for: " + componentNid + " uuids: " + Get.identifierService().getUuidsForNid(componentNid);
   }


   public static String getTextForComponent(Chronology component, StampFilter stampFilter,
                                            LanguageCoordinate languageCoordinate) {
      switch (component.getVersionType()) {
         case CONCEPT: {
            Optional<String> latestDescriptionText = languageCoordinate.getRegularDescriptionText(component.getNid(), stampFilter);
            if (latestDescriptionText.isPresent()) {
               return latestDescriptionText.get();
            }
            return "No description for concept: " + Arrays.toString(Get.identifierService().getUuidArrayForNid(component.getNid()));
         }
         case DESCRIPTION: {
            LatestVersion<DescriptionVersion> latest = component.getLatestVersion(stampFilter);
            if (latest.isPresent()) {
               return latest.get().getText();
            } else if (!latest.versionList().isEmpty()) {
               return latest.versionList().get(0).getText();
            }
            return "No versions for: " + component.getVersionType() + " " + component.getNid() + " "
                    + Get.identifierService().getUuidsForNid(component.getNid());
         }

         default:
            LatestVersion<Version>  latest = component.getLatestVersion(stampFilter);
            if (latest.isPresent()) {
               return latest.get().toUserString();
            } else if (!latest.versionList().isEmpty()) {
               return latest.versionList().get(0).toUserString();
            }
            return "No versions for: " + component;

      }
   }

   public static String conceptDescriptionWithNid(int nid) {
      StringBuilder sb = new StringBuilder();
      sb.append(Get.conceptDescriptionText(nid))
              .append(" <")
              .append(nid)
              .append(">");
      return sb.toString();
   }

   public static String conceptDescriptionWithNidAndUuids(int nid) {
      StringBuilder sb = new StringBuilder();
      sb.append(Get.conceptDescriptionText(nid))
              .append(" <")
              .append(nid)
              .append(" ")
              .append(Get.identifierService().getUuidsForNid(nid))
              .append(">");
      return sb.toString();
   }

}

