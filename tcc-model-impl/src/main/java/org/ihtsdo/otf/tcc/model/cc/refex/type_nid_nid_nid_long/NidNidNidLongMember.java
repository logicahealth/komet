package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid_long;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long
   .RefexNidNidNidLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long
   .RefexNidNidNidLongVersionBI;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long
   .TtkRefexUuidUuidUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long
   .TtkRefexUuidUuidUuidLongRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;

public class NidNidNidLongMember
        extends RefexMember<NidNidNidLongRevision, NidNidNidLongMember>
        implements RefexNidNidNidLongVersionBI<NidNidNidLongRevision>,
                   RefexNidNidNidLongAnalogBI<NidNidNidLongRevision> {
   private static VersionComputer<RefexMember<NidNidNidLongRevision, NidNidNidLongMember>.Version> computer =
      new VersionComputer<>();
   private int   nid1;
   private int   nid2;
   private int   nid3;
   private long long1;

   public NidNidNidLongMember() {
      super();
   }

   public NidNidNidLongMember(int enclosingConceptNid, TupleInput input)
           throws IOException {
      super(enclosingConceptNid, input);
   }

   public NidNidNidLongMember(TtkRefexUuidUuidUuidLongMemberChronicle refsetMember,
                               int enclosingConceptNid)
           throws IOException {
      super(refsetMember, enclosingConceptNid);
      nid1   = P.s.getNidForUuids(refsetMember.getUuid1());
      nid2   = P.s.getNidForUuids(refsetMember.getUuid2());
      nid3   = P.s.getNidForUuids(refsetMember.getUuid3());
      long1 = refsetMember.long1;

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidUuidUuidLongRevision eVersion :
                 refsetMember.getRevisionList()) {
            revisions.add(new NidNidNidLongRevision(eVersion, this));
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
      rcs.with(ComponentProperty.LONG_EXTENSION_1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidNidNidLongMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidLongMember another = (NidNidNidLongMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.long1 == another.long1)
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
   public NidNidNidLongRevision makeAnalog() {
      return new NidNidNidLongRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidLongRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidNidNidLongRevision newR = new NidNidNidLongRevision(status, time,
                                       authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected void readMemberFields(TupleInput input) {
      nid1 = input.readInt();
      nid2 = input.readInt();
      nid3 = input.readInt();
      long1 = input.readLong();
   }

   @Override
   protected final NidNidNidLongRevision readMemberRevision(TupleInput input) {
      return new NidNidNidLongRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert nid1 != Integer.MAX_VALUE;
      assert nid2 != Integer.MAX_VALUE;
      assert nid3 != Integer.MAX_VALUE;

      return true;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidNidNidLongRevision,
           NidNidNidLongMember> obj) {
      if (NidNidNidLongMember.class.isAssignableFrom(obj.getClass())) {
         NidNidNidLongMember another = (NidNidNidLongMember) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3) && (this.long1 == another.long1);
      }

      return false;
   }

   @Override
   public boolean refexFieldsEqual(RefexVersionBI another) {
      if (RefexNidNidNidLongVersionBI.class.isAssignableFrom(another.getClass())) {
         RefexNidNidNidLongVersionBI cv = (RefexNidNidNidLongVersionBI) another;

         return (this.nid1 == cv.getNid1()) && (this.nid2 == cv.getNid2())
                && (this.nid3 == cv.getNid3() && (this.long1 == cv.getLong1()));
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
      buf.append(" long1: ");
      buf.append(long1);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(nid1);
      output.writeInt(nid2);
      output.writeInt(nid3);
      output.writeLong(long1);
   }

   @Override
   public long getLong1() {
      return long1;
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
      return RefexType.CID_CID_CID_LONG;
   }

   @Override
   public int getTypeNid() {
      return RefexType.CID_CID_CID_LONG.getTypeToken();
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
   protected VersionComputer<RefexMember<NidNidNidLongRevision,
           NidNidNidLongMember>.Version> getVersionComputer() {
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
            for (NidNidNidLongRevision r : revisions) {
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
   public void setLong1(long long1) {
      this.long1 = long1;
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
           extends RefexMember<NidNidNidLongRevision,
                               NidNidNidLongMember>.Version
           implements RefexNidNidNidLongVersionBI<NidNidNidLongRevision> {
      private Version(RefexNidNidNidLongAnalogBI cv) {
         super(cv);
      }

      RefexNidNidNidLongAnalogBI getCv() {
         return (RefexNidNidNidLongAnalogBI) cv;
      }

      @Override
      public TtkRefexUuidUuidUuidLongMemberChronicle getERefsetMember()
              throws IOException {
         return new TtkRefexUuidUuidUuidLongMemberChronicle(this);
      }

      @Override
      public TtkRefexUuidUuidUuidLongRevision getERefsetRevision()
              throws IOException {
         return new TtkRefexUuidUuidUuidLongRevision(this);
      }

      @Override
      public long getLong1() {
         return getCv().getLong1();
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
