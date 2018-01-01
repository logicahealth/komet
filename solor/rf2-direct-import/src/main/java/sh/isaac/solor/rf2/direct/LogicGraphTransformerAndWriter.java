/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.solor.rf2.direct;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SufficientSet;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;

/**
 *
 * @author kec
 */
public class LogicGraphTransformerAndWriter extends TimedTaskWithProgressTracker<Void> {

   /**
    * The never role group set.
    */
   private final NidSet neverRoleGroupSet = new NidSet();

   private final NidSet definingCharacteristicSet = new NidSet();

   private final int isaNid = TermAux.IS_A.getNid();

   private final int legacyImplicationAssemblageNid = TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getNid();

   private final int sufficientDefinition = TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid();

   private final int primitiveDefinition = TermAux.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION.getNid();
   private final int solorOverlayModuleNid = TermAux.SOLOR_OVERLAY_MODULE.getNid();
   private final int statedAssemblageNid = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid();
   private final int inferredAssemblageNid = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid();
   private final int authorNid = TermAux.USER.getNid();
   private final int developmentPathNid = TermAux.DEVELOPMENT_PATH.getNid();
   private final TaxonomyService taxonomyService;

   {
      this.neverRoleGroupSet.add(TermAux.PART_OF.getNid());
      this.neverRoleGroupSet.add(TermAux.LATERALITY.getNid());
      this.neverRoleGroupSet.add(TermAux.HAS_ACTIVE_INGREDIENT.getNid());
      this.neverRoleGroupSet.add(TermAux.HAS_DOSE_FORM.getNid());

      this.definingCharacteristicSet.add(MetaData.INFERRED_PREMISE_TYPE____SOLOR.getNid());
      this.definingCharacteristicSet.add(MetaData.STATED_PREMISE_TYPE____SOLOR.getNid());
   }

   private final Semaphore writeSemaphore;
   private final List<TransformationGroup> transformationRecords;
   private final List<IndexBuilderService> indexers;

   public LogicGraphTransformerAndWriter(List<TransformationGroup> transformationRecords, Semaphore writeSemaphore) {
      this.transformationRecords = transformationRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      this.taxonomyService = Get.taxonomyService();
      indexers = LookupService.get().getAllServices(IndexBuilderService.class);
      updateTitle("EL++ transformation");
      updateMessage("");
      addToTotalWork(transformationRecords.size());
      Get.activeTasks().add(this);
   }
   protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

   private void index(Chronology chronicle) {
      if (chronicle instanceof SemanticChronology) {
         this.taxonomyService.updateTaxonomy((SemanticChronology) chronicle);
      }
      for (IndexBuilderService indexer: indexers) {
         try {
            indexer.index(chronicle).get();
         } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex);
         }
      }
      
   }

   @Override
   protected Void call() throws Exception {
      try {
         int count = 0;
         for (TransformationGroup transformationGroup : transformationRecords) {
            transformRelationships(transformationGroup.conceptNid, transformationGroup.relationshipNids, transformationGroup.getPremiseType());
            if (count % 1000 == 0) {
               updateMessage("Processing concept: " + Get.conceptDescriptionText(transformationGroup.conceptNid));
            }
            count++;
            completedUnitOfWork();
         }
         return null;
      } finally {
         this.writeSemaphore.release();
         for (IndexBuilderService indexer : indexers) {
            indexer.sync().get();
         }
         this.done();
         Get.activeTasks().remove(this);
      }
   }

   private void transformAtTimePath(StampPosition stampPosition, int conceptNid, List<SemanticChronology> relationships, PremiseType premiseType) {
      final LogicalExpressionBuilder logicalExpressionBuilder = Get.logicalExpressionBuilderService()
              .getLogicalExpressionBuilder();
      final ArrayList<Assertion> assertions = new ArrayList<>();
      final HashMap<Integer, ArrayList<Assertion>> groupedAssertions = new HashMap<>();

      StampCoordinate stampCoordinate = new StampCoordinateImpl(StampPrecedence.PATH,
              stampPosition,
              new NidSet(),
              Status.makeActiveOnlySet());

      // for each relationship, add to assertion or grouped assertions. 
      for (final SemanticChronology rb : relationships) {
         LatestVersion<Rf2Relationship> latestRel = rb.getLatestVersion(stampCoordinate);
         if (latestRel.isPresent()) {
            Rf2Relationship relationship = latestRel.get();

            if (definingCharacteristicSet.contains(relationship.getCharacteristicNid())) {

               if (relationship.getRelationshipGroup() == 0) {

                  if (isaNid == relationship.getTypeNid()) {
                     assertions.add(ConceptAssertion(relationship.getDestinationNid(),
                             logicalExpressionBuilder));
                  } else {
                     if (this.neverRoleGroupSet.contains(relationship.getTypeNid())) {
                        assertions.add(SomeRole(relationship.getTypeNid(),
                                ConceptAssertion(relationship.getDestinationNid(),
                                        logicalExpressionBuilder)));
                     } else {
                        assertions.add(SomeRole(MetaData.ROLE_GROUP____SOLOR.getNid(),
                                And(SomeRole(relationship.getTypeNid(),
                                        ConceptAssertion(relationship.getDestinationNid(),
                                                logicalExpressionBuilder)))));
                     }
                  }
               } else {
                  ArrayList<Assertion> groupAssertions = groupedAssertions.get(relationship.getRelationshipGroup());

                  if (groupAssertions == null) {
                     groupAssertions = new ArrayList<>();
                     groupedAssertions.put(relationship.getRelationshipGroup(), groupAssertions);
                  }
                  groupAssertions.add(SomeRole(relationship.getTypeNid(),
                          ConceptAssertion(relationship.getDestinationNid(),
                                  logicalExpressionBuilder)));
               }
            }
         }
      }

      // handle relationship groups
      for (final ArrayList<Assertion> groupAssertions : groupedAssertions.values()) {
         assertions.add(SomeRole(MetaData.ROLE_GROUP____SOLOR.getNid(),
                 And(groupAssertions.toArray(new Assertion[groupAssertions.size()]))));
      }

      if (assertions.size() > 0) {
         boolean defined = false; // Change to use list instead of stream...
         Stream<SemanticChronology> implicationChronologyStream = Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(conceptNid, legacyImplicationAssemblageNid);
         List<SemanticChronology> implicationList = implicationChronologyStream.collect(Collectors.toList());
         if (implicationList.size() == 1) {
            SemanticChronology implicationChronology = implicationList.get(0);
            LatestVersion<ComponentNidVersion> latestImplication = implicationChronology.getLatestVersion(stampCoordinate);
            if (latestImplication.isPresent()) {
               ComponentNidVersion definitionStatus = latestImplication.get();
               if (definitionStatus.getComponentNid() == sufficientDefinition) {
                  defined = true;
               } else if (definitionStatus.getComponentNid() == primitiveDefinition) {
                  defined = false;
               } else {
                  throw new RuntimeException("Unexpected concept definition status: " + definitionStatus);
               }
            } else {
               LOG.error("No implication to: " + Get.conceptDescriptionText(conceptNid));
            }
            if (defined) {
               SufficientSet(And(assertions.toArray(new Assertion[assertions.size()])));
            } else {
               NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
            }

            final LogicalExpression le = logicalExpressionBuilder.build();

            if (le.isMeaningful()) {

               // TODO [graph] what if the modules are different across the graph rels?
               addLogicGraph(conceptNid,
                       le,
                       premiseType,
                       stampPosition.getTime(),
                       solorOverlayModuleNid);
            } else {
               LOG.error("expression not meaningful?");
            }
         }

      }
   }

   /**
    * Transform relationships.
    *
    * @param stated the stated
    * @throws SQLException the SQL exception
    */
   private void transformRelationships(int conceptNid, int[] relNids, PremiseType premiseType) {
      updateMessage("Converting " + premiseType + " relationships into logic graphs");

      List<SemanticChronology> relationshipChronologiesForConcept = new ArrayList<>();
      TreeSet<StampPosition> stampPositionsToProcess = new TreeSet<>();
      for (int relNid : relNids) {
         SemanticChronology relationshipChronology = Get.assemblageService().getSemanticChronology(relNid);
         for (int stamp : relationshipChronology.getVersionStampSequences()) {
            StampService stampService = Get.stampService();
            stampPositionsToProcess.add(new StampPositionImpl(stampService.getTimeForStamp(stamp), stampService.getPathNidForStamp(stamp)));
         }
         relationshipChronologiesForConcept.add(relationshipChronology);
      }
      for (StampPosition stampPosition : stampPositionsToProcess) {
         transformAtTimePath(stampPosition, conceptNid, relationshipChronologiesForConcept, premiseType);
      }

   }

   /**
    * Adds the relationship graph.
    *
    * @param conceptNid the conceptNid
    * @param logicalExpression the logical expression
    * @param premiseType the premise type
    * @param time the time
    * @param moduleNid the module
    * @return the sememe chronology
    */
   public SemanticChronology addLogicGraph(int conceptNid,
           LogicalExpression logicalExpression,
           PremiseType premiseType,
           long time,
           int moduleNid) {
      int graphAssemblageNid = statedAssemblageNid;
      if (premiseType == PremiseType.INFERRED) {
         graphAssemblageNid = inferredAssemblageNid;
      }

      final SemanticBuilder sb = Get.semanticBuilderService().getLogicalExpressionBuilder(logicalExpression,
              conceptNid,
              graphAssemblageNid);

      UUID nameSpace = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid();
      if (premiseType == PremiseType.INFERRED) {
         nameSpace = TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getPrimordialUuid();
      }
      // Create UUID from seed and assign SemanticBuilder the value

      final UUID generatedGraphPrimordialUuid = UuidT5Generator.get(nameSpace, Get.concept(conceptNid).getPrimordialUuid().toString());

      sb.setPrimordialUuid(generatedGraphPrimordialUuid);

      final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();
      int stamp = Get.stampService().getStampSequence(Status.ACTIVE, time, authorNid, moduleNid, developmentPathNid);
      final SemanticChronology sci = (SemanticChronology) sb.build(stamp,
              builtObjects);
      // There should be no other build objects, so ignore the builtObjects list...
      
      if (builtObjects.size() != 1) {
         throw new IllegalStateException("More than one build object: " + builtObjects);
      }
      index(sci);
      Get.assemblageService().writeSemanticChronology(sci);

      return sci;
   }

}
