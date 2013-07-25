package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_boolean;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_boolean.RefexNidBooleanAnalogBI;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.refex.type_nid_boolean.RefexNidBooleanVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanRevision;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/03/27
 * @author         Enter your name here...    
 */
public class NidBooleanMember extends RefexMember<NidBooleanRevision, NidBooleanMember>
        implements RefexNidBooleanAnalogBI<NidBooleanRevision> {

   /** Field description */
   private static VersionComputer<RefexMember<NidBooleanRevision, NidBooleanMember>.Version> computer =
      new VersionComputer<>();

   /** Field description */
   private int c1Nid;

   /** Field description */
   private boolean boolean1;

   /**
    * Constructs ...
    *
    */
   public NidBooleanMember() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param enclosingConceptNid
    * @param input
    *
    * @throws IOException
    */
   public NidBooleanMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   /**
    * Constructs ...
    *
    *
    * @param refsetMember
    * @param enclosingConceptNid
    *
    * @throws IOException
    */
   public NidBooleanMember(TtkRefexUuidBooleanMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid      = P.s.getNidForUuids(refsetMember.getUuid1());
      boolean1 = refsetMember.boolean1;

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRefexUuidBooleanRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new NidBooleanRevision(eVersion, this));
         }
      }
   }

   /**
    * Method description
    *
    *
    * @param allNids
    */
   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   /**
    * Method description
    *
    *
    * @param rcs
    */
   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
      rcs.with(ComponentProperty.BOOLEAN_EXTENSION_1, getBoolean1());
   }

   /**
    * Method description
    *
    *
    * @param obj
    *
    * @return
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (NidBooleanMember.class.isAssignableFrom(obj.getClass())) {
         NidBooleanMember another = (NidBooleanMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public NidBooleanRevision makeAnalog() {
      return new NidBooleanRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),
                                    this);
   }

   /**
    * Method description
    *
    *
    * @param statusNid
    * @param time
    * @param authorNid
    * @param moduleNid
    * @param pathNid
    *
    * @return
    */
   @Override
   public NidBooleanRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      NidBooleanRevision newR = new NidBooleanRevision(status, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   /**
    * Method description
    *
    *
    * @param input
    */
   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid      = input.readInt();
      boolean1 = input.readBoolean();
   }

   /**
    * Method description
    *
    *
    * @param input
    *
    * @return
    */
   @Override
   protected final NidBooleanRevision readMemberRevision(TupleInput input) {
      return new NidBooleanRevision(input, this);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public boolean readyToWriteRefsetMember() {
      assert c1Nid != Integer.MAX_VALUE;

      return true;
   }

   /**
    * Method description
    *
    *
    * @param obj
    *
    * @return
    */
   @Override
   protected boolean refexFieldsEqual(ConceptComponent<NidBooleanRevision, NidBooleanMember> obj) {
      if (NidBooleanMember.class.isAssignableFrom(obj.getClass())) {
         NidBooleanMember another = (NidBooleanMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.boolean1 == another.boolean1);
      }

      return false;
   }

   /**
    * Method description
    *
    *
    * @param another
    *
    * @return
    */
   @Override
   public boolean refexFieldsEqual(RefexVersionBI another) {
      if (RefexNidBooleanVersionBI.class.isAssignableFrom(another.getClass())) {
         RefexNidBooleanVersionBI cv = (RefexNidBooleanVersionBI) another;

         return (this.c1Nid == cv.getNid1()) && (this.boolean1 == cv.getBoolean1());
      }

      return false;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" c1Nid: ");
      addNidToBuffer(buf, c1Nid);
      buf.append(" boolean1:").append(this.boolean1);
      buf.append(super.toString());

      return buf.toString();
   }

   /**
    * Method description
    *
    *
    * @param output
    */
   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeBoolean(boolean1);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public boolean getBoolean1() {
      return boolean1;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public int getC1Nid() {
      return c1Nid;
   }


   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public int getNid1() {
      return c1Nid;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_BOOLEAN;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public int getTypeNid() {
      return RefexType.CID_BOOLEAN.getTypeToken();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(3);

      variableNids.add(getC1Nid());

      return variableNids;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   protected VersionComputer<RefexMember<NidBooleanRevision, NidBooleanMember>.Version> getVersionComputer() {
      return computer;
   }

   /**
    * Method description
    *
    *
    * @return
    */
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
            for (NidBooleanRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   /**
    * Method description
    *
    *
    * @param boolean1
    */
   @Override
   public void setBoolean1(boolean boolean1) {
      this.boolean1 = boolean1;
   }

   /**
    * Method description
    *
    *
    * @param c1Nid
    */
   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }



   /**
    * Method description
    *
    *
    * @param cnid
    *
    * @throws PropertyVetoException
    */
   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/03/27
    * @author         Enter your name here...    
    */
   public class Version extends RefexMember<NidBooleanRevision, NidBooleanMember>.Version
           implements RefexNidBooleanAnalogBI<NidBooleanRevision> {

      /**
       * Constructs ...
       *
       *
       * @param cv
       */
      private Version(RefexNidBooleanAnalogBI cv) {
         super(cv);
      }

      /**
       * Method description
       *
       *
       * @return
       */
      RefexNidBooleanAnalogBI getCv() {
         return (RefexNidBooleanAnalogBI) cv;
      }

      /**
       * Method description
       *
       *
       * @return
       *
       * @throws IOException
       */
      @Override
      public TtkRefexUuidFloatMemberChronicle getERefsetMember() throws IOException {
         return new TtkRefexUuidFloatMemberChronicle(this);
      }

      /**
       * Method description
       *
       *
       * @return
       *
       * @throws IOException
       */
      @Override
      public TtkRefexUuidBooleanRevision getERefsetRevision() throws IOException {
         return new TtkRefexUuidBooleanRevision(this);
      }

      /**
       * Method description
       *
       *
       * @return
       */
      @Override
      public boolean getBoolean1() {
         return getCv().getBoolean1();
      }

      /**
       * Method description
       *
       *
       * @return
       */
      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      /**
       * Method description
       *
       *
       * @param f
       *
       * @throws PropertyVetoException
       */
      @Override
      public void setBoolean1(boolean b) throws PropertyVetoException {
         getCv().setBoolean1(b);
      }

      /**
       * Method description
       *
       *
       * @param cnid1
       *
       * @throws PropertyVetoException
       */
      @Override
      public void setNid1(int cnid1) throws PropertyVetoException {
         getCv().setNid1(cnid1);
      }
   }
}
