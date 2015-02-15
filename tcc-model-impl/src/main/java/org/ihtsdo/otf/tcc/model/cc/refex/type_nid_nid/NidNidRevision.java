package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class NidNidRevision extends RefexRevision<NidNidRevision, NidNidMember>
        implements RefexNidNidAnalogBI<NidNidRevision> {
   protected int c1Nid;
   protected int c2Nid;

   //~--- constructors --------------------------------------------------------

   public NidNidRevision() {
      super();
   }

   public NidNidRevision(int statusAtPositionNid, NidNidMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
   }

   public NidNidRevision(TtkRefexUuidUuidRevision eVersion, NidNidMember member) throws IOException {
      super(eVersion, member);
      c1Nid = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      c2Nid = PersistentStore.get().getNidForUuids(eVersion.getUuid2());
   }

   public NidNidRevision(Status status, long time, int authorNid,
           int moduleNid, int pathNid, NidNidMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
   }

   protected NidNidRevision(Status status, long time, int authorNid,
           int moduleNid, int pathNid, NidNidRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid = another.c1Nid;
      c2Nid = another.c2Nid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
      allNids.add(c2Nid);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_2_ID, getNid2());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidRevision.class.isAssignableFrom(obj.getClass())) {
         NidNidRevision another = (NidNidRevision) obj;

         if ((this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)) {
            return super.equals(obj);
         }
      }

      return false;
   }

   @Override
   public NidNidRevision makeAnalog() {
      return new NidNidRevision(getStatus(), getTime(), getAuthorNid(),
              getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidNidRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);
         return this;
      }

      NidNidRevision newR = new NidNidRevision(status, time,
              authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert c2Nid != Integer.MAX_VALUE;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append(" c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" c2Nid: ");
      ConceptComponent.addNidToBuffer(buf, c2Nid);
      buf.append(super.toString());

      return buf.toString();
   }


   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   public int getC2Nid() {
      return c2Nid;
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
   protected RefexType getTkRefsetType() {
      return RefexType.CID_CID;
   }

   @Override
   public NidNidMemberVersion getVersion(ViewCoordinate c) throws ContradictionException {
      return (NidNidMemberVersion) ((NidNidMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<NidNidMemberVersion> getVersions() {
      return ((NidNidMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidNidRevision>> getVersions(ViewCoordinate c) {
      return ((NidNidMember) primordialComponent).getVersions(c);
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
}
