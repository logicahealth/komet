package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_boolean;

//~--- non-JDK imports --------------------------------------------------------


import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refex.type_nid_boolean.RefexNidBooleanAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanRevision;

public class NidBooleanRevision extends RefexRevision<NidBooleanRevision, NidBooleanMember>
        implements RefexNidBooleanAnalogBI<NidBooleanRevision> {
   protected int   nid1;
   protected boolean boolean1;

   //~--- constructors --------------------------------------------------------

   public NidBooleanRevision() {
      super();
   }

   public NidBooleanRevision(int statusAtPositionNid, NidBooleanMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      nid1      = primoridalMember.getC1Nid();
      boolean1 = primoridalMember.getBoolean1();
   }

   public NidBooleanRevision(TtkRefexUuidBooleanRevision eVersion, NidBooleanMember member) throws IOException {
      super(eVersion, member);
      nid1      = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      boolean1 = eVersion.boolean1;
   }

   public NidBooleanRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, 
                           NidBooleanMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      nid1      = primoridalMember.getC1Nid();
      boolean1 = primoridalMember.getBoolean1();
   }

   protected NidBooleanRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                              NidBooleanRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      nid1      = another.nid1;
      boolean1 = another.boolean1;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(nid1);
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.BOOLEAN_EXTENSION_1, getBoolean1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidBooleanRevision.class.isAssignableFrom(obj.getClass())) {
         NidBooleanRevision another = (NidBooleanRevision) obj;

         return (this.nid1 == another.nid1) && (this.boolean1 == another.boolean1) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidBooleanRevision makeAnalog() {
      return new NidBooleanRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidBooleanRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidBooleanRevision newR = new NidBooleanRevision(status, time, authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert nid1 != Integer.MAX_VALUE;

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
      ConceptComponent.addNidToBuffer(buf, nid1);
        buf.append(" boolean1:").append(this.boolean1);
      buf.append(super.toString());

      return buf.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return nid1;
   }

   @Override
   public int getNid1() {
      return nid1;
   }

   @Override
   public boolean getBoolean1() {
      return this.boolean1;
   }

    @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_FLOAT;
   }

   @Override
   public NidBooleanMemberVersion getVersion(ViewCoordinate c) throws ContradictionException {
      return (NidBooleanMemberVersion) ((NidBooleanMember) primordialComponent).getVersion(c);
   }

   @Override
   public List<NidBooleanMemberVersion> getVersions() {
      return ((NidBooleanMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidBooleanRevision>> getVersions(ViewCoordinate c) {
      return ((NidBooleanMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.nid1 = c1Nid;
      modified();
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.nid1 = cnid;
      modified();
   }

   @Override
   public void setBoolean1(boolean boolean1) throws PropertyVetoException {
      this.boolean1 = boolean1;
      modified();
   }
}
