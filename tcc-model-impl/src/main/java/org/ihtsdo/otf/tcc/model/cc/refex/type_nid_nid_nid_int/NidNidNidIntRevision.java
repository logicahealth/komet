package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_int;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int
   .RefexNidNidNidIntAnalogBI;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int
   .TtkRefexUuidUuidUuidIntRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

import org.ihtsdo.otf.tcc.api.coordinate.Status;

public class NidNidNidIntRevision
        extends RefexRevision<NidNidNidIntRevision, NidNidNidIntMember>
        implements RefexNidNidNidIntAnalogBI<NidNidNidIntRevision> {
    protected int   nid1;
    protected int   nid2;
    protected int   nid3;
    protected int int1;

   public NidNidNidIntRevision() {
      super();
   }

   public NidNidNidIntRevision(int statusAtPositionNid,
                                 NidNidNidIntMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      int1 = primoridalMember.getInt1();
   }

   public NidNidNidIntRevision(TtkRefexUuidUuidUuidIntRevision eVersion,
                                 NidNidNidIntMember member)
           throws IOException {
      super(eVersion, member);
      nid1  = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      nid2  = PersistentStore.get().getNidForUuids(eVersion.getUuid2());
      nid3  = PersistentStore.get().getNidForUuids(eVersion.getUuid3());
      int1 = eVersion.getInt1();
   }


   public NidNidNidIntRevision(Status status, long time, int authorNid,
                                 int moduleNid, int pathNid,
                                 NidNidNidIntMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      int1 = primoridalMember.getInt1();
   }

   protected NidNidNidIntRevision(Status status, long time, int authorNid,
                                    int moduleNid, int pathNid,
                                    NidNidNidIntRevision another) {
      super(status, time, authorNid, moduleNid, pathNid,
            another.primordialComponent);
      nid1  = another.nid1;
      nid2  = another.nid2;
      nid3  = another.nid3;
      int1 = another.int1;
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
      rcs.with(ComponentProperty.INTEGER_EXTENSION_1, getInt1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidIntRevision.class.isAssignableFrom(obj.getClass())) {
         NidNidNidIntRevision another = (NidNidNidIntRevision) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3)
                && (this.int1 == another.int1) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidNidNidIntRevision makeAnalog() {
      return new NidNidNidIntRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidIntRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidNidNidIntRevision newR = new NidNidNidIntRevision(status, time,
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
      buf.append(" int1: ").append(int1);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public int getInt1() {
      return int1;
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
      return RefexType.CID_CID_CID_INT;
   }

   @Override
   public NidNidNidIntMemberVersion getVersion(ViewCoordinate c)
           throws ContradictionException {
      return (NidNidNidIntMemberVersion) ((NidNidNidIntMember) primordialComponent).getVersion(c);
   }

   @Override
   public List<NidNidNidIntMemberVersion> getVersions() {
      return ((NidNidNidIntMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidNidNidIntRevision>> getVersions(
           ViewCoordinate c) {
      return ((NidNidNidIntMember) primordialComponent).getVersions(c);
   }

   @Override
   public void setInt1(int int1) {
      this.int1 = int1;
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
