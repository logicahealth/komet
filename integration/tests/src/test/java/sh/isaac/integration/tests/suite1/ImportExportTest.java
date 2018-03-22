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



package sh.isaac.integration.tests.suite1;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Paths;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jvnet.testing.hk2testng.HK2;

import org.testng.Assert;
import org.testng.annotations.Test;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.tree.Tree;
import sh.isaac.MetaData;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.model.logic.LogicByteArrayConverterService;
import sh.isaac.model.logic.definition.LogicalExpressionBuilderProvider;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.Feature;
import static sh.isaac.api.logic.LogicalExpressionBuilder.FloatLiteral;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SufficientSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/2/16.
 */
@HK2("integration")
@Test(suiteName="suite1")
public class ImportExportTest {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The builder provider. */
   LogicalExpressionBuilderProvider builderProvider = new LogicalExpressionBuilderProvider();

   /** The import stats. */
   IsaacExternalizableStatsTestFilter importStats;

   //~--- methods -------------------------------------------------------------

   /**
    * Test classify.
    */
   
   @Test(
      groups           = { "load" },
      dependsOnMethods = { "testExportImport" }
   )
   public void testClassify() {
      LOG.info("Classifying");

      final StampCoordinate stampCoordinate = Get.coordinateFactory()
                                                 .createDevelopmentLatestStampCoordinate();
      final LogicCoordinate logicCoordinate = Get.coordinateFactory()
                                                 .createStandardElProfileLogicCoordinate();
      final EditCoordinate  editCoordinate  = Get.coordinateFactory()
                                                 .createClassifierSolorOverlayEditCoordinate();
      final ClassifierService logicService = Get.logicService()
                                                .getClassifierService(stampCoordinate, logicCoordinate, editCoordinate);
      final Task<ClassifierResults> classifyTask = logicService.classify();
      
      try {
         final ClassifierResults classifierResults = classifyTask.get();
         // TODO classifyTask.get() does not block until UpdateTaxonomyAfterCommitTask is complete...
         // Is updateTaxomy being called twice? Once during UpdateTaxonomyAfterCommitTask and once during classify?
         LOG.info("Classify results: " + classifierResults);
      } catch (InterruptedException | ExecutionException e) {
         LOG.error("Classify failed", e);
         Assert.fail("Classify failed.", e);
      }
   }

   /**
    * Test convert logic graph form.
    *
    * @throws Exception the exception
    */
   @Test(
      groups           = { "load" },
      dependsOnMethods = { "testLoad" }
   )
   public void testConvertLogicGraphForm()
            throws Exception {
      final LogicalExpressionBuilder defBuilder = this.builderProvider.getLogicalExpressionBuilder();

      SufficientSet(And(SomeRole(MetaData.ROLE_GROUP____SOLOR,
                                 And(Feature(MetaData.HAS_STRENGTH____SOLOR, FloatLiteral(1.2345F, defBuilder)),
                                     ConceptAssertion(MetaData.MASTER_PATH____SOLOR, defBuilder)))));

      final LogicalExpression              logicGraphDef    = defBuilder.build();
      final LogicByteArrayConverterService converter        = new LogicByteArrayConverterService();
      final byte[][]                       internalizedData = logicGraphDef.getData(DataTarget.INTERNAL);
      final byte[][] externalizedData = converter.convertLogicGraphForm(internalizedData, DataTarget.EXTERNAL);
      final byte[][] reinternalizedData = converter.convertLogicGraphForm(externalizedData, DataTarget.INTERNAL);

      if (!Arrays.deepEquals(internalizedData, reinternalizedData)) {
         Assert.fail(
             "convertLogicGraphForm() FAILED: Reinternalized LogicGraph LogicalExpression does not match original internalized version");
      }
   }

   /**
    * Test export after classify.
    */
   @Test(
      groups           = { "load" },
      dependsOnMethods = { "testClassify" }
   )
   public void testExportAfterClassify() {
      LOG.info("Testing export after classify");

      try {
         final IsaacExternalizableStatsTestFilter exportStats = new IsaacExternalizableStatsTestFilter();
         final DataWriterService writer = Get.binaryDataWriter(Paths.get("target",
                                                                         "data",
                                                                         "exported",
                                                                         "IsaacMetadataAuxiliary.export.ibdf"));

         Get.isaacExternalizableStream()
            .filter(exportStats)
            .forEach((ochreExternalizable) -> {
                        writer.put(ochreExternalizable);

                        if (ochreExternalizable.getIsaacObjectType() == IsaacObjectType.STAMP_ALIAS) {
                           LOG.info(ochreExternalizable);
                        }
                     });
         writer.close();
         LOG.info("exported components: " + exportStats);

         if (exportStats.concepts.get() != this.importStats.concepts.get()) {
            Get.conceptService()
               .getConceptChronologyStream(TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid())
               .forEach((conceptChronology) -> LOG.info(conceptChronology));
         }

         Assert.assertEquals(exportStats.concepts.get(), this.importStats.concepts.get());

         // One new semantic for every concept except the root concept from classification...
         Assert.assertEquals(exportStats.semantics.get(),
                             this.importStats.semantics.get() + exportStats.concepts.get() - 1);

         // One new stamp comment for the classify writeback
         Assert.assertEquals(exportStats.stampComments.get(), this.importStats.stampComments.get() + 1);
         Assert.assertEquals(exportStats.stampAliases.get(), this.importStats.stampAliases.get());
      } catch (final IOException e) {
         Assert.fail("File not found", e);
      }
   }

   /**
    * Test export import.
    */
   @Test(
      groups           = { "load" },
      dependsOnMethods = { "testLoad" }
   )
   public void testExportImport() {
      LOG.info("Testing exportImport");

      try {
         final AtomicInteger                      exportCount = new AtomicInteger(0);
         final AtomicInteger                      importCount = new AtomicInteger(0);
         final IsaacExternalizableStatsTestFilter exportStats = new IsaacExternalizableStatsTestFilter();
         final DataWriterService writer = Get.binaryDataWriter(Paths.get("target",
                                                                         "data",
                                                                         "exported",
                                                                         "IsaacMetadataAuxiliary.export.ibdf"));

         Get.isaacExternalizableStream()
            .filter(exportStats)
            .forEach((ochreExternalizable) -> {
                        writer.put(ochreExternalizable);
                        exportCount.incrementAndGet();
                     });
         writer.close();
         LOG.info("exported components: " + exportStats);
         Assert.assertEquals(exportStats, this.importStats);

         final BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target",
                                                                               "data",
                                                                               "exported",
                                                                               "IsaacMetadataAuxiliary.export.ibdf"));
         final IsaacExternalizableStatsTestFilter localImportStats   = new IsaacExternalizableStatsTestFilter();
         final CommitService                      commitService = Get.commitService();

         reader.getStream()
               .filter(localImportStats)
               .forEach((object) -> {
            try {
               importCount.incrementAndGet();
               commitService.importNoChecks(object);
            } catch (Throwable e) {
               e.printStackTrace();
               throw e;
            }
                        });
         commitService.postProcessImportNoChecks();
         LOG.info("imported components: " + localImportStats);
         Assert.assertEquals(exportCount.get(), importCount.get());
         Assert.assertEquals(exportStats, localImportStats);
      } catch (final IOException e) {
         Assert.fail("File not found", e);
      }
   }

   /**
    * Test inferred taxonomy.
    */
   @Test(
      groups           = { "load" },
      dependsOnMethods = { "testClassify" }
   )
   public void testInferredTaxonomy() {
      LOG.info("Testing inferred taxonomy");
      final ManifoldCoordinate manifoldCoordinate = Get.configurationService().getGlobalDatastoreConfiguration()
              .getDefaultManifoldCoordinate()
              .makeCoordinateAnalog(PremiseType.INFERRED);
      TaxonomySnapshotService taxonomySnapshotService = Get.taxonomyService().getSnapshot(manifoldCoordinate);
      final int[] roots = taxonomySnapshotService.getRoots();
      final NidSet rootAssemblages = new NidSet();
      for (int rootNid: roots) {
         rootAssemblages.add(ModelGet.identifierService().getAssemblageNid(rootNid).getAsInt());
      }
      StringBuilder rootsMessage = new StringBuilder();
      for (int root: roots) {
         rootsMessage.append(Get.conceptDescriptionText(root)).append("; ");
         
         rootsMessage.append("\n");
      }
      String message = rootsMessage.toString();
      if (roots.length != 1) {
         LOG.warn(message);
      }
      Assert.assertEquals(roots.length, 1, message);
      final Tree taxonomyTree  = taxonomySnapshotService.getTaxonomyTree();
      final AtomicInteger taxonomyCount = new AtomicInteger(0);
      taxonomyTree.depthFirstProcess(roots[0],
              (TreeNodeVisitData t,
                      int conceptSequence) -> {
                 taxonomyCount.incrementAndGet();
              }, Get.taxonomyService().getTreeNodeVisitDataSupplier(taxonomyTree.getAssemblageNid()));
      Assert.assertEquals(taxonomyCount.get(), this.importStats.concepts.get());
      logTree(roots[0], taxonomyTree);
   }

   /**
    * Test load.
    */
   @Test(groups = { "load" })
   public void testLoad() {
      LOG.info("Testing load");
      
      int descriptionAssemblageNid = TermAux.DESCRIPTION_ASSEMBLAGE.getNid();
      int chroniclePropertiesNid = MetaData.CHRONICLE_PROPERTIES____SOLOR.getNid();
      int statedAssemblageNid = MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid();

      try {
         final BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target",
                                                                               "data",
                                                                               "IsaacMetadataAuxiliary.ibdf"));
         final CommitService commitService = Get.commitService();

         this.importStats = new IsaacExternalizableStatsTestFilter();
         reader.getStream()
               .filter(this.importStats)
               .forEach((object) -> {
                  if (object instanceof SemanticChronology) {
                     SemanticChronology sc = (SemanticChronology) object;
                     if (sc.getReferencedComponentNid() == chroniclePropertiesNid || sc.getReferencedComponentNid() == descriptionAssemblageNid) {
                        if (sc.getAssemblageNid() == statedAssemblageNid) {
                            try {
                                LOG.info("Found watch def: " + sc);
                            } catch (Throwable e) {
                                e.printStackTrace();
                                throw e;
                            }
                        }
                        
                     }
                  }
                           commitService.importNoChecks(object);
                        });
         Get.startIndexTask().get();
         commitService.postProcessImportNoChecks();
         LOG.info("Loaded components: " + this.importStats);
         LOG.info("Concept count: " + Get.identifierService().getNidStreamOfType(IsaacObjectType.CONCEPT).count());
      } catch (final FileNotFoundException e) {
         Assert.fail("File not found", e);
      } catch (InterruptedException ex) {
         Assert.fail("Interrupted", ex);
      } catch (ExecutionException ex) {
         Assert.fail("Execution exception", ex);
      }
   }

   /**
    * Test stated taxonomy.
    */
   @Test(
      groups           = { "load" },
      dependsOnMethods = { "testLoad" }
   )
   public void testStatedTaxonomy() {
      LOG.info("Testing stated taxonomy");
      final ManifoldCoordinate manifoldCoordinate = Get.configurationService().getGlobalDatastoreConfiguration()
              .getDefaultManifoldCoordinate()
              .makeCoordinateAnalog(PremiseType.STATED);
      LOG.info("Concepts in database: " + Get.conceptService().getConceptCount());
      TaxonomySnapshotService taxonomySnapshotService = Get.taxonomyService().getSnapshot(manifoldCoordinate);
      final int[] roots = taxonomySnapshotService.getRoots();
      Assert.assertEquals(roots.length, 1, "Root count != 1: " + Arrays.asList(roots));
      final Tree          taxonomyTree  = taxonomySnapshotService.getTaxonomyTree();
      final AtomicInteger taxonomyCount = new AtomicInteger(0);
      taxonomyTree.depthFirstProcess(roots[0],
              (TreeNodeVisitData t,
                      int conceptSequence) -> {
                 taxonomyCount.incrementAndGet();
              }, Get.taxonomyService().getTreeNodeVisitDataSupplier(taxonomyTree.getAssemblageNid()));
      logTree(roots[0], taxonomyTree);
      Assert.assertEquals(taxonomyCount.get(), this.importStats.concepts.get());
   }

   /**
    * Log tree.
    *
    * @param root the root
    * @param taxonomyTree the taxonomy tree
    */
   private void logTree(int root, Tree taxonomyTree) {
      taxonomyTree.depthFirstProcess(root,
                                     (TreeNodeVisitData t,
                                      int conceptSequence) -> {
                                        final int    paddingSize = t.getDistance(conceptSequence) * 2;
                                        final char[] padding     = new char[paddingSize];

                                        Arrays.fill(padding, ' ');
                                        LOG.info(new String(padding) + Get.conceptDescriptionText(conceptSequence));
                                     }, Get.taxonomyService().getTreeNodeVisitDataSupplier(taxonomyTree.getAssemblageNid()));
   }
}

