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
package org.ihtsdo.otf.tcc.model.cc.refex2;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexMemberAnalogBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * {@link RefexRevisionImpl}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexRevisionImpl extends RefexRevision<RefexRevisionImpl, RefexMemberImpl> implements RefexMemberAnalogBI<RefexRevisionImpl>
{

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getVersion(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public RefexVersionBI<RefexRevisionImpl> getVersion(ViewCoordinate c) throws ContradictionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getVersions(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
	 */
	@Override
	public Collection<? extends RefexVersionBI<RefexRevisionImpl>> getVersions(ViewCoordinate c)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI#getVersions()
	 */
	@Override
	public Collection<? extends RefexVersionBI<RefexRevisionImpl>> getVersions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexMemberVersionBI#getRefexUsageDescriptorNid()
	 */
	@Override
	public int getRefexUsageDescriptorNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexMemberVersionBI#getRefexUsageDescription()
	 */
	@Override
	public RefexUsageDescriptionBI getRefexUsageDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexMemberVersionBI#getData()
	 */
	@Override
	public RefexDataBI[] getData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexMemberVersionBI#getData(int)
	 */
	@Override
	public RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexMemberAnalogBI#setRefexUsageDescriptorNid(int)
	 */
	@Override
	public void setRefexUsageDescriptorNid(int refexUsageDescriptorNid)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexMemberAnalogBI#setData(org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI[])
	 */
	@Override
	public void setData(RefexDataBI[] data) throws PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexMemberAnalogBI#setData(int, org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI)
	 */
	@Override
	public void setData(int columnNumber, RefexDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexRevision#addRefsetTypeNids(java.util.Set)
	 */
	@Override
	protected void addRefsetTypeNids(Set<Integer> allNids)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexRevision#addSpecProperties(org.ihtsdo.otf.tcc.api.blueprint.RefexCAB)
	 */
	@Override
	protected void addSpecProperties(RefexCAB rcs)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexRevision#makeAnalog()
	 */
	@Override
	public RefexRevisionImpl makeAnalog()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexRevision#readyToWriteRefsetRevision()
	 */
	@Override
	public boolean readyToWriteRefsetRevision()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexRevision#getTkRefsetType()
	 */
	@Override
	protected RefexType getTkRefsetType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.component.Revision#makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status, long, int, int, int)
	 */
	@Override
	public RefexRevisionImpl makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.component.Revision#writeFieldsToBdb(com.sleepycat.bind.tuple.TupleOutput)
	 */
	@Override
	protected void writeFieldsToBdb(TupleOutput output)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.component.Revision#getVariableVersionNids()
	 */
	@Override
	public IntArrayList getVariableVersionNids()
	{
		// TODO Auto-generated method stub
		return null;
	}

}