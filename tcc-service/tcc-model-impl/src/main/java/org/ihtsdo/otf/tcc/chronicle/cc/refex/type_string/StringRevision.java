package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_string;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

public class StringRevision extends RefexRevision<StringRevision, StringMember>
        implements RefexStringAnalogBI<StringRevision> {
   private String stringValue;

   //~--- constructors --------------------------------------------------------

   public StringRevision() {
      super();
   }

   public StringRevision(int statusAtPositionNid, StringMember another) {
      super(statusAtPositionNid, another);
      stringValue = another.getString1();
   }

   public StringRevision(TtkRefexStringRevision eVersion, StringMember primoridalMember) throws IOException {
      super(eVersion, primoridalMember);
      this.stringValue = eVersion.getString1();
   }

   public StringRevision(TupleInput input, StringMember primoridalMember) {
      super(input, primoridalMember);
      stringValue = input.readString();
   }

   public StringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, StringMember another) {
      super(status, time, authorNid, moduleNid, pathNid, another);
      stringValue = another.getString1();
   }

   protected StringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, StringRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      stringValue = another.stringValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(ComponentProperty.STRING_EXTENSION_1, getString1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (StringRevision.class.isAssignableFrom(obj.getClass())) {
         StringRevision another = (StringRevision) obj;

         return stringValue.equals(another.stringValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public StringRevision makeAnalog() {
      return new StringRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public StringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      StringRevision newR = new StringRevision(status, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert stringValue != null;

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
      buf.append(" stringValue:" + "'").append(this.stringValue).append("' ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeString(stringValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getString1() {
      return stringValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.STR;
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      return new IntArrayList(2);
   }

   @Override
   public StringMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (StringMember.Version) ((StringMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<StringMember.Version> getVersions() {
      return ((StringMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<StringRevision>> getVersions(ViewCoordinate c) {
      return ((StringMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.stringValue = str;
      modified();
   }

   public void setStringValue(String stringValue) {
      this.stringValue = stringValue;
      modified();
   }
}
