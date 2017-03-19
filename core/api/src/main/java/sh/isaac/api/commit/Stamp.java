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



package sh.isaac.api.commit;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.time.Instant;

import java.util.Collection;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.set.AbstractIntSet;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.util.Hashcode;

//~--- classes ----------------------------------------------------------------

public class Stamp
         implements Comparable<Stamp> {
   public int          hashCode = Integer.MAX_VALUE;
   private final int   authorSequence;
   private final int   pathSequence;
   private final State status;
   private final int   moduleSequence;
   private final long  time;

   //~--- constructors --------------------------------------------------------

   public Stamp(DataInput in)
            throws IOException {
      super();
      this.status         = State.values()[in.readInt()];
      this.time           = in.readLong();
      this.authorSequence = in.readInt();
      this.moduleSequence = in.readInt();
      this.pathSequence   = in.readInt();
      assert this.time != 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorSequence + " " + " m: " + this.moduleSequence + " p: " +
             this.pathSequence;
      assert this.status != null:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorSequence + " " + " m: " + this.moduleSequence + " p: " +
             this.pathSequence;
      assert this.pathSequence > 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorSequence + " " + " m: " + this.moduleSequence + " p: " +
             this.pathSequence;
      assert this.moduleSequence > 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorSequence + " " + " m: " + this.moduleSequence + " p: " +
             this.pathSequence;
      assert this.authorSequence > 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorSequence + " " + " m: " + this.moduleSequence + " p: " +
             this.pathSequence;
   }

   public Stamp(State status, long time, int authorSequence, int moduleSequence, int pathSequence) {
      super();
      this.status         = status;
      this.time           = time;
      this.authorSequence = authorSequence;
      this.moduleSequence = moduleSequence;
      this.pathSequence   = pathSequence;
      assert time != 0:
             "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " +
             pathSequence;
      assert status != null:
             "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " +
             pathSequence;
      assert pathSequence > 0:
             "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " +
             pathSequence;
      assert moduleSequence > 0:
             "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " +
             pathSequence;
      assert authorSequence > 0:
             "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " +
             pathSequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(Stamp o) {
      if (this.time > o.time) {
         return 1;
      }

      if (this.time < o.time) {
         return -1;
      }

      if (this.status != o.status) {
         return this.status.ordinal() - o.status.ordinal();
      }

      if (this.authorSequence != o.authorSequence) {
         return this.authorSequence - o.authorSequence;
      }

      if (this.moduleSequence != o.moduleSequence) {
         return this.moduleSequence - o.moduleSequence;
      }

      return this.pathSequence - o.pathSequence;
   }

   @Override
   public boolean equals(Object obj) {
      if (Stamp.class.isAssignableFrom(obj.getClass())) {
         return compareTo((Stamp) obj) == 0;
      }

      return false;
   }

   @Override
   public int hashCode() {
      if (this.hashCode == Integer.MAX_VALUE) {
         this.hashCode = Hashcode.compute(new int[] { this.authorSequence, this.status.ordinal(), this.pathSequence, (int) this.time });
      }

      return this.hashCode;
   }

   public static String stampArrayToString(AbstractIntSet stampSet) {
      final StringBuilder sb = setupToString();

      stampSet.forEachKey((int stamp) -> {
                             sb.append(stampFromIntStamp(stamp));
                             sb.append(",");
                             return true;
                          });
      finishToString(sb);
      return sb.toString();
   }

   public static String stampArrayToString(Collection<Integer> stampCollection) {
      final StringBuilder sb = setupToString();

      stampCollection.stream()
                     .map((stamp) -> {
                             sb.append(stampFromIntStamp(stamp));
                             return stamp;
                          })
                     .forEach((_item) -> {
                                 sb.append(",");
                              });
      finishToString(sb);
      return sb.toString();
   }

   public static String stampArrayToString(int[] stampArray) {
      final StringBuilder sb = setupToString();

      for (final int stamp: stampArray) {
         sb.append(stampFromIntStamp(stamp));
         sb.append(",");
      }

      finishToString(sb);
      return sb.toString();
   }

   public static Stamp stampFromIntStamp(int stamp) {
      final State status         = Get.stampService()
                                .getStatusForStamp(stamp);
      final long  time           = Get.stampService()
                                .getTimeForStamp(stamp);
      final int   authorSequence = Get.stampService()
                                .getAuthorSequenceForStamp(stamp);
      final int   moduleSequence = Get.stampService()
                                .getModuleSequenceForStamp(stamp);
      final int   pathSequence   = Get.stampService()
                                .getPathSequenceForStamp(stamp);

      return new Stamp(status, time, authorSequence, moduleSequence, pathSequence);
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("Stamp{s:");
      sb.append(this.status);
      sb.append(", t:");
      sb.append(getTimeAsInstant());
      sb.append(", a:");
      sb.append(Get.conceptDescriptionText(this.authorSequence));
      sb.append(", m:");
      sb.append(Get.conceptDescriptionText(this.moduleSequence));
      sb.append(", p: ");
      sb.append(Get.conceptDescriptionText(this.pathSequence));
      sb.append('}');
      return sb.toString();
   }

   public void write(DataOutput out)
            throws IOException {
      out.writeInt(this.status.ordinal());
      out.writeLong(this.time);
      out.writeInt(this.authorSequence);
      out.writeInt(this.moduleSequence);
      out.writeInt(this.pathSequence);
   }

   private static void finishToString(StringBuilder sb) {
      final int lastIndexOf = sb.lastIndexOf(",");

      sb.delete(lastIndexOf, lastIndexOf + 1);
      sb.append("]");
   }

   private static StringBuilder setupToString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("[");
      return sb;
   }

   //~--- get methods ---------------------------------------------------------

   public int getAuthorSequence() {
      return this.authorSequence;
   }

   public int getModuleSequence() {
      return this.moduleSequence;
   }

   public int getPathSequence() {
      return this.pathSequence;
   }

   public State getStatus() {
      return this.status;
   }

   public long getTime() {
      return this.time;
   }

   public Instant getTimeAsInstant() {
      return Instant.ofEpochMilli(this.time);
   }
}

