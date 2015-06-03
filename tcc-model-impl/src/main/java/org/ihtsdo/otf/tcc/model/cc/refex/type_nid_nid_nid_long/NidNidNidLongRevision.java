package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_long;

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
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

public class NidNidNidLongRevision
        extends RefexRevision<NidNidNidLongRevision, NidNidNidLongMember>
        implements RefexNidNidNidLongAnalogBI<NidNidNidLongRevision> {
    protected int   nid1;
    protected int   nid2;
    protected int   nid3;
    protected long long1;

   public NidNidNidLongRevision() {
      super();
   }

   public NidNidNidLongRevision(int statusAtPositionNid,
                                 NidNidNidLongMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      long1 = primoridalMember.getLong1();
   }

   public NidNidNidLongRevision(TtkRefexUuidUuidUuidLongRevision eVersion,
                                 NidNidNidLongMember member)
           throws IOException {
      super(eVersion, member);
      nid1  = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      nid2  = PersistentStore.get().getNidForUuids(eVersion.getUuid2());
      nid3  = PersistentStore.get().getNidForUuids(eVersion.getUuid3());
      long1 = eVersion.getLong1();
   }

   public NidNidNidLongRevision(Status status, long time, int authorNid,
                                 int moduleNid, int pathNid,
                                 NidNidNidLongMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      long1 = primoridalMember.getLong1();
   }

   protected NidNidNidLongRevision(Status status, long time, int authorNid,
                                    int moduleNid, int pathNid,
                                    NidNidNidLongRevision another) {
      super(status, time, authorNid, moduleNid, pathNid,
            another.primordialComponent);
      nid1  = another.nid1;
      nid2  = another.nid2;
      nid3  = another.nid3;
      long1 = another.long1;
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
      rcs.with(ComponentProperty.LONG_EXTENSION_1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidLongRevision.class.isAssignableFrom(obj.getClass())) {
         NidNidNidLongRevision another = (NidNidNidLongRevision) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3)
                && (this.long1 == another.long1) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidNidNidLongRevision makeAnalog() {
      return new NidNidNidLongRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidLongRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidNidNidLongRevision newR = new NidNidNidLongRevision(status, time,
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
      buf.append(" long1: ").append(long1);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public long getLong1() {
      return long1;
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
      return RefexType.CID_CID_CID_LONG;
   }

   @Override
   public Optional<NidNidNidLongMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      Optional<RefexMemberVersion<NidNidNidLongRevision, NidNidNidLongMember>> temp =  ((NidNidNidLongMember) primordialComponent).getVersion(c);
      return Optional.ofNullable(temp.isPresent() ? (NidNidNidLongMemberVersion)temp.get() : null);
   }

   @Override
   public List<NidNidNidLongMemberVersion> getVersions() {
      return ((NidNidNidLongMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidNidNidLongRevision>> getVersions(
           ViewCoordinate c) {
      return ((NidNidNidLongMember) primordialComponent).getVersions(c);
   }

   @Override
   public void setLong1(long long1) {
      this.long1 = long1;
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
