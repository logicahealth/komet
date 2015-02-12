package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_int;

//~--- non-JDK imports --------------------------------------------------------

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int.RefexNidNidNidIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int
   .RefexNidNidNidIntVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int
   .TtkRefexUuidUuidUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

public class NidNidNidIntMember
        extends RefexMember<NidNidNidIntRevision, NidNidNidIntMember>
        implements RefexNidNidNidIntVersionBI<NidNidNidIntRevision>,
                   RefexNidNidNidIntAnalogBI<NidNidNidIntRevision> {
   private static VersionComputer<RefexMemberVersion<NidNidNidIntRevision, NidNidNidIntMember>> computer =
      new VersionComputer<>();
    protected int nid1;
    protected int nid2;
    protected int nid3;
    protected int int1;

   public NidNidNidIntMember() {
      super();
   }

   public NidNidNidIntMember(TtkRefexUuidUuidUuidIntMemberChronicle refsetMember,
                             int enclosingConceptNid)
           throws IOException {
      super(refsetMember, enclosingConceptNid);
      nid1 = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      nid2 = PersistentStore.get().getNidForUuids(refsetMember.getUuid2());
      nid3 = PersistentStore.get().getNidForUuids(refsetMember.getUuid3());
      int1 = refsetMember.int1;

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidUuidUuidIntRevision eVersion :
                 refsetMember.getRevisionList()) {
            revisions.add(new NidNidNidIntRevision(eVersion, this));
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
      rcs.with(ComponentProperty.INTEGER_EXTENSION_1, getInt1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidIntMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidIntMember another = (NidNidNidIntMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.int1 == another.int1)
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
   public NidNidNidIntRevision makeAnalog() {
      return new NidNidNidIntRevision(getStatus(), getTime(),
                                      getAuthorNid(), getModuleNid(),
                                      getPathNid(), this);
   }

   @Override
   public NidNidNidIntRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidNidNidIntRevision newR = new NidNidNidIntRevision(status, time,
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
   protected boolean refexFieldsEqual(ConceptComponent<NidNidNidIntRevision,
           NidNidNidIntMember> obj) {
      if (NidNidNidIntMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidIntMember another = (NidNidNidIntMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.int1 == another.int1);
      }

      return false;
   }

   @Override
   public boolean refexFieldsEqual(RefexVersionBI another) {
      if (RefexNidNidNidIntVersionBI.class.isAssignableFrom(
              another.getClass())) {
         RefexNidNidNidIntVersionBI cv = (RefexNidNidNidIntVersionBI) another;

         return (this.nid1 == cv.getNid1()) && (this.nid2 == cv.getNid2())
                && ((this.nid3 == cv.getNid3()) && (this.int1 == cv.getInt1()));
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
      buf.append(" int1: ");
      buf.append(int1);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public int getInt1() {
      return int1;
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
      return RefexType.CID_CID_CID_INT;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_CID_CID_INT.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidNidNidIntRevision,
           NidNidNidIntMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidNidNidIntMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidNidNidIntMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidNidNidIntMemberVersion(this, this, primordialStamp));
            for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                list.add(new NidNidNidIntMemberVersion(this, this, stampAlias));
            }
         }

         if (revisions != null) {
            for (NidNidNidIntRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new NidNidNidIntMemberVersion(r, this, r.stamp));
                    for (int stampAlias : getCommitManager().getAliases(r.stamp)) {
                        list.add(new NidNidNidIntMemberVersion(r, this, stampAlias));
                    }
               }
            }
         }

         versions = list;
      }

      return (List<NidNidNidIntMemberVersion>) versions;
   }

   @Override
   public void setInt1(int int1) {
      this.int1 = int1;
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
