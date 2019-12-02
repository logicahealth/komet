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
import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.AbstractIntIntMap;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.collections.NidSet;

import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;

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
   protected NidSet conceptNidsInCommit;

   /** The semantic nids in a commit. */
   protected NidSet semanticNidsInCommit;

   //~--- constructors --------------------------------------------------------

   private CommitRecord(ByteArrayDataBuffer data) {
      this.commitTime = Instant.ofEpochMilli(data.getLong());
      this.stampsInCommit = StampSequenceSet.of(data.getIntArray());
      int mapSize = data.getInt();
      this.stampAliases = new OpenIntIntHashMap(mapSize);
      for (int i = 0; i < mapSize; i++) {
         stampAliases.put(data.getInt(), data.getInt());
      }
      this.commitComment = data.getUTF();
      this.conceptNidsInCommit = NidSet.of(data.getNidArray());
      this.semanticNidsInCommit = NidSet.of(data.getNidArray());
   }

   public final void putExternal(ByteArrayDataBuffer out) {
      out.putLong(commitTime.toEpochMilli());
      out.putIntArray(stampsInCommit.asArray());
      out.putInt(stampAliases.size());
      stampAliases.forEachPair((first, second) -> {
         out.putInt(first);
         out.putInt(second);
         return true;
      });
      out.putUTF(commitComment);
      out.putNidArray(conceptNidsInCommit.asArray());
      out.putNidArray(semanticNidsInCommit.asArray());
   }

   public static final CommitRecord make(ByteArrayDataBuffer data) {
      return new CommitRecord(data);
   }

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
    * @param conceptNidsInCommit the concepts in commit
    * @param semanticNidsInCommit the semantics in commit
    * @param commitComment the commit comment
    */
   public CommitRecord(Instant commitTime,
                       StampSequenceSet stampsInCommit,
                       OpenIntIntHashMap stampAliases,
                       NidSet conceptNidsInCommit,
                       NidSet semanticNidsInCommit,
                       String commitComment) {
      this.commitTime       = commitTime;
      this.stampsInCommit   = StampSequenceSet.of(stampsInCommit);
      this.stampAliases     = stampAliases.copy();
      this.conceptNidsInCommit = NidSet.of(conceptNidsInCommit);
      this.semanticNidsInCommit  = NidSet.of(semanticNidsInCommit);
      this.commitComment    = commitComment;
   }

   /**
    * Instantiates a new commit record.
    *
    * @param commitTime the commit time
    * @param stampsInCommit the stamps in commit
    * @param stampAliases the stamp aliases
    * @param componentsInCommit the components in commit
    * @param commitComment the commit comment
    */
   public CommitRecord(Instant commitTime,
                       StampSequenceSet stampsInCommit,
                       OpenIntIntHashMap stampAliases,
                       Set<Integer> componentsInCommit,
                       String commitComment) {
      this.commitTime       = commitTime;
      this.stampsInCommit   = StampSequenceSet.of(stampsInCommit);
      this.stampAliases     = stampAliases.copy();

      this.conceptNidsInCommit = new NidSet();
      this.semanticNidsInCommit  = new NidSet();
      IdentifierService idService = Get.identifierService();
      for (Integer nid: componentsInCommit) {
         if (idService.getObjectTypeForComponent(nid) == IsaacObjectType.CONCEPT) {
            this.conceptNidsInCommit.add(nid);
         } else {
            this.semanticNidsInCommit.add(nid);
         }
      }
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
             ", stampAliases=" + this.stampAliases + ", commitComment=" + this.commitComment + ", conceptNidsInCommit=" +
             this.conceptNidsInCommit + ", semanticNidsInCommit=" + this.semanticNidsInCommit + '}';
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
   public NidSet getConceptsInCommit() {
      return this.conceptNidsInCommit;
   }

   /**
    * Gets the semantic nids in commit.
    *
    * @return the semantic nids in commit
    */
   public NidSet getSemanticNidsInCommit() {
      return this.semanticNidsInCommit;
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

