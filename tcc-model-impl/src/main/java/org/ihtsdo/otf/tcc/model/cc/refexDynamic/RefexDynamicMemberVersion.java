package org.ihtsdo.otf.tcc.model.cc.refexDynamic;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.naming.InvalidNameException;
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
import org.ihtsdo.otf.tcc.model.cc.component.Version;

/**
 * Created by kec on 7/12/14.
 */
public class RefexDynamicMemberVersion extends Version<RefexDynamicRevision, RefexDynamicMember>
        implements RefexDynamicVersionBI<RefexDynamicRevision>, RefexDynamicBuilderBI {

    public RefexDynamicMemberVersion(RefexDynamicVersionBI<RefexDynamicRevision> cv, 
            RefexDynamicMember rdm, int stamp) {
        super(cv, rdm, stamp);
    }

    //~--- methods ----------------------------------------------------------
    public RefexDynamicRevision makeAnalog() {
        throw new UnsupportedOperationException("Must use Blueprints");
        //TODO (artf231845) - so it turns out that this is still used by the mergeConcept process when merging TK concepts... 
        //We don't need that at the moment, leaving unimplemented in hopes that the rest of the API gets fixed to actually use blueprints...

    }

    @Override
    public RefexDynamicRevision makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        //TODO (artf231845) - so it turns out that this is still used by the mergeConcept process when merging TK concepts... 
        //We don't need that at the moment, leaving unimplemented in hopes that the rest of the API gets fixed to actually use blueprints...
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
        return getCv().getAssemblageNid();
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
        return (RefexDynamicMember) cc;
    }

    @Override
    public int getReferencedComponentNid() {
        return getCv().getReferencedComponentNid();
    }

    @Override
    public RefexDynamicCAB makeBlueprint(ViewCoordinate vc,
                                         IdDirective idDirective, RefexDirective refexDirective) throws IOException, InvalidCAB, ContradictionException {
        return getCv().makeBlueprint(vc, idDirective, refexDirective);
    }


    public IntArrayList getVariableVersionNids() {
        return ((RefexDynamicRevision) getCv()).getVariableVersionNids();
    }

    @Override
    public Optional<RefexDynamicMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
        return ((RefexDynamicMember) cc).getVersion(c);
    }

    @Override
    public List<? extends RefexDynamicMemberVersion> getVersions() {
        return ((RefexDynamicMember) cc).getVersions();
    }

   @Override
    public List<? extends RefexDynamicMemberVersion> getVersionList() {
        return ((RefexDynamicMember) cc).getVersions();
    }

    @Override
    public Collection<RefexDynamicMemberVersion> getVersions(ViewCoordinate c) {
        return ((RefexDynamicMember) cc).getVersions(c);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
        ((RefexDynamicMember) cc).setAssemblageNid(collectionNid);
    }

    @Override
    public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
        ((RefexDynamicMember) cc).setReferencedComponentNid(componentNid);
    }

    /**
     * @return 
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

