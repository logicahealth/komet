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

package gov.vha.isaac.ochre.model.sememe.version;

import java.util.Arrays;
import javax.naming.InvalidNameException;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI;
import gov.vha.isaac.ochre.model.DataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeData;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeTypeToClassUtility;

/**
 * 
 * {@link DynamicSememeImpl}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeImpl extends SememeVersionImpl<DynamicSememeImpl>implements MutableDynamicSememe<DynamicSememeImpl>
{
	private DynamicSememeDataBI[] data_ = null;

	public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container, int stampSequence, short versionSequence, DataBuffer data)
	{
		super(container, stampSequence, versionSequence);

		// read the following format - dataFieldCount [dataFieldType dataFieldBytes] [dataFieldType dataFieldBytes] ...
		int colCount = data.getInt();
		data_ = new DynamicSememeDataBI[colCount];
		for (int i = 0; i < colCount; i++)
		{
			DynamicSememeDataType dt = DynamicSememeDataType.getFromToken(data.getInt());
			if (dt == DynamicSememeDataType.UNKNOWN)
			{
				data_[i] = null;
			}
			else
			{
				data_[i] = DynamicSememeTypeToClassUtility.typeToClass(dt, data.getByteArrayField(), getAssemblageSequence(), i);
			}
		}
	}

	//TODO setData and constructor with data will need to run the validator!
	
	public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container, int stampSequence, short versionSequence)
	{
		super(container, stampSequence, versionSequence);
	}

	@Override
	protected void writeVersionData(DataBuffer data)
	{
		super.writeVersionData(data);
		//Write with the following format - 
		//dataFieldCount [dataFieldType dataFieldBytes] [dataFieldType dataFieldBytes] ...
		if (getData() != null)
		{
			data.putInt(getData().length);
			for (DynamicSememeDataBI column : getData())
			{
				if (column == null)
				{
					data.putInt(DynamicSememeDataType.UNKNOWN.getTypeToken());
				}
				else
				{
					data.putInt(column.getDynamicSememeDataType().getTypeToken());
					data.putByteArrayField(column.getData());
				}
			}
		}
		else
		{
			data.putInt(0);
		}
	}

	@Override
	public SememeType getSememeType()
	{
		return SememeType.DYNAMIC;
	};

	/**
	 * @see gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe#getData()
	 */
	@Override
	public DynamicSememeDataBI[] getData()
	{
		return data_ == null ? new DynamicSememeData[] {} : data_;
	}

	@Override
	public DynamicSememeDataBI getData(int columnNumber) throws IndexOutOfBoundsException
	{
		return getData()[columnNumber];
	}
	
	//TODO - Dan not sure if there is still the notion of an index of components that references nids... would need to port some code that reads 
	//the extra columns of the dynamic sememe, if so

	@Override
	public DynamicSememeDataBI getData(String columnName) throws InvalidNameException
	{
		for (DynamicSememeColumnInfo ci : getDynamicSememeUsageDescription().getColumnInfo())
		{
			if (ci.getColumnName().equals(columnName))
			{
				return getData(ci.getColumnOrder());
			}
		}
		throw new InvalidNameException("Could not find a column with name '" + columnName + "'");
	}

	@Override
	public DynamicSememeUsageDescriptionBI getDynamicSememeUsageDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(DynamicSememeDataBI[] data)
	{
		if (data_ != null)
		{
			checkUncommitted();
		}
		data_ = data == null ? new DynamicSememeData[] {} : data;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DynamicSememeData≤");
		sb.append(Arrays.toString(getData()));
		toString(sb);//stamp info
		sb.append('≥');
		return sb.toString();
	}

	//TODO dan remove if we don't actually need equals
	//    @Override
	//    public boolean equals(Object obj) {
	//        if (obj == null) {
	//            return false;
	//        }
	//
	//        if (RefexDynamicMember.class.isAssignableFrom(obj.getClass())) {
	//            RefexDynamicMember another = (RefexDynamicMember) obj;
	//            if (this.getAssemblageNid() == another.getAssemblageNid() && this.getReferencedComponentNid() == another.getReferencedComponentNid() 
	//                    && Arrays.deepEquals(this.getData(), another.getData())) {
	//                return true;
	//            }
	//        }
	//
	//        return false;
	//    }
	//
	//    @Override
	//    public boolean fieldsEqual(ConceptComponent<RefexDynamicRevision, RefexDynamicMember> obj) {
	//        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
	//            RefexDynamicMember another = (RefexDynamicMember) obj;
	//
	//            if (this.getAssemblageNid() != another.getAssemblageNid()) {
	//                return false;
	//            }
	//
	//            if (refexFieldsEqual(obj)) {
	//                return conceptComponentFieldsEqual(another);
	//            }
	//        }
	//
	//        return false;
	//    }
	
//	 protected boolean refexFieldsEqual(ConceptComponent<RefexDynamicRevision, RefexDynamicMember> obj) {
//	       if (RefexDynamicMember.class.isAssignableFrom(obj.getClass())) {
//	          RefexDynamicMember another = (RefexDynamicMember) obj;
//	          return refexDataFieldsEqual(another.getData());
//	       }
//	       return false;
//	    }
//	    
//	    @Override
//	    public boolean refexDataFieldsEqual(RefexDynamicDataBI[] another) {
//	        return Arrays.deepEquals(getData(), another);
//	    }
	//
	//    @Override
	//    public int hashCode() {
	//        return Hashcode.compute(new int[]{referencedComponentNid, assemblageNid, Arrays.deepHashCode(getData())});
	//    }
	
//TODO see if we have a need to construct from TTK?
	
	//    public RefexDynamicMember(TtkRefexDynamicMemberChronicle refsetMember, int enclosingConceptNid) throws IOException {
//        super(refsetMember, enclosingConceptNid);
//        assemblageNid = PersistentStore.get().getNidForUuids(refsetMember.refexAssemblageUuid);
//        referencedComponentNid = PersistentStore.get().getNidForUuids(refsetMember.getComponentUuid());
//        primordialStamp = PersistentStore.get().getStamp(refsetMember);
//        assert primordialStamp != Integer.MAX_VALUE;
//        assert referencedComponentNid != Integer.MAX_VALUE;
//        assert assemblageNid != Integer.MAX_VALUE;
//        
//        if (refsetMember.getData() != null)
//        {
//            data_= new RefexDynamicDataBI[refsetMember.getData().length];
//            for (int i = 0; i < data_.length; i++)
//            {
//                if (refsetMember.getData()[i] == null)
//                {
//                    data_[i] = null;
//                }
//                else
//                {
//                    data_[i] = DynamicSememeTypeToClassUtility.typeToClass(refsetMember.getData()[i].getRefexDataType(), refsetMember.getData()[i].getData(), 
//                        assemblageNid, i);
//                }
//            }
//        }
//
//        if (refsetMember.getRevisionList() != null) {
//            revisions = new RevisionSet(primordialStamp);
//
//            for (TtkRefexDynamicRevision eVersion : refsetMember.getRevisionList()) {
//                revisions.add(new RefexDynamicRevision(eVersion, this));
//            }
//        }
//        
//    }

}
