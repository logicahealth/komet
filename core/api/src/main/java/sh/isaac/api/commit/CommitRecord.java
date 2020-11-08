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

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.mahout.math.map.AbstractIntIntMap;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.transaction.Transaction;


/**
 * Used to notify listeners of a commit event.
 * @author kec
 */
public class CommitRecord {

   private static final short SERIAL_VERSION = 2;
   
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
    
   Optional<String> transactionName;
   Optional<String> transactionId;
   
   private transient Optional<Transaction> transaction = Optional.empty();

   private CommitRecord(ByteArrayDataBuffer data) {
      //Yes, this is broken if it tries to read serial version 1, because the serialization format never properly wrote the version.
      //If we cared, would have to snoop at the bits, and try to figure out if there is a short there that parses to 1, but, I don't care...
      int serVersion = data.getShort();
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
      if (serVersion > 1) {
         String temp = data.getUTF();
         transactionName = StringUtils.isBlank(temp) ? Optional.empty() : Optional.of(temp);
         temp = data.getUTF();
         transactionId = StringUtils.isBlank(temp) ? Optional.empty() : Optional.of(temp);
      }
    }

   public final void putExternal(ByteArrayDataBuffer out) {
      out.putShort(SERIAL_VERSION);
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
      out.putUTF(transactionName.orElse(""));
      out.putUTF(transactionId.orElse(""));
   }

   public static final CommitRecord make(ByteArrayDataBuffer data) {
      return new CommitRecord(data);
   }

   /**
    * Instantiates a new commit record.
    */
   public CommitRecord(String comment) {
      this.commitTime = Instant.now();
      this.stampsInCommit   = StampSequenceSet.of();
      this.stampAliases     = new OpenIntIntHashMap();
      this.conceptNidsInCommit = NidSet.of(new int[0]);
      this.semanticNidsInCommit  = NidSet.of(new int[0]);
      this.commitComment    = comment;
      this.transactionName = Optional.empty();
      this.transactionId = Optional.empty();
      this.transaction = Optional.empty();
   }

   /**
    * Instantiates a new commit record.
    *
    * @param commitTime the commit time
    * @param stampsInCommit the stamps in commit
    * @param stampAliases the stamp aliases - optional - null allowed
    * @param conceptNidsInCommit the concepts in commit
    * @param semanticNidsInCommit the semantics in commit
    * @param commitComment the commit comment
    * @param transaction the transaction involved in this commit
    */
   public CommitRecord(Instant commitTime,
                       StampSequenceSet stampsInCommit,
                       OpenIntIntHashMap stampAliases,
                       NidSet conceptNidsInCommit,
                       NidSet semanticNidsInCommit,
                       String commitComment, 
                       Transaction transaction) {
      this.commitTime       = commitTime;
      this.stampsInCommit   = StampSequenceSet.of(stampsInCommit);
      this.stampAliases     = stampAliases == null ? new OpenIntIntHashMap() : stampAliases.copy();
      this.conceptNidsInCommit = NidSet.of(conceptNidsInCommit);
      this.semanticNidsInCommit  = NidSet.of(semanticNidsInCommit);
      this.commitComment    = commitComment;
      this.transactionName = transaction == null ? Optional.empty() : transaction.getTransactionName();
      this.transactionId = transaction == null ? Optional.empty() : Optional.of(transaction.getTransactionId().toString());
      this.transaction = Optional.ofNullable(transaction);
   }

   /**
    * Instantiates a new commit record.
    *
    * @param commitTime the commit time
    * @param stampsInCommit the stamps in commit
    * @param stampAliases the stamp aliases
    * @param componentsInCommit the components in commit
    * @param commitComment the commit comment
    * @param transaction the transaction involved in this commit
    */
   public CommitRecord(Instant commitTime,
                       StampSequenceSet stampsInCommit,
                       OpenIntIntHashMap stampAliases,
                       Set<Integer> componentsInCommit,
                       String commitComment, 
                       Transaction transaction) {
      this.commitTime       = commitTime;
      this.stampsInCommit   = StampSequenceSet.of(stampsInCommit);
      this.stampAliases     = stampAliases.copy();
      this.transactionName = transaction == null ? Optional.empty() : transaction.getTransactionName();
      this.transactionId = transaction == null ? Optional.empty() : Optional.of(transaction.getTransactionId().toString());
      this.transaction = Optional.ofNullable(transaction);

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

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("CommitRecord{commitTime=");
      sb.append(this.commitTime);
      sb.append(", stampsInCommit=");
      sb.append(this.stampsInCommit);
      sb.append(", stampAliases=");
      sb.append(this.stampAliases);
      sb.append(", commitComment=");
      sb.append(this.commitComment);
      sb.append(", conceptNidsInCommit=");
      sb.append(this.conceptNidsInCommit);
      sb.append(", semanticNidsInCommit=");
      sb.append(this.semanticNidsInCommit);
      sb.append(", transactionName=");
      if (transactionName.isPresent()) {
         sb.append(transactionName.get());
      } else {
         sb.append("null");
      }

      sb.append(", transactionId=");
      sb.append(transactionId);
      sb.append("}");
      return sb.toString();
   }

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

    /**
     * @return the transaction, if present
     */
    public Optional<Transaction> getTransaction()
    {
        return transaction;
    }
}

