package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_float;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_float.RefexNidFloatAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

public class NidFloatRevision extends RefexRevision<NidFloatRevision, NidFloatMember>
        implements RefexNidFloatAnalogBI<NidFloatRevision> {
   protected int   c1Nid;
   protected float floatValue;

   //~--- constructors --------------------------------------------------------

   public NidFloatRevision() {
      super();
   }

   public NidFloatRevision(int statusAtPositionNid, NidFloatMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid      = primoridalMember.getC1Nid();
      floatValue = primoridalMember.getFloatValue();
   }

   public NidFloatRevision(TtkRefexUuidFloatRevision eVersion, NidFloatMember member) throws IOException {
      super(eVersion, member);
      c1Nid      = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      floatValue = eVersion.getFloat1();
   }

   public NidFloatRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                           NidFloatMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid      = primoridalMember.getC1Nid();
      floatValue = primoridalMember.getFloatValue();
   }

   protected NidFloatRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                              NidFloatRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid      = another.c1Nid;
      floatValue = another.floatValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.FLOAT_EXTENSION_1, getFloat1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidFloatRevision.class.isAssignableFrom(obj.getClass())) {
         NidFloatRevision another = (NidFloatRevision) obj;

         return (this.c1Nid == another.c1Nid) && (this.floatValue == another.floatValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidFloatRevision makeAnalog() {
      return new NidFloatRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidFloatRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidFloatRevision newR = new NidFloatRevision(status, time, authorNid, moduleNid, pathNid,this);

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

        buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append(" c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" floatValue:").append(this.floatValue);
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
   public float getFloat1() {
      return this.floatValue;
   }

   public float getFloatValue() {
      return floatValue;
   }

    @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_FLOAT;
   }

   @Override
   public Optional<NidFloatMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      Optional<RefexMemberVersion<NidFloatRevision, NidFloatMember>> temp =  ((NidFloatMember) primordialComponent).getVersion(c);
      return Optional.ofNullable(temp.isPresent() ? (NidFloatMemberVersion)temp.get() : null);
   }

   @Override
   public List<NidFloatMemberVersion> getVersions() {
      return ((NidFloatMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidFloatRevision>> getVersions(ViewCoordinate c) {
      return ((NidFloatMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setFloat1(float f) throws PropertyVetoException {
      this.floatValue = f;
      modified();
   }

   public void setFloatValue(float floatValue) {
      this.floatValue = floatValue;
      modified();
   }
}
