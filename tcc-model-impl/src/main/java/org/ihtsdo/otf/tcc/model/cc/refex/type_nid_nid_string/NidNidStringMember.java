package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_string.RefexNidNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringRevision;
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

public class NidNidStringMember extends RefexMember<NidNidStringRevision, NidNidStringMember>
        implements RefexNidNidStringAnalogBI<NidNidStringRevision> {
   private static VersionComputer<RefexMemberVersion<NidNidStringRevision, NidNidStringMember>> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

    protected int    c1Nid;
    protected int    c2Nid;
    protected String string1;

   //~--- constructors --------------------------------------------------------

   public NidNidStringMember() {
      super();
   }

   public NidNidStringMember(TtkRefexUuidUuidStringMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid    = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      c2Nid    = PersistentStore.get().getNidForUuids(refsetMember.getUuid2());
      string1 = refsetMember.getString1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidUuidStringRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidNidStringRevision(eVersion, this));
         }
      }
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

      if (NidNidStringMember.class.isAssignableFrom(obj.getClass())) {
         NidNidStringMember another = (NidNidStringMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid) && (this.nid == another.nid)
                && (this.referencedComponentNid == another.referencedComponentNid);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid, c2Nid });
   }

   @Override
   public NidNidStringRevision makeAnalog() {
      return new NidNidStringRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidNidStringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidNidStringRevision newR = new NidNidStringRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidNidStringRevision, NidNidStringMember> obj) {
      if (NidNidStringMember.class.isAssignableFrom(obj.getClass())) {
         NidNidStringMember another = (NidNidStringMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                && this.string1.equals(another.string1);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidNidStringVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidNidStringVersionBI cv = (RefexNidNidStringVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.c2Nid == cv.getNid2())
                    && this.string1.equals(cv.getString1());
        }
        return false;
    }

   @Override
   public boolean readyToWriteRefsetMember() {
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
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append(" c1Nid: ");
      addNidToBuffer(buf, c1Nid);
      buf.append(" c2Nid: ");
      addNidToBuffer(buf, c2Nid);
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

   public String getStrValue() {
      return string1;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_CID_STR;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_CID_STR.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidNidStringRevision, NidNidStringMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidNidStringMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidNidStringMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidNidStringMemberVersion(this, this, primordialStamp));
                for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                    list.add(new NidNidStringMemberVersion(this, this, stampAlias));
                }
         }

         if (revisions != null) {
            for (RefexNidNidStringAnalogBI r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new NidNidStringMemberVersion(r, this, r.getStamp()));
                        for (int stampAlias : getCommitManager().getAliases(r.getStamp())) {
                            list.add(new NidNidStringMemberVersion(r, this, stampAlias));
                        }
               }
            }
         }

         versions = list;
      }

      return (List<NidNidStringMemberVersion>) versions;
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
   public void setNid1(int cnid1) throws PropertyVetoException {
      this.c1Nid = cnid1;
      modified();
   }

   @Override
   public void setNid2(int cnid2) throws PropertyVetoException {
      this.c2Nid = cnid2;
      modified();
   }

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.string1 = str;
      modified();
   }

   public void setStrValue(String strValue) {
      this.string1 = strValue;
      modified();
   }

}
