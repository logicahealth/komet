package org.ihtsdo.otf.tcc.model.cc.identifier;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

//import org.ihtsdo.db.bdb.Bdb;
//~--- JDK imports ------------------------------------------------------------

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

   protected IdentifierVersion(DataInputStream input) throws IOException {
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

   public final void writeIdPartToBdb(DataOutput output) throws IOException {
      output.writeInt(stamp);
      output.writeInt(authorityNid);
      writeSourceIdToBdb(output);
   }

   protected abstract void writeSourceIdToBdb(DataOutput output) throws IOException;

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
