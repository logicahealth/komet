/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.logic.csiro.classify;

import au.csiro.ontology.Ontology;
import au.csiro.ontology.classification.IReasoner;
import au.csiro.snorocket.core.SnorocketReasoner;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.logic.csiro.axioms.GraphToAxiomTranslator;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kec
 */
public class ClassifierData implements ChronologyChangeListener {

    private static final Logger log = LogManager.getLogger();

    private static final AtomicReference<ClassifierData> singletonReference = new AtomicReference<>();

    private final UUID listenerUuid = UUID.randomUUID();
    private boolean incrementalAllowed = false;
    GraphToAxiomTranslator allGraphsToAxiomTranslator = new GraphToAxiomTranslator();
    GraphToAxiomTranslator incrementalToAxiomTranslator = new GraphToAxiomTranslator();
    IReasoner reasoner = new SnorocketReasoner();

    ConceptSequenceSet loadedConcepts = new ConceptSequenceSet();
    Instant lastClassifyInstant;
    ClassificationType lastClassifyType;

    StampCoordinate stampCoordinate;
    LogicCoordinate logicCoordinate;

    private ClassifierData(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
        this.stampCoordinate = stampCoordinate;
        this.logicCoordinate = logicCoordinate;
    }

    public static ClassifierData get(StampCoordinate stampCoordinate, LogicCoordinate logicCoordinate) {
        if (singletonReference.get() == null) {
            singletonReference.compareAndSet(null, new ClassifierData(stampCoordinate, logicCoordinate));
        } else {
            ClassifierData classifierData = singletonReference.get();
            while (!classifierData.stampCoordinate.equals(stampCoordinate) || !classifierData.logicCoordinate.equals(logicCoordinate)) {
                Get.commitService().removeChangeListener(classifierData);
                ClassifierData newClassifierData = new ClassifierData(stampCoordinate, logicCoordinate);
                singletonReference.compareAndSet(classifierData, newClassifierData);
                classifierData = singletonReference.get();
            }
        }
        Get.commitService().addChangeListener(singletonReference.get());
        return singletonReference.get();
    }

    public void clearAxioms() {
        allGraphsToAxiomTranslator.clear();
        incrementalToAxiomTranslator.clear();
    }

    public void translate(LogicGraphSememeImpl lgs) {
        allGraphsToAxiomTranslator.convertToAxiomsAndAdd(lgs);
    }

    public void loadAxioms() {
        if (incrementalAllowed) {
            reasoner.loadAxioms(incrementalToAxiomTranslator.getAxioms());
            loadedConcepts = incrementalToAxiomTranslator.getLoadedConcepts();
        } else {
            reasoner.loadAxioms(allGraphsToAxiomTranslator.getAxioms());
            loadedConcepts = allGraphsToAxiomTranslator.getLoadedConcepts();
        }
    }

    public IReasoner classify() {
        loadedConcepts = allGraphsToAxiomTranslator.getLoadedConcepts();
        allGraphsToAxiomTranslator.clear();
        lastClassifyInstant = Instant.now();
        
        if (lastClassifyType == null) {
            lastClassifyType = ClassificationType.COMPLETE;
            incrementalAllowed = true;
        } else {
            if (incrementalAllowed) {
                lastClassifyType = ClassificationType.INCREMENTAL;
                incrementalToAxiomTranslator.clear();
            } else {
                lastClassifyType = ClassificationType.COMPLETE;
            }
        }
        return reasoner.classify();
    }

    public boolean isIncrementalAllowed() {
        return incrementalAllowed;
    }

    public Ontology getClassifiedOntology() {
        return reasoner.getClassifiedOntology();
    }

    public boolean isClassified() {
        return reasoner.isClassified();
    }

    public Instant getLastClassifyInstant() {
        return this.lastClassifyInstant;
    }

    @Override
    public void handleChange(ConceptChronology cc) {
        // Nothing to do... Only concerned about changes to logic graph. 
    }

    @Override
    public UUID getListenerUuid() {
        return listenerUuid;
    }

    @Override
    public void handleChange(SememeChronology sc) {
        if (sc.getAssemblageSequence() == logicCoordinate.getStatedAssemblageSequence()) {
            log.info("Stated form change: " + sc);
            // only process if incremental is a possibility. 
            if (incrementalAllowed) {
                Optional<LatestVersion<LogicGraphSememeImpl>> optionalLatest = sc.getLatestVersion(LogicGraphSememeImpl.class, stampCoordinate);
                if (optionalLatest.isPresent()) {
                    LatestVersion<LogicGraphSememeImpl> latest = optionalLatest.get();
                    // get stampCoordinate for last classify. 
                    StampCoordinate stampToCompare = stampCoordinate.makeAnalog(lastClassifyInstant.toEpochMilli());
                    // See if there is a change in the optionalLatest vs the last classify. 
                    Optional<LatestVersion<LogicGraphSememeImpl>> optionalPrevious = sc.getLatestVersion(LogicGraphSememeImpl.class, stampToCompare);
                    if (optionalPrevious.isPresent()) {
                        // See if the change has deletions, if so then incremental is not allowed. 
                        LatestVersion<LogicGraphSememeImpl> previous = optionalPrevious.get();
                        boolean deletions = false;
                        if (latest.value().getGraphData().length <= previous.value().getGraphData().length) {
                            // If nodes where deleted, or an existing node was changed but the size remains the same assume deletions
                            deletions = true;
                            // TODO use a real subtree isomorphism algorithm. 
                        }
                        if (deletions) {
                            incrementalAllowed = false;
                            incrementalToAxiomTranslator.clear();
                            reasoner = new SnorocketReasoner();
                        } else {
                            // Otherwise add axioms...
                            incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.value());
                        }
                    } else {
                        // Otherwise add axioms...
                        incrementalToAxiomTranslator.convertToAxiomsAndAdd(latest.value());
                    }
                }
            }
        }
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        // already handled with the handle change above. 
    }
    
    public ConceptSequenceSet getAffectedConceptSequenceSet() {
        ConceptSequenceSet affectedConceptSequences = new ConceptSequenceSet();
        if (lastClassifyType == ClassificationType.INCREMENTAL) {
            // not returning loaded concepts here, because incremental classification 
            // can affect concepts other than what was loaded. 
            reasoner.getClassifiedOntology().getAffectedNodes().forEach((node) -> {
					if (node != null) { //TODO why does the classifier include null in the affected node set. 
						node.getEquivalentConcepts().forEach((equalivent) -> affectedConceptSequences.add(Integer.parseInt(equalivent)));
					}              
            });
        } else {
            return  loadedConcepts;

        }
        return affectedConceptSequences;
    }

    @Override
    public String toString() {
        return "ClassifierData{"
                + "graphToAxiomTranslator=" + allGraphsToAxiomTranslator
                + ",\n incrementalToAxiomTranslator=" + incrementalToAxiomTranslator
                + ",\n reasoner=" + reasoner
                + ",\n lastClassifyInstant=" + lastClassifyInstant
                + ",\n lastClassifyType=" + lastClassifyType
                + ",\n stampCoordinate=" + stampCoordinate
                + ",\n logicCoordinate=" + logicCoordinate
                + '}';
    }
}
