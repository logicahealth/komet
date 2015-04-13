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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;

/**
 * {@link RefexDynamicData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class RefexDynamicData implements RefexDynamicDataBI
{
	//name_ is a cache of data looked up from the assemblageNid and columnNumber.
	//Note that name is only required by the JavaFX getDataXXXProperty() calls in the subclasses
	//normally, this is never used at all.
	private transient int assemblageNid_;
	private transient int columnNumber_ = -1;
	private transient String name_;
	
	protected byte[] data_;
	
	protected RefexDynamicData(byte[] data)
	{
		data_ = data;
	}
	
	protected RefexDynamicData(byte[] data, int assemblageNid, int columnNumber)
	{
		data_ = data;
		assemblageNid_ = assemblageNid;
		columnNumber_ = columnNumber;
		name_ = null;
	}
	
	protected RefexDynamicData()
	{
	}
	
	protected String getName() throws IOException, ContradictionException
	{
		if (name_ == null)
		{
			if (columnNumber_ == -1)
			{
				throw new ContradictionException("No data is available to lookup the name.  Has this refex been added to a RefexDynamicCAB via a setData(...) call?");
			}
			else
			{
				name_ = RefexDynamicUsageDescription.read(assemblageNid_).getColumnInfo()[columnNumber_].getColumnName();
			}
		}
		return name_;
	}
	
	/**
	 * This method is not intended for public use.
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#setNameIfAbsent(java.lang.String)
	 */
	@Override
	public void setNameIfAbsent(String name)
	{
		if (name_ == null && columnNumber_ == -1)
		{
			name_ = name;
		}
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
		catch (Exception e)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error reading name", e);
			name = "???";
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
}
