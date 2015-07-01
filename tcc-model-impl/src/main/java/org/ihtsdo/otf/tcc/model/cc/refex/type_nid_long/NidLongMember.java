package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.Get;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NidLongMember extends RefexMember<NidLongRevision, NidLongMember>
        implements RefexNidLongAnalogBI<NidLongRevision> {
   private static VersionComputer<RefexMemberVersion<NidLongRevision, NidLongMember>> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   protected int  c1Nid;
   protected long longValue;

   //~--- constructors --------------------------------------------------------

   public NidLongMember() {
      super();
   }

   public NidLongMember(TtkRefexUuidLongMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid     = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      longValue = refsetMember.getLong1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidLongRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidLongRevision(eVersion, this));
         }
      }
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

      if (NidLongMember.class.isAssignableFrom(obj.getClass())) {
         NidLongMember another = (NidLongMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public NidLongRevision makeAnalog() {
      NidLongRevision newR = new NidLongRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);

      return newR;
   }

   @Override
   public NidLongRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidLongRevision newR = new NidLongRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidLongRevision, NidLongMember> obj) {
      if (NidLongMember.class.isAssignableFrom(obj.getClass())) {
         NidLongMember another = (NidLongMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.longValue == another.longValue);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidLongVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidLongVersionBI cv = (RefexNidLongVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.longValue == cv.getLong1());
        }
        return false;
    }

   @Override
   public boolean readyToWriteRefsetMember() {
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
      addNidToBuffer(buf, c1Nid);
      buf.append(" longValue:").append(this.longValue);
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
   public long getLong1() {
      return this.longValue;
   }

   public long getLongValue() {
      return longValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_LONG;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_LONG.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidLongRevision, NidLongMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidLongMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidLongMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidLongMemberVersion(this, this, primordialStamp));
            for (int stampAlias : Get.commitService().getAliases(primordialStamp)) {
                list.add(new NidLongMemberVersion(this, this, stampAlias));
            }

         }

         if (revisions != null) {
            for (NidLongRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new NidLongMemberVersion(r, this, r.stamp));
                    for (int stampAlias : Get.commitService().getAliases(r.stamp)) {
                        list.add(new NidLongMemberVersion(r, this, stampAlias));
                    }
               }
            }
         }

         versions = list;
      }

      return (List<NidLongMemberVersion>) versions;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setNid1(int cnid1) throws PropertyVetoException {
      this.c1Nid = cnid1;
      modified();
   }

   @Override
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }

   public void setLongValue(long longValue) {
      this.longValue = longValue;
      modified();
   }

}
