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
package gov.vha.isaac.ochre.model.sememe.dataTypes;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

/**
 * {@link DynamicSememeTypeToClassUtility}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class DynamicSememeTypeToClassUtility
{
	public static DynamicSememeData typeToClass(DynamicSememeDataType type, byte[] data, int assemblageSequence, int columnNumber) 
	{
		switch (type)
		{
			case ARRAY: return new DynamicSememeArray<DynamicSememeData>(data, assemblageSequence, columnNumber);
			case BOOLEAN: return new DynamicSememeBoolean(data, assemblageSequence, columnNumber);
			case BYTEARRAY: return new DynamicSememeByteArray(data, assemblageSequence, columnNumber);
			case DOUBLE: return new DynamicSememeDouble(data, assemblageSequence, columnNumber);
			case FLOAT: return new DynamicSememeFloat(data, assemblageSequence, columnNumber);
			case INTEGER: return new DynamicSememeInteger(data, assemblageSequence, columnNumber);
			case LONG: return new DynamicSememeLong(data, assemblageSequence, columnNumber);
			case NID: return new DynamicSememeNid(data, assemblageSequence, columnNumber);
			case STRING: return new DynamicSememeString(data, assemblageSequence, columnNumber);
			case UUID: return new DynamicSememeUUID(data, assemblageSequence, columnNumber);
			case SEQUENCE: return new DynamicSememeSequence(data, assemblageSequence, columnNumber);
			case POLYMORPHIC: case UNKNOWN: throw new RuntimeException("No implementation exists for type unknown");
			default: throw new RuntimeException("Implementation error");
		}
	}
	
	protected static DynamicSememeData typeToClass(DynamicSememeDataType type, byte[] data)
	{
		switch (type)
		{
			case ARRAY: return new DynamicSememeArray<DynamicSememeData>(data);
			case BOOLEAN: return new DynamicSememeBoolean(data);
			case BYTEARRAY: return new DynamicSememeByteArray(data);
			case DOUBLE: return new DynamicSememeDouble(data);
			case FLOAT: return new DynamicSememeFloat(data);
			case INTEGER: return new DynamicSememeInteger(data);
			case LONG: return new DynamicSememeLong(data);
			case NID: return new DynamicSememeNid(data);
			case STRING: return new DynamicSememeString(data);
			case UUID: return new DynamicSememeUUID(data);
			case SEQUENCE: return new DynamicSememeSequence(data);
			case UNKNOWN: case POLYMORPHIC: throw new RuntimeException("Should be impossible");
			default:
				throw new RuntimeException("Design failure");
		}
	}
	
	protected static Class<? extends DynamicSememeData> implClassForType(DynamicSememeDataType type)
	{
		switch (type)
		{
			case ARRAY: return DynamicSememeArray.class;
			case BOOLEAN: return DynamicSememeBoolean.class;
			case BYTEARRAY: return DynamicSememeByteArray.class;
			case DOUBLE: return DynamicSememeDouble.class;
			case FLOAT: return DynamicSememeFloat.class;
			case INTEGER: return DynamicSememeInteger.class;
			case LONG: return DynamicSememeLong.class;
			case NID: return DynamicSememeNid.class;
			case STRING: return DynamicSememeString.class;
			case UUID: return DynamicSememeUUID.class;
			case SEQUENCE: return DynamicSememeSequence.class;
			case UNKNOWN: case POLYMORPHIC: throw new RuntimeException("Should be impossible");
			default:
				throw new RuntimeException("Design failure");
		}
	}
}
