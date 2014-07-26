package org.ihtsdo.otf.tcc.model.cc.refexDynamic;

import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicRevision;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

import javax.naming.InvalidNameException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by kec on 7/12/14.
 */
public class RefexDynamicMemberVersion extends Version<RefexDynamicRevision, RefexDynamicMember>
        implements RefexDynamicVersionBI<RefexDynamicRevision>, RefexDynamicBuilderBI {

    RefexDynamicMember rdm;
    public RefexDynamicMemberVersion(RefexDynamicVersionBI<RefexDynamicRevision> cv, RefexDynamicMember rdm) {
        super(cv, rdm);
        this.rdm = rdm;
    }

    //~--- methods ----------------------------------------------------------
    public RefexDynamicRevision makeAnalog() {
        throw new UnsupportedOperationException("Must use Blueprints");
    }

    @Override
    public RefexDynamicRevision makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        throw new UnsupportedOperationException("Must use Blueprints");
    }

    @Override
    public boolean fieldsEqual(@SuppressWarnings("rawtypes") Version another) {
        RefexDynamicMemberVersion anotherVersion = (RefexDynamicMemberVersion) another;

        if (this.getAssemblageNid() != anotherVersion.getAssemblageNid()) {
            return false;
        }

        if (this.getReferencedComponentNid() != anotherVersion.getReferencedComponentNid()) {
            return false;
        }

        if (this.refexDataFieldsEqual(anotherVersion.getData())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean refexDataFieldsEqual(RefexDynamicDataBI[] another) {
        return getCv().refexDataFieldsEqual(another);
    }

    //~--- get methods ------------------------------------------------------
    @Override
    public int getAssemblageNid() {
        return rdm.getAssemblageNid();
    }

    @SuppressWarnings("unchecked")
    RefexDynamicVersionBI<RefexDynamicRevision> getCv() {
        return (RefexDynamicVersionBI<RefexDynamicRevision>) cv;
    }

    public TtkRefexDynamicMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexDynamicMemberChronicle(this);
    }

    public TtkRevision getERefsetRevision() throws IOException {
        return new TtkRefexDynamicRevision(this);
    }

    @Override
    public RefexDynamicMember getPrimordialVersion() {
        return rdm;
    }

    @Override
    public int getReferencedComponentNid() {
        return rdm.getReferencedComponentNid();
    }

    @Override
    public RefexDynamicCAB makeBlueprint(ViewCoordinate vc,
                                         IdDirective idDirective, RefexDirective refexDirective) throws IOException, InvalidCAB, ContradictionException {
        return getCv().makeBlueprint(vc, idDirective, refexDirective);
    }


    public IntArrayList getVariableVersionNids() {
        if (rdm != getCv()) {
            return ((RefexDynamicRevision) getCv()).getVariableVersionNids();
        } else {
            return rdm.getVariableVersionNids();
        }
    }

    @Override
    public RefexDynamicMemberVersion getVersion(ViewCoordinate c) throws ContradictionException {
        return rdm.getVersion(c);
    }

    @Override
    public List<? extends RefexDynamicMemberVersion> getVersions() {
        return rdm.getVersions();
    }

    @Override
    public Collection<RefexDynamicMemberVersion> getVersions(ViewCoordinate c) {
        return rdm.getVersions(c);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
        rdm.setAssemblageNid(collectionNid);
    }

    @Override
    public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
        rdm.setReferencedComponentNid(componentNid);
    }

    /**
     * @throws ContradictionException
     * @throws IOException
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getRefexDynamicUsageDescription()
     */
    @Override
    public RefexDynamicUsageDescription getRefexDynamicUsageDescription() throws IOException, ContradictionException {
        return getCv().getRefexDynamicUsageDescription();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getData()
     */
    @Override
    public RefexDynamicDataBI[] getData() {
        return getCv().getData();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getData(int)
     */
    @Override
    public RefexDynamicDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
        return getCv().getData(columnNumber);
    }

    /**
     * @throws ContradictionException
     * @throws IOException
     * @throws javax.naming.InvalidNameException
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(java.lang.String)
     */
    @Override
    public RefexDynamicDataBI getData(String columnName) throws IndexOutOfBoundsException, InvalidNameException, IOException, ContradictionException {
        return getCv().getData(columnName);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI[])
     */
    @Override
    public void setData(RefexDynamicDataBI[] data) throws PropertyVetoException
    {
        ((RefexDynamicRevision)getCv()).setData(data);

    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(int, org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI)
     */
    @Override
    public void setData(int columnNumber, RefexDynamicDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
    {
        ((RefexDynamicRevision)getCv()).setData(columnNumber, data);
    }
}

