package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_long;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.chronicle.cc.P;

public class NidLongRevision extends RefexRevision<NidLongRevision, NidLongMember>
        implements RefexNidLongAnalogBI<NidLongRevision> {
   private int  c1Nid;
   private long longValue;

   //~--- constructors --------------------------------------------------------

   public NidLongRevision() {
      super();
   }

   protected NidLongRevision(int statusAtPositionNid, NidLongMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid     = primoridalMember.getC1Nid();
      longValue = primoridalMember.getLongValue();
   }

   public NidLongRevision(TtkRefexUuidLongRevision eVersion, NidLongMember member) throws IOException {
      super(eVersion, member);
      c1Nid     = P.s.getNidForUuids(eVersion.getUuid1());
      longValue = eVersion.getLong1();
   }

   public NidLongRevision(TupleInput input, NidLongMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid     = input.readInt();
      longValue = input.readLong();
   }

   protected NidLongRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, 
                             NidLongMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid     = primoridalMember.getC1Nid();
      longValue = primoridalMember.getLongValue();
   }

   protected NidLongRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
           NidLongRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid     = another.c1Nid;
      longValue = another.longValue;
   }
   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.LONG_EXTENSION_1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidLongRevision.class.isAssignableFrom(obj.getClass())) {
         NidLongRevision another = (NidLongRevision) obj;

         return (this.c1Nid == another.c1Nid) && (longValue == another.longValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidLongRevision makeAnalog() {
      return new NidLongRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidLongRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);
         return this;
      }

      NidLongRevision newR = new NidLongRevision(status, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" longValue:").append(this.longValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeLong(longValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public long getLong1() {
      return this.longValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_LONG;
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(3);

      variableNids.add(getNid1());

      return variableNids;
   }

   @Override
   public NidLongMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (NidLongMember.Version) ((NidLongMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<NidLongMember.Version> getVersions() {
      return ((NidLongMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidLongRevision>> getVersions(ViewCoordinate c) {
      return ((NidLongMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }
}
