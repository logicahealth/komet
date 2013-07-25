package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid;

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
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.chronicle.cc.P;

public class NidNidNidRevision extends RefexRevision<NidNidNidRevision, NidNidNidMember>
        implements RefexNidNidNidAnalogBI<NidNidNidRevision> {
   private int c1Nid;
   private int c2Nid;
   private int c3Nid;

   //~--- constructors --------------------------------------------------------

   public NidNidNidRevision() {
      super();
   }

   public NidNidNidRevision(int statusAtPositionNid, NidNidNidMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
      c3Nid = primoridalMember.getC3Nid();
   }

   public NidNidNidRevision(TtkRefexUuidUuidUuidRevision eVersion, NidNidNidMember member) throws IOException {
      super(eVersion, member);
      c1Nid = P.s.getNidForUuids(eVersion.getUuid1());
      c2Nid = P.s.getNidForUuids(eVersion.getUuid2());
      c3Nid = P.s.getNidForUuids(eVersion.getUuid3());
   }

   public NidNidNidRevision(TupleInput input, NidNidNidMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid = input.readInt();
      c2Nid = input.readInt();
      c3Nid = input.readInt();
   }

   public NidNidNidRevision(Status status, long time, int authorNid, int moduleNid,
           int pathNid, NidNidNidMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
      c3Nid = primoridalMember.getC3Nid();
   }

   protected NidNidNidRevision(Status status, long time, int authorNid, int moduleNid,
           int pathNid, NidNidNidRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid = another.c1Nid;
      c2Nid = another.c2Nid;
      c3Nid = another.c3Nid;
   }
   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
      allNids.add(c2Nid);
      allNids.add(c3Nid);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_2_ID, getNid2());
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_3_ID, getNid3());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidRevision.class.isAssignableFrom(obj.getClass())) {
         NidNidNidRevision another = (NidNidNidRevision) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                && (this.c3Nid == another.c3Nid) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidNidNidRevision makeAnalog() {
      return new NidNidNidRevision(getStatus(), getTime(), 
              getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public NidNidNidRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidNidNidRevision newR = new NidNidNidRevision(status, time,
              authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert c2Nid != Integer.MAX_VALUE;
      assert c3Nid != Integer.MAX_VALUE;

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
      buf.append(" c2Nid: ");
      ConceptComponent.addNidToBuffer(buf, c2Nid);
      buf.append(" c3Nid: ");
      ConceptComponent.addNidToBuffer(buf, c3Nid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(c2Nid);
      output.writeInt(c3Nid);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   public int getC2Nid() {
      return c2Nid;
   }

   public int getC3Nid() {
      return c3Nid;
   }

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public int getNid2() {
      return c2Nid;
   }

   @Override
   public int getNid3() {
      return c3Nid;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_CID_CID;
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(5);

      variableNids.add(getC1Nid());
      variableNids.add(getC2Nid());
      variableNids.add(getC3Nid());

      return variableNids;
   }

   @Override
   public NidNidNidMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (NidNidNidMember.Version) ((NidNidNidMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<NidNidNidMember.Version> getVersions() {
      return ((NidNidNidMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidNidNidRevision>> getVersions(ViewCoordinate c) {
      return ((NidNidNidMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   public void setC2Nid(int c2Nid) {
      this.c2Nid = c2Nid;
      modified();
   }

   public void setC3Nid(int c3Nid) {
      this.c3Nid = c3Nid;
      modified();
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setNid2(int cnid) throws PropertyVetoException {
      this.c2Nid = cnid;
      modified();
   }

   @Override
   public void setNid3(int cnid) throws PropertyVetoException {
      this.c3Nid = cnid;
      modified();
   }
}
