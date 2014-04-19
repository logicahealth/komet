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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
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
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * {@link RefexDynamicRevision}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("deprecation")

public class RefexDynamicRevision extends Revision<RefexDynamicRevision, RefexDynamicMember> implements RefexDynamicVersionBI<RefexDynamicRevision>, RefexDynamicBuilderBI
{

    public RefexDynamicRevision() {
        super();
    }

    public RefexDynamicRevision(int statusAtPositionNid, RefexDynamicMember primordialComponent) {
        super(statusAtPositionNid, primordialComponent);
    }

    public RefexDynamicRevision(TtkRevision eVersion, RefexDynamicMember member)  throws IOException{
        super(eVersion.getStatus(), eVersion.getTime(), P.s.getNidForUuids(eVersion.getAuthorUuid()),
                 P.s.getNidForUuids(eVersion.getModuleUuid()), P.s.getNidForUuids(eVersion.getPathUuid()),  member);
    }

    public RefexDynamicRevision(TupleInput input, RefexDynamicMember primordialComponent) {
        super(input, primordialComponent);
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
//TODO enhance for new data fields
        if (RefexDynamicRevision.class.isAssignableFrom(obj.getClass())) {
            RefexDynamicRevision another = (RefexDynamicRevision) obj;

            if (this.stamp == another.stamp) {
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
        //TODO enhance

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
//        RefexCAB rcs = new RefexCAB(
//                getTkRefsetType(),
//                P.s.getUuidPrimordialForNid(getReferencedComponentNid()),
//                getAssemblageNid(),
//                getVersion(vc), 
//                vc, 
//                idDirective, 
//                refexDirective);

//        addSpecProperties(rcs);
//TODO fix CAB stuff
        return null;//rcs;
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

       //
    }

    protected void addSpecProperties(RefexCAB rcs) {

       // no fields to add...
    }

    public boolean readyToWriteRefsetRevision() {
       return true;
    }

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        //TODO enhance
       // nothing to write
    }


    @Override
    public IntArrayList getVariableVersionNids() {
       return new IntArrayList(2);
    }

    @Override
    public RefexDynamicMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
       return (RefexDynamicMember.Version) ((RefexDynamicMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<? extends RefexDynamicMember.Version> getVersions() {
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
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getRefexUsageDescription()
     */
    @Override
    public RefexDynamicUsageDescription getRefexUsageDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData()
     */
    @Override
    public RefexDynamicDataBI[] getData() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(int)
     */
    @Override
    public RefexDynamicDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(java.lang.String)
     */
    @Override
    public RefexDynamicDataBI getData(String columnName) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
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
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(int, org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI)
	 */
	@Override
	public void setData(int columnNumber, RefexDynamicDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}
}