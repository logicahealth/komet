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

import java.util.Arrays;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;

/**
 * {@link RefexDynamicData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class RefexDynamicData implements RefexDynamicDataBI
{
	private transient String name_;
	private transient RefexDynamicDataType type_;
	
	protected byte[] data_;
	
	protected RefexDynamicData(RefexDynamicDataType type, String name)
	{
		type_ = type;
		name_ = name;
	}
	
	protected String getName()
	{
		return name_;
	}
	
	//TODO [REFEX] define a *magic* method that can be called after this RefexData object is read from the DB, 
	//because name and type do not need to be stored - the name can be determined from the array position of 
	//this data object in combination with reading the metadata about this refex.  (on second thought, due to polymorphic,
	//we may now have to write out the type)
	//after the data is read from the DB, but before it is handed back to the user.  Ideally, we just store a ref 
	//to the description nid and column here, then, if getName() is called, we go look at it, and fetch the name
	
	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getRefexDataType()
	 */
	@Override
	public RefexDynamicDataType getRefexDataType()
	{
		return type_;
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
		return "(" + getRefexDataType().name() + " -" + getName() + " - " + getDataObject() +")";
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
}
