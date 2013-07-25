package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_long;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongRevision;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;

public class LongMember extends RefexMember<LongRevision, LongMember>
        implements RefexLongAnalogBI<LongRevision> {
   private static VersionComputer<RefexMember<LongRevision, LongMember>.Version> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   private long longValue;

   //~--- constructors --------------------------------------------------------

   public LongMember() {
      super();
   }

   public LongMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public LongMember(TtkRefexLongMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      longValue = refsetMember.getLongValue();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexLongRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new LongRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.LONG_EXTENSION_1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (LongMember.class.isAssignableFrom(obj.getClass())) {
         LongMember another = (LongMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public LongRevision makeAnalog() {
      LongRevision newR = new LongRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);

      return newR;
   }

   @Override
   public LongRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      LongRevision newR = new LongRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<LongRevision, LongMember> obj) {
      if (LongMember.class.isAssignableFrom(obj.getClass())) {
         LongMember another = (LongMember) obj;

         return this.longValue == another.longValue;
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexLongVersionBI.class.isAssignableFrom(another.getClass())){
            RefexLongVersionBI lv = (RefexLongVersionBI) another;
            return this.longValue == lv.getLong1();
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      longValue = input.readLong();
   }

   @Override
   protected final LongRevision readMemberRevision(TupleInput input) {
      return new LongRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      return true;
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName());
      buf.append(" longValue:").append(this.longValue);
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeLong(longValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public long getLong1() {
      return longValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.LONG;
   }

   @Override
   public int getTypeNid() {
      return RefexType.LONG.getTypeToken();
   }

   @Override
   protected IntArrayList getVariableVersionNids() {
      return new IntArrayList(2);
   }

   @Override
   protected VersionComputer<RefexMember<LongRevision, LongMember>.Version> getVersionComputer() {
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
            for (LongRevision lr : revisions) {
               if (lr.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(lr));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefexMember<LongRevision, LongMember>.Version
           implements RefexLongAnalogBI<LongRevision> {
      private Version(RefexLongAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      //~--- get methods ------------------------------------------------------

      RefexLongAnalogBI getCv() {
         return (RefexLongAnalogBI) cv;
      }

      @Override
      public TtkRefexLongMemberChronicle getERefsetMember() throws IOException {
         return new TtkRefexLongMemberChronicle(this);
      }

      @Override
      public TtkRefexLongRevision getERefsetRevision() throws IOException {
         return new TtkRefexLongRevision(this);
      }

      @Override
      public long getLong1() {
         return getCv().getLong1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setLong1(long l) throws PropertyVetoException {
         getCv().setLong1(l);
      }

   }
}
