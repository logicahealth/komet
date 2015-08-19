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

import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicBoolean;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicByteArray;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicDouble;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicFloat;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicInteger;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicLong;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicNid;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicPolymorphic;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicSequence;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicString;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicUUID;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

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
	public DynamicSememeDataType getRefexDataType()
	{
		if (this instanceof TtkRefexDynamicNid) {
			return DynamicSememeDataType.NID;
		}
		if (this instanceof TtkRefexDynamicString) {
			return DynamicSememeDataType.STRING;
		}
		if (this instanceof TtkRefexDynamicInteger) {
			return DynamicSememeDataType.INTEGER;
		}
		if (this instanceof TtkRefexDynamicBoolean) {
			return DynamicSememeDataType.BOOLEAN;
		}
		if (this instanceof TtkRefexDynamicLong) {
			return DynamicSememeDataType.LONG;
		}
		if (this instanceof TtkRefexDynamicByteArray) {
			return DynamicSememeDataType.BYTEARRAY;
		}
		if (this instanceof TtkRefexDynamicFloat) {
			return DynamicSememeDataType.FLOAT;
		}
		if (this instanceof TtkRefexDynamicDouble) {
			return DynamicSememeDataType.DOUBLE;
		}
		if (this instanceof TtkRefexDynamicUUID) {
			return DynamicSememeDataType.UUID;
		}
		if (this instanceof TtkRefexDynamicPolymorphic) {
			return DynamicSememeDataType.POLYMORPHIC;
		}
		if (this instanceof TtkRefexDynamicSequence) {
			return DynamicSememeDataType.SEQUENCE;
		}
		return DynamicSememeDataType.UNKNOWN;
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
	 * available within implementations of the {@link DynamicSememeDataBI} interface.
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

	public static TtkRefexDynamicData typeToClass(DynamicSememeDataType type, byte[] data)
	{
		if (DynamicSememeDataType.NID == type)
		{
			return new TtkRefexDynamicNid(data);
		}
		else if (DynamicSememeDataType.STRING == type)
		{
			return new TtkRefexDynamicString(data);
		}
		else if (DynamicSememeDataType.INTEGER == type)
		{
			return new TtkRefexDynamicInteger(data);
		}
		else if (DynamicSememeDataType.BOOLEAN == type)
		{
			return new TtkRefexDynamicBoolean(data);
		}
		else if (DynamicSememeDataType.LONG == type)
		{
			return new TtkRefexDynamicLong(data);
		}
		else if (DynamicSememeDataType.BYTEARRAY == type)
		{
			return new TtkRefexDynamicByteArray(data);
		}
		else if (DynamicSememeDataType.FLOAT == type)
		{
			return new TtkRefexDynamicFloat(data);
		}
		else if (DynamicSememeDataType.DOUBLE == type)
		{
			return new TtkRefexDynamicDouble(data);
		}
		else if (DynamicSememeDataType.UUID == type)
		{
			return new TtkRefexDynamicUUID(data);
		}
		else if (DynamicSememeDataType.POLYMORPHIC == type)
		{
			return new TtkRefexDynamicPolymorphic(data);
		}
		else if (DynamicSememeDataType.UNKNOWN == type)
		{
			throw new RuntimeException("No implementation exists for type unknown");
		}
		throw new RuntimeException("Implementation error");
	}
}
