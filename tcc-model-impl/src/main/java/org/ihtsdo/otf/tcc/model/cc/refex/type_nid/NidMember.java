package org.ihtsdo.otf.tcc.model.cc.refex.type_nid;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidRevision;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

public class NidMember extends RefexMember<NidRevision, NidMember>
        implements RefexNidAnalogBI<NidRevision> {
   private static VersionComputer<RefexMemberVersion<NidRevision, NidMember>> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   private int c1Nid;

   //~--- constructors --------------------------------------------------------

   public NidMember() {
      super();
   }

   public NidMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public NidMember(TtkRefexUuidMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid = P.s.getNidForUuids(refsetMember.getUuid1());

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<NidRevision, NidMember>(primordialStamp);

         for (TtkRefexUuidRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidRevision(eVersion, this));
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
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidMember.class.isAssignableFrom(obj.getClass())) {
         NidMember another = (NidMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.nid == another.nid)
                && (this.referencedComponentNid == another.referencedComponentNid);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public NidRevision makeAnalog() {
      NidRevision newR = new NidRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);

      return newR;
   }


   @Override
   public NidRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidRevision newR = new NidRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidRevision, NidMember> obj) {
      if (NidMember.class.isAssignableFrom(obj.getClass())) {
         NidMember another = (NidMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidVersionBI cv = (RefexNidVersionBI) another;
            return (this.c1Nid == cv.getNid1());
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid = input.readInt();
   }

   @Override
   protected final NidRevision readMemberRevision(TupleInput input) {
      return new NidRevision(input, this);
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

      buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append("c1Nid: ");
      addNidToBuffer(buf, c1Nid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
      ComponentVersionBI c1Component = snapshot.getComponentVersion(c1Nid);

      return super.toUserString(snapshot) + " c1: " + c1Component.toUserString(snapshot);
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
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
   protected RefexType getTkRefsetType() {
      return RefexType.CID;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID.getTypeToken();
   }

   @Override
   protected VersionComputer<RefexMemberVersion<NidRevision, NidMember>> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<NidMemberVersion> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<NidMemberVersion> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new NidMemberVersion(this, this));
         }

         if (revisions != null) {
            for (NidRevision cr : revisions) {
               if (cr.getTime() != Long.MIN_VALUE) {
                  list.add(new NidMemberVersion(cr, this));
               }
            }
         }

         versions = list;
      }

      return (List<NidMemberVersion>) versions;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setNid1(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

}
