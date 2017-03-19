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
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.externalizable.OchreExternalizable;

//~--- classes ----------------------------------------------------------------

/**
 * Used to notify listeners of a commit event.
 * @author kec
 */
public class CommitRecord {
   protected Instant            commitTime;
   protected StampSequenceSet   stampsInCommit;
   protected AbstractIntIntMap  stampAliases;
   protected String             commitComment;
   protected ConceptSequenceSet conceptsInCommit;
   protected SememeSequenceSet  sememesInCommit;

   //~--- constructors --------------------------------------------------------

   public CommitRecord() {}

   public CommitRecord(Instant commitTime,
                       StampSequenceSet stampsInCommit,
                       OpenIntIntHashMap stampAliases,
                       ConceptSequenceSet conceptsInCommit,
                       SememeSequenceSet sememesInCommit,
                       String commitComment) {
      this.commitTime       = commitTime;
      this.stampsInCommit   = StampSequenceSet.of(stampsInCommit);
      this.stampAliases     = stampAliases.copy();
      this.conceptsInCommit = ConceptSequenceSet.of(conceptsInCommit);
      this.sememesInCommit  = SememeSequenceSet.of(sememesInCommit);
      this.commitComment    = commitComment;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public String toString() {
      return "CommitRecord{" + "commitTime=" + commitTime + ", stampsInCommit=" + stampsInCommit + ", stampAliases=" +
             stampAliases + ", commitComment=" + commitComment + ", conceptsInCommit=" + conceptsInCommit +
             ", sememesInCommit=" + sememesInCommit + '}';
   }

   //~--- get methods ---------------------------------------------------------

   public String getCommitComment() {
      return commitComment;
   }

   public Instant getCommitTime() {
      return commitTime;
   }

   public ConceptSequenceSet getConceptsInCommit() {
      return conceptsInCommit;
   }

   public SememeSequenceSet getSememesInCommit() {
      return sememesInCommit;
   }

   public AbstractIntIntMap getStampAliases() {
      return stampAliases;
   }

   public StampSequenceSet getStampsInCommit() {
      return stampsInCommit;
   }
}

