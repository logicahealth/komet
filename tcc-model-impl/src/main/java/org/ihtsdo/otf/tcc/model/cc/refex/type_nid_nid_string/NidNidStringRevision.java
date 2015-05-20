package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string;

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
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_string.RefexNidNidStringAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

public class NidNidStringRevision extends RefexRevision<NidNidStringRevision, NidNidStringMember>
        implements RefexNidNidStringAnalogBI<NidNidStringRevision> {
    protected int    c1Nid;
    protected int    c2Nid;
    protected String string1;

   //~--- constructors --------------------------------------------------------

   public NidNidStringRevision() {
      super();
   }

   public NidNidStringRevision(int statusAtPositionNid, NidNidStringMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      c2Nid    = primoridalMember.getC2Nid();
      string1 = primoridalMember.getString1();
   }

   public NidNidStringRevision(TtkRefexUuidUuidStringRevision eVersion, NidNidStringMember member) throws IOException {
      super(eVersion, member);
      c1Nid    = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      c2Nid    = PersistentStore.get().getNidForUuids(eVersion.getUuid2());
      string1 = eVersion.getString1();
   }


   public NidNidStringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                            NidNidStringMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      c2Nid    = primoridalMember.getC2Nid();
      string1 = primoridalMember.getString1();
   }

   protected NidNidStringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                               NidNidStringRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid    = another.c1Nid;
      c2Nid    = another.c2Nid;
      string1 = another.string1;
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
      rcs.with(ComponentProperty.STRING_EXTENSION_1, getString1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidStringRevision.class.isAssignableFrom(obj.getClass())) {
         NidNidStringRevision another = (NidNidStringRevision) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                && this.string1.equals(another.string1) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidNidStringRevision makeAnalog() {
      return new NidNidStringRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidNidStringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidNidStringRevision newR = new NidNidStringRevision(status, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert c2Nid != Integer.MAX_VALUE;
      assert string1 != null;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

        buf.append(this.getClass().getSimpleName()).append(" ");
        buf.append(" c1Nid:").append(this.c1Nid);
        buf.append(" c2Nid:").append(this.c2Nid);
        buf.append(" string1:" + "'").append(this.string1).append("'");
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
   public String getString1() {
      return this.string1;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_CID_STR;
   }

   @Override
   public Optional<NidNidStringMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      Optional<RefexMemberVersion<NidNidStringRevision, NidNidStringMember>> temp =  ((NidNidStringMember) primordialComponent).getVersion(c);
      return Optional.ofNullable(temp.isPresent() ? (NidNidStringMemberVersion)temp.get() : null);
   }

   @Override
   public List<NidNidStringMemberVersion> getVersions() {
      return ((NidNidStringMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidNidStringRevision>> getVersions(ViewCoordinate c) {
      return ((NidNidStringMember) primordialComponent).getVersions(c);
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

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.string1 = str;
      modified();
   }
}
