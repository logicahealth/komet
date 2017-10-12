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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.commit;

//~--- JDK imports ------------------------------------------------------------

import java.time.Instant;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.AbstractIntIntMap;
import org.apache.mahout.math.map.OpenIntIntHashMap;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SemanticSequenceSet;
import sh.isaac.api.collections.StampSequenceSet;

//~--- classes ----------------------------------------------------------------

/**
 * Used to notify listeners of a commit event.
 * @author kec
 */
public class CommitRecord {
   /** The commit time. */
   protected Instant commitTime;

   /** The stamps in commit. */
   protected StampSequenceSet stampsInCommit;

   /** The stamp aliases. */
   protected AbstractIntIntMap stampAliases;

   /** The commit comment. */
   protected String commitComment;

   /** The concepts in commit. */
   protected ConceptSequenceSet conceptSequencesInCommit;

   /** The semantic sequences in a commit. */
   protected SemanticSequenceSet semanticSequencesInCommit;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new commit record.
    */
   public CommitRecord() {}

   /**
    * Instantiates a new commit record.
    *
    * @param commitTime the commit time
    * @param stampsInCommit the stamps in commit
    * @param stampAliases the stamp aliases
    * @param conceptSequencesInCommit the concepts in commit
    * @param semanticSequencesInCommit the semantics in commit
    * @param commitComment the commit comment
    */
   public CommitRecord(Instant commitTime,
                       StampSequenceSet stampsInCommit,
                       OpenIntIntHashMap stampAliases,
                       ConceptSequenceSet conceptSequencesInCommit,
                       SemanticSequenceSet semanticSequencesInCommit,
                       String commitComment) {
      this.commitTime       = commitTime;
      this.stampsInCommit   = StampSequenceSet.of(stampsInCommit);
      this.stampAliases     = stampAliases.copy();
      this.conceptSequencesInCommit = ConceptSequenceSet.of(conceptSequencesInCommit);
      this.semanticSequencesInCommit  = SemanticSequenceSet.of(semanticSequencesInCommit);
      this.commitComment    = commitComment;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "CommitRecord{" + "commitTime=" + this.commitTime + ", stampsInCommit=" + this.stampsInCommit +
             ", stampAliases=" + this.stampAliases + ", commitComment=" + this.commitComment + ", conceptSequencesInCommit=" +
             this.conceptSequencesInCommit + ", semanticSequencesInCommit=" + this.semanticSequencesInCommit + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the commit comment.
    *
    * @return the commit comment
    */
   public String getCommitComment() {
      return this.commitComment;
   }

   /**
    * Gets the commit time.
    *
    * @return the commit time
    */
   public Instant getCommitTime() {
      return this.commitTime;
   }

   /**
    * Gets the concepts in commit.
    *
    * @return the concepts in commit
    */
   public ConceptSequenceSet getConceptsInCommit() {
      return this.conceptSequencesInCommit;
   }

   /**
    * Gets the semantic sequences in commit.
    *
    * @return the semantic sequences in commit
    */
   public SemanticSequenceSet getSemanticSequencesInCommit() {
      return this.semanticSequencesInCommit;
   }

   /**
    * Gets the stamp aliases.
    *
    * @return the stamp aliases
    */
   public AbstractIntIntMap getStampAliases() {
      return this.stampAliases;
   }

   /**
    * Gets the stamps in commit.
    *
    * @return the stamps in commit
    */
   public StampSequenceSet getStampsInCommit() {
      return this.stampsInCommit;
   }
}

