package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_long;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NidNidNidLongMember
        extends RefexMember<NidNidNidLongRevision, NidNidNidLongMember>
        implements RefexNidNidNidLongVersionBI<NidNidNidLongRevision>,
                   RefexNidNidNidLongAnalogBI<NidNidNidLongRevision> {
   private static VersionComputer<RefexMemberVersion<NidNidNidLongRevision, NidNidNidLongMember>> computer =
      new VersionComputer<>();
    protected int   nid1;
    protected int   nid2;
    protected int   nid3;
    protected long long1;

   public NidNidNidLongMember() {
      super();
   }

   public NidNidNidLongMember(TtkRefexUuidUuidUuidLongMemberChronicle refsetMember,
                               int enclosingConceptNid)
           throws IOException {
      super(refsetMember, enclosingConceptNid);
      nid1   = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      nid2   = PersistentStore.get().getNidForUuids(refsetMember.getUuid2());
      nid3   = PersistentStore.get().getNidForUuids(refsetMember.getUuid3());
      long1 = refsetMember.long1;

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<NidNidNidLongRevision, NidNidNidLongMember>(primordialStamp);

         for (TtkRefexUuidUuidUuidLongRevision eVersion :
                 refsetMember.getRevisionList()) {
            revisions.add(new NidNidNidLongRevision(eVersion, this));
         }
      }
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

      if (NidNidNidLongMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidLongMember another = (NidNidNidLongMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.long1 == another.long1)
                && (this.referencedComponentNid
                    == another.referencedComponentNid);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { nid1, nid2, nid3 });
   }

   @Override
   public NidNidNidLongRevision makeAnalog() {
      return new NidNidNidLongRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidLongRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidNidNidLongRevision newR = new NidNidNidLongRevision(status, time,
                                       authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }



   @Override
   public boolean readyToWriteRefsetMember() {
      assert nid1 != Integer.MAX_VALUE;
      assert nid2 != Integer.MAX_VALUE;
      assert nid3 != Integer.MAX_VALUE;

      return true;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidNidNidLongRevision,
           NidNidNidLongMember> obj) {
      if (NidNidNidLongMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidLongMember another = (NidNidNidLongMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.long1 == another.long1);
      }

      return false;
   }

   @Override
   public boolean refexFieldsEqual(RefexVersionBI another) {
      if (RefexNidNidNidLongVersionBI.class.isAssignableFrom(another.getClass())) {
         RefexNidNidNidLongVersionBI cv = (RefexNidNidNidLongVersionBI) another;

         return (this.nid1 == cv.getNid1()) && (this.nid2 == cv.getNid2())
                && (this.nid3 == cv.getNid3() && (this.long1 == cv.getLong1()));
      }

      return false;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append(" nid1: ");
      addNidToBuffer(buf, nid1);
      buf.append(" nid2: ");
      addNidToBuffer(buf, nid2);
      buf.append(" nid3: ");
      addNidToBuffer(buf, nid3);
      buf.append(" long1: ");
      buf.append(long1);
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
   public int getTypeNid() {
      return RefexType.CID_CID_CID_LONG.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidNidNidLongRevision,
           NidNidNidLongMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidNidNidLongMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidNidNidLongMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidNidNidLongMemberVersion(this, this));
         }

         if (revisions != null) {
            for (NidNidNidLongRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new NidNidNidLongMemberVersion(r, this));
               }
            }
         }

         versions = list;
      }

      return (List<NidNidNidLongMemberVersion>) versions;
   }

   @Override
   public void setLong1(long long1) {
      this.long1 = long1;
      modified();
   }

   @Override
   public void setNid1(int cnid1) throws PropertyVetoException {
      this.nid1 = cnid1;
      modified();
   }

   @Override
   public void setNid2(int cnid2) throws PropertyVetoException {
      this.nid2 = cnid2;
      modified();
   }

   @Override
   public void setNid3(int cnid) throws PropertyVetoException {
      this.nid3 = cnid;
      modified();
   }

}
