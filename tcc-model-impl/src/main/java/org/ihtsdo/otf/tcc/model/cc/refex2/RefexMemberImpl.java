package org.ihtsdo.otf.tcc.model.cc.refex2;

import java.beans.PropertyVetoException;
import java.util.Set;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex2.RefexMemberAnalogBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.computer.version.VersionComputer;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RefexMemberImpl extends RefexMember<RefexRevisionImpl, RefexMemberImpl> implements RefexMemberAnalogBI<RefexRevisionImpl>
{

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI#refexFieldsEqual(org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI)
	 */
	@Override
	public boolean refexFieldsEqual(RefexVersionBI another)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.AnalogGeneratorBI#makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status, long, int, int, int)
	 */
	@Override
	public RefexRevisionImpl makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid)
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
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#addRefsetTypeNids(java.util.Set)
	 */
	@Override
	protected void addRefsetTypeNids(Set<Integer> allNids)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#addSpecProperties(org.ihtsdo.otf.tcc.api.blueprint.RefexCAB)
	 */
	@Override
	protected void addSpecProperties(RefexCAB rcs)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#getTypeNid()
	 */
	@Override
	public int getTypeNid()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#makeAnalog()
	 */
	@Override
	public RefexRevisionImpl makeAnalog()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#refexFieldsEqual(org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent)
	 */
	@Override
	protected boolean refexFieldsEqual(ConceptComponent<RefexRevisionImpl, RefexMemberImpl> obj)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#readMemberFields(com.sleepycat.bind.tuple.TupleInput)
	 */
	@Override
	protected void readMemberFields(TupleInput input)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#readMemberRevision(com.sleepycat.bind.tuple.TupleInput)
	 */
	@Override
	protected RefexRevisionImpl readMemberRevision(TupleInput input)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#readyToWriteRefsetMember()
	 */
	@Override
	public boolean readyToWriteRefsetMember()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#writeMember(com.sleepycat.bind.tuple.TupleOutput)
	 */
	@Override
	protected void writeMember(TupleOutput output)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#getTkRefsetType()
	 */
	@Override
	protected RefexType getTkRefsetType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.refex2.RefexMember#getVersionComputer()
	 */
	@Override
	protected VersionComputer<RefexMember<RefexRevisionImpl, RefexMemberImpl>.Version> getVersionComputer()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent#getVariableVersionNids()
	 */
	@Override
	protected IntArrayList getVariableVersionNids()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
