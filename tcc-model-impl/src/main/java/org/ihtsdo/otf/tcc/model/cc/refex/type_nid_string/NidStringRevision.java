package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_string;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.chronicle.cc.P;

public class NidStringRevision extends RefexRevision<NidStringRevision, NidStringMember>
        implements RefexNidStringAnalogBI<NidStringRevision> {
   private int    c1Nid;
   private String strValue;

   //~--- constructors --------------------------------------------------------

   public NidStringRevision() {
      super();
   }

   public NidStringRevision(int statusAtPositionNid, NidStringMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      strValue = primoridalMember.getString1();
   }
   
   public NidStringRevision(TtkRefexUuidStringRevision eVersion, NidStringMember member) throws IOException {
      super(eVersion, member);
      c1Nid    = P.s.getNidForUuids(eVersion.getUuid1());
      strValue = eVersion.getString1();
   }

   public NidStringRevision(TupleInput input, NidStringMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid    = input.readInt();
      strValue = input.readString();
   }

   public NidStringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                         NidStringMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      strValue = primoridalMember.getString1();
   }

   protected NidStringRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, NidStringRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid    = another.c1Nid;
      strValue = another.strValue;
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

      if (NidStringRevision.class.isAssignableFrom(obj.getClass())) {
         NidStringRevision another = (NidStringRevision) obj;

         return (this.c1Nid == another.c1Nid) && this.strValue.equals(another.strValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidStringRevision makeAnalog() {
      return new NidStringRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public NidStringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidStringRevision newR = new NidStringRevision(status, time, authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert strValue != null;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" strValue:" + "'").append(this.strValue).append("'");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeString(strValue);
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
      return strValue;
   }

   @Override
   protected RefexType getTkRefsetType() {
      return RefexType.CID_STR;
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList variableNids = new IntArrayList(3);

      variableNids.add(getNid1());

      return variableNids;
   }

   @Override
   public NidStringMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (NidStringMember.Version) ((NidStringMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<NidStringMember.Version> getVersions() {
      return ((NidStringMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidStringRevision>> getVersions(ViewCoordinate c) {
      return ((NidStringMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.strValue = str;
      modified();
   }

   public void setStringValue(String strValue) {
      this.strValue = strValue;
      modified();
   }
}
