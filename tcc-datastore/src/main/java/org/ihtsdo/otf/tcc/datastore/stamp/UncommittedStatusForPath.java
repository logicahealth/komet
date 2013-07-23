package org.ihtsdo.otf.tcc.datastore.stamp;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

public class UncommittedStatusForPath {
   public int hashCode = Integer.MAX_VALUE;
   public int authorNid;
   public int pathNid;
   public Status status;
   public int moduleNid;

   //~--- constructors --------------------------------------------------------

   public UncommittedStatusForPath(Status status, int authorNid, int moduleNid, int pathNid) {
      super();
      this.status = status;
      this.authorNid = authorNid;
      this.pathNid   = pathNid;
      this.moduleNid = moduleNid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UncommittedStatusForPath) {
         UncommittedStatusForPath other = (UncommittedStatusForPath) obj;

         if ((status == other.status) && (authorNid == other.authorNid) 
                 && (pathNid == other.pathNid) && (moduleNid == other.moduleNid)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      if (hashCode == Integer.MAX_VALUE) {
         hashCode = Hashcode.compute(new int[] { status.ordinal(), authorNid, pathNid, moduleNid });
      }

      return hashCode;
   }
}
