
/**
 *
 */
package org.ihtsdo.otf.tcc.datastore.stamp;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

public class Stamp implements Comparable<Stamp> {
   public int   hashCode = Integer.MAX_VALUE;
   private int  authorNid;
   private int  pathNid;
   private Status  status;
   private int moduleNid;
   private long time;

   //~--- constructors --------------------------------------------------------

   Stamp(Status status, long time, int authorNid, int moduleNid, int pathNid) {
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

   public int getAuthorNid() {
      return authorNid;
   }

   public int getPathNid() {
      return pathNid;
   }

   public Status getStatus() {
      return status;
   }
   
   public int getModuleNid() {
      return moduleNid;
   }

   public long getTime() {
      return time;
   }
}
