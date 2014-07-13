package org.ihtsdo.otf.tcc.model.cc.refex.type_long;

//~--- non-JDK imports --------------------------------------------------------

import java.beans.PropertyVetoException;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.type_long.LongMemberVersion;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongRevision;
import org.ihtsdo.otf.tcc.api.refex.RefexType;

public class LongRevision extends RefexRevision<LongRevision, LongMember>
        implements RefexLongAnalogBI<LongRevision> {
   private long longValue;

   //~--- constructors --------------------------------------------------------

   public LongRevision() {
      super();
   }

   public LongRevision(int statusAtPositionNid, LongMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      longValue = primoridalMember.getLong1();
   }

   public LongRevision(TtkRefexLongRevision eVersion, LongMember member) throws IOException {
      super(eVersion, member);
      this.longValue = eVersion.getLongValue();
   }

   public LongRevision(DataInputStream input, LongMember primoridalMember) throws IOException {
      super(input, primoridalMember);
      longValue = input.readLong();
   }

   public LongRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, LongMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      longValue = primoridalMember.getLong1();
   }

   protected LongRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, LongRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      longValue = another.longValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.LONG_EXTENSION_1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (LongRevision.class.isAssignableFrom(obj.getClass())) {
         LongRevision another = (LongRevision) obj;

         return (this.longValue == another.longValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public LongRevision makeAnalog() {
      return new LongRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }
   
   @Override
   public LongRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
       if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }
      LongRevision newR = new LongRevision(status, time, authorNid,
              moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" longValue:").append(this.longValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(DataOutput output) throws IOException {
      output.writeLong(longValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public long getLong1() {
      return longValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.LONG;
   }

   @Override
   public LongMemberVersion getVersion(ViewCoordinate c) throws ContradictionException {
      return (LongMemberVersion) ((LongMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<LongMemberVersion> getVersions() {
      return ((LongMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<LongRevision>> getVersions(ViewCoordinate c) {
      return ((LongMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }

   public void setLongValue(long longValue) {
      this.longValue = longValue;
      modified();
   }
}
