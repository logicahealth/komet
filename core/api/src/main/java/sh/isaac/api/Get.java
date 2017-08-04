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

//~--- JDK imports ------------------------------------------------------------

import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.commit.ChangeSetWriterService;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.PostCommitService;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.sememe.SememeBuilderService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.coordinate.CoordinateFactory;
import sh.isaac.api.externalizable.BinaryDataDifferService;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.BinaryDataServiceFactory;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacExternalizableSpliterator;
import sh.isaac.api.index.GenerateIndexes;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.api.index.IndexService;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.externalizable.IsaacExternalizable;

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
         implements OchreCache {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The active task set. */
   private static ActiveTasks activeTaskSet;

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

   /** The binary data differ service. */
   private static BinaryDataDifferService binaryDataDifferService;

   /** The path service. */
   private static PathService pathService;

   /** The sememe builder service. */
   private static SememeBuilderService<?> sememeBuilderService;

   /** The sememe service. */
   private static AssemblageService sememeService;

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
   private static ChangeSetWriterService changeSetWriterService;
   
   private static ObservableChronologyService observableChronologyService;
   
   private static SerializationService serializationService;

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
    * Binary data differ service.
    *
    * @return the binary data differ service
    */
   public static BinaryDataDifferService binaryDataDifferService() {
      if (binaryDataDifferService == null) {
         binaryDataDifferService = getService(BinaryDataDifferService.class);
      }

      return binaryDataDifferService;
   }

   /**
    * Binary data queue reader.
    *
    * @param dataPath the data path
    * @return the binary data reader queue service
    * @throws FileNotFoundException the file not found exception
    */
   public static BinaryDataReaderQueueService binaryDataQueueReader(Path dataPath)
            throws FileNotFoundException {
      return getService(BinaryDataServiceFactory.class).getQueueReader(dataPath);
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
    * @param conceptId nid or sequence of the concept to get the description
    * for
    * @return a description for this concept. If no description can be found,
    * {@code "No desc for: " + conceptId;} will be returned.
    *  TODO: make getDescriptionOptional return a LatestVersion, which has optional value, rather than returning an 
    *  Optional&gt;LatestVersion>&lt;
    */
   public static String conceptDescriptionText(int conceptId) {
      final LatestVersion<DescriptionVersion> descriptionOptional =
         defaultConceptSnapshotService().getDescriptionOptional(conceptId);

      if (descriptionOptional.isPresent()) {
            return descriptionOptional
                    .get()
                    .getText();
      }

      return "No desc for: " + conceptId;
   }

   /**
    * Concept description text list.
    *
    * @param conceptIds the concept ids
    * @return the string
    */
   public static String conceptDescriptionTextList(ConceptSequenceSet conceptIds) {
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
         Arrays.stream(conceptIds).forEach((conceptId) -> {
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
    * Concept snapshot.
    *
    * @return a {@code ConceptSnapshotService} configured using the default
    * {@code StampCoordinate} and {@code LanguageCoordinate} provided by the
    * configuration service.
    */
   public static ConceptSnapshotService defaultConceptSnapshotService() {
      if (conceptSnapshot == null) {
         conceptSnapshot = getService(ConceptService.class).getSnapshot(Get.configurationService()
               .getDefaultManifoldCoordinate());
      }

      return conceptSnapshot;
   }

   /**
    * Note, this method may fail during bootstrap, if concept being requested is not already loaded
    * into the concept service.
    * @param id either a nid or a concept sequence.
    * @return A concept specification for the corresponding identifier
    */
   public static ConceptSpecification conceptSpecification(int id) {
      id = identifierService().getConceptNid(id);
      return new ConceptProxy(conceptDescriptionText(id), identifierService().getUuidArrayForNid(id));
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
    * 
    * @return the default manifold coordinate from the configuration service. 
    */
   public static ManifoldCoordinate defaultCoordinate() {
      return configurationService().getDefaultManifoldCoordinate();
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
    * Inferred definition chronology.
    *
    * @param conceptId either a concept nid or sequence.
    * @return the inferred definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SememeChronology> inferredDefinitionChronology(int conceptId) {
      conceptId = identifierService().getConceptNid(conceptId);
      return sememeService().getSememesForComponentFromAssemblage(conceptId,
            configurationService().getDefaultLogicCoordinate()
                                  .getInferredAssemblageSequence())
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

   /**
    * Ochre externalizable stream.
    *
    * @return the stream
    */
   public static Stream<IsaacExternalizable> ochreExternalizableStream() {
      return StreamSupport.stream(new IsaacExternalizableSpliterator(), false);
   }

   /**
    * Path service.
    *
    * @return the path service
    */
   public static PathService pathService() {
      if (pathService == null) {
         pathService = getService(PathService.class);
      }

      return pathService;
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
      pathService                     = null;
      sememeBuilderService            = null;
      sememeService                   = null;
      taxonomyService                 = null;
      workExecutors                   = null;
      stampService                    = null;
      binaryDataDifferService         = null;
      postCommitService               = null;
      changeSetWriterService          = null;
      observableChronologyService     = null;
      serializationService = null;
   }

   /**
    * Sememe builder service. 
    *
    * @return the sememe builder service<? extends sememe chronology<? extends sememe version<?>>>
    */
   public static SememeBuilderService<? extends SememeChronology> sememeBuilderService() {
      if (sememeBuilderService == null) {
         sememeBuilderService = getService(SememeBuilderService.class);
      }

      return sememeBuilderService;
   }

   /**
    * Sememe service.
    *
    * @return the sememe service
    */
   public static AssemblageService sememeService() {
      if (sememeService == null) {
         sememeService = getService(AssemblageService.class);
      }

      return sememeService;
   }

   public static SerializationService serializationService() {
      if (serializationService == null) {
         serializationService = getService(SerializationService.class);
      }
      return serializationService;
   }

   
   
   /**
    * Sememe service available.
    *
    * @return true, if successful
    */
   public static boolean sememeServiceAvailable() {
      if (sememeService == null) {
         sememeService = LookupService.getService(AssemblageService.class);
      }

      return sememeService != null;
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
    * Cause all index generators implementing the {@link IndexService} to first
    * <code>clearIndex()</code> then iterate over all sememes in the database
    * and pass those chronicles to {@link IndexService#index(sh.isaac.api.chronicle.ObjectChronology)}
    * and when complete, to call <code>commitWriter()</code>.
    * {@link IndexService} services will be discovered using the HK2 dependency injection framework.
    * @param indexersToReindex - if null or empty - all indexes found via HK2 will be cleared and
    * reindexed.  Otherwise, only clear and reindex the instances of {@link IndexService} which match the specified
    * class list.  Classes passed in should be an extension of {@link IndexService}
    *
    * @return Task that indicates progress.
    */
   public static Task<Void> startIndexTask(
           @SuppressWarnings("unchecked") Class<? extends IndexService>... indexersToReindex) {
      final GenerateIndexes indexingTask = new GenerateIndexes(indexersToReindex);

      LookupService.getService(WorkExecutors.class)
                   .getExecutor()
                   .execute(indexingTask);
      return indexingTask;
   }

   /**
    * Stated definition chronology.
    *
    * @param conceptId either a concept nid or sequence.
    * @return the stated definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SememeChronology> statedDefinitionChronology(int conceptId) {
      conceptId = identifierService().getConceptNid(conceptId);
      return sememeService().getSememesForComponentFromAssemblage(conceptId,
            configurationService().getDefaultLogicCoordinate()
                                  .getStatedAssemblageSequence())
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
   
   public static ObservableChronologyService observableChronologyService() {
      if (observableChronologyService == null) {
         observableChronologyService = getService(ObservableChronologyService.class);
      }
      return observableChronologyService;
   }
   
   public static ObservableSnapshotService observableSnapshotService(ManifoldCoordinate manifoldCoordinate) {
      return observableChronologyService().getObservableSnapshotService(manifoldCoordinate);
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
   public static ThreadPoolExecutor executor() {
      return workExecutors().getExecutor();
   }
   
   public static ScheduledExecutorService scheduledExecutor() {
      return workExecutors().getScheduledThreadPoolExecutor();
   }

   //~--- get methods ---------------------------------------------------------

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
         throw new RuntimeException("No service for contract '" + clazz.getName() +
                                    "'... Is the service provider on the classpath?");
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

   public static <T> T service(Class<T> clazz) {
      return getService(clazz);
   }
   public static <T> List<T> services(Class<T> clazz) {
      return getServices(clazz);
   }
}

