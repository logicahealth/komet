package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_float;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatRevision;
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

public class NidNidNidFloatMember
        extends RefexMember<NidNidNidFloatRevision, NidNidNidFloatMember>
        implements RefexNidNidNidFloatVersionBI<NidNidNidFloatRevision>,
                   RefexNidNidNidFloatAnalogBI<NidNidNidFloatRevision> {
   private static VersionComputer<RefexMemberVersion<NidNidNidFloatRevision, NidNidNidFloatMember>> computer =
      new VersionComputer<>();
    protected int   nid1;
    protected int   nid2;
    protected int   nid3;
    protected float float1;

   public NidNidNidFloatMember() {
      super();
   }

   public NidNidNidFloatMember(TtkRefexUuidUuidUuidFloatMemberChronicle refsetMember,
                               int enclosingConceptNid)
           throws IOException {
      super(refsetMember, enclosingConceptNid);
      nid1   = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      nid2   = PersistentStore.get().getNidForUuids(refsetMember.getUuid2());
      nid3   = PersistentStore.get().getNidForUuids(refsetMember.getUuid3());
      float1 = refsetMember.float1;

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidUuidUuidFloatRevision eVersion :
                 refsetMember.getRevisionList()) {
            revisions.add(new NidNidNidFloatRevision(eVersion, this));
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
      rcs.with(ComponentProperty.FLOAT_EXTENSION_1, getFloat1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidFloatMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidFloatMember another = (NidNidNidFloatMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.float1 == another.float1)
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
   public NidNidNidFloatRevision makeAnalog() {
      return new NidNidNidFloatRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidFloatRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidNidNidFloatRevision newR = new NidNidNidFloatRevision(status, time,
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
   protected boolean refexFieldsEqual(ConceptComponent<NidNidNidFloatRevision,
           NidNidNidFloatMember> obj) {
      if (NidNidNidFloatMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidFloatMember another = (NidNidNidFloatMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.float1 == another.float1);
      }

      return false;
   }

   @Override
   public boolean refexFieldsEqual(RefexVersionBI another) {
      if (RefexNidNidNidFloatVersionBI.class.isAssignableFrom(another.getClass())) {
         RefexNidNidNidFloatVersionBI cv = (RefexNidNidNidFloatVersionBI) another;

         return (this.nid1 == cv.getNid1()) && (this.nid2 == cv.getNid2())
                && (this.nid3 == cv.getNid3() && (this.float1 == cv.getFloat1()));
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
      buf.append(" float1: ");
      buf.append(float1);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public float getFloat1() {
      return float1;
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
      return RefexType.CID_CID_CID_FLOAT;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_CID_CID_FLOAT.getTypeToken();
   }
   
   @Override
   protected VersionComputer<RefexMemberVersion<NidNidNidFloatRevision,
           NidNidNidFloatMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidNidNidFloatMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidNidNidFloatMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidNidNidFloatMemberVersion(this, this, primordialStamp));
            for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                list.add(new NidNidNidFloatMemberVersion(this, this, stampAlias));
            }
         }

         if (revisions != null) {
            for (NidNidNidFloatRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new NidNidNidFloatMemberVersion(r, this, r.stamp));
                  for (int stampAlias : getCommitManager().getAliases(r.stamp)) {
                        list.add(new NidNidNidFloatMemberVersion(this, this, stampAlias));
                  }
               }
            }
         }

         versions = list;
      }

      return (List<NidNidNidFloatMemberVersion>) versions;
   }

   @Override
   public void setFloat1(float float1) {
      this.float1 = float1;
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
