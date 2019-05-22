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
import sh.isaac.api.Status;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.util.Hashcode;

//~--- classes ----------------------------------------------------------------

/**
 * The Class Stamp.
 * TODO: add license and copyright nids to the class. . 
 */
public class Stamp
         implements Comparable<Stamp> {
   /** The hash code. */
   public int hashCode = Integer.MAX_VALUE;

   /** The author nid. */
   private final int authorNid;

   /** The path nid. */
   private final int pathNid;

   /** The status. */
   private final Status status;

   /** The module nid. */
   private final int moduleNid;

   /** The time. */
   private final long time;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new stamp.
    *
    * @param in the in
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public Stamp(DataInput in)
            throws IOException {
      super();
      this.status         = Status.valueOf(in.readUTF());
      this.time           = in.readLong();
      this.authorNid = in.readInt();
      this.moduleNid = in.readInt();
      this.pathNid   = in.readInt();
      assert this.time != 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorNid + " " + " m: " +
             this.moduleNid + " p: " + this.pathNid;
      assert this.status != null:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorNid + " " + " m: " +
             this.moduleNid + " p: " + this.pathNid;
      assert this.pathNid < 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorNid + " " + " m: " +
             this.moduleNid + " p: " + this.pathNid;
      assert this.moduleNid < 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorNid + " " + " m: " +
             this.moduleNid + " p: " + this.pathNid;
      assert this.authorNid < 0:
             "s: " + this.status + " t: " + this.time + " a: " + this.authorNid + " " + " m: " +
             this.moduleNid + " p: " + this.pathNid;
   }

   /**
    * Instantiates a new stamp.
    *
    * @param status the status
    * @param time the time
    * @param authorNid the author nid
    * @param moduleNid the module nid
    * @param pathNid the path nid
    */
   public Stamp(Status status, long time, int authorNid, int moduleNid, int pathNid) {
      super();
      this.status         = status;
      this.time           = time;
      this.authorNid = authorNid;
      this.moduleNid = moduleNid;
      this.pathNid   = pathNid;
      assert time != 0:
             "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " +
             pathNid;
      assert status != null:
             "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " +
             pathNid;
      assert pathNid < 0:
             "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " +
             pathNid;
      assert moduleNid < 0:
             "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " +
             pathNid;
      assert authorNid < 0:
             "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " +
             pathNid;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
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

      if (this.authorNid != o.authorNid) {
         return this.authorNid - o.authorNid;
      }

      if (this.moduleNid != o.moduleNid) {
         return this.moduleNid - o.moduleNid;
      }

      return this.pathNid - o.pathNid;
   }

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (Stamp.class.isAssignableFrom(obj.getClass())) {
         return compareTo((Stamp) obj) == 0;
      }

      return false;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      if (this.hashCode == Integer.MAX_VALUE) {
         this.hashCode = Hashcode.compute(new int[] { this.authorNid, this.status.ordinal(), this.pathNid,
               (int) this.time });
      }

      return this.hashCode;
   }

   /**
    * Stamp array to string.
    *
    * @param stampSet the stamp set
    * @return the string
    */
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

   /**
    * Stamp array to string.
    *
    * @param stampCollection the stamp collection
    * @return the string
    */
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

   /**
    * Stamp array to string.
    *
    * @param stampArray the stamp array
    * @return the string
    */
   public static String stampArrayToString(int[] stampArray) {
      final StringBuilder sb = setupToString();

      for (final int stamp: stampArray) {
         sb.append(stampFromIntStamp(stamp));
         sb.append(",");
      }

      finishToString(sb);
      return sb.toString();
   }

   /**
    * Stamp from int stamp.
    *
    * @param stamp the stamp
    * @return the stamp
    */
   public static Stamp stampFromIntStamp(int stamp) {
      final Stamp s = Get.stampService().getStamp(stamp);
      return new Stamp(s.getStatus(), s.getTime(), s.getAuthorNid(), s.getModuleNid(), s.getPathNid());
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("Stamp{s:");
      sb.append(this.status);
      sb.append(", t:");
      if (this.time == Long.MAX_VALUE) {
          sb.append(" UNCOMMITTED");
      } else if (this.time == Long.MIN_VALUE) {
          sb.append(" CANCELED");
      } else {
          sb.append(getTimeAsInstant());
      }
      sb.append(", a:");
      sb.append(Get.conceptDescriptionText(this.authorNid));
      sb.append(", m:");
      sb.append(Get.conceptDescriptionText(this.moduleNid));
      sb.append(", p: ");
      sb.append(Get.conceptDescriptionText(this.pathNid));
      sb.append('}');
      return sb.toString();
   }

   /**
    * Write.
    *
    * @param out the out
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void write(DataOutput out)
            throws IOException {
      out.writeUTF(this.status.name());
      out.writeLong(this.time);
      out.writeInt(this.authorNid);
      out.writeInt(this.moduleNid);
      out.writeInt(this.pathNid);
   }

   /**
    * Finish to string.
    *
    * @param sb the sb
    */
   private static void finishToString(StringBuilder sb) {
      final int lastIndexOf = sb.lastIndexOf(",");

      sb.delete(lastIndexOf, lastIndexOf + 1);
      sb.append("]");
   }

   /**
    * Setup to string.
    *
    * @return the string builder
    */
   private static StringBuilder setupToString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("[");
      return sb;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the author nid.
    *
    * @return the author nid
    */
   public int getAuthorNid() {
      return this.authorNid;
   }

   /**
    * Gets the module nid.
    *
    * @return the module nid
    */
   public int getModuleNid() {
      return this.moduleNid;
   }

   /**
    * Gets the path nid.
    *
    * @return the path nid
    */
   public int getPathNid() {
      return this.pathNid;
   }

   /**
    * Gets the status.
    *
    * @return the status
    */
   public Status getStatus() {
      return this.status;
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   public long getTime() {
      return this.time;
   }

   /**
    * Gets the time as instant.
    *
    * @return the time as instant
    */
   public Instant getTimeAsInstant() {
      return Instant.ofEpochMilli(this.time);
   }
}

