package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int;

//~--- non-JDK imports --------------------------------------------------------

import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_int.RefexNidIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntRevision;

public class NidIntRevision extends RefexRevision<NidIntRevision, NidIntMember>
        implements RefexNidIntAnalogBI<NidIntRevision> {

    protected int c1Nid;
    protected int intValue;

    //~--- constructors --------------------------------------------------------
    public NidIntRevision() {
        super();
    }

    protected NidIntRevision(int statusAtPositionNid, NidIntMember primoridalMember) {
        super(statusAtPositionNid, primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        intValue = primoridalMember.getInt1();
    }

    public NidIntRevision(TtkRefexUuidIntRevision eVersion, NidIntMember member) throws IOException {
        super(eVersion, member);
        c1Nid = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
        intValue = eVersion.getIntValue();
    }


    protected NidIntRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, 
                            NidIntMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      intValue = primoridalMember.getInt1();
   }

   protected NidIntRevision(Status status, long time, int authorNid, int moduleNid,
           int pathNid, NidIntRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid    = another.c1Nid;
      intValue = another.intValue;
   }
   
   public NidIntRevision(RefexNidIntAnalogBI another, Status status, long time, int authorNid,
            int moduleNid, int pathNid, NidIntMember primoridalMember) {
        super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
        this.c1Nid = another.getNid1();
        this.intValue = another.getInt1();
    }
    
    public NidIntRevision(RefexNidIntAnalogBI another, NidIntMember primordialMember){
        super(another.getStatus(), another.getTime(), another.getAuthorNid(), another.getModuleNid(),
              another.getPathNid(), primordialMember);
        this.c1Nid = another.getNid1();
        this.intValue = another.getInt1();
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        allNids.add(c1Nid);
    }

    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
        rcs.with(ComponentProperty.INTEGER_EXTENSION_1, getInt1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (NidIntRevision.class.isAssignableFrom(obj.getClass())) {
            NidIntRevision another = (NidIntRevision) obj;

            return (this.c1Nid == another.c1Nid) && (this.intValue == another.intValue) && super.equals(obj);
        }

        return false;
    }

    @Override
    public NidIntRevision makeAnalog() {
        return new NidIntRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
    }

    @Override
    public NidIntRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

            return this;
        }

        NidIntRevision newR = new NidIntRevision(status, time, authorNid, moduleNid, pathNid, this);

        primordialComponent.addRevision(newR);

        return newR;
    }

    @Override
    public boolean readyToWriteRefsetRevision() {
        assert c1Nid != Integer.MAX_VALUE;

        return true;
    }

    /*
     * (non-Javadoc) @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" intValue:").append(this.intValue);
        buf.append(super.toString());

        return buf.toString();
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
    public int getInt1() {
        return intValue;
    }

    @Override
    protected RefexType getTkRefsetType() {
        return RefexType.CID_INT;
    }

    @Override
    public NidIntMemberVersion getVersion(ViewCoordinate c) throws ContradictionException {
        return (NidIntMemberVersion) ((NidIntMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<NidIntMemberVersion> getVersions() {
        return ((NidIntMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexVersionBI<NidIntRevision>> getVersions(ViewCoordinate c) {
        return ((NidIntMember) primordialComponent).getVersions(c);
    }

    //~--- set methods ---------------------------------------------------------
    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    @Override
    public void setNid1(int cnid) throws PropertyVetoException {
        this.c1Nid = cnid;
        modified();
    }

    @Override
    public void setInt1(int l) throws PropertyVetoException {
        this.intValue = l;
        modified();
    }
}
