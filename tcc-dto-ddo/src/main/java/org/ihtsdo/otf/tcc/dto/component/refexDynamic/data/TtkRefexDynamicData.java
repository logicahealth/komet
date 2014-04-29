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
package org.ihtsdo.otf.tcc.dto.component.refexDynamic.data;

import java.util.Arrays;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexBoolean;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexByteArray;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDouble;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexFloat;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexInteger;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexLong;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexNid;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexPolymorphic;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexString;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexUUID;

/**
 * {@link TtkRefexDynamicData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class TtkRefexDynamicData
{
	protected byte[] data_;

	protected TtkRefexDynamicData(byte[] data)
	{
		data_ = data;
	}

	protected TtkRefexDynamicData()
	{
	}

	/**
	 * @return The type information of the data
	 */
	public RefexDynamicDataType getRefexDataType()
	{
		if (this instanceof TtkRefexNid) {
			return RefexDynamicDataType.NID;
		}
		if (this instanceof TtkRefexString) {
			return RefexDynamicDataType.STRING;
		}
		if (this instanceof TtkRefexInteger) {
			return RefexDynamicDataType.INTEGER;
		}
		if (this instanceof TtkRefexBoolean) {
			return RefexDynamicDataType.BOOLEAN;
		}
		if (this instanceof TtkRefexLong) {
			return RefexDynamicDataType.LONG;
		}
		if (this instanceof TtkRefexByteArray) {
			return RefexDynamicDataType.BYTEARRAY;
		}
		if (this instanceof TtkRefexFloat) {
			return RefexDynamicDataType.FLOAT;
		}
		if (this instanceof TtkRefexDouble) {
			return RefexDynamicDataType.DOUBLE;
		}
		if (this instanceof TtkRefexUUID) {
			return RefexDynamicDataType.UUID;
		}
		if (this instanceof TtkRefexPolymorphic) {
			return RefexDynamicDataType.POLYMORPHIC;
		}
		return RefexDynamicDataType.UNKNOWN;
	}

	/**
	 * @return The data object itself, in its most compact, serialized form. You
	 * probably don't want this method unless you are doing something clever....
	 * For a getData() method that doesn't require deserialization, see the {@link #getDataObject()} method.
	 * For a method that doesn't require casting the output, see the getDataXXX() method available within
	 * implementations of the {@link TtkRefexDynamicData} abstract class.
	 */
	public byte[] getData()
	{
		return data_;
	}

	/**
	 * @return The data object itself.
	 * For a getData() method that doesn't require casting of the output, see the getDataXXX() method
	 * available within implementations of the {@link RefexDynamicDataBI} interface.
	 */
	public abstract Object getDataObject();

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "(" + getRefexDataType().name() + " - " + getDataObject() + ")";
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
		TtkRefexDynamicData other = (TtkRefexDynamicData) obj;
		if (!Arrays.equals(data_, other.data_))
			return false;
		return true;
	}

	public static TtkRefexDynamicData typeToClass(RefexDynamicDataType type, byte[] data)
	{
		if (RefexDynamicDataType.NID == type)
		{
			return new TtkRefexNid(data);
		}
		else if (RefexDynamicDataType.STRING == type)
		{
			return new TtkRefexString(data);
		}
		else if (RefexDynamicDataType.INTEGER == type)
		{
			return new TtkRefexInteger(data);
		}
		else if (RefexDynamicDataType.BOOLEAN == type)
		{
			return new TtkRefexBoolean(data);
		}
		else if (RefexDynamicDataType.LONG == type)
		{
			return new TtkRefexLong(data);
		}
		else if (RefexDynamicDataType.BYTEARRAY == type)
		{
			return new TtkRefexByteArray(data);
		}
		else if (RefexDynamicDataType.FLOAT == type)
		{
			return new TtkRefexFloat(data);
		}
		else if (RefexDynamicDataType.DOUBLE == type)
		{
			return new TtkRefexDouble(data);
		}
		else if (RefexDynamicDataType.UUID == type)
		{
			return new TtkRefexUUID(data);
		}
		else if (RefexDynamicDataType.POLYMORPHIC == type)
		{
			return new TtkRefexPolymorphic(data);
		}
		else if (RefexDynamicDataType.UNKNOWN == type)
		{
			throw new RuntimeException("No implementation exists for type unknown");
		}
		throw new RuntimeException("Implementation error");
	}
}
