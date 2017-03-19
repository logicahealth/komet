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
import sh.isaac.api.component.sememe.SememeService;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.CoordinateFactory;
import sh.isaac.api.externalizable.BinaryDataDifferService;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.BinaryDataServiceFactory;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableSpliterator;
import sh.isaac.api.index.GenerateIndexes;
import sh.isaac.api.index.IndexServiceBI;
import sh.isaac.api.logic.LogicService;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.progress.ActiveTasks;
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
         implements OchreCache {
   private static final Logger                    LOG = LogManager.getLogger();
   private static ActiveTasks                     activeTaskSet;
   private static ConfigurationService            configurationService;
   private static CommitService                   commitService;
   private static ConceptActiveService            conceptActiveService;
   private static ConceptService                  conceptService;
   private static MetaContentService              metaContentService;
   private static ConceptSnapshotService          conceptSnapshot;
   private static IdentifiedObjectService         identifiedObjectService;
   private static IdentifierService               identifierService;
   private static LanguageCoordinateService       languageCoordinateService;
   private static LogicalExpressionBuilderService logicalExpressionBuilderService;
   private static LogicService                    logicService;
   private static BinaryDataDifferService         binaryDataDifferService;
   private static PathService                     pathService;
   private static SememeBuilderService<?>         sememeBuilderService;
   private static SememeService                   sememeService;
   private static CoordinateFactory               coordinateFactory;
   private static TaxonomyService                 taxonomyService;
   private static WorkExecutors                   workExecutors;
   private static ConceptBuilderService           conceptBuilderService;
   private static StampService                    stampService;
   private static PostCommitService               postCommitService;
   private static ChangeSetWriterService          changeSetWriterService;

   //~--- constructors --------------------------------------------------------

   public Get() {}

   //~--- methods -------------------------------------------------------------

   public static ActiveTasks activeTasks() {
      if (activeTaskSet == null) {
         activeTaskSet = getService(ActiveTasks.class);
      }

      return activeTaskSet;
   }

   public static BinaryDataDifferService binaryDataDifferService() {
      if (binaryDataDifferService == null) {
         binaryDataDifferService = getService(BinaryDataDifferService.class);
      }

      return binaryDataDifferService;
   }

   public static BinaryDataReaderQueueService binaryDataQueueReader(Path dataPath)
            throws FileNotFoundException {
      return getService(BinaryDataServiceFactory.class).getQueueReader(dataPath);
   }

   public static BinaryDataReaderService binaryDataReader(Path dataPath)
            throws FileNotFoundException {
      return getService(BinaryDataServiceFactory.class).getReader(dataPath);
   }

   public static DataWriterService binaryDataWriter(Path dataPath)
            throws IOException {
      return getService(BinaryDataServiceFactory.class).getWriter(dataPath);
   }

   public static ChangeSetWriterService changeSetWriterService() {
      if (changeSetWriterService == null) {
         changeSetWriterService = getService(ChangeSetWriterService.class);
      }

      return changeSetWriterService;
   }

   public static CommitService commitService() {
      if (commitService == null) {
         commitService = getService(CommitService.class);
      }

      return commitService;
   }

   public static ConceptActiveService conceptActiveService() {
      if (conceptActiveService == null) {
         conceptActiveService = getService(ConceptActiveService.class);
      }

      return conceptActiveService;
   }

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
    */
   public static String conceptDescriptionText(int conceptId) {
      Optional<LatestVersion<DescriptionSememe<?>>> descriptionOptional =
         conceptSnapshot().getDescriptionOptional(conceptId);

      if (descriptionOptional.isPresent()) {
         return descriptionOptional.get()
                                   .value()
                                   .getText();
      }

      return "No desc for: " + conceptId;
   }

   public static String conceptDescriptionTextList(ConceptSequenceSet conceptIds) {
      return conceptDescriptionTextList(conceptIds.asArray());
   }

   public static String conceptDescriptionTextList(int[] conceptIds) {
      if ((conceptIds != null) && (conceptIds.length > 0)) {
         StringBuilder builder = new StringBuilder();

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

   public static String conceptDescriptionTextList(List<Integer> conceptIds) {
      return conceptDescriptionTextList(conceptIds.stream()
            .mapToInt((boxedInt) -> (int) boxedInt)
            .toArray());
   }

   public static ConceptService conceptService() {
      if (conceptService == null) {
         conceptService = getService(ConceptService.class);
      }

      return conceptService;
   }

   /**
    *
    * @return a {@code ConceptSnapshotService} configured using the default
    * {@code StampCoordinate} and {@code LanguageCoordinate} provided by the
    * configuration service.
    */
   public static ConceptSnapshotService conceptSnapshot() {
      if (conceptSnapshot == null) {
         conceptSnapshot = getService(ConceptService.class).getSnapshot(Get.configurationService()
               .getDefaultStampCoordinate(),
               Get.configurationService()
                  .getDefaultLanguageCoordinate());
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

   public static ConfigurationService configurationService() {
      if (configurationService == null) {
         configurationService = getService(ConfigurationService.class);
      }

      return configurationService;
   }

   public static CoordinateFactory coordinateFactory() {
      if (coordinateFactory == null) {
         coordinateFactory = getService(CoordinateFactory.class);
      }

      return coordinateFactory;
   }

   public static IdentifiedObjectService identifiedObjectService() {
      if (identifiedObjectService == null) {
         identifiedObjectService = getService(IdentifiedObjectService.class);
      }

      return identifiedObjectService;
   }

   public static IdentifierService identifierService() {
      if (identifierService == null) {
         identifierService = getService(IdentifierService.class);
      }

      return identifierService;
   }

   /**
    *
    * @param conceptId either a concept nid or sequence.
    * @return the inferred definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SememeChronology<? extends SememeVersion<?>>> inferredDefinitionChronology(int conceptId) {
      conceptId = identifierService().getConceptNid(conceptId);
      return sememeService().getSememesForComponentFromAssemblage(conceptId,
            configurationService().getDefaultLogicCoordinate()
                                  .getInferredAssemblageSequence())
                            .findAny();
   }

   public static LanguageCoordinateService languageCoordinateService() {
      if (languageCoordinateService == null) {
         languageCoordinateService = getService(LanguageCoordinateService.class);
      }

      return languageCoordinateService;
   }

   public static LogicService logicService() {
      if (logicService == null) {
         logicService = getService(LogicService.class);
      }

      return logicService;
   }

   public static LogicalExpressionBuilderService logicalExpressionBuilderService() {
      if (logicalExpressionBuilderService == null) {
         logicalExpressionBuilderService = getService(LogicalExpressionBuilderService.class);
      }

      return logicalExpressionBuilderService;
   }

   public static MetaContentService metaContentService() {
      if (metaContentService == null) {
         metaContentService = getService(MetaContentService.class);
      }

      return metaContentService;
   }

   public static Stream<OchreExternalizable> ochreExternalizableStream() {
      return StreamSupport.stream(new OchreExternalizableSpliterator(), false);
   }

   public static PathService pathService() {
      if (pathService == null) {
         pathService = getService(PathService.class);
      }

      return pathService;
   }

   public static PostCommitService postCommitService() {
      if (postCommitService == null) {
         postCommitService = getService(PostCommitService.class);
      }

      return postCommitService;
   }

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
   }

   public static SememeBuilderService<? extends SememeChronology<? extends SememeVersion<?>>> sememeBuilderService() {
      if (sememeBuilderService == null) {
         sememeBuilderService = getService(SememeBuilderService.class);
      }

      return sememeBuilderService;
   }

   public static SememeService sememeService() {
      if (sememeService == null) {
         sememeService = getService(SememeService.class);
      }

      return sememeService;
   }

   public static boolean sememeServiceAvailable() {
      if (sememeService == null) {
         sememeService = LookupService.getService(SememeService.class);
      }

      return sememeService != null;
   }

   public static StampService stampService() {
      if (stampService == null) {
         stampService = getService(StampService.class);
      }

      return stampService;
   }

   /**
    * Perform indexing according to all installed indexers.
    *
    * Cause all index generators implementing the {@link IndexServiceBI} to first
    * <code>clearIndex()</code> then iterate over all sememes in the database
    * and pass those chronicles to {@link IndexServiceBI#index(sh.isaac.api.chronicle.ObjectChronology)}
    * and when complete, to call <code>commitWriter()</code>.
    * {@link IndexServiceBI} services will be discovered using the HK2 dependency injection framework.
    * @param indexersToReindex - if null or empty - all indexes found via HK2 will be cleared and
    * reindexed.  Otherwise, only clear and reindex the instances of {@link IndexServiceBI} which match the specified
    * class list.  Classes passed in should be an extension of {@link IndexServiceBI}
    *
    * @return Task that indicates progress.
    */
   public static Task<Void> startIndexTask(
           @SuppressWarnings("unchecked") Class<? extends IndexServiceBI>... indexersToReindex) {
      GenerateIndexes indexingTask = new GenerateIndexes(indexersToReindex);

      LookupService.getService(WorkExecutors.class)
                   .getExecutor()
                   .execute(indexingTask);
      return indexingTask;
   }

   /**
    *
    * @param conceptId either a concept nid or sequence.
    * @return the stated definition chronology for the specified concept
    * according to the default logic coordinate.
    */
   public static Optional<SememeChronology<? extends SememeVersion<?>>> statedDefinitionChronology(int conceptId) {
      conceptId = identifierService().getConceptNid(conceptId);
      return sememeService().getSememesForComponentFromAssemblage(conceptId,
            configurationService().getDefaultLogicCoordinate()
                                  .getStatedAssemblageSequence())
                            .findAny();
   }

   public static TaxonomyService taxonomyService() {
      if (taxonomyService == null) {
         taxonomyService = getService(TaxonomyService.class);
      }

      return taxonomyService;
   }

   public static WorkExecutors workExecutors() {
      if (workExecutors == null) {
         workExecutors = getService(WorkExecutors.class);
      }

      return workExecutors;
   }

   //~--- get methods ---------------------------------------------------------

   private static <T> T getService(Class<T> clazz) {
      T service = LookupService.getService(clazz);

      if (service == null) {
         throw new RuntimeException("No service for contract '" + clazz.getName() +
                                    "'... Is the service provider on the classpath?");
      }

      return service;
   }
}

