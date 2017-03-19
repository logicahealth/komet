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

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.commit.CommitRecord;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ClassifierResults.
 *
 * @author kec
 */
public class ClassifierResults {
   /** The affected concepts. */
   final ConceptSequenceSet affectedConcepts;

   /** The equivalent sets. */
   final Set<ConceptSequenceSet> equivalentSets;

   /** The commit record. */
   final Optional<CommitRecord> commitRecord;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new classifier results.
    *
    * @param affectedConcepts the affected concepts
    * @param equivalentSets the equivalent sets
    * @param commitRecord the commit record
    */
   public ClassifierResults(ConceptSequenceSet affectedConcepts,
                            Set<ConceptSequenceSet> equivalentSets,
                            Optional<CommitRecord> commitRecord) {
      this.affectedConcepts = affectedConcepts;
      this.equivalentSets   = equivalentSets;
      this.commitRecord     = commitRecord;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ClassifierResults{" + "affectedConcepts=" + this.affectedConcepts.size() + ", equivalentSets=" +
             this.equivalentSets.size() + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the affected concepts.
    *
    * @return the affected concepts
    */
   public ConceptSequenceSet getAffectedConcepts() {
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
   public Set<ConceptSequenceSet> getEquivalentSets() {
      return this.equivalentSets;
   }
}

