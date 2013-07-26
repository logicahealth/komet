package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid_string;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string
   .RefexNidNidNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string
   .RefexNidNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string
   .TtkRefexUuidUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string
   .TtkRefexUuidUuidUuidStringRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;

public class NidNidNidStringMember
        extends RefexMember<NidNidNidStringRevision, NidNidNidStringMember>
        implements RefexNidNidNidStringVersionBI<NidNidNidStringRevision>,
                   RefexNidNidNidStringAnalogBI<NidNidNidStringRevision> {
   private static VersionComputer<RefexMember<NidNidNidStringRevision, NidNidNidStringMember>.Version> computer =
      new VersionComputer<>();
   private int   nid1;
   private int   nid2;
   private int   nid3;
   private String string1;

   public NidNidNidStringMember() {
      super();
   }

   public NidNidNidStringMember(int enclosingConceptNid, TupleInput input)
           throws IOException {
      super(enclosingConceptNid, input);
   }

   public NidNidNidStringMember(TtkRefexUuidUuidUuidStringMemberChronicle refsetMember,
                               int enclosingConceptNid)
           throws IOException {
      super(refsetMember, enclosingConceptNid);
      nid1   = P.s.getNidForUuids(refsetMember.getUuid1());
      nid2   = P.s.getNidForUuids(refsetMember.getUuid2());
      nid3   = P.s.getNidForUuids(refsetMember.getUuid3());
      string1 = refsetMember.string1;

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidUuidUuidStringRevision eVersion :
                 refsetMember.getRevisionList()) {
            revisions.add(new NidNidNidStringRevision(eVersion, this));
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
      rcs.with(ComponentProperty.STRING_EXTENSION_1, getString1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidStringMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidStringMember another = (NidNidNidStringMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.string1 == another.string1)
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
   public NidNidNidStringRevision makeAnalog() {
      return new NidNidNidStringRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidStringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidNidNidStringRevision newR = new NidNidNidStringRevision(status, time,
                                       authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected void readMemberFields(TupleInput input) {
      nid1 = input.readInt();
      nid2 = input.readInt();
      nid3 = input.readInt();
      string1 = input.readString();
   }

   @Override
   protected final NidNidNidStringRevision readMemberRevision(TupleInput input) {
      return new NidNidNidStringRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert nid1 != Integer.MAX_VALUE;
      assert nid2 != Integer.MAX_VALUE;
      assert nid3 != Integer.MAX_VALUE;

      return true;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidNidNidStringRevision,
           NidNidNidStringMember> obj) {
      if (NidNidNidStringMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidStringMember another = (NidNidNidStringMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.string1 == another.string1);
      }

      return false;
   }

   @Override
   public boolean refexFieldsEqual(RefexVersionBI another) {
      if (RefexNidNidNidStringVersionBI.class.isAssignableFrom(another.getClass())) {
         RefexNidNidNidStringVersionBI cv = (RefexNidNidNidStringVersionBI) another;

         return (this.nid1 == cv.getNid1()) && (this.nid2 == cv.getNid2())
                && (this.nid3 == cv.getNid3() && (this.string1 == cv.getString1()));
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
      buf.append(" string1: ");
      buf.append(string1);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(nid1);
      output.writeInt(nid2);
      output.writeInt(nid3);
      output.writeString(string1);
   }

   @Override
   public String getString1() {
      return string1;
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
      return RefexType.CID_CID_CID_STRING;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_CID_CID_STRING.getTypeToken();
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(5);

      variableNids.add(nid1);
      variableNids.add(nid2);
      variableNids.add(nid3);

      return variableNids;
   }

   @Override
   protected VersionComputer<RefexMember<NidNidNidStringRevision,
           NidNidNidStringMember>.Version> getVersionComputer() {
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
            for (NidNidNidStringRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   @Override
   public void setString1(String string1) {
      this.string1 = string1;
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

   public class Version
           extends RefexMember<NidNidNidStringRevision,
                               NidNidNidStringMember>.Version
           implements RefexNidNidNidStringVersionBI<NidNidNidStringRevision> {
      private Version(RefexNidNidNidStringAnalogBI cv) {
         super(cv);
      }

      RefexNidNidNidStringAnalogBI getCv() {
         return (RefexNidNidNidStringAnalogBI) cv;
      }

      @Override
      public TtkRefexUuidUuidUuidStringMemberChronicle getERefsetMember()
              throws IOException {
         return new TtkRefexUuidUuidUuidStringMemberChronicle(this);
      }

      @Override
      public TtkRefexUuidUuidUuidStringRevision getERefsetRevision()
              throws IOException {
         return new TtkRefexUuidUuidUuidStringRevision(this);
      }

      @Override
      public String getString1() {
         return getCv().getString1();
      }

      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      @Override
      public int getNid2() {
         return getCv().getNid2();
      }

      @Override
      public int getNid3() {
         return getCv().getNid3();
      }
   }
}
