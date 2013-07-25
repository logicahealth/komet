package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidRevision;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;

public class NidNidMember extends RefexMember<NidNidRevision, NidNidMember>
        implements RefexNidNidAnalogBI<NidNidRevision> {
   private static VersionComputer<RefexMember<NidNidRevision, NidNidMember>.Version> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   private int c1Nid;
   private int c2Nid;

   //~--- constructors --------------------------------------------------------

   public NidNidMember() {
      super();
   }

   public NidNidMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public NidNidMember(TtkRefexUuidUuidMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid = P.s.getNidForUuids(refsetMember.getUuid1());
      c2Nid = P.s.getNidForUuids(refsetMember.getUuid2());

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidUuidRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidNidRevision(eVersion, this));
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
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidMember.class.isAssignableFrom(obj.getClass())) {
         NidNidMember another = (NidNidMember) obj;

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
   public NidNidRevision makeAnalog() {
      return new NidNidRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public NidNidRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidNidRevision newR = new NidNidRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidNidRevision, NidNidMember> obj) {
      if (NidNidMember.class.isAssignableFrom(obj.getClass())) {
         NidNidMember another = (NidNidMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidNidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidNidVersionBI cv = (RefexNidNidVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.c2Nid == cv.getNid2());
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid = input.readInt();
      c2Nid = input.readInt();
   }

   @Override
   protected final NidNidRevision readMemberRevision(TupleInput input) {
      return new NidNidRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert c1Nid != Integer.MAX_VALUE;
      assert c2Nid != Integer.MAX_VALUE;

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
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(c2Nid);
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
   protected RefexType getTkRefsetType() {
      return RefexType.CID_CID;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_CID.getTypeToken();
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(4);

      variableNids.add(getC1Nid());
      variableNids.add(getC2Nid());

      return variableNids;
   }

   @Override
   protected VersionComputer<RefexMember<NidNidRevision, NidNidMember>.Version> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Version> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<Version> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new Version(this));
         }

         if (revisions != null) {
            for (NidNidRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
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

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefexMember<NidNidRevision, NidNidMember>.Version
           implements RefexNidNidAnalogBI<NidNidRevision> {
      private Version(RefexNidNidAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------


      //~--- get methods ------------------------------------------------------


      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      @Override
      public int getNid2() {
         return getCv().getNid2();
      }

      RefexNidNidAnalogBI getCv() {
         return (RefexNidNidAnalogBI) cv;
      }

      @Override
      public TtkRefexUuidUuidMemberChronicle getERefsetMember() throws IOException {
         return new TtkRefexUuidUuidMemberChronicle(this);
      }

      @Override
      public TtkRefexUuidUuidRevision getERefsetRevision() throws IOException {
         return new TtkRefexUuidUuidRevision(this);
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setNid1(int cnid1) throws PropertyVetoException {
         getCv().setNid1(cnid1);
      }

      @Override
      public void setNid2(int cnid2) throws PropertyVetoException {
         getCv().setNid2(cnid2);
      }
   }
}
