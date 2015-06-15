package org.ihtsdo.otf.tcc.model.cc.refex.type_nid;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

public class NidRevision extends RefexRevision<NidRevision, NidMember>
        implements RefexNidAnalogBI<NidRevision> {

    protected int nid1;

    //~--- constructors --------------------------------------------------------
    public NidRevision() {
        super();
    }

    protected NidRevision(int statusAtPositionNid, NidMember primoridalMember) {
        super(statusAtPositionNid, primoridalMember);
        nid1 = primoridalMember.getC1Nid();
    }

    public NidRevision(TtkRefexUuidRevision eVersion, NidMember member) throws IOException {
        super(eVersion, member);
        nid1 = PersistentStore.get().getNidForUuids(eVersion.getUuid1());
    }

    public NidRevision(DataInputStream input, NidMember primoridalMember) throws IOException {
        super(input, primoridalMember);
        nid1 = input.readInt();
    }

    protected NidRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, NidMember primoridalMember) {
        super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
        nid1 = primoridalMember.getC1Nid();
    }

    protected NidRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, NidRevision another) {
        super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
        nid1 = another.nid1;
    }
    
    public NidRevision(RefexNidAnalogBI another, Status status, long time, int authorNid,
            int moduleNid, int pathNid, NidMember primoridalMember) {
        super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
        this.nid1 = another.getNid1();
    }
    
    public NidRevision(RefexNidAnalogBI another, NidMember primordialMember){
        super(another.getStatus(), another.getTime(), another.getAuthorNid(), another.getModuleNid(),
              another.getPathNid(), primordialMember);
        this.nid1 = another.getNid1();
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        allNids.add(nid1);
    }

    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (NidRevision.class.isAssignableFrom(obj.getClass())) {
            NidRevision another = (NidRevision) obj;

            if (this.nid1 == another.nid1) {
                return super.equals(obj);
            }
        }

        return false;
    }

    @Override
    public NidRevision makeAnalog() {
        return new NidRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
    }

    @Override
    public NidRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);
            return this;
        }

        NidRevision newR = new NidRevision(status, time, authorNid, moduleNid, pathNid, this);

        primordialComponent.addRevision(newR);

        return newR;
    }

    @Override
    public boolean readyToWriteRefsetRevision() {
        assert nid1 != Integer.MAX_VALUE;

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
        ConceptComponent.addNidToBuffer(buf, this.nid1);
        buf.append(super.toString());

        return buf.toString();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getNid1() {
        return nid1;
    }

    @Override
    protected RefexType getTkRefsetType() {
        return RefexType.CID;
    }

    @Override
    public Optional<NidMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
        Optional<RefexMemberVersion<NidRevision, NidMember>> temp =  ((NidMember) primordialComponent).getVersion(c);
        return Optional.ofNullable(temp.isPresent() ? (NidMemberVersion)temp.get() : null);
    }

    @Override
    public List<NidMemberVersion> getVersions() {
        return ((NidMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexVersionBI<NidRevision>> getVersions(ViewCoordinate c) {
        return ((NidMember) primordialComponent).getVersions(c);
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setNid1(int c1Nid) {
        this.nid1 = c1Nid;
        modified();
    }
}
