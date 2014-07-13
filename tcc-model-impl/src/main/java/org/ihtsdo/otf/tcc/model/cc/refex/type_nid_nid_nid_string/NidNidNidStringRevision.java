package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_string;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string.RefexNidNidNidStringAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringRevision;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

import java.beans.PropertyVetoException;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

public class NidNidNidStringRevision
        extends RefexRevision<NidNidNidStringRevision, NidNidNidStringMember>
        implements RefexNidNidNidStringAnalogBI<NidNidNidStringRevision> {
   private int   nid1;
   private int   nid2;
   private int   nid3;
   private String string1;

   public NidNidNidStringRevision() {
      super();
   }

   public NidNidNidStringRevision(int statusAtPositionNid,
                                 NidNidNidStringMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      string1 = primoridalMember.getString1();
   }

   public NidNidNidStringRevision(TtkRefexUuidUuidUuidStringRevision eVersion,
                                 NidNidNidStringMember member)
           throws IOException {
      super(eVersion, member);
      nid1  = P.s.getNidForUuids(eVersion.getUuid1());
      nid2  = P.s.getNidForUuids(eVersion.getUuid2());
      nid3  = P.s.getNidForUuids(eVersion.getUuid3());
      string1 = eVersion.getString1();
   }

   public NidNidNidStringRevision(DataInputStream input,
                                 NidNidNidStringMember primoridalMember) throws IOException {
      super(input, primoridalMember);
      nid1  = input.readInt();
      nid2  = input.readInt();
      nid3  = input.readInt();
      string1 = input.readUTF();
   }

   public NidNidNidStringRevision(Status status, long time, int authorNid,
                                 int moduleNid, int pathNid,
                                 NidNidNidStringMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      nid1  = primoridalMember.getNid1();
      nid2  = primoridalMember.getNid2();
      nid3  = primoridalMember.getNid3();
      string1 = primoridalMember.getString1();
   }

   protected NidNidNidStringRevision(Status status, long time, int authorNid,
                                    int moduleNid, int pathNid,
                                    NidNidNidStringRevision another) {
      super(status, time, authorNid, moduleNid, pathNid,
            another.primordialComponent);
      nid1  = another.nid1;
      nid2  = another.nid2;
      nid3  = another.nid3;
      string1 = another.string1;
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

      if (NidNidNidStringRevision.class.isAssignableFrom(obj.getClass())) {
         NidNidNidStringRevision another = (NidNidNidStringRevision) obj;

         return (this.nid1 == another.nid1) && (this.nid2 == another.nid2)
                && (this.nid3 == another.nid3)
                && (this.string1.equals(another.string1)) && super.equals(obj);
      }

      return false;
   }

   @Override
   public NidNidNidStringRevision makeAnalog() {
      return new NidNidNidStringRevision(getStatus(), getTime(),
                                        getAuthorNid(), getModuleNid(),
                                        getPathNid(), this);
   }

   @Override
   public NidNidNidStringRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      NidNidNidStringRevision newR = new NidNidNidStringRevision(status, time,
                                       authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert nid1 != Integer.MAX_VALUE;
      assert nid2 != Integer.MAX_VALUE;
      assert nid3 != Integer.MAX_VALUE;

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
      buf.append(" nid1: ");
      ConceptComponent.addNidToBuffer(buf, nid1);
      buf.append(" nid2: ");
      ConceptComponent.addNidToBuffer(buf, nid2);
      buf.append(" nid3: ");
      ConceptComponent.addNidToBuffer(buf, nid3);
      buf.append(" string1: ").append(string1);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(DataOutput output) throws IOException {
      output.writeInt(nid1);
      output.writeInt(nid2);
      output.writeInt(nid3);
      output.writeUTF(string1);
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
   public NidNidNidStringMemberVersion getVersion(ViewCoordinate c)
           throws ContradictionException {
      return (NidNidNidStringMemberVersion) ((NidNidNidStringMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<NidNidNidStringMemberVersion> getVersions() {
      return ((NidNidNidStringMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<NidNidNidStringRevision>> getVersions(
           ViewCoordinate c) {
      return ((NidNidNidStringMember) primordialComponent).getVersions(c);
   }

   @Override
   public void setString1(String string1) {
      this.string1 = string1;
      modified();
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.nid1 = cnid;
      modified();
   }

   @Override
   public void setNid2(int cnid) throws PropertyVetoException {
      this.nid2 = cnid;
      modified();
   }

   @Override
   public void setNid3(int cnid) throws PropertyVetoException {
      this.nid3 = cnid;
      modified();
   }
}
