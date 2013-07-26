package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_boolean;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_boolean.BooleanMember.Version;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanRevision;
import org.ihtsdo.otf.tcc.api.refex.RefexType;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

public class BooleanRevision extends RefexRevision<BooleanRevision, BooleanMember>
        implements RefexBooleanAnalogBI<BooleanRevision> {
   private boolean booleanValue;

   //~--- constructors --------------------------------------------------------

   public BooleanRevision() {
      super();
   }

   protected BooleanRevision(int statusAtPositionNid, BooleanMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      this.booleanValue = primoridalMember.getBoolean1();
   }

   public BooleanRevision(TtkRefexBooleanRevision eVersion, BooleanMember booleanMember) throws IOException {
      super(eVersion, booleanMember);
      this.booleanValue = eVersion.isBooleanValue();
   }

   public BooleanRevision(TupleInput input, BooleanMember primoridalMember) {
      super(input, primoridalMember);
      booleanValue = input.readBoolean();
   }

   protected BooleanRevision(Status status, long time, int authorNid, int moduleNid,
           int pathNid, BooleanMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.booleanValue = primoridalMember.getBoolean1();
   }

   protected BooleanRevision(Status status, long time, int authorNid, int moduleNid,
           int pathNid, BooleanRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      this.booleanValue = another.getBooleanValue();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.BOOLEAN_EXTENSION_1, getBoolean1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (BooleanRevision.class.isAssignableFrom(obj.getClass())) {
         BooleanRevision another = (BooleanRevision) obj;

         return (this.booleanValue == another.booleanValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public BooleanRevision makeAnalog() {
      return new BooleanRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public BooleanRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      BooleanRevision newR = new BooleanRevision(status, time, authorNid,
              moduleNid, pathNid, this);

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
      buf.append(" booleanValue:").append(this.booleanValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeBoolean(booleanValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public boolean getBoolean1() {
      return this.booleanValue;
   }

   public boolean getBooleanValue() {
      return booleanValue;
   }

    @Override
   protected RefexType getTkRefsetType() {
      return RefexType.BOOLEAN;
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(2);

      return variableNids;
   }

   @Override
   public BooleanMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (Version) ((BooleanMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<BooleanMember.Version> getVersions() {
      return ((BooleanMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<BooleanRevision>> getVersions(ViewCoordinate c) {
      return ((BooleanMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setBoolean1(boolean l) throws PropertyVetoException {
      this.booleanValue = l;
      modified();
   }

   public void setBooleanValue(boolean booleanValue) {
      this.booleanValue = booleanValue;
      modified();
   }
}
