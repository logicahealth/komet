package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.Get;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringRevision;
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

public class NidStringMember extends RefexMember<NidStringRevision, NidStringMember>
        implements RefexNidStringAnalogBI<NidStringRevision> {
   private static VersionComputer<RefexMemberVersion<NidStringRevision, NidStringMember>> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

    protected int    c1Nid;
    protected String string1;

   //~--- constructors --------------------------------------------------------

   public NidStringMember() {
      super();
   }

   public NidStringMember(TtkRefexUuidStringMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid    = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      string1 = refsetMember.getString1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidStringRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidStringRevision(eVersion, this));
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
      rcs.with(ComponentProperty.STRING_EXTENSION_1, getString1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidStringMember.class.isAssignableFrom(obj.getClass())) {
         NidStringMember another = (NidStringMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public NidStringRevision makeAnalog() {
      NidStringRevision newR = new NidStringRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);

      return newR;
   }

   @Override
   public NidStringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidStringRevision newR = new NidStringRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidStringRevision, NidStringMember> obj) {
      if (NidStringMember.class.isAssignableFrom(obj.getClass())) {
         NidStringMember another = (NidStringMember) obj;

         return (this.c1Nid == another.c1Nid) && this.string1.equals(another.string1);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidStringVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidStringVersionBI cv = (RefexNidStringVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && this.string1.equals(cv.getString1());
        }
        return false;
    }

   @Override
   public boolean readyToWriteRefsetMember() {
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

      buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append(" c1Nid: ");
      addNidToBuffer(buf, c1Nid);
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
   public int getTypeNid() {
      return RefexType.CID_STR.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidStringRevision, NidStringMember>> getVersionComputer() {
      return computer;
   }

    @SuppressWarnings("unchecked")
    @Override
    public List<NidStringMemberVersion> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<NidStringMemberVersion> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new NidStringMemberVersion(this, this, primordialStamp));
                for (int stampAlias : Get.commitService().getAliases(primordialStamp)) {
                    list.add(new NidStringMemberVersion(this, this, stampAlias));
                }
            }

            if (revisions != null) {
                for (RefexNidStringAnalogBI r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new NidStringMemberVersion(r, this, r.getStamp()));
                        for (int stampAlias : Get.commitService().getAliases(r.getStamp())) {
                            list.add(new NidStringMemberVersion(r, this, stampAlias));
                        }
                    }
                }
            }

            versions = list;
        }

        return (List<NidStringMemberVersion>) versions;
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
   public void setString1(String str) throws PropertyVetoException {
      this.string1 = str;
      modified();
   }

}
