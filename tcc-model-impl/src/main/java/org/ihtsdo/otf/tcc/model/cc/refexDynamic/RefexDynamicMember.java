/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.model.cc.refexDynamic;

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicRevision;
import org.ihtsdo.otf.tcc.model.cc.NidPair;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicTypeToClassUtility;



/**
 * {@link RefexDynamicMember}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicMember extends ConceptComponent<RefexDynamicRevision, RefexDynamicMember> implements RefexDynamicChronicleBI<RefexDynamicRevision>, 
    RefexDynamicVersionBI<RefexDynamicRevision>, RefexDynamicBuilderBI
{
    public int referencedComponentNid;
    public int assemblageNid;
    protected RefexDynamicDataBI[] data_;
    protected List<? extends RefexDynamicMemberVersion> versions;

    //~--- constructors --------------------------------------------------------
    public RefexDynamicMember() {
        super();
        referencedComponentNid = Integer.MAX_VALUE;
        assemblageNid = Integer.MAX_VALUE;
    }

    public RefexDynamicMember(TtkRefexDynamicMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        assemblageNid = PersistentStore.get().getNidForUuids(refsetMember.refexAssemblageUuid);
        referencedComponentNid = PersistentStore.get().getNidForUuids(refsetMember.getComponentUuid());
        primordialStamp = PersistentStore.get().getStamp(refsetMember);
        assert primordialStamp != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        assert assemblageNid != Integer.MAX_VALUE;
        
        if (refsetMember.getData() != null)
        {
            data_= new RefexDynamicDataBI[refsetMember.getData().length];
            for (int i = 0; i < data_.length; i++)
            {
                if (refsetMember.getData()[i] == null)
                {
                    data_[i] = null;
                }
                else
                {
                    data_[i] = RefexDynamicTypeToClassUtility.typeToClass(refsetMember.getData()[i].getRefexDataType(), refsetMember.getData()[i].getData(), 
                        assemblageNid, i);
                }
            }
        }

        if (refsetMember.getRevisionList() != null) {
            revisions = new RevisionSet(primordialStamp);

            for (TtkRefexDynamicRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new RefexDynamicRevision(eVersion, this));
            }
        }
        
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(referencedComponentNid);
        allNids.add(assemblageNid);
        addRefsetTypeNids(allNids);
    }

    @Override
    public void clearVersions() {
        versions = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (RefexDynamicMember.class.isAssignableFrom(obj.getClass())) {
            RefexDynamicMember another = (RefexDynamicMember) obj;
            if (this.getAssemblageNid() == another.getAssemblageNid() && this.getReferencedComponentNid() == another.getReferencedComponentNid() 
                    && Arrays.deepEquals(this.getData(), another.getData())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<RefexDynamicRevision, RefexDynamicMember> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            RefexDynamicMember another = (RefexDynamicMember) obj;

            if (this.getAssemblageNid() != another.getAssemblageNid()) {
                return false;
            }

            if (refexFieldsEqual(obj)) {
                return conceptComponentFieldsEqual(another);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{referencedComponentNid, assemblageNid, Arrays.deepHashCode(getData())});
    }

    @Override
    public RefexDynamicMember merge(RefexDynamicMember component) throws IOException {
        return (RefexDynamicMember) super.merge(component);
    }


    @Override
    public final boolean readyToWriteComponent() {
        assert referencedComponentNid != Integer.MAX_VALUE : assertionString();
        assert referencedComponentNid != 0 : assertionString();
        assert assemblageNid != Integer.MAX_VALUE : assertionString();
        assert assemblageNid != 0 : assertionString();
        assert readyToWriteRefsetMember() : assertionString();

        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(" refex:");
        addNidToBuffer(buf, assemblageNid);
        buf.append(" rcNid:");
        addNidToBuffer(buf, referencedComponentNid);
        buf.append(" ");
        buf.append(Arrays.toString(getData()));
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
        ComponentVersionBI c1Component = snapshot.getConceptVersion(assemblageNid);

        return "refex: " + c1Component.toUserString(snapshot);
    }

    //TODO (artf231857) [REFEX] no idea what this is for, or if we need it
//    /**
//     * Test method to check to see if two objects are equal in all respects.
//     *
//     * @param another
//     * @return either a zero length String, or a String containing a description
//     * of the validation failures.
//     * @throws IOException
//     */
//    public String validate(RefexDynamicMember another) throws IOException {
//        assert another != null;
//
//        StringBuilder buf = new StringBuilder();
//
//        if (this.referencedComponentNid != another.referencedComponentNid) {
//            buf.append(
//                    "\tRefsetMember.referencedComponentNid not equal: \n"
//                    + "\t\tthis.referencedComponentNid = ").append(this.referencedComponentNid).append(
//                    "\n" + "\t\tanother.referencedComponentNid = ").append(
//                    another.referencedComponentNid).append("\n");
//        }
//
//        // Compare the parents
//        buf.append(super.validate(another));
//
//        return buf.toString();
//    }
    //~--- get methods ---------------------------------------------------------
    @Override
    public int getAssemblageNid() {
        return assemblageNid;
    }

    @Override
    public RefexDynamicMember getPrimordialVersion() {
        return RefexDynamicMember.this;
    }

    @Override
    public int getReferencedComponentNid() {
        return referencedComponentNid;
    }

    @Override
    public RefexDynamicCAB makeBlueprint(ViewCoordinate vc,
            IdDirective idDirective, RefexDirective refexDirective) throws IOException,
            InvalidCAB, ContradictionException {

        RefexDynamicCAB rdc = new RefexDynamicCAB(
                PersistentStore.get().getUuidPrimordialForNid(getReferencedComponentNid()),
                getAssemblageNid(),
                getVersion(vc), 
                vc, 
                idDirective, 
                refexDirective);

        rdc.setData(getData(), vc);
        return rdc;
    }

    @Override
    public RefexDynamicMemberVersion getVersion(ViewCoordinate c) throws ContradictionException {
        List<RefexDynamicMemberVersion> vForC = getVersions(c);

        if (vForC.isEmpty()) {
            return null;
        }

        if (vForC.size() > 1) {
            vForC = c.getContradictionManager().resolveVersions(vForC);
        }

        if (vForC.size() > 1) {
            throw new ContradictionException(vForC.toString());
        }

        if (!vForC.isEmpty()) {
            return vForC.get(0);
        }
        return null;
    }

    @Override
    public List<? extends RefexDynamicVersionBI<RefexDynamicRevision>> getVersionList() {
        return getVersions();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends RefexDynamicMemberVersion> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<RefexDynamicMemberVersion> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new RefexDynamicMemberVersion(this, this, primordialStamp));
                for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                    list.add(new RefexDynamicMemberVersion(this, this, stampAlias));
                }
            }

            if (revisions != null) {
                for (RefexDynamicRevision rv : revisions) {
                    list.add(new RefexDynamicMemberVersion(rv, this, rv.stamp));
                    for (int stampAlias : getCommitManager().getAliases(rv.getStamp())) {
                        list.add(new RefexDynamicMemberVersion(rv, this, stampAlias));
                    }
                }
            }

            versions = list;
        }

        return (List<RefexDynamicMemberVersion>) versions;
    }

    @Override
    public List<RefexDynamicMemberVersion> getVersions(ViewCoordinate c) {
        List<RefexDynamicMemberVersion> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getViewPosition(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public List<RefexDynamicMemberVersion> getVersions(ViewCoordinate c, long time) {
        List<RefexDynamicMemberVersion> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getViewPosition(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager(), time);

        return returnTuples;
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
        if ((this.assemblageNid == Integer.MAX_VALUE) || (this.assemblageNid == collectionNid)
                || (getTime() == Long.MAX_VALUE)) {
            if (this.assemblageNid != collectionNid) {
                if ((this.assemblageNid != 0) && (this.nid != 0)) {
                    NidPairForRefex oldNpr = NidPair.getRefexNidMemberNidPair(this.assemblageNid, this.nid);

                    PersistentStore.get().forgetXrefPair(this.referencedComponentNid, oldNpr);
                }

                // new xref is added on the dbWrite.
                this.assemblageNid = collectionNid;
                modified();
            }
        } else {
            throw new PropertyVetoException("Cannot change refset unless member is uncommitted...", null);
        }
    }

    @Override
    public void setReferencedComponentNid(int referencedComponentNid) throws IOException {
        assert referencedComponentNid != Integer.MAX_VALUE : "referencedComponentNid is Integer.MAX_VALUE";
        assert assemblageNid != Integer.MAX_VALUE : "assemblageNid is Integer.MAX_VALUE";
        assert nid != Integer.MAX_VALUE : "nid is Integer.MAX_VALUE";
        if (this.referencedComponentNid != referencedComponentNid) {
            if ((this.referencedComponentNid != Integer.MAX_VALUE) && (this.assemblageNid != 0) && (this.nid != 0)) {
                NidPairForRefex oldNpr = NidPair.getRefexNidMemberNidPair(this.assemblageNid, this.nid);

                PersistentStore.get().forgetXrefPair(this.referencedComponentNid, oldNpr);
            }

            // new xref is added on the dbWrite.
            this.referencedComponentNid = referencedComponentNid;
            modified();
        }
    }
    /**
     * From MembershipMember below here
     */
    
    private static VersionComputer<RefexDynamicMemberVersion> computer = new VersionComputer<>();

    protected void addRefsetTypeNids(Set<Integer> allNids) {
        for (RefexDynamicDataBI data : getData())
        {
            if (data == null)
            {
                continue;
            }
            if (data.getRefexDataType() == RefexDynamicDataType.NID)
            {
                allNids.add((int)data.getDataObject());
            }
        }
    }

    @Override
    public RefexDynamicRevision makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        //This should be unsupported, but the blueprint code uses it.  It needs a new name, in the non-blueprint world
        RefexDynamicRevision newR = new RefexDynamicRevision(status, time, authorNid, moduleNid, pathNid, this);

        addRevision(newR);

        return newR;
    }

    protected boolean refexFieldsEqual(ConceptComponent<RefexDynamicRevision, RefexDynamicMember> obj) {
       if (RefexDynamicMember.class.isAssignableFrom(obj.getClass())) {
          RefexDynamicMember another = (RefexDynamicMember) obj;
          return refexDataFieldsEqual(another.getData());
       }
       return false;
    }
    
    @Override
    public boolean refexDataFieldsEqual(RefexDynamicDataBI[] another) {
        return Arrays.deepEquals(getData(), another);
    }

    protected final RefexDynamicRevision readMemberRevision(DataInputStream input) throws IOException {
       return new RefexDynamicRevision(input, this);
    }

    public boolean readyToWriteRefsetMember() {
        //I don't think I need to do any validation here, as the blueprint process shouldn't allow the creation of data that isn't ready
       return true;
    }

    protected void writeMember(DataOutput output) throws IOException {

        //Write with the following format - 
        //dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
        if (getData() != null)
        {
            output.writeInt(getData().length);
            for (RefexDynamicDataBI column : getData())
            {
                if (column == null)
                {
                    output.writeInt(RefexDynamicDataType.UNKNOWN.getTypeToken());
                }
                else
                {
                    output.writeInt(column.getRefexDataType().getTypeToken());
                    output.writeInt(column.getData().length);
                    output.write(column.getData());
                }
            }
        }
        else
        {
            output.writeInt(0);
        }
    }

    protected VersionComputer<RefexDynamicMemberVersion> getVersionComputer() {
       return computer;
    }

    /**
     * New methods here down
     */
    
    /**
     * @throws ContradictionException 
     * @throws IOException 
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getRefexDynamicUsageDescription()
     */
    @Override
    public RefexDynamicUsageDescription getRefexDynamicUsageDescription() throws IOException, ContradictionException {
        return RefexDynamicUsageDescription.read(getAssemblageNid());
    }
    
    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData()
     */
    @Override
    public RefexDynamicDataBI[] getData() {
        if (data_ == null)
        {
            data_ = new RefexDynamicData[] {};
        }
        return data_;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(int)
     */
    @Override
    public RefexDynamicDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
        RefexDynamicDataBI[] temp = getData();
        if (columnNumber >= temp.length)
        {
            throw new IndexOutOfBoundsException("Data contains " + temp.length + " columns.  Can't ask for column " + columnNumber);
        }
        return temp[columnNumber];
    }
    
    /**
     * @throws ContradictionException 
     * @throws IOException 
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(java.lang.String)
     */
    @Override
    public RefexDynamicDataBI getData(String columnName) throws IndexOutOfBoundsException, IOException, ContradictionException {
        for (RefexDynamicColumnInfo ci : getRefexDynamicUsageDescription().getColumnInfo())
        {
            if (ci.getColumnName().equals(columnName))
            {
                return getData(ci.getColumnOrder());
            }
        }
        throw new IndexOutOfBoundsException("Could not find a column with name '" + columnName + "'");
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI[])
     */
    @Override
    public void setData(RefexDynamicDataBI[] data) throws PropertyVetoException
    {
        if (data == null)
        {
            data_ = new RefexDynamicData[] {};
        }
        else
        {
            data_ = data;
        }
        modified();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(int, org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI)
     */
    @Override
    public void setData(int columnNumber, RefexDynamicDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
    {
        RefexDynamicDataBI[] temp = getData();
        if (columnNumber >= temp.length)
        {
            throw new IndexOutOfBoundsException("Data size is " + temp.length + " columns.  Can't set column " + columnNumber);
        }
        temp[columnNumber] = data;
        modified();
    }

    @Override
    public Optional<LatestVersion<RefexDynamicVersionBI<RefexDynamicRevision>>> getLatestVersion(Class<RefexDynamicVersionBI<RefexDynamicRevision>> type, StampCoordinate coordinate) {
        return RelativePositionCalculator.getCalculator(coordinate)
                .getLatestVersion(this);
 
    }
    
    
}
