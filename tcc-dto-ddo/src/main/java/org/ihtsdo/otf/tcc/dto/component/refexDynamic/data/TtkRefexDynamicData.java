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

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeArray;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeBoolean;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeByteArray;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeDouble;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeFloat;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeInteger;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeSequence;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUID;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import org.apache.logging.log4j.LogManager;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes.TtkRefexDynamicArray;
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

/**
 * {@link TtkRefexDynamicData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class TtkRefexDynamicData
{
	@XmlAttribute
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
		if (this instanceof TtkRefexDynamicArray) {
			return DynamicSememeDataType.ARRAY;
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
		else if (DynamicSememeDataType.ARRAY== type)
		{
			return new TtkRefexDynamicArray<TtkRefexDynamicData>(data);
		}
		else if (DynamicSememeDataType.UNKNOWN == type)
		{
			throw new RuntimeException("No implementation exists for type unknown");
		}
		throw new RuntimeException("Implementation error");
	}
	
	protected static Class<? extends TtkRefexDynamicData> implClassForType(DynamicSememeDataType type)
	{
		switch (type)
		{
			case ARRAY: return TtkRefexDynamicArray.class;
			case BOOLEAN: return TtkRefexDynamicBoolean.class;
			case BYTEARRAY: return TtkRefexDynamicByteArray.class;
			case DOUBLE: return TtkRefexDynamicDouble.class;
			case FLOAT: return TtkRefexDynamicFloat.class;
			case INTEGER: return TtkRefexDynamicInteger.class;
			case LONG: return TtkRefexDynamicLong.class;
			case NID: return TtkRefexDynamicNid.class;
			case STRING: return TtkRefexDynamicString.class;
			case UUID: return TtkRefexDynamicUUID.class;
			case SEQUENCE: return TtkRefexDynamicSequence.class;
			case UNKNOWN: case POLYMORPHIC: throw new RuntimeException("Should be impossible");
			default:
				throw new RuntimeException("Design failure");
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static TtkRefexDynamicData convertPolymorphicDataColumn(DynamicSememeDataBI defaultValue, DynamicSememeDataType columnType) throws PropertyVetoException
	{
		TtkRefexDynamicData result;

		if (defaultValue != null)
		{
			try
			{
				if (DynamicSememeDataType.ARRAY == columnType)
				{
					result = new TtkRefexDynamicArray((TtkRefexDynamicData[]) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.BOOLEAN == columnType)
				{
					result = new TtkRefexDynamicBoolean((Boolean) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.BYTEARRAY == columnType)
				{
					result = new TtkRefexDynamicByteArray((byte[]) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.DOUBLE == columnType)
				{
					result = new TtkRefexDynamicDouble((Double) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.FLOAT == columnType)
				{
					result = new TtkRefexDynamicFloat((Float) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.INTEGER == columnType)
				{
					result = new TtkRefexDynamicInteger((Integer) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.LONG == columnType)
				{
					result = new TtkRefexDynamicLong((Long) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.NID == columnType)
				{
					result = new TtkRefexDynamicNid((Integer) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.STRING == columnType)
				{
					result = new TtkRefexDynamicString((String) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.UUID == columnType)
				{
					result = new TtkRefexDynamicUUID((UUID) defaultValue.getDataObject());
				}
				else if (DynamicSememeDataType.POLYMORPHIC == columnType)
				{
					throw new RuntimeException("Error in column - if default value is provided, the type cannot be polymorphic");
				}
				else
				{
					throw new RuntimeException("Actually, the implementation is broken.  Ooops.");
				}
			}
			catch (ClassCastException e)
			{
				throw new RuntimeException("Error in column - if default value is provided, the type must be compatible with the the column descriptor type");
			}
		}
		else
		{
			result = null;
		}
		return result;
	}
	
	public static DynamicSememeDataBI[] convertFromTTK(TtkRefexDynamicData[] data)
	{
		DynamicSememeDataBI[] convertedData = data == null ? null : new DynamicSememeDataBI[data.length];
		if (data != null)
		{
			for (int i = 0; i < data.length; i++)
			{
				convertedData[i] = convertFromTTK(data[i]);
			}
		}
		return convertedData;
	}
	
	public static DynamicSememeDataBI convertFromTTK(TtkRefexDynamicData data)
	{
		if (data == null)
		{
			return null;
		}
		switch (data.getRefexDataType())
		{
			case ARRAY: 
			{
				TtkRefexDynamicData[] castData = (TtkRefexDynamicData[])data.getDataObject();
				DynamicSememeDataBI[] convertedData = new DynamicSememeDataBI[castData.length];
				for (int i = 0; i < convertedData.length; i++)
				{
					convertedData[i] = convertFromTTK(castData[i]);
				}
				
				return new DynamicSememeArray<>(convertedData);
			}
			case BOOLEAN: return new DynamicSememeBoolean(((TtkRefexDynamicBoolean)data).getDataBoolean());
			case BYTEARRAY: return new DynamicSememeByteArray(((TtkRefexDynamicByteArray)data).getDataByteArray());
			case DOUBLE: return new DynamicSememeDouble(((TtkRefexDynamicDouble)data).getDataDouble());
			case FLOAT: return new DynamicSememeFloat(((TtkRefexDynamicFloat)data).getDataFloat());
			case INTEGER:  return new DynamicSememeInteger(((TtkRefexDynamicInteger)data).getDataInteger());
			case NID: return new DynamicSememeNid(((TtkRefexDynamicNid)data).getDataNid());
			case STRING: return new DynamicSememeString(((TtkRefexDynamicString)data).getDataString());
			case UUID: return new DynamicSememeUUID(((TtkRefexDynamicUUID)data).getDataUUID());
			case SEQUENCE: return new DynamicSememeSequence(((TtkRefexDynamicSequence)data).getDataSequence());
			case UNKNOWN: case POLYMORPHIC: throw new RuntimeException("Should be impossible");
			default:
				throw new RuntimeException("Design failure");
		}
	}
	
	public static DynamicSememeDataType classToType(Class<?> c) 
	{
		if (TtkRefexDynamicNid.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.NID;
		}
		if (TtkRefexDynamicString.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.STRING;
		}
		if (TtkRefexDynamicInteger.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.INTEGER;
		}
		if (TtkRefexDynamicBoolean.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.BOOLEAN;
		}
		if (TtkRefexDynamicLong.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.LONG;
		}
		if (TtkRefexDynamicByteArray.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.BYTEARRAY;
		}
		if (TtkRefexDynamicFloat.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.FLOAT;
		}
		if (TtkRefexDynamicDouble.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.DOUBLE;
		}
		if (TtkRefexDynamicUUID.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.UUID;
		}
		if (TtkRefexDynamicPolymorphic.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.POLYMORPHIC;
		}
		if (TtkRefexDynamicArray.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.ARRAY;
		}
		if (TtkRefexDynamicSequence.class.isAssignableFrom(c)) {
			return DynamicSememeDataType.SEQUENCE;
		}
		LogManager.getLogger().warn("Couldn't map class {} to type!", c);
		return DynamicSememeDataType.UNKNOWN;
	}
}
