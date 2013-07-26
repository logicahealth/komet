package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntRevision;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;

public class IntMember extends RefexMember<IntRevision, IntMember>
        implements RefexIntAnalogBI<IntRevision> {
   private static VersionComputer<RefexMember<IntRevision, IntMember>.Version> computer =
      new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   private int int1;

   //~--- constructors --------------------------------------------------------

   public IntMember() {
      super();
   }

   public IntMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public IntMember(TtkRefexIntMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      int1 = refsetMember.getIntValue();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexIntRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new IntRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.INTEGER_EXTENSION_1, this.int1);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IntMember.class.isAssignableFrom(obj.getClass())) {
         IntMember another = (IntMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public IntRevision makeAnalog() {
      IntRevision newR = new IntRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);

      return newR;
   }

   @Override
   public IntRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      IntRevision newR = new IntRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<IntRevision, IntMember> obj) {
      if (IntMember.class.isAssignableFrom(obj.getClass())) {
         IntMember another = (IntMember) obj;

         return this.int1 == another.int1;
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexIntVersionBI.class.isAssignableFrom(another.getClass())){
            RefexIntVersionBI iv = (RefexIntVersionBI) another;
            return this.int1 == iv.getInt1();
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      int1 = input.readInt();
   }

   @Override
   protected final IntRevision readMemberRevision(TupleInput input) {
      return new IntRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append(this.int1);
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(int1);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getInt1() {
      return int1;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.INT;
   }

   @Override
   public int getTypeNid() {
      return RefexType.INT.getTypeToken();
   }

   @Override
   protected IntArrayList getVariableVersionNids() {
      return new IntArrayList(2);
   }

   @Override
   protected VersionComputer<RefexMember<IntRevision, IntMember>.Version> getVersionComputer() {
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
            for (RefexIntAnalogBI r : revisions) {
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

   @Override
   public void setInt1(int intValue) throws PropertyVetoException {
      this.int1 = intValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefexMember<IntRevision, IntMember>.Version
           implements RefexIntAnalogBI<IntRevision> {
      private Version(RefexIntAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------



      //~--- get methods ------------------------------------------------------

      RefexIntAnalogBI getCv() {
         return (RefexIntAnalogBI) cv;
      }

      @Override
      public TtkRefexIntMemberChronicle getERefsetMember() throws IOException {
         return new TtkRefexIntMemberChronicle(this);
      }

      @Override
      public TtkRefexIntRevision getERefsetRevision() throws IOException {
         return new TtkRefexIntRevision(this);
      }

      @Override
      public int getInt1() {
         return getCv().getInt1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setInt1(int value) throws PropertyVetoException {
         getCv().setInt1(value);
      }
   }
}
