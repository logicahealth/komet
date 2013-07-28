package org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid.NidMember.Version;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidRevision;

public class NidRevision extends RefexRevision<NidRevision, NidMember>
        implements RefexNidAnalogBI<NidRevision> {

    private int nid1;

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
        nid1 = P.s.getNidForUuids(eVersion.getUuid1());
    }

    public NidRevision(TupleInput input, NidMember primoridalMember) {
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

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(nid1);
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
    public IntArrayList getVariableVersionNids() {
        IntArrayList variableNids = new IntArrayList(3);

        variableNids.add(getNid1());

        return variableNids;
    }

    @Override
    public NidMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
        return (Version) ((NidMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<NidMember.Version> getVersions() {
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
