package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string;

//~--- non-JDK imports --------------------------------------------------------



import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

import org.ihtsdo.otf.tcc.api.coordinate.Status;

public class NidStringRevision extends RefexRevision<NidStringRevision, NidStringMember>
        implements RefexNidStringAnalogBI<NidStringRevision> {
    protected int    c1Nid;
    protected String string1;

   //~--- constructors --------------------------------------------------------

   public NidStringRevision() {
      super();
   }

   public NidStringRevision(int statusAtPositionNid, NidStringMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      string1 = primoridalMember.getString1();
   }
   
   public NidStringRevision(TtkRefexUuidStringRevision eVersion, NidStringMember member) throws IOException {
      super(eVersion, member);
      c1Nid    = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      string1 = eVersion.getString1();
   }

   public NidStringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                         NidStringMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      string1 = primoridalMember.getString1();
   }

   protected NidStringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, NidStringRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid    = another.c1Nid;
      string1 = another.string1;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.STRING_EXTENSION_1, getString1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidStringRevision.class.isAssignableFrom(obj.getClass())) {
         NidStringRevision another = (NidStringRevision) obj;

         return (this.c1Nid == another.c1Nid) && this.string1.equals(another.string1) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidStringRevision makeAnalog() {
      return new NidStringRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public NidStringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidStringRevision newR = new NidStringRevision(status, time, authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert string1 != null;

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
      buf.append(" string1:" + "'").append(this.string1).append("'");
      buf.append(super.toString());

      return buf.toString();
   }


   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public String getString1() {
      return string1;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_STR;
   }

   @Override
   public NidStringMemberVersion getVersion(ViewCoordinate c) throws ContradictionException {
      return (NidStringMemberVersion) ((NidStringMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<NidStringMemberVersion> getVersions() {
      return ((NidStringMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidStringRevision>> getVersions(ViewCoordinate c) {
      return ((NidStringMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.string1 = str;
      modified();
   }

   public void setStringValue(String strValue) {
      this.string1 = strValue;
      modified();
   }
}
