package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.Get;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NidNidNidMember extends RefexMember<NidNidNidRevision, NidNidNidMember>
        implements
        RefexNidNidNidVersionBI<NidNidNidRevision>,
        RefexNidNidNidAnalogBI<NidNidNidRevision> {

    private static VersionComputer<RefexMemberVersion<NidNidNidRevision, NidNidNidMember>> computer =
            new VersionComputer<>();
    //~--- fields --------------------------------------------------------------
    protected int c1Nid;
    protected int c2Nid;
    protected int c3Nid;

    //~--- constructors --------------------------------------------------------
    public NidNidNidMember() {
        super();
    }

    public NidNidNidMember(TtkRefexUuidUuidUuidMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = PersistentStore.get().getNidForUuids(refsetMember.getUuid1());
        c2Nid = PersistentStore.get().getNidForUuids(refsetMember.getUuid2());
        c3Nid = PersistentStore.get().getNidForUuids(refsetMember.getUuid3());

        if (refsetMember.getRevisionList() != null) {
            revisions = new RevisionSet<>(primordialStamp);

            for (TtkRefexUuidUuidUuidRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new NidNidNidRevision(eVersion, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        allNids.add(c1Nid);
        allNids.add(c2Nid);
        allNids.add(c3Nid);
    }

    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(ComponentProperty.COMPONENT_EXTENSION_1_ID, getNid1());
        rcs.with(ComponentProperty.COMPONENT_EXTENSION_2_ID, getNid2());
        rcs.with(ComponentProperty.COMPONENT_EXTENSION_3_ID, getNid3());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (NidNidNidMember.class.isAssignableFrom(obj.getClass())) {
            NidNidNidMember another = (NidNidNidMember) obj;

            return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                    && (this.c3Nid == another.c3Nid) && (this.nid == another.nid)
                    && (this.referencedComponentNid == another.referencedComponentNid);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{c1Nid, c2Nid, c3Nid});
    }

    @Override
    public NidNidNidRevision makeAnalog() {
        return new NidNidNidRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
    }

    @Override
    public NidNidNidRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        NidNidNidRevision newR = new NidNidNidRevision(status, time, authorNid, moduleNid, pathNid, this);

        addRevision(newR);

        return newR;
    }

    @Override
    protected boolean refexFieldsEqual(ConceptComponent<NidNidNidRevision, NidNidNidMember> obj) {
        if (NidNidNidMember.class.isAssignableFrom(obj.getClass())) {
            NidNidNidMember another = (NidNidNidMember) obj;

            return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                    && (this.c3Nid == another.c3Nid);
        }

        return false;
    }
    
    
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidNidNidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidNidNidVersionBI cv = (RefexNidNidNidVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.c2Nid == cv.getNid2())
                    && (this.c3Nid == cv.getNid3());
        }
        return false;
    }

    @Override
    public boolean readyToWriteRefsetMember() {
        assert c1Nid != Integer.MAX_VALUE;
        assert c2Nid != Integer.MAX_VALUE;
        assert c3Nid != Integer.MAX_VALUE;

        return true;
    }

    /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName()).append(" ");
        buf.append(" c1Nid: ");
        addNidToBuffer(buf, c1Nid);
        buf.append(" c2Nid: ");
        addNidToBuffer(buf, c2Nid);
        buf.append(" c3Nid: ");
        addNidToBuffer(buf, c3Nid);
        buf.append(super.toString());

        return buf.toString();
    }

    //~--- get methods ---------------------------------------------------------
    public int getC1Nid() {
        return c1Nid;
    }

    public int getC2Nid() {
        return c2Nid;
    }

    public int getC3Nid() {
        return c3Nid;
    }

    @Override
    public int getNid1() {
        return c1Nid;
    }

    @Override
    public int getNid2() {
        return c2Nid;
    }

    @Override
    public int getNid3() {
        return c3Nid;
    }

    @Override
    protected RefexType getTkRefsetType() {
        return RefexType.CID_CID_CID;
    }

    @Override
    public int getTypeNid() {
        return RefexType.CID_CID_CID.getTypeToken();
    }

    @Override
    protected VersionComputer<RefexMemberVersion<NidNidNidRevision, NidNidNidMember>> getVersionComputer() {
        return computer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<NidNidNidMemberVersion> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<NidNidNidMemberVersion> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new NidNidNidMemberVersion(this, this, primordialStamp));
                for (int stampAlias : Get.commitService().getAliases(primordialStamp)) {
                    list.add(new NidNidNidMemberVersion(this, this, stampAlias));
                }
            }

            if (revisions != null) {
                for (NidNidNidRevision r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new NidNidNidMemberVersion(r, this, r.stamp));
                        for (int stampAlias : Get.commitService().getAliases(r.stamp)) {
                            list.add(new NidNidNidMemberVersion(r, this, stampAlias));
                        }
                    }
                }
            }

            versions = list;
        }

        return (List<NidNidNidMemberVersion>) versions;
    }

    //~--- set methods ---------------------------------------------------------
    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    public void setC2Nid(int c2Nid) {
        this.c2Nid = c2Nid;
        modified();
    }

    public void setC3Nid(int c3Nid) {
        this.c3Nid = c3Nid;
        modified();
    }

    @Override
    public void setNid1(int cnid1) throws PropertyVetoException {
        this.c1Nid = cnid1;
        modified();
    }

    @Override
    public void setNid2(int cnid2) throws PropertyVetoException {
        this.c2Nid = cnid2;
        modified();
    }

    @Override
    public void setNid3(int cnid) throws PropertyVetoException {
        this.c3Nid = cnid;
        modified();
    }

}
