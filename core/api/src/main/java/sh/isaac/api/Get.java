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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import com.lmax.disruptor.dsl.Disruptor;
import javafx.concurrent.Task;
import sh.isaac.api.alert.AlertEvent;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.commit.ChangeSetWriterService;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.PostCommitService;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.CoordinateFactory;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.BinaryDataServiceFactory;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacExternalizableSpliterator;
import sh.isaac.api.index.GenerateIndexes;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.index.IndexDescriptionQueryService;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.progress.CompletedTasks;
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
   private static LogicService logicService;

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
   
   private static DataStore dataStore;
   
   
   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new gets the.
    */
   public Get() {}

   //~--- methods -------------------------------------------------------------

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
    * method will try first to return the fully specified description, or the
    * preferred description, as specified in the default
    * {@code StampCoordinate} and the default {@code LanguageCoordinate}.
    *
    * @param conceptNid nid of the concept to get the description
    * for
    * @return a description for this concept. If no description can be found,
    * {@code "No desc for: " + conceptNid;} will be returned.
    *  TODO: make getDescriptionOptional return a LatestVersion, which has optional value, rather than returning an
    *  Optional&gt;LatestVersion>&lt;
    */
   public static String conceptDescriptionText(int conceptNid) {
     if (conceptNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + conceptNid);
      }
      final LatestVersion<DescriptionVersion> descriptionOptional =
         defaultConceptSnapshotService().getDescriptionOptional(conceptNid);

      if (descriptionOptional.isPresent()) {
         return descriptionOptional.get()
                                   .getText();
      }

      return "No desc for: " + conceptNid;
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

   /**
    * Concept description text list.
    *
    * @param conceptIds the concept ids
    * @return the string
    */
   public static String conceptDescriptionTextList(List<Integer> conceptIds) {
      return conceptDescriptionTextList(conceptIds.stream()
            .mapToInt((boxedInt) -> (int) boxedInt)
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
    * Note, this method may fail during bootstrap, if concept being requested is not already loaded
    * into the concept service.
    * @param nid a concept nid
    * @return A concept specification for the corresponding identifier
    */
   public static ConceptSpecification conceptSpecification(int nid) {
      if (nid >= 0) {
         throw new IllegalStateException("Nids must be < 0: " + nid);
      }
      return new ConceptProxy(conceptDescriptionText(nid), identifierService().getUuidArrayForNid(nid));
   }

   /**
    * Note, this method may fail during bootstrap, if concept being requested is not already loaded
    * into the concept service.
    * @param uuid a concept uuid
    * @return A concept specification for the corresponding identifier
    */
   public static ConceptSpecification conceptSpecification(UUID uuid) {
       int nid = Get.identifierService().getNidForUuids(uuid);
      return new ConceptProxy(nid);
   }

   public static ConceptSpecification conceptSpecification(String uuidString) {
      return conceptSpecification(UUID.fromString(uuidString));
   }

   
   /**
    * Configuration service.
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
    * Coordinate factory.
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
                   .getGlobalDatastoreConfiguration().getDefaultManifoldCoordinate());
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
                                .getInferredAssemblageNid())
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
         logicService = getService(LogicService.class);
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
    * Meta content service.
    *
    * @return the meta content service
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

   public static ObservableSnapshotService observableSnapshotService(ManifoldCoordinate manifoldCoordinate) {
      return observableChronologyService().getObservableSnapshotService(manifoldCoordinate);
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
         dataStore = getService(DataStore.class);
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
      workExecutors                   = null;
      stampService                    = null;
      postCommitService               = null;
      changeSetWriterService          = null;
      observableChronologyService     = null;
      serializationService            = null;
      descriptionIndexer              = null;
      semanticIndexer                 = null;
      dataStore                       = null;
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
   
   public static MetadataService metadataService() {
      return service(MetadataService.class);
   }

   /**
    * Stamp service.
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
                                .getStatedAssemblageNid())
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

   //~--- get methods ---------------------------------------------------------

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
}

