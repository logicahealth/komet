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
package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;



/**
 * Fields to treat as descriptions
 * 
 * @author Daniel Armbrust
 *
 */
public class BPT_Descriptions extends PropertyType
{
	//These values can be used as the starting point for establishing the hierarchy of synonym types.
	//Descriptions are typically sorted (ascending) by the propertySubType values.
	//The lowest number found will be used as the FSN.
	//The next higher number will be used as the 'preferred' synonym.
	//The next higher number will be used as the 'acceptable' synonym - continuing until the value is above the description threshold.
	//Then, the first found description will be the 'preferred' description - the rest will be 'acceptable'.
	
	public static final int FSN = 0;
	public static final int SYNONYM = 200;
	public static final int DEFINITION = 400;

	public BPT_Descriptions(String terminologyName)
	{
		super("Description Types", terminologyName + " Description Type");
	}
}
