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
import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDynamicTypeToClassUtility;

/**
 * {@link RefexDynamicRevision}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class RefexDynamicRevision extends Revision<RefexDynamicRevision, RefexDynamicMember> implements RefexDynamicVersionBI<RefexDynamicRevision>, RefexDynamicBuilderBI
{
    protected RefexDynamicDataBI[] data_;
    
    public RefexDynamicRevision() {
        super();
    }

    public RefexDynamicRevision(int statusAtPositionNid, RefexDynamicMember primordialComponent) {
        super(statusAtPositionNid, primordialComponent);
    }

    public RefexDynamicRevision(TtkRevision eVersion, RefexDynamicMember member)  throws IOException{
        super(eVersion.getStatus(), eVersion.getTime(), PersistentStore.get().getNidForUuids(eVersion.getAuthorUuid()),
                 PersistentStore.get().getNidForUuids(eVersion.getModuleUuid()), PersistentStore.get().getNidForUuids(eVersion.getPathUuid()),  member);
    }

    public RefexDynamicRevision(DataInputStream input, RefexDynamicMember primordialComponent) throws IOException {
        super(input, primordialComponent);
        readMemberFields(input);
    }

    public RefexDynamicRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, RefexDynamicMember primordialComponent) {
        super(status, time, authorNid, moduleNid, pathNid, primordialComponent);
    }
    
    protected RefexDynamicRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
            RefexDynamicRevision another) {
        super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(primordialComponent.referencedComponentNid);
        allNids.add(primordialComponent.assemblageNid);
        addRefsetTypeNids(allNids);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (RefexDynamicRevision.class.isAssignableFrom(obj.getClass())) {
            RefexDynamicRevision another = (RefexDynamicRevision) obj;

            if (this.stamp == another.stamp && this.getAssemblageNid() == another.getAssemblageNid() 
                    && this.getReferencedComponentNid() == another.getReferencedComponentNid() && Arrays.deepEquals(this.getData(), another.getData())) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public boolean refexDataFieldsEqual(RefexDynamicDataBI[] another) {
        return primordialComponent.refexDataFieldsEqual(another);
    }

    @Override
    public final boolean readyToWriteRevision() {
        assert readyToWriteRefsetRevision() : assertionString();

        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(super.toString());
        buf.append(Arrays.toString(getData()));

        return buf.toString();
    }

    @Override
    public String toUserString() {
        return toString();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getAssemblageNid() {
        return primordialComponent.assemblageNid;
    }

    @Override
    public RefexDynamicMember getPrimordialVersion() {
        return primordialComponent;
    }

    @Override
    public int getReferencedComponentNid() {
        return primordialComponent.getReferencedComponentNid();
    }

    @Override
    public RefexDynamicCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException,
            InvalidCAB, ContradictionException {
        RefexDynamicCAB rdc = new RefexDynamicCAB(
                PersistentStore.get().getUuidPrimordialForNid(getReferencedComponentNid()),
                getAssemblageNid(),
                Optional.of(this), 
                Optional.of(vc), 
                idDirective, 
                refexDirective);

        rdc.setData(getData(), vc);
        return rdc;
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
        primordialComponent.setAssemblageNid(collectionNid);
    }

    @Override
    public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
        primordialComponent.setReferencedComponentNid(componentNid);
    }

    /**
     * From MembershipRevision below here
     */
    
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

    public boolean readyToWriteRefsetRevision() {
        //I don't think we need to do anything here - with construction via Blueprint only, it should be impossible to create one that 
        //isn't ready to write
       return true;
    }

    protected void readMemberFields(DataInputStream input) throws IOException {

        //read the following format - 
        //dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
        int colCount = input.readInt();
        data_ = new RefexDynamicDataBI[colCount];
        for (int i = 0; i < colCount; i++)
        {
            RefexDynamicDataType dt = RefexDynamicDataType.getFromToken(input.readInt());
            if (dt == RefexDynamicDataType.UNKNOWN)
            {
                data_[i] = null;
            }
            else
            {
                int dataLength = input.readInt();
                byte[] data = new byte[dataLength];
                input.read(data);
                
                data_[i] = RefexDynamicTypeToClassUtility.typeToClass(dt, data, getAssemblageNid(), i);
            }
        }
    }

    public IntArrayList getVariableVersionNids() {
        //No idea what this is for.  It doesn't seem to be used...
       //return new IntArrayList(0);
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RefexDynamicMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
       return ((RefexDynamicMember) primordialComponent).getVersion(c);
    }

    @Override
    public List<? extends RefexDynamicMemberVersion> getVersions() {
       return ((RefexDynamicMember) primordialComponent).getVersions();
    }


    @Override
    public List<? extends RefexDynamicMemberVersion> getVersionList() {
       return ((RefexDynamicMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexDynamicVersionBI<RefexDynamicRevision>> getVersions(ViewCoordinate c) {
       return ((RefexDynamicMember) primordialComponent).getVersions(c);
    }
    
    /**
     * New data fields from here down
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
     * 
     * Will not return null
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
     * 
     * Note that this incurs a performance penalty over using {@link #getData(int)}
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
     * @see org.ihtsdo.otf.tcc.model.cc.component.Revision#makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status, long, int, int, int)
     */
    @Override
    public RefexDynamicRevision makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        throw new UnsupportedOperationException();
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
    public Optional<LatestVersion<RefexDynamicVersionBI<RefexDynamicRevision>>> getLatestVersion(Class<RefexDynamicVersionBI<RefexDynamicRevision>> type, StampCoordinate<?> coordinate) {
        return primordialComponent.getLatestVersion(type, coordinate);
    }
}