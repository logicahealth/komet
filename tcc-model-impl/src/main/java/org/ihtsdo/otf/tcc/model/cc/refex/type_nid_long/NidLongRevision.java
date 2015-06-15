package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long;

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
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

public class NidLongRevision extends RefexRevision<NidLongRevision, NidLongMember>
        implements RefexNidLongAnalogBI<NidLongRevision> {
   protected int  c1Nid;
   protected long longValue;

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
      c1Nid     = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
      longValue = eVersion.getLong1();
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
   
   public NidLongRevision(RefexNidLongAnalogBI another, Status status, long time, int authorNid,
            int moduleNid, int pathNid, NidLongMember primoridalMember) {
        super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
        this.c1Nid = another.getNid1();
        this.longValue = another.getLong1();
    }
    
    public NidLongRevision(RefexNidLongAnalogBI another, NidLongMember primordialMember){
        super(another.getStatus(), another.getTime(), another.getAuthorNid(), another.getModuleNid(),
              another.getPathNid(), primordialMember);
        this.c1Nid = another.getNid1();
        this.longValue = another.getLong1();
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
   public Optional<NidLongMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      Optional<RefexMemberVersion<NidLongRevision, NidLongMember>> temp =  ((NidLongMember) primordialComponent).getVersion(c);
      return Optional.ofNullable(temp.isPresent() ? (NidLongMemberVersion)temp.get() : null);
   }

   @Override
   public List<NidLongMemberVersion> getVersions() {
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
