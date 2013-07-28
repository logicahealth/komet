package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

public class IntRevision extends RefexRevision<IntRevision, IntMember>
        implements RefexIntAnalogBI<IntRevision> {
   private int intValue;

   //~--- constructors --------------------------------------------------------

   public IntRevision() {
      super();
   }

   public IntRevision(int statusAtPositionNid, IntMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      intValue = primoridalMember.getInt1();
   }

   public IntRevision(TtkRefexIntRevision eVersion, IntMember member) throws IOException {
      super(eVersion, member);
      this.intValue = eVersion.getIntValue();
   }

   public IntRevision(TupleInput input, IntMember primoridalMember) {
      super(input, primoridalMember);
      intValue = input.readInt();
   }

   public IntRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, IntMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      intValue = primoridalMember.getInt1();
   }

   protected IntRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, IntRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      intValue = another.intValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.INTEGER_EXTENSION_1, this.intValue);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IntRevision.class.isAssignableFrom(obj.getClass())) {
         IntRevision another = (IntRevision) obj;

         return (intValue == another.intValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public IntRevision makeAnalog() {
      return new IntRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public IntRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      IntRevision newR = new IntRevision(status, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" intValue:").append(this.intValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(intValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getInt1() {
      return intValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.INT;
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      return new IntArrayList(2);
   }

   @Override
   public IntMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (IntMember.Version) ((IntMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<IntMember.Version> getVersions() {
      return ((IntMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<IntRevision>> getVersions(ViewCoordinate c) {
      return ((IntMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setInt1(int l) throws PropertyVetoException {
      this.intValue = l;
      modified();
   }
}
