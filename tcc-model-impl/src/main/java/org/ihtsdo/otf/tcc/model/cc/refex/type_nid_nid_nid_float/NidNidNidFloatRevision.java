package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_float;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

public class NidNidNidFloatRevision
        extends RefexRevision<NidNidNidFloatRevision, NidNidNidFloatMember>
        implements RefexNidNidNidFloatAnalogBI<NidNidNidFloatRevision> {
    protected int   nid1;
    protected int   nid2;
    protected int   nid3;
    protected float float1;

   public NidNidNidFloatRevision() {
      super();
   }

   public NidNidNidFloatRevision(int statusAtPositionNid,
                                 NidNidNidFloatMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      float1 = primoridalMember.getFloat1();
   }

   public NidNidNidFloatRevision(TtkRefexUuidUuidUuidFloatRevision eVersion,
                                 NidNidNidFloatMember member)
           throws IOException {
      super(eVersion, member);
      nid1  = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      nid2  = PersistentStore.get().getNidForUuids(eVersion.getUuid2());
      nid3  = PersistentStore.get().getNidForUuids(eVersion.getUuid3());
      float1 = eVersion.getFloat1();
   }


   public NidNidNidFloatRevision(Status status, long time, int authorNid,
                                 int moduleNid, int pathNid,
                                 NidNidNidFloatMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      float1 = primoridalMember.getFloat1();
   }

   protected NidNidNidFloatRevision(Status status, long time, int authorNid,
                                    int moduleNid, int pathNid,
                                    NidNidNidFloatRevision another) {
      super(status, time, authorNid, moduleNid, pathNid,
            another.primordialComponent);
      nid1  = another.nid1;
      nid2  = another.nid2;
      nid3  = another.nid3;
      float1 = another.float1;
   }

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(nid1);
      allNids.add(nid2);
      allNids.add(nid3);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_2_ID, getNid2());
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_3_ID, getNid3());
      rcs.with(ComponentProperty.FLOAT_EXTENSION_1, getFloat1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidFloatRevision.class.isAssignableFrom(obj.getClass())) {
         NidNidNidFloatRevision another = (NidNidNidFloatRevision) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3)
                && (this.float1 == another.float1) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidNidNidFloatRevision makeAnalog() {
      return new NidNidNidFloatRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidFloatRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidNidNidFloatRevision newR = new NidNidNidFloatRevision(status, time,
                                       authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert nid1 != Integer.MAX_VALUE;
      assert nid2 != Integer.MAX_VALUE;
      assert nid3 != Integer.MAX_VALUE;

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
      buf.append(" nid1: ");
      ConceptComponent.addNidToBuffer(buf, nid1);
      buf.append(" nid2: ");
      ConceptComponent.addNidToBuffer(buf, nid2);
      buf.append(" nid3: ");
      ConceptComponent.addNidToBuffer(buf, nid3);
      buf.append(" float1: ").append(float1);
      buf.append(super.toString());

      return buf.toString();
   }


   @Override
   public float getFloat1() {
      return float1;
   }

   @Override
   public int getNid1() {
      return nid1;
   }

   @Override
   public int getNid2() {
      return nid2;
   }

   @Override
   public int getNid3() {
      return nid3;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_CID_CID_FLOAT;
   }

   @Override
   public NidNidNidFloatMemberVersion getVersion(ViewCoordinate c)
           throws ContradictionException {
      return (NidNidNidFloatMemberVersion) ((NidNidNidFloatMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<NidNidNidFloatMemberVersion> getVersions() {
      return ((NidNidNidFloatMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidNidNidFloatRevision>> getVersions(
           ViewCoordinate c) {
      return ((NidNidNidFloatMember) primordialComponent).getVersions(c);
   }

   @Override
   public void setFloat1(float float1) {
      this.float1 = float1;
      modified();
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.nid1 = cnid;
      modified();
   }

   @Override
   public void setNid2(int cnid) throws PropertyVetoException {
      this.nid2 = cnid;
      modified();
   }

   @Override
   public void setNid3(int cnid) throws PropertyVetoException {
      this.nid3 = cnid;
      modified();
   }
}
