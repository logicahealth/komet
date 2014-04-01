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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.model.cc.refex3.type_dynamic;

import java.beans.PropertyVetoException;
import java.util.Set;

import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI;
import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI;
import org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicAnalogBI;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * {@link DynamicMember}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class DynamicMember  extends RefexMember<DynamicRevision, DynamicMember>
implements RefexDynamicAnalogBI<DynamicRevision> {

    /**
     * @see org.ihtsdo.otf.tcc.api.refex.RefexVersionBI#refexFieldsEqual(org.ihtsdo.otf.tcc.api.refex.RefexVersionBI)
     */
    @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.AnalogGeneratorBI#makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status, long, int, int, int)
     */
    @Override
    public DynamicRevision makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicVersionBI#getRefexUsageDescriptorNid()
     */
    @Override
    public int getRefexUsageDescriptorNid() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicVersionBI#getRefexUsageDescription()
     */
    @Override
    public RefexUsageDescriptionBI getRefexUsageDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicVersionBI#getData()
     */
    @Override
    public RefexDataBI[] getData() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicVersionBI#getData(int)
     */
    @Override
    public RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicAnalogBI#setRefexUsageDescriptorNid(int)
     */
    @Override
    public void setRefexUsageDescriptorNid(int refexUsageDescriptorNid) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicAnalogBI#setData(org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI[])
     */
    @Override
    public void setData(RefexDataBI[] data) throws PropertyVetoException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex3.type_dynamic.RefexDynamicAnalogBI#setData(int, org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI)
     */
    @Override
    public void setData(int columnNumber, RefexDataBI data) throws IndexOutOfBoundsException, PropertyVetoException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#addRefsetTypeNids(java.util.Set)
     */
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#addSpecProperties(org.ihtsdo.otf.tcc.api.blueprint.RefexCAB)
     */
    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#getTypeNid()
     */
    @Override
    public int getTypeNid() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#makeAnalog()
     */
    @Override
    public DynamicRevision makeAnalog() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#refexFieldsEqual(org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent)
     */
    @Override
    protected boolean refexFieldsEqual(ConceptComponent<DynamicRevision, DynamicMember> obj) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#readMemberFields(com.sleepycat.bind.tuple.TupleInput)
     */
    @Override
    protected void readMemberFields(TupleInput input) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#readMemberRevision(com.sleepycat.bind.tuple.TupleInput)
     */
    @Override
    protected DynamicRevision readMemberRevision(TupleInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#readyToWriteRefsetMember()
     */
    @Override
    public boolean readyToWriteRefsetMember() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#writeMember(com.sleepycat.bind.tuple.TupleOutput)
     */
    @Override
    protected void writeMember(TupleOutput output) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#getTkRefsetType()
     */
    @Override
    protected RefexType getTkRefsetType() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.refex.RefexMember#getVersionComputer()
     */
    @Override
    protected VersionComputer<RefexMember<DynamicRevision, DynamicMember>.Version> getVersionComputer() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent#getVariableVersionNids()
     */
    @Override
    protected IntArrayList getVariableVersionNids() {
        // TODO Auto-generated method stub
        return null;
    }

}
