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
 * The Class ClassifierData.
 *
 * @author kec
 */
public class ClassifierData
         implements ChronologyChangeListener {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   /** The Constant singletonReference. */
   private static final AtomicReference<ClassifierData> singletonReference = new AtomicReference<>();

   //~--- fields --------------------------------------------------------------

   /** The listener uuid. */
   private final UUID listenerUuid = UUID.randomUUID();

   /** The incremental allowed. */
   private boolean incrementalAllowed = false;

   /** The all graphs to axiom translator. */
   GraphToAxiomTranslator allGraphsToAxiomTranslator = new GraphToAxiomTranslator();

   /** The incremental to axiom translator. */
   GraphToAxiomTranslator incrementalToAxiomTranslator = new GraphToAxiomTranslator();

   /** The reasoner. */
   IReasoner reasoner = new SnorocketReasoner();

   /** The loaded concepts. */
   ConceptSequenceSet loadedConcepts = new ConceptSequenceSet();

   /** The last classify instant. */
   Instant lastClassifyInstant;

   /** The last classify type. */
   ClassificationType lastClassifyType;

   /** The stamp coordinate. */
   StampCoordinate stampCoordinate;

   /** The logic coordinate. */
   LogicCoordinate logicCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new classifier data.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    */
   private ClassifierData(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
      this.stampCoordinate = stampCoordinate;
      this.logicCoordinate = logicCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Classify.
    *
    * @return the i reasoner
    */
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

   /**
    * Clear axioms.
    */
   public void clearAxioms() {
      this.allGraphsToAxiomTranslator.clear();
      this.incrementalToAxiomTranslator.clear();
   }

   /**
    * Handle change.
    *
    * @param cc the cc
    */
   @Override
   public void handleChange(ConceptChronology cc) {
      // Nothing to do... Only concerned about changes to logic graph.
   }

   /**
    * Handle change.
    *
    * @param sc the sc
    */
   @Override
   public void handleChange(SememeChronology sc) {
      if (sc.getAssemblageSequence() == this.logicCoordinate.getStatedAssemblageSequence()) {
         log.info("Stated form change: " + sc);

         // only process if incremental is a possibility.
         if (this.incrementalAllowed) {
            final LatestVersion<LogicGraphSememeImpl> optionalLatest =
               sc.getLatestVersion(LogicGraphSememeImpl.class,
                                   this.stampCoordinate);

            if (optionalLatest.value().isPresent()) {
               final LatestVersion<LogicGraphSememeImpl> latest = optionalLatest;

               // get stampCoordinate for last classify.
               final StampCoordinate stampToCompare =
                  this.stampCoordinate.makeCoordinateAnalog(this.lastClassifyInstant.toEpochMilli());

               // See if there is a change in the optionalLatest vs the last classify.
               final LatestVersion<LogicGraphSememeImpl> optionalPrevious =
                  sc.getLatestVersion(LogicGraphSememeImpl.class,
                                      stampToCompare);

               if (optionalPrevious.value().isPresent()) {
                  // See if the change has deletions, if so then incremental is not allowed.
                  final LatestVersion<LogicGraphSememeImpl> previous  = optionalPrevious;
                  boolean                                   deletions = false;

                  if (latest.value().get()
                            .getGraphData().length <= previous.value().get().getGraphData().length) {
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
                     this.incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.value().get());
                  }
               } else {
                  // Otherwise add axioms...
                  this.incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.value().get());
               }
            }
         }
      }
   }

   /**
    * Handle commit.
    *
    * @param commitRecord the commit record
    */
   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // already handled with the handle change above.
   }

   /**
    * Load axioms.
    */
   public void loadAxioms() {
      if (this.incrementalAllowed) {
         this.reasoner.loadAxioms(this.incrementalToAxiomTranslator.getAxioms());
         this.loadedConcepts = this.incrementalToAxiomTranslator.getLoadedConcepts();
      } else {
         this.reasoner.loadAxioms(this.allGraphsToAxiomTranslator.getAxioms());
         this.loadedConcepts = this.allGraphsToAxiomTranslator.getLoadedConcepts();
      }
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ClassifierData{" + "graphToAxiomTranslator=" + this.allGraphsToAxiomTranslator +
             ",\n incrementalToAxiomTranslator=" + this.incrementalToAxiomTranslator + ",\n reasoner=" +
             this.reasoner + ",\n lastClassifyInstant=" + this.lastClassifyInstant + ",\n lastClassifyType=" +
             this.lastClassifyType + ",\n stampCoordinate=" + this.stampCoordinate + ",\n logicCoordinate=" +
             this.logicCoordinate + '}';
   }

   /**
    * Translate.
    *
    * @param lgs the lgs
    */
   public void translate(LogicGraphSememeImpl lgs) {
      this.allGraphsToAxiomTranslator.convertToAxiomsAndAdd(lgs);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the affected concept sequence set.
    *
    * @return the affected concept sequence set
    */
   public ConceptSequenceSet getAffectedConceptSequenceSet() {
      final ConceptSequenceSet affectedConceptSequences = new ConceptSequenceSet();

      if (this.lastClassifyType == ClassificationType.INCREMENTAL) {
         // not returning loaded concepts here, because incremental classification
         // can affect concepts other than what was loaded.
         this.reasoner.getClassifiedOntology().getAffectedNodes().forEach((node) -> {
                                  if (node !=
                                      null) {  // TODO why does the classifier include null in the affected node set.
                                     node.getEquivalentConcepts()
                                         .forEach(
                                             (equalivent) -> affectedConceptSequences.add(
                                                 Integer.parseInt(equalivent)));
                                  }
                               });
      } else {
         return this.loadedConcepts;
      }

      return affectedConceptSequences;
   }

   /**
    * Checks if classified.
    *
    * @return true, if classified
    */
   public boolean isClassified() {
      return this.reasoner.isClassified();
   }

   /**
    * Gets the classified ontology.
    *
    * @return the classified ontology
    */
   public Ontology getClassifiedOntology() {
      return this.reasoner.getClassifiedOntology();
   }

   /**
    * Gets the.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    * @return the classifier data
    */
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

   /**
    * Checks if incremental allowed.
    *
    * @return true, if incremental allowed
    */
   public boolean isIncrementalAllowed() {
      return this.incrementalAllowed;
   }

   /**
    * Gets the last classify instant.
    *
    * @return the last classify instant
    */
   public Instant getLastClassifyInstant() {
      return this.lastClassifyInstant;
   }

   /**
    * Gets the listener uuid.
    *
    * @return the listener uuid
    */
   @Override
   public UUID getListenerUuid() {
      return this.listenerUuid;
   }
}

