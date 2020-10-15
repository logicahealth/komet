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

import au.csiro.ontology.Node;
import au.csiro.ontology.Ontology;
import au.csiro.ontology.classification.IReasoner;
import au.csiro.snorocket.core.SnorocketReasoner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.provider.logic.csiro.axioms.GraphToAxiomTranslator;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The Class ClassifierData.
 *
 * @author kec
 */
public class ClassifierData
        implements ChronologyChangeListener {

    /**
     * The Constant log.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The Constant singletonReference.
     */
    private static final AtomicReference<ClassifierData> SINGLETON = new AtomicReference<>();

    /**
     * The listener uuid.
     */
    private final UUID listenerUuid = UUID.randomUUID();

    /**
     * The incremental allowed.
     */
    private boolean incrementalAllowed = false;

    /**
     * The all graphs to axiom translator.
     */
    GraphToAxiomTranslator allGraphsToAxiomTranslator = new GraphToAxiomTranslator();

    /**
     * The incremental to axiom translator.
     */
    GraphToAxiomTranslator incrementalToAxiomTranslator = new GraphToAxiomTranslator();

    /**
     * The reasoner.
     */
    IReasoner reasoner = new SnorocketReasoner();

    /**
     * The loaded concepts.
     */
    Set<Integer> loadedConcepts;

    /**
     * The last classify instant.
     */
    Instant lastClassifyInstant;

    /**
     * The last classify type.
     */
//    ClassificationType lastClassifyType;

    ManifoldCoordinateImmutable manifoldCoordinate;

    private ClassifierData(ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate.toManifoldCoordinateImmutable();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Classify.
     *
     * @return the i reasoner
     */
    public IReasoner classify() {
        this.loadedConcepts = 
                new ConcurrentSkipListSet<>(this.allGraphsToAxiomTranslator.getLoadedConcepts());
        this.allGraphsToAxiomTranslator.clear();
        this.lastClassifyInstant = Instant.now();

//        if (this.lastClassifyType == null) {
//            this.lastClassifyType = ClassificationType.COMPLETE;
//            this.incrementalAllowed = true;
//        } else {
//            if (this.incrementalAllowed) {
//                this.lastClassifyType = ClassificationType.INCREMENTAL;
//                this.incrementalToAxiomTranslator.clear();
//            } else {
//                this.lastClassifyType = ClassificationType.COMPLETE;
//            }
//        }

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
    public void handleChange(SemanticChronology sc) {
        if (sc.getAssemblageNid() == this.manifoldCoordinate.getLogicCoordinate().getStatedAssemblageNid()) {
            LOG.info("Stated form change on: {}" + sc.getNid());  //DO NOT call toString on the chronology here, in a builder pattern, descriptions may not be built yet.

            // only process if incremental is a possibility.
//            if (this.incrementalAllowed) {
//                final LatestVersion<LogicGraphVersionImpl> optionalLatest
//                        = sc.getLatestVersion(this.manifoldCoordinate.getViewStampFilter());
//
//                if (optionalLatest.isPresent()) {
//                    final LatestVersion<LogicGraphVersionImpl> latest = optionalLatest;
//
//                    // get stampCoordinate for last classify.
//                    final StampFilter stampToCompare
//                            = this.manifoldCoordinate.getViewStampFilter().makeCoordinateAnalog(this.lastClassifyInstant.toEpochMilli());
//
//                    // See if there is a change in the optionalLatest vs the last classify.
//                    final LatestVersion<LogicGraphVersionImpl> optionalPrevious
//                            = sc.getLatestVersion(stampToCompare);
//
//                    if (optionalPrevious.isPresent()) {
//                        // See if the change has deletions, if so then incremental is not allowed.
//                        final LatestVersion<LogicGraphVersionImpl> previous = optionalPrevious;
//                        boolean deletions = false;
//
//                        if (latest.get()
//                                .getGraphData().length <= previous.get().getGraphData().length) {
//                            // If nodes where deleted, or an existing node was changed but the size remains the same assume deletions
//                            deletions = true;
//
//                            // TODO use a real subtree isomorphism algorithm.
//                        }
//
//                        if (deletions) {
//                            this.incrementalAllowed = false;
//                            this.incrementalToAxiomTranslator.clear();
//                            this.reasoner = new SnorocketReasoner();
//                        } else {
//                            // Otherwise add axioms...
//                            this.incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.get());
//                        }
//                    } else {
//                        // Otherwise add axioms...
//                        this.incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.get());
//                    }
//                }
//            }
        }
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        // already handled with the handle change above.
    }

    /**
     * Load axioms.
     */
    public void loadAxioms() {
//        if (this.incrementalAllowed) {
//            this.reasoner.loadAxioms(this.incrementalToAxiomTranslator.getAxioms());
//            this.loadedConcepts = this.incrementalToAxiomTranslator.getLoadedConcepts();
//            LOG.info("Incremental load of " + this.incrementalToAxiomTranslator.getAxioms().size() + " axioms. ");
//        } else {
            this.reasoner.loadAxioms(this.allGraphsToAxiomTranslator.getAxioms());
            this.loadedConcepts = this.allGraphsToAxiomTranslator.getLoadedConcepts();
            LOG.info("Complete load of " + this.allGraphsToAxiomTranslator.getAxioms().size() + " axioms. ");
//        }

    }

    @Override
    public String toString() {
        return "ClassifierData{" + "graphToAxiomTranslator=" + this.allGraphsToAxiomTranslator
                + ",\n incrementalToAxiomTranslator=" + this.incrementalToAxiomTranslator + ",\n reasoner="
                + this.reasoner + ",\n lastClassifyInstant=" + this.lastClassifyInstant 
//                + ",\n lastClassifyType=" + this.lastClassifyType 
                + ",\n manifoldCoordinate=" + this.manifoldCoordinate + '}';
    }

    /**
     * Translate.
     *
     * @param lgs the lgs
     */
    public void translate(LogicGraphVersionImpl lgs) {
        this.allGraphsToAxiomTranslator.convertToAxiomsAndAdd(lgs);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the affected concept nid set.
     *
     * @return the affected concept nid set
     */
    public Set<Integer> getAffectedConceptNidSet() {

//        if (this.lastClassifyType == ClassificationType.COMPLETE) {
            return this.loadedConcepts;
//        }
//
//        final Set<Integer> affectedConceptNids = new ConcurrentSkipListSet<>();
//
//
//        for (Node node : this.reasoner.getClassifiedOntology().getAffectedNodes()) {
//            if (node != null) {
//                // TODO why does the classifier include null in the affected node set.
//                for (String equivalent : node.getEquivalentConcepts()) {
//                    int nid = Integer.parseInt(equivalent);
//                    affectedConceptNids.add(nid);
//                }
//            }
//        }
//
//        return affectedConceptNids;
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
     * @return the classifier data
     */
    public static ClassifierData get(ManifoldCoordinate manifoldCoordinate) {
        if (SINGLETON.get() == null) {
            SINGLETON.compareAndSet(null, new ClassifierData(manifoldCoordinate));
        } else {
            ClassifierData classifierData = SINGLETON.get();

            while (!classifierData.manifoldCoordinate.equals(manifoldCoordinate)) {
                Get.commitService()
                        .removeChangeListener(classifierData);

                final ClassifierData newClassifierData = new ClassifierData(manifoldCoordinate);

                SINGLETON.compareAndSet(classifierData, newClassifierData);
                classifierData = SINGLETON.get();
            }
        }

        Get.commitService()
                .addChangeListener(SINGLETON.get());
        return SINGLETON.get();
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

    @Override
    public UUID getListenerUuid() {
        return this.listenerUuid;
    }
    
    public LogicCoordinate getLogicCoordinate() {
        return this.manifoldCoordinate.getLogicCoordinate();
    }
    
    public StampFilter getStampFilter() {
        return this.manifoldCoordinate.getViewStampFilter();
    }
}