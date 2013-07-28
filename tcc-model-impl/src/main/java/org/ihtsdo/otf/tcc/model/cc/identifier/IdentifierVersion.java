package org.ihtsdo.otf.tcc.chronicle.cc.identifier;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.Revision;
import org.ihtsdo.otf.tcc.api.id.IdBI;

//import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.Date;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

public abstract class IdentifierVersion implements IdBI {
   protected int authorityNid;
   private int   stamp;

   //~--- constructors --------------------------------------------------------

   protected IdentifierVersion() {
      super();
   }

   protected IdentifierVersion(TtkIdentifier idv) throws IOException {
      super();
      this.stamp = P.s.getStamp(idv.getStatus(), idv.getTime(),
                                    P.s.getNidForUuids(idv.getAuthorUuid()),
                                    P.s.getNidForUuids(idv.getModuleUuid()),
                                    P.s.getNidForUuids(idv.getPathUuid()));
      this.authorityNid = P.s.getNidForUuids(idv.getAuthorityUuid());
   }

   protected IdentifierVersion(TupleInput input) {
      super();
      stamp     = input.readInt();
      authorityNid = input.readInt();
   }

   protected IdentifierVersion(Status status, long time, int authorNid, int moduleNid, int pathNid,
                               IdentifierVersion idVersion) {
      this(status, time, authorNid, moduleNid, pathNid, idVersion.authorityNid);
   }

   protected IdentifierVersion(Status status, long time, int authorNid, int moduleNid, int pathNid,
                               int authorityNid) {
      this.stamp     = P.s.getStamp(status, time, authorNid, moduleNid, pathNid);
      this.authorityNid = authorityNid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IdentifierVersion.class.isAssignableFrom(obj.getClass())) {
         IdentifierVersion another = (IdentifierVersion) obj;

         return (this.stamp == another.stamp) && (this.authorityNid == another.authorityNid);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { stamp, authorityNid });
   }

   public final boolean readyToWrite() {
      assert stamp != Integer.MAX_VALUE : toString();
      assert authorityNid != Integer.MAX_VALUE : toString();
      assert readyToWriteIdentifier() : toString();

      return true;
   }

   public abstract boolean readyToWriteIdentifier();

   public boolean stampIsInRange(int min, int max) {
      return (stamp >= min) && (stamp <= max);
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append("authority:");
      ConceptComponent.addNidToBuffer(buf, authorityNid);
      buf.append(" stamp:").append(stamp);
      buf.append(" s:");
      buf.append(getStatus());
      buf.append(" t:");

      if (getTime() == Long.MAX_VALUE) {
         buf.append(" uncommitted");
      } else if (getTime() == Long.MIN_VALUE) {
         buf.append(" uncommitted");
      } else {
         buf.append(Revision.fileDateFormat.format(new Date(getTime())));
      }

      buf.append(" a:");
      ConceptComponent.addNidToBuffer(buf, getAuthorNid());
      buf.append(" m:");
      ConceptComponent.addNidToBuffer(buf, getModuleNid());
      buf.append(" p:");
      ConceptComponent.addNidToBuffer(buf, getPathNid());

      return buf.toString();
   }

   public final void writeIdPartToBdb(TupleOutput output) {
      output.writeInt(stamp);
      output.writeInt(authorityNid);
      writeSourceIdToBdb(output);
   }

   protected abstract void writeSourceIdToBdb(TupleOutput output);

   //~--- get methods ---------------------------------------------------------

   @Override
   public Set<Integer> getAllNidsForId() throws IOException {
      HashSet<Integer> allNids = new HashSet<>();

      allNids.add(authorityNid);
      allNids.add(getAuthorNid());
      allNids.add(getModuleNid());
      allNids.add(getPathNid());

      return allNids;
   }

   public int getAuthorId() {
      return P.s.getAuthorNidForStamp(stamp);
   }

   @Override
   public int getAuthorNid() {
      return P.s.getAuthorNidForStamp(stamp);
   }

   @Override
   public int getAuthorityNid() {
      return authorityNid;
   }

   @Override
   public int getModuleNid() {
      return P.s.getModuleNidForStamp(stamp);
   }

   @Override
   public int getPathNid() {
      return P.s.getPathNidForStamp(stamp);
   }

   @Override
   public int getStamp() {
      return stamp;
   }

   @Override
   public Status getStatus() {
      return P.s.getStatusForStamp(stamp);
   }

   @Override
   public long getTime() {
      return P.s.getTimeForStamp(stamp);
   }

   public abstract ConceptComponent.IDENTIFIER_PART_TYPES getType();

   protected IntArrayList getVariableVersionNids() {
      IntArrayList nids = new IntArrayList(3);

      nids.add(authorityNid);

      return nids;
   }

   //~--- set methods ---------------------------------------------------------

   public void setStamp(int stamp) {
      this.stamp = stamp;
   }

   public void setTime(long time) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Time alreay committed.");
      }

      this.stamp = P.s.getStamp(getStatus(), time, getAuthorNid(), getModuleNid(), getPathNid());
   }
}
