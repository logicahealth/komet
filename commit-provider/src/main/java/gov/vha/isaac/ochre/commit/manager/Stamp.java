/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.commit.manager;


//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import org.apache.mahout.math.set.AbstractIntSet;
import gov.vha.isaac.ochre.util.Hashcode;

public class Stamp implements Comparable<Stamp>  {
   public int   hashCode = Integer.MAX_VALUE;
   private final int  authorSequence;
   private final int  pathSequence;
   private final State  status;
   private final int moduleSequence;
   private final long time;

   //~--- constructors --------------------------------------------------------

   public Stamp(State status, long time, int authorSequence, int moduleSequence, int pathSequence) {
      super();
      this.status = status;
      this.time      = time;
      this.authorSequence = authorSequence;
      this.moduleSequence = moduleSequence;
      this.pathSequence   = pathSequence;
      
      assert time != 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert status != null: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert pathSequence  > 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert moduleSequence > 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert authorSequence > 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;

   }
   
     public Stamp(DataInput in) throws IOException {
      super();
      this.status = State.values()[in.readInt()];
      this.time      = in.readLong();
      this.authorSequence = in.readInt();
      this.moduleSequence = in.readInt();
      this.pathSequence   = in.readInt();
      
      assert time != 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert status != null: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert pathSequence > 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert moduleSequence > 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;
      assert authorSequence > 0: "s: " + status + " t: " + time + " a: " + authorSequence + " " + " m: " + moduleSequence + " p: " + pathSequence;

   }

   public void write(DataOutput out) throws IOException {
       out.writeInt(status.ordinal());
       out.writeLong(time);
       out.writeInt(authorSequence);
       out.writeInt(moduleSequence);
       out.writeInt(pathSequence);
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
      if (hashCode == Integer.MAX_VALUE) {
         hashCode = Hashcode.compute(new int[] { authorSequence, status.ordinal(), pathSequence, (int) time });
      }

      return hashCode;
   }

   //~--- get methods ---------------------------------------------------------

      public int getAuthorSequence() {
      return authorSequence;
   }

   public int getPathSequence() {
      return pathSequence;
   }

   public State getStatus() {
      return status;
   }
   
   public int getModuleSequence() {
      return moduleSequence;
   }

    public long getTime() {
      return time;
   }

   public Instant getTimeAsInstant() {
      return Instant.ofEpochMilli(time);
   }
    @Override
    public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Stamp{s:");
             sb.append(status);
             sb.append(", t:");
             sb.append(getTimeAsInstant());
             sb.append(", a:");
             sb.append(Get.conceptDescriptionText(authorSequence));
             sb.append(", m:");
             sb.append(Get.conceptDescriptionText(moduleSequence));
             sb.append(", p: ");
             sb.append(Get.conceptDescriptionText(pathSequence));
             sb.append('}');
             return sb.toString();
    }
    
    public static Stamp stampFromIntStamp(int stamp) {
        State status = Get.commitService().getStatusForStamp(stamp);
        long time = Get.commitService().getTimeForStamp(stamp);
        int authorSequence = Get.commitService().getAuthorSequenceForStamp(stamp);
        int moduleSequence = Get.commitService().getModuleSequenceForStamp(stamp);
        int pathSequence = Get.commitService().getPathSequenceForStamp(stamp);
        return new Stamp(status, time, authorSequence, moduleSequence, pathSequence);
    }
    
    public static String stampArrayToString(AbstractIntSet stampSet) {
        StringBuilder sb = setupToString();
        stampSet.forEachKey((int stamp) -> {
            sb.append(stampFromIntStamp(stamp));
            sb.append(",");
            return true;
        });
        finishToString(sb);
        return sb.toString();
    }

    private static void finishToString(StringBuilder sb) {
        int lastIndexOf = sb.lastIndexOf(",");
        sb.delete(lastIndexOf, lastIndexOf+1);
        sb.append("]");
    }
    
    public static String stampArrayToString(int[] stampArray) {
        StringBuilder sb = setupToString();
        for (int stamp: stampArray) {
            sb.append(stampFromIntStamp(stamp));
            sb.append(",");
        }
        finishToString(sb);
        return sb.toString();
    }
    public static String stampArrayToString(Collection<Integer> stampCollection) {
        StringBuilder sb = setupToString();
        stampCollection.stream().map((stamp) -> {
            sb.append(stampFromIntStamp(stamp));
           return stamp;
       }).forEach((_item) -> {
           sb.append(",");
       });
        finishToString(sb);
       return sb.toString();
    }

    private static StringBuilder setupToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        return sb;
    }
}
