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



package sh.isaac.api.classifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import sh.isaac.api.commit.CommitRecord;

/**
 * The Class ClassifierResults.
 *
 * @author kec
 */
public class ClassifierResults {
   /**
    * Set of concepts potentially affected by the last classification.
    */
   private final Set<Integer> affectedConcepts;

   /** The equivalent sets. */
   private final Set<IntArrayList> equivalentSets;

   /** The commit record. */
   private final Optional<CommitRecord> commitRecord;
   
   //A map of a concept nid, to a HashSet of int arrays, where each int[] is a cycle present on the concept.
   private Optional<Map<Integer, Set<int[]>>> conceptsWithCycles = Optional.empty();
   
   private HashSet<Integer> orphanedConcepts = new HashSet<>();

   /**
    * Instantiates a new classifier results.
    *
    * @param affectedConcepts the affected concepts
    * @param equivalentSets the equivalent sets
    * @param commitRecord the commit record
    */
   public ClassifierResults(Set<Integer> affectedConcepts,
                            Set<IntArrayList> equivalentSets,
                            Optional<CommitRecord> commitRecord) {
      this.affectedConcepts = affectedConcepts;
      this.equivalentSets   = equivalentSets;
      this.commitRecord     = commitRecord;
   }
   
   /**
    * This constructor is only intended to be used when a classification wasn't performed, because there were cycles present.
    * @param conceptsWithCycles
    * @param orphans
    */
   public ClassifierResults(Map<Integer, Set<int[]>> conceptsWithCycles, Set<Integer> orphans) {
      this.affectedConcepts = new HashSet<>();
      this.equivalentSets   = new HashSet<>();
      this.commitRecord     = Optional.empty();
      this.conceptsWithCycles = Optional.of(conceptsWithCycles);
      this.orphanedConcepts.addAll(orphans);
   }

   @Override
   public String toString() {
      return "ClassifierResults{" + "written semantics: " 
            + (this.commitRecord.isPresent() && this.commitRecord.get().getSemanticNidsInCommit() != null ? this.commitRecord.get().getSemanticNidsInCommit().size(): "0") 
            + " affectedConcepts=" + this.affectedConcepts.size() + ", equivalentSets=" 
            + this.equivalentSets.size() + ", Orphans detected=" + orphanedConcepts.size() 
            + " Concepts with cycles=" + (conceptsWithCycles.isPresent() ? conceptsWithCycles.get().size() : 0) + '}';
   }

   /**
    * Get the set of concepts potentially affected by the last classification.
    *
    * @return the affected concepts
    */
   public Set<Integer> getAffectedConcepts() {
      return this.affectedConcepts;
   }

   /**
    * Gets the commit record.
    *
    * @return the commit record
    */
   public Optional<CommitRecord> getCommitRecord() {
      return this.commitRecord;
   }

   /**
    * Gets the equivalent sets.
    *
    * @return the equivalent sets
    */
   public Set<IntArrayList> getEquivalentSets() {
      return this.equivalentSets;
   }
   
   /**
    * If this returns a value, then the classification was not performed due to these cycles that were found.
    * 
    * When a cycle was detected, the rest of the classification is aborted, so no other details in this class are populated.
    * 
    * @return A map of concept nids to sets of nid arrays, each set represents a cycle that the concept nid is 
    * involved in, and the nid[] is the cycle path.  Returns an empty object, if no cycles were detected.
    */
   public Optional<Map<Integer, Set<int[]>>> getCycles() {
      return conceptsWithCycles;
   }
   
   /**
    * Add concept nids that were detected as orphans
    * @param orphans
    */
   public void addOrphans(Set<Integer> orphans) {
      orphanedConcepts.addAll(orphans);
   }
   
   /**
    * @return The list of orphaned concept nids that were detected during classification
    */
   public Set<Integer> getOrphans() {
      return orphanedConcepts;
   }
}