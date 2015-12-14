package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.Get;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import gov.vha.isaac.ochre.util.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_int.RefexNidIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntRevision;
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

public class NidIntMember extends RefexMember<NidIntRevision, NidIntMember>
        implements RefexNidIntAnalogBI<NidIntRevision> {
   private static VersionComputer<RefexMemberVersion<NidIntRevision, NidIntMember>> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   protected int c1Nid;
   protected int intValue;

   //~--- constructors --------------------------------------------------------

   public NidIntMember() {
      super();
   }

   public NidIntMember(TtkRefexUuidIntMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid    = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      intValue = refsetMember.getInt1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidIntRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidIntRevision(eVersion, this));
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
      rcs.with(ComponentProperty.INTEGER_EXTENSION_1, getInt1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidIntMember.class.isAssignableFrom(obj.getClass())) {
         NidIntMember another = (NidIntMember) obj;

         if (super.equals(another)) {
            return (this.c1Nid == another.c1Nid) && (this.intValue == another.intValue);
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public NidIntRevision makeAnalog() {
      NidIntRevision newR = new NidIntRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);

      return newR;
   }

   @Override
   public NidIntRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidIntRevision newR = new NidIntRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidIntRevision, NidIntMember> obj) {
      if (NidIntMember.class.isAssignableFrom(obj.getClass())) {
         NidIntMember another = (NidIntMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.intValue == another.intValue);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidIntVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidIntVersionBI cv = (RefexNidIntVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.intValue == cv.getInt1());
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
      buf.append("c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" intValue: ").append(this.intValue);
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
   public int getInt1() {
      return intValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_INT;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_INT.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidIntRevision, NidIntMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidIntMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidIntMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidIntMemberVersion(this, this, primordialStamp));
            for (int stampAlias : Get.commitService().getAliases(primordialStamp)) {
                list.add(new NidIntMemberVersion(this, this, stampAlias));
            }
         }

         if (revisions != null) {
            for (NidIntRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new NidIntMemberVersion(r, this, r.stamp));
                    for (int stampAlias : Get.commitService().getAliases(r.stamp)) {
                        list.add(new NidIntMemberVersion(r, this, stampAlias));
                    }
               }
            }
         }

         versions = list;
      }

      return (List<NidIntMemberVersion>) versions;
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
   public void setInt1(int l) throws PropertyVetoException {
      this.intValue = l;
      modified();
   }

}
