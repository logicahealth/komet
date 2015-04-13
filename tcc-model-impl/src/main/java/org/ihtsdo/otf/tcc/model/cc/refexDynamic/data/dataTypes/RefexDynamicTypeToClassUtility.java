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
package org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes;

import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;

/**
 * {@link RefexDynamicTypeToClassUtility}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexDynamicTypeToClassUtility
{
	public static RefexDynamicData typeToClass(RefexDynamicDataType type, byte[] data, int assemblageNid, int columnNumber) 
	{
		switch (type)
		{
			case ARRAY: return new RefexDynamicArray<RefexDynamicData>(data, assemblageNid, columnNumber);
			case BOOLEAN: return new RefexDynamicBoolean(data, assemblageNid, columnNumber);
			case BYTEARRAY: return new RefexDynamicByteArray(data, assemblageNid, columnNumber);
			case DOUBLE: return new RefexDynamicDouble(data, assemblageNid, columnNumber);
			case FLOAT: return new RefexDynamicFloat(data, assemblageNid, columnNumber);
			case INTEGER: return new RefexDynamicInteger(data, assemblageNid, columnNumber);
			case LONG: return new RefexDynamicLong(data, assemblageNid, columnNumber);
			case NID: return new RefexDynamicNid(data, assemblageNid, columnNumber);
			case STRING: return new RefexDynamicString(data, assemblageNid, columnNumber);
			case UUID: return new RefexDynamicUUID(data, assemblageNid, columnNumber);
			case POLYMORPHIC: case UNKNOWN: throw new RuntimeException("No implementation exists for type unknown");
			default: throw new RuntimeException("Implementation error");
		}
	}
	
	protected static RefexDynamicData typeToClass(RefexDynamicDataType type, byte[] data)
	{
		switch (type)
		{
			case ARRAY: return new RefexDynamicArray<RefexDynamicData>(data);
			case BOOLEAN: return new RefexDynamicBoolean(data);
			case BYTEARRAY: return new RefexDynamicByteArray(data);
			case DOUBLE: return new RefexDynamicDouble(data);
			case FLOAT: return new RefexDynamicFloat(data);
			case INTEGER: return new RefexDynamicInteger(data);
			case LONG: return new RefexDynamicLong(data);
			case NID: return new RefexDynamicNid(data);
			case STRING: return new RefexDynamicString(data);
			case UUID: return new RefexDynamicUUID(data);
			case UNKNOWN: case POLYMORPHIC: throw new RuntimeException("Should be impossible");
			default:
				throw new RuntimeException("Design failure");
		}
	}
	
	protected static Class<? extends RefexDynamicData> implClassForType(RefexDynamicDataType type)
	{
		switch (type)
		{
			case ARRAY: return RefexDynamicArray.class;
			case BOOLEAN: return RefexDynamicBoolean.class;
			case BYTEARRAY: return RefexDynamicByteArray.class;
			case DOUBLE: return RefexDynamicDouble.class;
			case FLOAT: return RefexDynamicFloat.class;
			case INTEGER: return RefexDynamicInteger.class;
			case LONG: return RefexDynamicLong.class;
			case NID: return RefexDynamicNid.class;
			case STRING: return RefexDynamicString.class;
			case UUID: return RefexDynamicUUID.class;
			case UNKNOWN: case POLYMORPHIC: throw new RuntimeException("Should be impossible");
			default:
				throw new RuntimeException("Design failure");
		}
	}
}
