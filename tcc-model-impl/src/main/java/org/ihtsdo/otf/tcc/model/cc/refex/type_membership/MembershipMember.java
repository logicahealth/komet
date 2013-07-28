package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_membership;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexRevision;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_member.RefexMemberAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_member.RefexMemberVersionBI;

public class MembershipMember extends RefexMember<MembershipRevision, MembershipMember> 
    implements RefexMemberAnalogBI<MembershipRevision> {
   
    private static VersionComputer<RefexMember<MembershipRevision, MembershipMember>.Version> computer =
      new VersionComputer<>();

   //~--- constructors --------------------------------------------------------

   public MembershipMember() {
      super();
   }

   public MembershipMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public MembershipMember(TtkRefexMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new MembershipRevision(eVersion, this));
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

      // no fields to add...
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (MembershipMember.class.isAssignableFrom(obj.getClass())) {
         MembershipMember another = (MembershipMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public MembershipRevision makeAnalog() {
      MembershipRevision newR = new MembershipRevision(getStatus(), getTime(), getModuleNid(), getAuthorNid(), getPathNid(), this);

      return newR;
   }

   @Override
   public MembershipRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      MembershipRevision newR = new MembershipRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<MembershipRevision, MembershipMember> obj) {
      if (MembershipMember.class.isAssignableFrom(obj.getClass())) {
         return true;
      }

      return false;
   }
   
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexMemberVersionBI.class.isAssignableFrom(another.getClass())){
            return true;
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {

      // nothing to read...
   }

   @Override
   protected final MembershipRevision readMemberRevision(TupleInput input) {
      return new MembershipRevision(input, this);
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
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {

      // nothing to write
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.MEMBER;
   }

   @Override
   public int getTypeNid() {
      return RefexType.MEMBER.getTypeToken();
   }

   @Override
   protected IntArrayList getVariableVersionNids() {
      return new IntArrayList(2);
   }

   @Override
   protected VersionComputer<RefexMember<MembershipRevision,
           MembershipMember>.Version> getVersionComputer() {
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
            for (MembershipRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefexMember<MembershipRevision, MembershipMember>.Version
           implements RefexAnalogBI<MembershipRevision> {
      private Version(RefexAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      //~--- get methods ------------------------------------------------------

      RefexLongAnalogBI getCv() {
         return (RefexLongAnalogBI) cv;
      }

      @Override
      public TtkRefexMemberChronicle getERefsetMember() throws IOException {
         return new TtkRefexMemberChronicle(this);
      }

      @Override
      public TtkRefexRevision getERefsetRevision() throws IOException {
         return new TtkRefexRevision(this);
      }
   }
}
