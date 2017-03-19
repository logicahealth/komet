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



package sh.isaac.provider.logic.csiro.classify;

//~--- JDK imports ------------------------------------------------------------

import java.time.Instant;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.csiro.ontology.Ontology;
import au.csiro.ontology.classification.IReasoner;
import au.csiro.snorocket.core.SnorocketReasoner;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.sememe.version.LogicGraphSememeImpl;
import sh.isaac.provider.logic.csiro.axioms.GraphToAxiomTranslator;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ClassifierData
         implements ChronologyChangeListener {
   private static final Logger                          log                = LogManager.getLogger();
   private static final AtomicReference<ClassifierData> singletonReference = new AtomicReference<>();

   //~--- fields --------------------------------------------------------------

   private final UUID     listenerUuid                 = UUID.randomUUID();
   private boolean        incrementalAllowed           = false;
   GraphToAxiomTranslator allGraphsToAxiomTranslator   = new GraphToAxiomTranslator();
   GraphToAxiomTranslator incrementalToAxiomTranslator = new GraphToAxiomTranslator();
   IReasoner              reasoner                     = new SnorocketReasoner();
   ConceptSequenceSet     loadedConcepts               = new ConceptSequenceSet();
   Instant                lastClassifyInstant;
   ClassificationType     lastClassifyType;
   StampCoordinate        stampCoordinate;
   LogicCoordinate        logicCoordinate;

   //~--- constructors --------------------------------------------------------

   private ClassifierData(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
      this.stampCoordinate = stampCoordinate;
      this.logicCoordinate = logicCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   public IReasoner classify() {
      this.loadedConcepts = this.allGraphsToAxiomTranslator.getLoadedConcepts();
      this.allGraphsToAxiomTranslator.clear();
      this.lastClassifyInstant = Instant.now();

      if (this.lastClassifyType == null) {
         this.lastClassifyType   = ClassificationType.COMPLETE;
         this.incrementalAllowed = true;
      } else {
         if (this.incrementalAllowed) {
            this.lastClassifyType = ClassificationType.INCREMENTAL;
            this.incrementalToAxiomTranslator.clear();
         } else {
            this.lastClassifyType = ClassificationType.COMPLETE;
         }
      }

      return this.reasoner.classify();
   }

   public void clearAxioms() {
      this.allGraphsToAxiomTranslator.clear();
      this.incrementalToAxiomTranslator.clear();
   }

   @Override
   public void handleChange(ConceptChronology cc) {
      // Nothing to do... Only concerned about changes to logic graph.
   }

   @Override
   public void handleChange(SememeChronology sc) {
      if (sc.getAssemblageSequence() == this.logicCoordinate.getStatedAssemblageSequence()) {
         log.info("Stated form change: " + sc);

         // only process if incremental is a possibility.
         if (this.incrementalAllowed) {
            final Optional<LatestVersion<LogicGraphSememeImpl>> optionalLatest =
               sc.getLatestVersion(LogicGraphSememeImpl.class,
                                   this.stampCoordinate);

            if (optionalLatest.isPresent()) {
               final LatestVersion<LogicGraphSememeImpl> latest = optionalLatest.get();

               // get stampCoordinate for last classify.
               final StampCoordinate stampToCompare = this.stampCoordinate.makeAnalog(this.lastClassifyInstant.toEpochMilli());

               // See if there is a change in the optionalLatest vs the last classify.
               final Optional<LatestVersion<LogicGraphSememeImpl>> optionalPrevious =
                  sc.getLatestVersion(LogicGraphSememeImpl.class,
                                      stampToCompare);

               if (optionalPrevious.isPresent()) {
                  // See if the change has deletions, if so then incremental is not allowed.
                  final LatestVersion<LogicGraphSememeImpl> previous  = optionalPrevious.get();
                  boolean                             deletions = false;

                  if (latest.value()
                            .getGraphData().length <= previous.value().getGraphData().length) {
                     // If nodes where deleted, or an existing node was changed but the size remains the same assume deletions
                     deletions = true;

                     // TODO use a real subtree isomorphism algorithm.
                  }

                  if (deletions) {
                     this.incrementalAllowed = false;
                     this.incrementalToAxiomTranslator.clear();
                     this.reasoner = new SnorocketReasoner();
                  } else {
                     // Otherwise add axioms...
                     this.incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.value());
                  }
               } else {
                  // Otherwise add axioms...
                  this.incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.value());
               }
            }
         }
      }
   }

   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // already handled with the handle change above.
   }

   public void loadAxioms() {
      if (this.incrementalAllowed) {
         this.reasoner.loadAxioms(this.incrementalToAxiomTranslator.getAxioms());
         this.loadedConcepts = this.incrementalToAxiomTranslator.getLoadedConcepts();
      } else {
         this.reasoner.loadAxioms(this.allGraphsToAxiomTranslator.getAxioms());
         this.loadedConcepts = this.allGraphsToAxiomTranslator.getLoadedConcepts();
      }
   }

   @Override
   public String toString() {
      return "ClassifierData{" + "graphToAxiomTranslator=" + this.allGraphsToAxiomTranslator +
             ",\n incrementalToAxiomTranslator=" + this.incrementalToAxiomTranslator + ",\n reasoner=" + this.reasoner +
             ",\n lastClassifyInstant=" + this.lastClassifyInstant + ",\n lastClassifyType=" + this.lastClassifyType +
             ",\n stampCoordinate=" + this.stampCoordinate + ",\n logicCoordinate=" + this.logicCoordinate + '}';
   }

   public void translate(LogicGraphSememeImpl lgs) {
      this.allGraphsToAxiomTranslator.convertToAxiomsAndAdd(lgs);
   }

   //~--- get methods ---------------------------------------------------------

   public ConceptSequenceSet getAffectedConceptSequenceSet() {
      final ConceptSequenceSet affectedConceptSequences = new ConceptSequenceSet();

      if (this.lastClassifyType == ClassificationType.INCREMENTAL) {
         // not returning loaded concepts here, because incremental classification
         // can affect concepts other than what was loaded.
         this.reasoner.getClassifiedOntology().getAffectedNodes().forEach((node) -> {
                             if (node != null) {  // TODO why does the classifier include null in the affected node set.
                                node.getEquivalentConcepts()
                                    .forEach(
                                        (equalivent) -> affectedConceptSequences.add(Integer.parseInt(equalivent)));
                             }
                          });
      } else {
         return this.loadedConcepts;
      }

      return affectedConceptSequences;
   }

   public boolean isClassified() {
      return this.reasoner.isClassified();
   }

   public Ontology getClassifiedOntology() {
      return this.reasoner.getClassifiedOntology();
   }

   public static ClassifierData get(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
      if (singletonReference.get() == null) {
         singletonReference.compareAndSet(null, new ClassifierData(stampCoordinate, logicCoordinate));
      } else {
         ClassifierData classifierData = singletonReference.get();

         while (!classifierData.stampCoordinate.equals(stampCoordinate) ||
                !classifierData.logicCoordinate.equals(logicCoordinate)) {
            Get.commitService()
               .removeChangeListener(classifierData);

            final ClassifierData newClassifierData = new ClassifierData(stampCoordinate, logicCoordinate);

            singletonReference.compareAndSet(classifierData, newClassifierData);
            classifierData = singletonReference.get();
         }
      }

      Get.commitService()
         .addChangeListener(singletonReference.get());
      return singletonReference.get();
   }

   public boolean isIncrementalAllowed() {
      return this.incrementalAllowed;
   }

   public Instant getLastClassifyInstant() {
      return this.lastClassifyInstant;
   }

   @Override
   public UUID getListenerUuid() {
      return this.listenerUuid;
   }
}

