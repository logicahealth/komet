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
package sh.isaac.model;

import java.util.List;
import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.transaction.Transaction;


//TODO when a setter is called that changes the stamp sequence, should the old stamp sequence be removed from the transaction?
//makeMutable puts the (current) stamp into the transaction.... then a set call to module or path changes the stamp, and puts the new 
//stamp in the transaction.  But should the old be removed?

/**
 * The Class VersionImpl.
 *
 * @author kec
 */
public abstract class VersionImpl
        implements Version {

   /**
    * The chronicle.
    */
   protected final ChronologyImpl chronicle;

   /**
    * The stamp sequence.
    */
   private int stampSequence;

   /**
    * Instantiates a new object version impl.
    *
    * @param chronicle the chronicle
    * @param stampSequence the stamp sequence
    */
   public VersionImpl(Chronology chronicle, int stampSequence) {
      this.chronicle = (ChronologyImpl) chronicle;
      this.stampSequence = stampSequence;
   }

   @Override
   public void addAdditionalUuids(UUID... uuids)
   {
      chronicle.addAdditionalUuids(uuids);
   }
   
   /**
    * Cancel.
    */
   public void cancel() {
      if (!isUncommitted()) {
         throw new RuntimeException("Attempt to cancel an already committed version: " + this);
      }
      int oldStampSequence = this.stampSequence;
      this.stampSequence = -1;
   }

   /**
    * Equals uses just STAMP comparison for fast evaluation, which works form committed versions, and most other cases.
    * For more complete evaluation, use deepEquals.
    *
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final VersionImpl other = (VersionImpl) obj;

      if (this.stampSequence != other.stampSequence) {
         return false;
      }

      return this.chronicle.getNid() == other.chronicle.getNid();
   }

   /**
    * DeepEquals considers all fields, not just the stamp and the assumptions that the commit manager will not allow
    * more one version for a given stamp. This extra consideration is necessary to support uncommitted versions, that
    * may change in a multi-user environment, including that an individual author may make changes on more than one path
    * at a time.
    *
    * @param other the object to compare.
    * @return true if all fields are equal, otherwise false.
    */
   @Override
   public final boolean deepEquals(Object other) {
      if (!(other instanceof VersionImpl)) {
         return false;
      }
      VersionImpl otherVersion = (VersionImpl) other;
      if (this.stampSequence != otherVersion.stampSequence) {
         return false;
      }
      return dataEquals(otherVersion);
   }

   /**
    * Returns true if the data of this version is equal to the data of another version.
    * Implementations should ignore STAMP attributes, and just compare the data.
    * @param other
    * @return true if equal
    */
   public abstract boolean dataEquals(VersionImpl other);

   /**
    * A representation of how different two versions are from each other. The author field is weighted such that a
    * difference of author is considered greater than all the other fields combined. Edit distance is always positive.
    *
    * @param other the version to compute the edit distance with respect to.
    * @return the edit distance.
    */
   public int editDistance(VersionImpl other) {
      int editDistance = 0;
      if (this.getStatus() != other.getStatus()) {
         editDistance++;
      }
      if (this.getTime() != other.getTime()) {
         editDistance++;
      }
      if (this.getAuthorNid() != other.getAuthorNid()) {
         // weight author to overwhelm all others... 
         editDistance = editDistance + 1000;
      }
      if (this.getModuleNid() != other.getModuleNid()) {
         // weight module to overwhelm all except author... 
         editDistance = editDistance + 100;
      }
      if (this.getPathNid() != other.getPathNid()) {
         // weight path... 
         editDistance = editDistance + 10;
      }
      return editDistance2(other, editDistance);
   }

   protected abstract int editDistance2(VersionImpl other, int editDistance);

   @Override
   public int hashCode() {
      int hash = 7;

      hash = 29 * hash + this.stampSequence;
      return hash;
   }

   @Override
   public String toString() {
      return toString(new StringBuilder()).toString();
   }

   /**
    * To string.
    *
    * @param builder the builder
    * @return the string builder
    */
   public StringBuilder toString(StringBuilder builder) {
      builder.append(" ")
              .append(Get.stampService()
                      .describeStampSequence(this.stampSequence));
      return builder;
   }

   @Override
   public String toUserString() {
      return toString();
   }

   /**
    * Check uncommitted.
    *
    * @throws RuntimeException the runtime exception
    */
   protected void checkUncommitted()
           throws RuntimeException {
      if (!this.isUncommitted()) {
         throw new RuntimeException("Component is already committed");
      }
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   protected void writeVersionData(ByteArrayDataBuffer data) {
      data.putStampSequence(this.stampSequence);
   }

   @Override
   public int getAuthorNid() {
      return Get.stampService()
              .getAuthorNidForStamp(this.stampSequence);
   }

   @Override
   public void setAuthorNid(int authorSequence, Transaction t) {
      if (this.stampSequence != -1) {
         checkUncommitted();
         int oldStampSequence = this.stampSequence;
         this.stampSequence = Get.stampService()
                 .getStampSequence(t, getStatus(),
                         getTime(),
                         authorSequence,
                         getModuleNid(),
                         getPathNid());
      }
   }

   @Override
   public CommitStates getCommitState() {
      if (isUncommitted()) {
         return CommitStates.UNCOMMITTED;
      }

      return CommitStates.COMMITTED;
   }

   @Override
   public int getModuleNid() {
      return Get.stampService()
              .getModuleNidForStamp(this.stampSequence);
   }

   @Override
   public void setModuleNid(int moduleSequence, Transaction t) {
      if (this.stampSequence != -1) {
         checkUncommitted();
         int oldStampSequence = this.stampSequence;
         this.stampSequence = Get.stampService()
                 .getStampSequence(t, getStatus(),
                         getTime(),
                         getAuthorNid(),
                         moduleSequence,
                         getPathNid());
      }
   }

   @Override
   public void setStatus(Status state, Transaction t) {
      if (this.stampSequence != -1) {
         checkUncommitted();
         int oldStampSequence = this.stampSequence;
         this.stampSequence = Get.stampService()
                 .getStampSequence(t, state,
                         getTime(),
                         getAuthorNid(),
                         getModuleNid(),
                         getPathNid());
      }
   }

   @Override
   public int getNid() {
      return this.chronicle.getNid();
   }

   @Override
   public int getPathNid() {
      return Get.stampService()
              .getPathNidForStamp(this.stampSequence);
   }

   @Override
   public void setPathNid(int pathSequence, Transaction t) {
      if (this.stampSequence != -1) {
         checkUncommitted();
         int oldStampSequence = this.stampSequence;
         this.stampSequence = Get.stampService()
                 .getStampSequence(t, getStatus(),
                         getTime(),
                         getAuthorNid(),
                         getModuleNid(),
                         pathSequence);
      }
   }

   @Override
   public UUID getPrimordialUuid() {
      return this.chronicle.getPrimordialUuid();
   }

   @Override
   public int getStampSequence() {
      return this.stampSequence;
   }

   @Override
   public Status getStatus() {
      return Get.stampService()
              .getStatusForStamp(this.stampSequence);
   }

   @Override
   public long getTime() {
      return Get.stampService()
              .getTimeForStamp(this.stampSequence);
   }

   @Override
   public void setTime(long time, Transaction t) {
      if (this.stampSequence != -1) {
         checkUncommitted();
         int oldStampSequence = this.stampSequence;
         this.stampSequence = Get.stampService()
                 .getStampSequence(t, getStatus(),
                         time,
                         getAuthorNid(),
                         getModuleNid(),
                         getPathNid());
      }
   }

   @Override
   public boolean isUncommitted() {
      return Get.stampService().isUncommitted(stampSequence);
   }

   @Override
   public List<UUID> getUuidList() {
      return this.chronicle.getUuidList();
   }
}
