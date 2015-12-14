
/**
 *
 */
package org.ihtsdo.otf.tcc.model.version;

//~--- non-JDK imports --------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import org.apache.mahout.math.set.AbstractIntSet;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.VersionPointBI;
import gov.vha.isaac.ochre.util.Hashcode;
import org.ihtsdo.otf.tcc.api.store.Ts;

public class Stamp implements Comparable<Stamp>, VersionPointBI {
   public int   hashCode = Integer.MAX_VALUE;
   private final int  authorNid;
   private final int  pathNid;
   private final Status  status;
   private final int moduleNid;
   private final long time;

   //~--- constructors --------------------------------------------------------

   public Stamp(Status status, long time, int authorNid, int moduleNid, int pathNid) {
      super();
      this.status = status;
      this.authorNid = authorNid;
      this.pathNid   = pathNid;
      this.moduleNid = moduleNid;
      this.time      = time;
      
      assert time != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert status != null: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert pathNid != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert moduleNid != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert authorNid != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;

   }
   
     public Stamp(DataInput in) throws IOException {
      super();
      this.status = Status.values()[in.readInt()];
      this.authorNid = in.readInt();
      this.pathNid   = in.readInt();
      this.moduleNid = in.readInt();
      this.time      = in.readLong();
      
      assert time != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert status != null: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert pathNid != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert moduleNid != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert authorNid != 0: "s: " + status + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;

   }

   public void write(DataOutput out) throws IOException {
       out.writeInt(status.ordinal());
       out.writeInt(authorNid);
       out.writeInt(pathNid);
       out.writeInt(moduleNid);
       out.writeLong(time);
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

      if (this.authorNid != o.authorNid) {
         return this.authorNid - o.authorNid;
      }
      
      if (this.moduleNid != o.moduleNid) {
         return this.moduleNid - o.moduleNid;
      }

      return this.pathNid - o.pathNid;
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
         hashCode = Hashcode.compute(new int[] { authorNid, status.ordinal(), pathNid, (int) time });
      }

      return hashCode;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAuthorNid() {
      return authorNid;
   }

   @Override
   public int getPathNid() {
      return pathNid;
   }

   public Status getStatus() {
      return status;
   }
   
   @Override
   public int getModuleNid() {
      return moduleNid;
   }

   @Override
   public long getTime() {
      return time;
   }

   @Override
   public Instant getTimeAsInstant() {
      return Instant.ofEpochMilli(time);
   }
    @Override
    public String toString() {
        if (Ts.get() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Stamp{s:");
             sb.append(status);
             sb.append(", t:");
             sb.append(getTimeAsInstant());
             sb.append(", a:");
             sb.append(Ts.get().informAboutNid(authorNid));
             sb.append(", m:");
             sb.append(Ts.get().informAboutNid(moduleNid));
             sb.append(", p: ");
             sb.append(Ts.get().informAboutNid(pathNid));
             sb.append('}');
             return sb.toString();
        }
        
        return "Stamp{s:" + status + ", t:" + getTimeAsInstant() + "a:" + authorNid + ", m:" + moduleNid + ", p: " + pathNid +'}';
    }
    
    public static Stamp stampFromIntStamp(int stamp) {
        Status status = Ts.get().getStatusForStamp(stamp);
        long time = Ts.get().getTimeForStamp(stamp);
        int authorNid = Ts.get().getAuthorNidForStamp(stamp);
        int moduleNid = Ts.get().getModuleNidForStamp(stamp);
        int pathNid = Ts.get().getPathNidForStamp(stamp);
        return new Stamp(status, time, authorNid, moduleNid, pathNid);
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
