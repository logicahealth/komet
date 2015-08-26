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

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

/**
 * @author Daniel Armbrust
 * 
 */
public class BPT_Associations extends PropertyType
{
	public BPT_Associations(String terminologyName)
	{
		super("Associations Types", terminologyName + " Association Type", true, DynamicSememeDataType.STRING);
	}
	
	public BPT_Associations(String propertyTypeDescription, String terminologyName)
	{
		super(propertyTypeDescription, terminologyName + " Association Type", true, DynamicSememeDataType.STRING);
	}
}
