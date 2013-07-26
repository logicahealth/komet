package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_string;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringRevision;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;

public class NidStringMember extends RefexMember<NidStringRevision, NidStringMember>
        implements RefexNidStringAnalogBI<NidStringRevision> {
   private static VersionComputer<RefexMember<NidStringRevision, NidStringMember>.Version> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   private int    c1Nid;
   private String string1;

   //~--- constructors --------------------------------------------------------

   public NidStringMember() {
      super();
   }

   public NidStringMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public NidStringMember(TtkRefexUuidStringMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid    = P.s.getNidForUuids(refsetMember.getUuid1());
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
   protected void readMemberFields(TupleInput input) {
      c1Nid    = input.readInt();
      string1 = input.readString();
   }

   @Override
   protected final NidStringRevision readMemberRevision(TupleInput input) {
      return new NidStringRevision(input, this);
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
      buf.append(" strValue:" + "'").append(this.string1).append("'");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeString(string1);
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
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(3);

      variableNids.add(getNid1());

      return variableNids;
   }

   @Override
   protected VersionComputer<RefexMember<NidStringRevision, NidStringMember>.Version> getVersionComputer() {
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
            for (RefexNidStringAnalogBI r : revisions) {
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

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefexMember<NidStringRevision, NidStringMember>.Version
           implements RefexNidStringAnalogBI<NidStringRevision> {
      private Version(RefexNidStringAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      //~--- get methods ------------------------------------------------------

        @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      RefexNidStringAnalogBI getCv() {
         return (RefexNidStringAnalogBI) cv;
      }

      @Override
      public TtkRefexUuidStringMemberChronicle getERefsetMember() throws IOException {
         return new TtkRefexUuidStringMemberChronicle(this);
      }

      @Override
      public TtkRefexUuidStringRevision getERefsetRevision() throws IOException {
         return new TtkRefexUuidStringRevision(this);
      }

      @Override
      public String getString1() {
         return getCv().getString1();
      }

      //~--- set methods ------------------------------------------------------

  
      @Override
      public void setNid1(int c1id) throws PropertyVetoException {
         getCv().setNid1(c1id);
      }

      @Override
      public void setString1(String value) throws PropertyVetoException {
         getCv().setString1(value);
      }
   }
}
