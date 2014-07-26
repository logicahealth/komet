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
package org.ihtsdo.otf.tcc.model.cc.refexDynamic.data;

import java.io.IOException;
import java.util.Arrays;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexBoolean;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexByteArray;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexDouble;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexFloat;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexInteger;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexLong;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexNid;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexString;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes.RefexUUID;

/**
 * {@link RefexDynamicData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class RefexDynamicData implements RefexDynamicDataBI
{
	//assemblageNid_,columnNumber and name_ are an either-or deal.  If we have the name, we 
	//don't need the assemblageNid/columnNumber.  If we don't have the name, we use the assemblage
	//nid to get it (on demand).  which one is populated will depend on how it was constructed.
	private transient int assemblageNid_, columnNumber_;
	private transient String name_;
	
	protected byte[] data_;
	
	protected RefexDynamicData(byte[] data, int assemblageNid, int columnNumber)
	{
		data_ = data;
		assemblageNid_ = assemblageNid;
		columnNumber_ = columnNumber;
		name_ = null;
	}
	
	protected RefexDynamicData(String name)
	{
		name_ = name;
	}
	
	protected String getName() throws IOException, ContradictionException
	{
		if (name_ == null)
		{
			name_ = RefexDynamicUsageDescription.read(assemblageNid_).getColumnInfo()[columnNumber_].getColumnName();
		}
		return name_;
	}
	
	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getRefexDataType()
	 */
	@Override
	public RefexDynamicDataType getRefexDataType()
	{
		return RefexDynamicDataType.classToType(this.getClass());
	}
	
	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getData()
	 */
	@Override
	public byte[] getData()
	{
		return data_;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String name;
		try
		{
			name = getName();
		}
		catch (IOException | ContradictionException e)
		{
			name = "Error getting column name from assemblageNid " + assemblageNid_;
		}
		
		return "(" + getRefexDataType().name() + " - " + name + " - " + getDataObject() +")";
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data_);
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RefexDynamicData other = (RefexDynamicData) obj;
		if (!Arrays.equals(data_, other.data_))
			return false;
		return true;
	}
	
	public static RefexDynamicData typeToClass(RefexDynamicDataType type, byte[] data, int assemblageNid, int columnNumber) 
	{
		if (RefexDynamicDataType.NID == type) {
			return new RefexNid(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.STRING == type) {
			return new RefexString(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.INTEGER == type) {
			return new RefexInteger(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.BOOLEAN == type) {
			return new RefexBoolean(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.LONG == type) {
			return new RefexLong(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.BYTEARRAY == type) {
			return new RefexByteArray(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.FLOAT == type) {
			return new RefexFloat(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.DOUBLE == type) {
			return new RefexDouble(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.UUID == type) {
			return new RefexUUID(data, assemblageNid, columnNumber);
		}
		if (RefexDynamicDataType.POLYMORPHIC == type || RefexDynamicDataType.UNKNOWN == type) {
			throw new RuntimeException("No implementation exists for type unknown");
		}
		throw new RuntimeException("Implementation error");
	}
}
