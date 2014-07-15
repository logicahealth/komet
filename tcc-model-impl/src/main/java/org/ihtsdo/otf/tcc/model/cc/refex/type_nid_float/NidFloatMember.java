package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_float;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_float.RefexNidFloatAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatRevision;
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

public class NidFloatMember extends RefexMember<NidFloatRevision, NidFloatMember>
        implements RefexNidFloatAnalogBI<NidFloatRevision> {
   private static VersionComputer<RefexMemberVersion<NidFloatRevision, NidFloatMember>> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

    protected int   c1Nid;
    protected float floatValue;

   //~--- constructors --------------------------------------------------------

   public NidFloatMember() {
      super();
   }

   public NidFloatMember(TtkRefexUuidFloatMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid      = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
      floatValue = refsetMember.getFloatValue();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<NidFloatRevision, NidFloatMember>(primordialStamp);

         for (TtkRefexUuidFloatRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidFloatRevision(eVersion, this));
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
      rcs.with(ComponentProperty.FLOAT_EXTENSION_1, getFloat1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidFloatMember.class.isAssignableFrom(obj.getClass())) {
         NidFloatMember another = (NidFloatMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public NidFloatRevision makeAnalog() {
      return new NidFloatRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidFloatRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidFloatRevision newR = new NidFloatRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidFloatRevision, NidFloatMember> obj) {
      if (NidFloatMember.class.isAssignableFrom(obj.getClass())) {
         NidFloatMember another = (NidFloatMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.floatValue == another.floatValue);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidFloatVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidFloatVersionBI cv = (RefexNidFloatVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.floatValue == cv.getFloat1());
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
      buf.append(" floatValue:").append(this.floatValue);
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
   public float getFloat1() {
      return this.floatValue;
   }

   public float getFloatValue() {
      return floatValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_FLOAT;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_FLOAT.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidFloatRevision, NidFloatMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidFloatMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidFloatMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidFloatMemberVersion(this, this));
         }

         if (revisions != null) {
            for (NidFloatRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new NidFloatMemberVersion(r, this));
               }
            }
         }

         versions = list;
      }

      return (List<NidFloatMemberVersion>) versions;
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
   public void setFloat1(float f) throws PropertyVetoException {
      this.floatValue = f;
      modified();
   }

   public void setFloatValue(float floatValue) {
      this.floatValue = floatValue;
      modified();
   }

}
